/* 
 * Author: Jon Mrowczynski
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

namespace KinectEmotionDeterminer
{
    /// <summary>
    /// 
    /// </summary>
    public partial class MainWindow : Window, INotifyPropertyChanged, IDisposable
    {
        /// <summary>
        /// The number of of times data is captured per second.
        /// </summary>
        public const int dataCollectionFramesPerSecond = 10;

        /// <summary>
        /// Displays the face information.
        /// </summary>
        private readonly DrawingGroup drawingGroup;

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
        /// Ellipses are used to display the facial points in the MainWindow.
        /// </summary>
        private readonly List<Ellipse> points = new List<Ellipse>();

        /// <summary>
        /// Used to dynamically display FSA values of the tracked face.
        /// </summary>
        private readonly TextBlock fsaTextBlock = new TextBlock();

        /// <summary>
        /// This array contains all of the FaceShapeAnimations that we want to keep track of.
        /// </summary>
        public static FaceShapeAnimations[] faceShapeAnimations = {
            FaceShapeAnimations.LipCornerPullerLeft,
            FaceShapeAnimations.LipCornerPullerRight,
            FaceShapeAnimations.LipCornerDepressorLeft,
            FaceShapeAnimations.LipCornerDepressorRight,
            FaceShapeAnimations.RighteyebrowLowerer,
            FaceShapeAnimations.LefteyebrowLowerer,
        };

        /// <summary>
        /// Acquire and reads the body frame data.
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
        /// The current status text to display.
        /// </summary>
        private string statusText = null;

        /// <summary>
        /// Stores the floating point value for each corresponding FaceShapeAnimation.
        /// </summary>
        private IReadOnlyDictionary<FaceShapeAnimations, float> animationUnits = null;

        /// <summary>
        /// The event that allows the window controls to bind to changeable data.
        /// </summary>
        public event PropertyChangedEventHandler PropertyChanged;

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
        /// Initializes a new MainWindow class instance.
        /// </summary>
        public MainWindow()
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
            drawingGroup = new DrawingGroup();
            DataContext = this;
            InitializeComponent();
        }

        /// <summary>
        /// Execute start up tasks.
        /// </summary>
        /// <param name="sender">object sending the event</param>
        /// <param name="e">event arguments</param>
        private void MainWindow_Loaded(object sender, RoutedEventArgs e)
        {
            fsaTextBlock.FontSize = 14;
            fsaTextBlock.TextWrapping = TextWrapping.Wrap;
            fsaTextBlock.FontWeight = FontWeights.Bold;
            fsaTextBlock.Foreground = new SolidColorBrush(Colors.Black);
            Canvas.SetLeft(fsaTextBlock, 0.0);
            Canvas.SetTop(fsaTextBlock, 10.0);
            textCanvas.Children.Add(fsaTextBlock);

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
        /// Execute shutdown tasks
        /// </summary>
        /// <param name="sender">object sending the event</param>
        /// <param name="e">event arguments</param>
        private void MainWindow_Closing(object sender, CancelEventArgs e)
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
        /// We connect a body to a face by setting the TrackingId property of the FaceSource
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>

        private void Reader_BodyFrameArrived(object sender, BodyFrameArrivedEventArgs e)
        {
            using (BodyFrame frame = e.FrameReference.AcquireFrame())
            {
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
        }

        readonly TextBlock faceTextBlock = new TextBlock();

        public static string currentEmotionDisplayed = string.Empty;

        /// <summary>
        /// 
        /// </summary>
        private void DrawFaceEmotion()
        {
            using (DrawingContext dc = drawingGroup.Open())
            {
                faceTextBlock.Text = "Emotion: " + currentEmotionDisplayed;
                faceTextBlock.FontSize = 20;
                faceTextBlock.TextWrapping = TextWrapping.Wrap;
                faceTextBlock.Foreground = new SolidColorBrush(Colors.White);
                Canvas.SetLeft(faceTextBlock, faceCanvas.Height / 2 - 50);
                Canvas.SetTop(faceTextBlock, 350);
                if (!faceCanvas.Children.Contains(faceTextBlock)) { faceCanvas.Children.Add(faceTextBlock); }
            }
        }

        public static bool readyToPassData = false;
        public static bool readyToReadData = false;

        public static float[,] faceData = new float[faceShapeAnimations.Length, dataCollectionFramesPerSecond];

        public void CollectFaceData()
        {
            if (!readyToPassData && readyToReadData)
            {
                int faceDataCounter = 0;
                for (int i = 0; i < faceShapeAnimations.Length; ++i)
                {
                    faceData[i, faceDataCounter] = animationUnits[faceShapeAnimations[i]];
                }
                readyToPassData = faceDataCounter++ >= dataCollectionFramesPerSecond - 1;
            }
        }

        public bool IsReadyToPassData() { return readyToPassData; }

        public int GetFaceShapeAnimationLength() { return faceShapeAnimations.Length; }

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
                    // Only update the ShapeAnimationUnit information to the screen every certain number of frames per second
                    UpdateFaceInformationText();
                    CollectFaceData();
                    UpdateFacePoints();
                    DrawFaceEmotion();
                }
            }
        }

        public static bool leftLipCornerPulled = false;
        public static bool rightLipCornerPulled = false;
        public static bool leftLipCornerDepressed = false;
        public static bool rightLipCornerDepressed = false;
        public static bool rightEyebrowLowered = false;
        public static bool leftEyebrowLowered = false;
        public static bool rightEyebrowRaised = false;
        public static bool leftEyebrowRaised = false;

        /// <summary>
        /// This method updates the text that is displayed in the Grid's left column which shows how much a person's face is in the given 
        /// states (FaceShapeAnimation units).
        /// </summary>
        private void UpdateFaceInformationText()
        {
            string faceInfoText = string.Empty;
            if (faceAlignment != null) // If there is a face being tracked...
            {
                animationUnits = faceAlignment.AnimationUnits;
                foreach (FaceShapeAnimations faceShapeAnimation in faceShapeAnimations)
                {
                    faceInfoText += faceShapeAnimation.ToString() + ": " + animationUnits[faceShapeAnimation].ToString() + "\n\n";
                }
            }
            fsaTextBlock.Text = faceInfoText;
        }

        /// <summary>
        /// We have alist of CameraSpacePoint objects and a list of Ellipse objects which we will add to the canvas and specify their x and
        /// y position. The x, y, and z coordiantes are measured in meters. In order to properly find the corresponding pixel values, 
        /// we use a CoordinateMapper which is a built-in mechanism that converts 3D space positions to 2D screen positions.
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
        /// Handles the event in which the sensor becomes unavailable (e.g. paused, closed, unplugged).
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