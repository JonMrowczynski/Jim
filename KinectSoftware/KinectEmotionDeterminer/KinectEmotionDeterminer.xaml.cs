﻿/*
 * MIT License
 *
 * Copyright (c) 2015-2019 Jon Mrowczynski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/* 
    This is where all of the interaction logic resides for the main window that currently displays all of the 
    facial points on one persons face as well as the Face Shape Animations with their correspdoning floating 
    point values that represent how much the tracked face is in that state.

    Author: Jon Mrowczynski
 */

using Microsoft.Kinect;
using Microsoft.Kinect.Face;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Shapes;
using System.Windows.Threading;

namespace KinectEmotionDeterminerNS
{
    /// <summary>
    /// This class contains all of the logic that displays all of the facial points on one person's face, the 
    /// FaceShapeAnimation units with their corresponding floating point values that represents how much the 
    /// tracked face is in that state, as well as the interpreted emotion after the calibration routine has been
    /// run.
    /// </summary>
    public partial class KinectEmotionDeterminer : Window, INotifyPropertyChanged, IDisposable
    {
        /// <summary>
        /// This array contains all of the FaceShapeAnimations that we want to keep track of.
        /// </summary>
        public static readonly FaceShapeAnimations[] faceShapeAnimations = {
            FaceShapeAnimations.LipCornerPullerLeft,
            FaceShapeAnimations.LipCornerPullerRight,
            FaceShapeAnimations.LipCornerDepressorLeft,
            FaceShapeAnimations.LipCornerDepressorRight,
            FaceShapeAnimations.RighteyebrowLowerer,
            FaceShapeAnimations.LefteyebrowLowerer,
        };

        /// <summary>
        /// This allows us to map 3D point t the 2D window.
        /// </summary>
        private readonly CoordinateMapper coordinateMapper = null;

        /// <summary>
        /// Acquires the HD face data.
        /// </summary>
        private readonly HighDefinitionFaceFrameSource hdFaceFrameSource = null;

        /// <summary>
        /// Gains access to the face verticies.
        /// </summary>
        private readonly FaceAlignment faceAlignment = null;

        /// <summary>
        /// Ellipses are used to display the facial points in the KinectEmotionDeterminer.
        /// </summary>
        private readonly List<Ellipse> points = new List<Ellipse>();

        /// <summary>
        /// Dynamically displays the FSA values of the tracked face.
        /// </summary>
        private readonly TextBlock fsaTextBlock = new TextBlock();

        /// <summary>
        /// Dynamically displays the human's displayed emotion.
        /// </summary>
        private readonly TextBlock emotionTextBlock = new TextBlock();

        /// <summary>
        /// Stores snapshots of the human's facial data.
        /// </summary>
        public double[] faceData;

        /// <summary>
        /// Acquire and reads the body frame data. Even though we only care about facial data, we need a BodyFrameReader
        /// in order for the Kinect to get a face because each face corresponds to a body.
        /// </summary>
        private BodyFrameReader bodyFrameReader = null;

        /// <summary>
        /// Reads the HD face data.
        /// </summary>
        private HighDefinitionFaceFrameReader hdFaceFrameReader = null;

        /// <summary>
        /// Gain access to the face model points.
        /// </summary>
        private FaceModel faceModel = null;

        /// <summary>
        /// The reference to the Kinext v2 sensor.
        /// </summary>
        private KinectSensor kinectSensor = null;

        /// <summary>
        /// Stores the floating point value for each corresponding FaceShapeAnimation.
        /// </summary>
        private IReadOnlyDictionary<FaceShapeAnimations, float> animationUnits = null;

        /// <summary>
        /// The event that allows the window controls to bind to changeable data.
        /// </summary>
        public event PropertyChangedEventHandler PropertyChanged;

        /// <summary>
        /// The current status text to display.
        /// </summary>
        private string statusText = null;

        /// <summary>
        /// Gets or sets the current status text to display.
        /// </summary>
        public string StatusText
        {
            get => statusText;
            set
            {
                if (statusText != value)
                {
                    statusText = value;
                    // Notify any bound elements that the text has changed
                    PropertyChanged?.Invoke(this, new PropertyChangedEventArgs("StatusText"));
                }
            }
        }

        /// <summary>
        /// The emotion that the human is currently displaying.
        /// </summary>
        private string currentDisplayedEmotion = string.Empty;

        /// <summary>
        /// Gets or sets the current displayed emotion.
        /// </summary>
        public string CurrentDisplayedEmotion
        {
            get => currentDisplayedEmotion;
            set
            {
                if (currentDisplayedEmotion != value)
                {
                    currentDisplayedEmotion = value;
                    Dispatcher.InvokeAsync(() => emotionTextBlock.Text = "Emotion: " + CurrentDisplayedEmotion);
                }
            }
        }

        /// <summary>
        /// A boolean that indicates whether another Thread is ready to read faceData.
        /// </summary>
        private volatile bool readyToReadData = false;

        /// <summary>
        /// Gets or sets whether faceData is ready to be read by another Thread. If readyToReadData is set to false, 
        /// then readyToPassData is also set to false.
        /// </summary>
        public bool ReadyToReadData
        {
            get => readyToReadData;
            set
            {
                if (value == false)
                {
                    ReadyToPassData = false;
                }
                readyToReadData = value;
            }
        }

        /// <summary>
        /// A boolean indicating whether faceData is ready to be passed to another Thread.
        /// </summary>
        private volatile bool readyToPassData = false;

        /// <summary>
        /// Gets or sets whether faceData is ready to be passed to another Thread.
        /// </summary>
        public bool ReadyToPassData
        {
            get => readyToPassData;
            set => readyToPassData = value;
        }

        /// <summary>
        /// Initializes a new KinectEmotionDeterminer class instance and sets up the Kinect to track a face.
        /// </summary>
        public KinectEmotionDeterminer()
        {
            kinectSensor = KinectSensor.GetDefault(); // Get the Kinect v2 sensor
            if (kinectSensor != null)
            {
                coordinateMapper = kinectSensor.CoordinateMapper;

                // Listen for the body data
                bodyFrameReader = kinectSensor.BodyFrameSource.OpenReader();

                // Listen for HD face data
                hdFaceFrameSource = new HighDefinitionFaceFrameSource(kinectSensor);
                hdFaceFrameReader = hdFaceFrameSource.OpenReader();

                faceModel = new FaceModel();
                faceAlignment = new FaceAlignment();

                kinectSensor.IsAvailableChanged += Sensor_IsAvailableChanged;
                kinectSensor.Open();

                StatusText = kinectSensor.IsAvailable ? Properties.Resources.RunningStatusText : Properties.Resources.NoSensorStatusText;
            }
            faceData = new double[faceShapeAnimations.Length];
            DataContext = this;
            InitializeComponent();
        }

        /// <summary>
        /// Execute start up tasks, especially ones that require the textCanvas and faceCanvas. 
        /// </summary>
        /// <param name="sender">object sending the event</param>
        /// <param name="e">event arguments</param>
        private void KinectEmotionDeterminer_Loaded(object sender, RoutedEventArgs e)
        {
            fsaTextBlock.FontSize = 13;
            fsaTextBlock.FontWeight = FontWeights.Bold;
            fsaTextBlock.Foreground = new SolidColorBrush(Colors.Black);
            Canvas.SetLeft(fsaTextBlock, 0.0);
            Canvas.SetTop(fsaTextBlock, 10.0);
            textCanvas.Children.Add(fsaTextBlock);

            emotionTextBlock.FontSize = 20;
            emotionTextBlock.Foreground = new SolidColorBrush(Colors.White);
            Canvas.SetLeft(emotionTextBlock, faceCanvas.Height / 2 - 50);
            Canvas.SetTop(emotionTextBlock, 350);
            faceCanvas.Children.Add(emotionTextBlock);

            if (bodyFrameReader != null)
            {
                bodyFrameReader.FrameArrived += Reader_BodyFrameArrived;
            }
            if (hdFaceFrameReader != null)
            {
                hdFaceFrameReader.FrameArrived += Reader_FaceFrameArrived;
            }
        }

        /// <summary>
        /// Execute shutdown tasks.
        /// </summary>
        /// <param name="sender">object sending the event</param>
        /// <param name="e">event arguments</param>
        private void KinectEmotionDeterminer_Closing(object sender, CancelEventArgs e)
        {
            // BodyFrameReader, HighDefinitionFaceFrameReader, and FaceModel are IDisposable 
            bodyFrameReader?.Dispose();
            bodyFrameReader = null;
            hdFaceFrameReader?.Dispose();
            hdFaceFrameReader = null;
            faceModel?.Dispose();
            faceModel = null;

            kinectSensor?.Close();
            kinectSensor = null;
        }

        /// <summary>
        /// Called when disposed of.
        /// </summary>
        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        /// <summary>
        /// Disposed based on whether or not managed or native resources should be freed.
        /// </summary>
        /// <param name="disposing">Set to true to free both native and managed resources, false otherwise</param>
        protected virtual void Dispose(bool disposing)
        {
            if (disposing && faceModel != null)
            {
                faceModel.Dispose();
                faceModel = null;
            }
        }

        /// <summary>
        /// We connect a body to a face by setting the TrackingId property of the FaceSource.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void Reader_BodyFrameArrived(object sender, BodyFrameArrivedEventArgs e)
        {
            using BodyFrame frame = e.FrameReference.AcquireFrame();
            if (frame != null) // The chance to acquire a frame may have been missed so execute this iff a frame has been acquired
            {
                Body[] bodies = new Body[frame.BodyCount];
                frame.GetAndRefreshBodyData(bodies);
                Body body = bodies.Where(b => b.IsTracked).FirstOrDefault();
                if (!hdFaceFrameSource.IsTrackingIdValid && body != null)
                {
                    hdFaceFrameSource.TrackingId = body.TrackingId;
                }
            }
        }

        /// <summary>
        /// Handles the face frame data arriving from the sensor. We need to check two conditions in order to access the face point data:
        /// 1. Check that the frame is not null
        /// 2. Ensure that the frame has at least one tracked face
        /// IF these two conditions are satisfied we can call the method GetAndRefreshFaceAlignmentResult so that the facial points
        /// and properties are updated.
        /// The facial points are represented by verticies (points in 3D space) projected onto a 2D screen.
        /// </summary>
        /// <param name="sender">object sending the event</param>
        /// <param name="hde">event arguments</param>
        private void Reader_FaceFrameArrived(object sender, HighDefinitionFaceFrameArrivedEventArgs hde)
        {
            using HighDefinitionFaceFrame frame = hde.FrameReference.AcquireFrame();
            if (frame != null && frame.IsFaceTracked)
            {
                frame.GetAndRefreshFaceAlignmentResult(faceAlignment);
                UpdateFaceInformation();
                UpdateFacePoints();
            }
        }

        /// <summary>
        /// This method updates the text that is displayed in the Grid's left column which shows how much a person's face is in the given 
        /// states (FaceShapeAnimation units). In addition, those values are added to a faceData array that can be accessed from 
        /// another Thread to perform analyses.
        /// </summary>
        private void UpdateFaceInformation()
        {
            string faceInfoText = string.Empty;
            if (faceAlignment != null) // If there is a face being tracked...
            {
                animationUnits = faceAlignment.AnimationUnits;
                for (int i = 0; i < faceShapeAnimations.Length; ++i)
                {
                    faceInfoText += faceShapeAnimations[i].ToString() + ": " + animationUnits[faceShapeAnimations[i]].ToString() + "\n\n";
                    if (ReadyToReadData && !ReadyToPassData)
                    {
                        faceData[i] = animationUnits[faceShapeAnimations[i]];
                    }
                }
                ReadyToPassData = true;
            }
            fsaTextBlock.Text = faceInfoText;
        }

        /// <summary>
        /// We have a list of CameraSpacePoint objects and a list of Ellipse objects which we will add to the faceCanvas and specify 
        /// their x and y position. The x, y, and z coordiantes are measured in meters. In order to properly find the corresponding 
        /// pixel values, we use a CoordinateMapper which is a built-in mechanism that converts 3D space positions to 2D screen positions.
        /// </summary>
        private void UpdateFacePoints()
        {
            if (faceModel == null) return;
            IReadOnlyList<CameraSpacePoint> verticies = faceModel.CalculateVerticesForAlignment(faceAlignment); // Get the facial points
            if (verticies.Count > 0)
            {
                if (points.Count == 0)
                {
                    for (int i = 0; i < verticies.Count; ++i)
                    {
                        Ellipse ellipse = new Ellipse
                        {
                            Width = 1.0,
                            Height = 1.0,
                            Fill = new SolidColorBrush(Colors.White)
                        };
                        points.Add(ellipse);
                    }
                    foreach (Ellipse ellipse in points)
                    {
                        faceCanvas.Children.Add(ellipse);
                    }
                }
                for (int i = 0; i < verticies.Count; ++i) // For all of the facial points, add them to the face canvas
                {
                    CameraSpacePoint vertex = verticies[i];
                    DepthSpacePoint point = coordinateMapper.MapCameraPointToDepthSpace(vertex);
                    double scaledX = 3 * point.X - faceCanvas.Width;
                    double scaledY = 3 * point.Y - faceCanvas.Height;
                    // Put the points on the faceCanvas iff they are on the screen 
                    if (double.IsInfinity(scaledX) || double.IsInfinity(scaledY)) return;
                    Ellipse ellipse = points[i];
                    Canvas.SetLeft(ellipse, scaledX);
                    Canvas.SetTop(ellipse, scaledY);
                }
            }
        }

        /// <summary>
        /// Handles sensor unavailablility events (e.g. paused, closed, unplugged).
        /// </summary>
        /// <param name="sender">object sending the event</param>
        /// <param name="e">event arguments</param>
        private void Sensor_IsAvailableChanged(object sender, IsAvailableChangedEventArgs e)
        {
            if (kinectSensor != null)
            {
                StatusText = kinectSensor.IsAvailable ? Properties.Resources.RunningStatusText : Properties.Resources.SensorNotAvailableStatusText;
            }
        }
    }
}