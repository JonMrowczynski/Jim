﻿/* 
 * File Name: MainWindow.xaml.cs
 * Version: 1.0
 * Author: Jon Mrowczynski
 * Last Date Edited: 1/18/2016
 * 
 * This is where all of the interaction logic resides for the main window that currently displays all of the 
 * facial points on one persons face as well as the Face Shape Animations with their correspdoning floating 
 * point values that represent how much the tracked face is in that state.
 * 
 * *NOTE*: The reason why we need a bodyFrameReader is because each face corresponds to a specific body.
 * Without the bodies, there can be no face. 
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

namespace ProjectEDDIE
{
    /// <summary>
    /// Interaction logic for ProjectEDDIE
    /// </summary>

    public partial class MainWindow : Window, INotifyPropertyChanged, IDisposable
    {
        /// <summary>
        /// Drawing group for displaying face information.
        /// </summary>

        private readonly DrawingGroup drawingGroup;

        /// <summary>
        /// Drawing image that we will display.
        /// </summary>

        private readonly DrawingImage imageSource;

        /// <summary>
        /// Allows us to map a 3D point to the 2D window.
        /// </summary>

        private readonly CoordinateMapper coordinateMapper = null;

        /// <summary>
        /// The reference to the Kinect v2 sensor.
        /// </summary>

        private readonly KinectSensor kinectSensor = null;

        /// <summary>
        /// Reads the body frame data.
        /// </summary>

        private readonly BodyFrameReader bodyFrameReader = null;

        /// <summary>
        /// Acquires the HD face data.
        /// </summary>

        private readonly HighDefinitionFaceFrameSource hdFaceFrameSource = null;

        /// <summary>
        /// Reads the HD face data.
        /// </summary>

        private readonly HighDefinitionFaceFrameReader hdFaceFrameReader = null;

        /// <summary>
        /// Access the face model points.
        /// </summary>

        private FaceModel faceModel = null;

        /// <summary>
        /// Accesses the face verticies.
        /// </summary>

        private readonly FaceAlignment faceAlignment = null;

        /// <summary>
        /// Used to display all of the facial points in the MainWindow.
        /// </summary>

        private readonly List<Ellipse> points = new List<Ellipse>();

        /// <summary>
        /// Used to dynamically display the AU values of the tracked face.
        /// </summary>

        private readonly TextBlock textBlock = new TextBlock();

        /// <summary>
        /// This array contains all of the FaceShapeAnimations that we want to keep track of
        /// </summary>

        private readonly FaceShapeAnimations[] faceShapeAnimations = {
            FaceShapeAnimations.JawOpen,
            FaceShapeAnimations.LipPucker,
            FaceShapeAnimations.JawSlideRight,
            FaceShapeAnimations.LipStretcherRight,
            FaceShapeAnimations.LipStretcherLeft,
            FaceShapeAnimations.LipCornerPullerLeft,
            FaceShapeAnimations.LipCornerPullerRight,
            FaceShapeAnimations.LipCornerDepressorLeft,
            FaceShapeAnimations.LipCornerDepressorRight,
            FaceShapeAnimations.LeftcheekPuff,
            FaceShapeAnimations.RightcheekPuff,
            FaceShapeAnimations.LefteyeClosed,
            FaceShapeAnimations.RighteyeClosed,
            FaceShapeAnimations.RighteyebrowLowerer,
            FaceShapeAnimations.LefteyebrowLowerer,
            FaceShapeAnimations.LowerlipDepressorLeft,
            FaceShapeAnimations.LowerlipDepressorRight,
        };

        /// <summary>
        /// The current status text to display.
        /// </summary>

        private string statusText = null;

        /// <summary>
        /// Stores the floating point value for each corresponding FaceShapeAnimation.
        /// </summary>

        private IReadOnlyDictionary<FaceShapeAnimations, float> animationUnits = null;

        /// <summary>
        /// Event to allow window controls to bind to changeable data.
        /// </summary>

        public event PropertyChangedEventHandler PropertyChanged;

        /// <summary>
        /// Gets the bitmap to display.
        /// </summary>

        public ImageSource ImageSource => imageSource;

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
                    // notify any bound elements that the text has changed
                    PropertyChanged?.Invoke(this, new PropertyChangedEventArgs("StatusText"));
                }
            }
        }

        /// <summary>
        /// Initialize a new instance of the MainWindow class.
        /// </summary>

        public MainWindow()
        {
            kinectSensor = KinectSensor.GetDefault(); // The Kinect v2 is the only sensor supported so simply get that

            if (kinectSensor != null)
            {
                coordinateMapper = kinectSensor.CoordinateMapper;

                // Listen for the body data
                bodyFrameReader = kinectSensor.BodyFrameSource.OpenReader();
                bodyFrameReader.FrameArrived += Reader_BodyFrameArrived;

                // Listen for HD face data.
                hdFaceFrameSource = new HighDefinitionFaceFrameSource(kinectSensor);
                hdFaceFrameReader = hdFaceFrameSource.OpenReader();
                hdFaceFrameReader.FrameArrived += Reader_FaceFrameArrived;

                faceModel = new FaceModel();
                faceAlignment = new FaceAlignment();

                kinectSensor.IsAvailableChanged += Sensor_IsAvailableChanged;
                kinectSensor.Open();

                StatusText = kinectSensor.IsAvailable ? Properties.Resources.RunningStatusText : Properties.Resources.NoSensorStatusText;
            }
            drawingGroup = new DrawingGroup();
            imageSource = new DrawingImage(drawingGroup);
            DataContext = this;
            InitializeComponent();
        }

        // Called when this object is no longer needed in the program (when it shutsdown).
        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        protected virtual void Dispose(bool disposing)
        {
            if (disposing && faceModel != null)
            {
                faceModel.Dispose();
                faceModel = null;
            }
        }

        /// <summary>
        /// Handles the body frame data arriving from the sensor and connects a body to a face by setting the 
        /// TrackingId property of the Face source.
        /// </summary>
        /// <param name="sender">object sending the event</param>
        /// <param name="e">event arguments</param>

        private void Reader_BodyFrameArrived(object sender, BodyFrameArrivedEventArgs e)
        {
            using (BodyFrame frame = e.FrameReference.AcquireFrame())
            {
                if (frame != null)
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
        }

        private const int framesPerSecond = 30;
        private const int wantedFramesPerSecond = 3;
        private int counter = 0;

        /// <summary>
        /// Handles the face frame data arriving from the sensor.
        /// We need to check two conditions in order to access the face point data:
        /// 1. Check that the frame is not null
        /// 2. Ensure that the frame has at least one tracked face
        /// Once these two conditions are satisfied, we can call the method GetAndRefreshFaceAlignmentResult so that the facial points
        /// and properties are updated.
        /// The facial points are represented by verticies (3D points). These verticies are of triangles such that a 3D triangular mesh
        /// can be constructed using these verticies to display the face.
        /// </summary>
        /// <param name="sender">object sending the event</param>
        /// <param name="hde">event arguments</param>

        private void Reader_FaceFrameArrived(object sender, HighDefinitionFaceFrameArrivedEventArgs hde)
        {
            using (HighDefinitionFaceFrame frame = hde.FrameReference.AcquireFrame())
            {
                if (frame != null && frame.IsFaceTracked)
                {
                    frame.GetAndRefreshFaceAlignmentResult(faceAlignment);
                    if (counter == framesPerSecond / wantedFramesPerSecond)
                    {
                        UpdateFaceInformationText();
                        counter = 0;
                    }
                    else
                    {
                        ++counter;
                    }
                    UpdateFacePoints();
                }
            }
        }

        /// <summary>
        /// We have a list of CameraSpacePoint objects and a list of Ellipse objects which we will add to the canvas and specify their x and 
        /// y positions. The x, y, and z coordiantes are measured in meters. In order to properly find the corresponding pixel values, we use 
        /// a CoordinateMapper which is a built-in mechanism that converts between 3D space ositions to 2D screen positions.
        /// </summary>

        private void UpdateFacePoints()
        {
            if (faceModel == null) return;
            IReadOnlyList<CameraSpacePoint> verticies = faceModel.CalculateVerticesForAlignment(faceAlignment);
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
                for (int i = 0; i < verticies.Count; ++i)
                {
                    CameraSpacePoint vertex = verticies[i];
                    DepthSpacePoint point = coordinateMapper.MapCameraPointToDepthSpace(vertex);
                    if (float.IsInfinity(point.X) || float.IsInfinity(point.Y)) return;
                    Ellipse ellipse = points[i];
                    Canvas.SetLeft(ellipse, point.X);
                    Canvas.SetTop(ellipse, point.Y);
                }
            }
        }

        private void UpdateFaceInformationText()
        {
            string faceInfoText = string.Empty;
            if (faceAlignment != null)
            {
                animationUnits = faceAlignment.AnimationUnits;
                foreach (FaceShapeAnimations faceShapeAnimation in faceShapeAnimations)
                {
                    faceInfoText += faceShapeAnimation.ToString() + " : ";
                    faceInfoText += animationUnits[faceShapeAnimation].ToString() + "\n\n";
                }
            }
            textBlock.Text = faceInfoText;
            textBlock.FontSize = 11;
            textBlock.TextWrapping = TextWrapping.Wrap;
            textBlock.Foreground = new SolidColorBrush(Colors.Black);
            Canvas.SetLeft(textBlock, 0.0);
            Canvas.SetTop(textBlock, 10.0);
            if (!textCanvas.Children.Contains(textBlock))
            {
                textCanvas.Children.Add(textBlock);
            }
        }

        /// <summary>
        /// Handles the event in which the sensor becomes unavailable (hde.g. paused, closed, unplugged).
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
