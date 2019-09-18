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

    public partial class MainWindow : Window, INotifyPropertyChanged, IDisposable
    {

        /// <summary>
        /// The number of of times data is captured per second.
        /// </summary>

        private const int dataCollectionFramesPerSecond = 10;

        /// <summary>
        /// Displays the face information.
        /// </summary>

        private DrawingGroup drawingGroup;

        /// <summary>
        /// Draws the image that is displayed.
        /// </summary>

        private DrawingImage imageSource;

        /// <summary>
        /// This allows us to map 3D point t the 2D window.
        /// </summary>

        private CoordinateMapper coordinateMapper = null;

        /// <summary>
        /// The reference to the Kinext v2 sensor.
        /// </summary>

        private KinectSensor kinectSensor = null;

        /// <summary>
        /// Acquire and reads the body frame data.
        /// </summary>

        private BodyFrameReader bodyFrameReader = null;

        /// <summary>
        /// Acquires the HD face data.
        /// </summary>

        private HighDefinitionFaceFrameSource hdFaceFrameSource = null;

        /// <summary>
        /// Reads the HD face data.
        /// </summary>

        private HighDefinitionFaceFrameReader hdFaceFrameReader = null;

        /// <summary>
        /// Gain access to the face model points.
        /// </summary>

        private FaceModel faceModel = null;

        /// <summary>
        /// Gains access to the face verticies.
        /// </summary>

        private FaceAlignment faceAlignment = null;

        /// <summary>
        /// Ellipses are used to display the facial points in the MainWindow.
        /// </summary>

        private readonly List<Ellipse> points = new List<Ellipse>();

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
        /// Array to store bodies.
        /// </summary>

        private Body[] bodies = null;

        /// <summary>
        /// The number of bodies tracked
        /// </summary>

        private int bodyCount;

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
        /// Initializes a new MainWindow class instance.
        /// </summary>

        public MainWindow()
        {
            DataContext = this;
            InitializeComponent();
        }

        public bool readyToPassData = false;
        public bool readyToReadData = false;

        public float[,] faceData = new float[faceShapeAnimations.Length, dataCollectionFramesPerSecond];

        private int faceDataCounter = 0;

        public void CollectFaceData()
        {
            if (!readyToPassData && readyToReadData)
            {
                for (int i = 0; i < faceShapeAnimations.Length; ++i)
                {
                    faceData[i, faceDataCounter] = animationUnits[faceShapeAnimations[i]];
                }
                ++faceDataCounter;
                if (faceDataCounter >= dataCollectionFramesPerSecond - 1)
                {
                    readyToPassData = true;
                    faceDataCounter = 0;
                }
                else { readyToPassData = false; }
            }
        }

        public bool IsReadyToPassData() { return readyToPassData; }

        public int GetFaceShapeAnimationLength() { return faceShapeAnimations.Length; }

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
                    // Notify any bound elements that the text has changed
                    PropertyChanged?.Invoke(this, new PropertyChangedEventArgs("StatusText"));
                }
            }
        }

        /* Called when this object is no longer needed in the program (when it shutsdown) */

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
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>

        private void MainWindow_Loaded(object sender, RoutedEventArgs e)
        {
            kinectSensor = KinectSensor.GetDefault(); // Get the Kinect v2 sensor
            if (kinectSensor != null)
            {
                coordinateMapper = kinectSensor.CoordinateMapper;
                // Listen for the body data
                bodyFrameReader = kinectSensor.BodyFrameSource.OpenReader();
                bodyFrameReader.FrameArrived += Reader_BodyFrameArrived;
                // Set the maximum number of bodies that would be tracked by the Kinect
                bodyCount = kinectSensor.BodyFrameSource.BodyCount;
                // Allocate storage to store body objects
                bodies = new Body[bodyCount];
                // Listen for HD face data
                hdFaceFrameSource = new HighDefinitionFaceFrameSource(kinectSensor);
                hdFaceFrameReader = hdFaceFrameSource.OpenReader();
                hdFaceFrameReader.FrameArrived += Reader_FaceFrameArrived;

                faceModel = new FaceModel();
                faceAlignment = new FaceAlignment();

                kinectSensor.IsAvailableChanged += Sensor_IsAvailableChanged;

                kinectSensor.Open();
            }
            drawingGroup = new DrawingGroup();
            imageSource = new DrawingImage(drawingGroup);
        }

        /// <summary>
        /// If the MainWindow is closed for whatever reason, make sure that everything is cleaned up.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>

        private void MainWindow_Closing(object sender, CancelEventArgs e)
        {
            hdFaceFrameReader?.Dispose();
            bodyFrameReader?.Dispose();
            kinectSensor?.Close();
            hdFaceFrameReader = null;
            bodyFrameReader = null;
            kinectSensor = null;
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

        /// <summary>
        /// We have alist of CameraSpacePoint objects and a list of Ellipse objects which we will add to the canvas and specify their x and
        /// y position. The x, y, and z coordiantes are measured in meters. In order to properly find the corresponding pixel values, 
        /// we use a CoordinateMapper which is a built-in mechanism that converts 3D space positions to 2D screen positions.
        /// </summary>

        private void UpdateFacePoints()
        {
            if (faceModel != null)
            {
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
                        // Put the points on the faceCanvas iff they are on the screen 
                        if (!(float.IsInfinity(point.X) || float.IsInfinity(point.Y)))
                        {
                            Ellipse ellipse = this.points[i];
                            Canvas.SetLeft(ellipse, point.X);
                            Canvas.SetTop(ellipse, point.Y);
                        }
                    }
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

        private readonly TextBlock lipCornerPullerLeftTextBlock = new TextBlock();
        private readonly TextBlock lipCornerPullerRightTextBlock = new TextBlock();
        private readonly TextBlock lipCornerDepressorLeftTextBlock = new TextBlock();
        private readonly TextBlock lipCornerDepressorRightTextBlock = new TextBlock();
        private readonly TextBlock rightEyebrowTextBlock = new TextBlock();
        private readonly TextBlock leftEyebrowTextBlock = new TextBlock();

        /// <summary>
        /// This method updates the text that is displayed in the Grid's left column which shows how much a person's face is in the given 
        /// states (FaceShapeAnimation units).
        /// </summary>

        private void UpdateFaceInformationText()
        {
            if (faceAlignment != null) // If there is a face being tracked...
            {
                animationUnits = faceAlignment.AnimationUnits;

                lipCornerPullerLeftTextBlock.Text = faceShapeAnimations[0].ToString() + ": " + animationUnits[faceShapeAnimations[0]].ToString();
                lipCornerPullerRightTextBlock.Text = faceShapeAnimations[1].ToString() + ": " + animationUnits[faceShapeAnimations[1]].ToString();
                lipCornerDepressorLeftTextBlock.Text = faceShapeAnimations[2].ToString() + ": " + animationUnits[faceShapeAnimations[2]].ToString();
                lipCornerDepressorRightTextBlock.Text = faceShapeAnimations[3].ToString() + ": " + animationUnits[faceShapeAnimations[3]].ToString();
                rightEyebrowTextBlock.Text = faceShapeAnimations[4].ToString() + ": " + animationUnits[faceShapeAnimations[4]].ToString();
                leftEyebrowTextBlock.Text = faceShapeAnimations[5].ToString() + ": " + animationUnits[faceShapeAnimations[5]].ToString();

                lipCornerPullerLeftTextBlock.FontSize = 14;
                lipCornerPullerRightTextBlock.FontSize = 14;
                lipCornerDepressorLeftTextBlock.FontSize = 14;
                lipCornerDepressorRightTextBlock.FontSize = 14;
                rightEyebrowTextBlock.FontSize = 14;
                leftEyebrowTextBlock.FontSize = 14;

                lipCornerPullerLeftTextBlock.FontWeight = FontWeights.Bold;
                lipCornerPullerRightTextBlock.FontWeight = FontWeights.Bold;
                lipCornerDepressorLeftTextBlock.FontWeight = FontWeights.Bold;
                lipCornerDepressorRightTextBlock.FontWeight = FontWeights.Bold;
                rightEyebrowTextBlock.FontWeight = FontWeights.Bold;
                leftEyebrowTextBlock.FontWeight = FontWeights.Bold;

                lipCornerPullerLeftTextBlock.Foreground = leftLipCornerPulled ? new SolidColorBrush(Colors.Blue) : new SolidColorBrush(Colors.Black);
                lipCornerPullerRightTextBlock.Foreground = rightLipCornerPulled ? new SolidColorBrush(Colors.Blue) : new SolidColorBrush(Colors.Black);
                lipCornerDepressorLeftTextBlock.Foreground = leftLipCornerDepressed ? new SolidColorBrush(Colors.Blue) : new SolidColorBrush(Colors.Black);
                lipCornerDepressorRightTextBlock.Foreground = rightLipCornerDepressed ? new SolidColorBrush(Colors.Blue) : new SolidColorBrush(Colors.Black);

                if (rightEyebrowLowered) { rightEyebrowTextBlock.Foreground = new SolidColorBrush(Colors.Blue); }
                else if (rightEyebrowRaised) { rightEyebrowTextBlock.Foreground = new SolidColorBrush(Colors.Red); }
                else { rightEyebrowTextBlock.Foreground = new SolidColorBrush(Colors.Black); }

                if (leftEyebrowLowered) { leftEyebrowTextBlock.Foreground = new SolidColorBrush(Colors.Blue); }
                else if (leftEyebrowRaised) { leftEyebrowTextBlock.Foreground = new SolidColorBrush(Colors.Red); }
                else { leftEyebrowTextBlock.Foreground = new SolidColorBrush(Colors.Black); }

                /*
                Canvas.SetLeft(FAUTextBlock, 0.0);
                Canvas.SetTop(FAUTextBlock, this.textCanvas.Width / 2);
                */

                Canvas.SetLeft(lipCornerPullerLeftTextBlock, 0.0);
                Canvas.SetLeft(lipCornerPullerRightTextBlock, 0.0);
                Canvas.SetLeft(lipCornerDepressorLeftTextBlock, 0.0);
                Canvas.SetLeft(lipCornerDepressorRightTextBlock, 0.0);
                Canvas.SetLeft(rightEyebrowTextBlock, 0.0);
                Canvas.SetLeft(leftEyebrowTextBlock, 0.0);

                Canvas.SetTop(lipCornerPullerLeftTextBlock, 150);
                Canvas.SetTop(lipCornerPullerRightTextBlock, 180.0);
                Canvas.SetTop(lipCornerDepressorLeftTextBlock, 210.0);
                Canvas.SetTop(lipCornerDepressorRightTextBlock, 240.0);
                Canvas.SetTop(rightEyebrowTextBlock, 270.0);
                Canvas.SetTop(leftEyebrowTextBlock, 300.0);

                if (!textCanvas.Children.Contains(lipCornerDepressorLeftTextBlock))
                {
                    textCanvas.Children.Add(lipCornerPullerLeftTextBlock);
                    textCanvas.Children.Add(lipCornerPullerRightTextBlock);
                    textCanvas.Children.Add(lipCornerDepressorLeftTextBlock);
                    textCanvas.Children.Add(lipCornerDepressorRightTextBlock);
                    textCanvas.Children.Add(rightEyebrowTextBlock);
                    textCanvas.Children.Add(leftEyebrowTextBlock);
                }
            }
        } // end of UpdateFaceInformationText

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

    } // end of partial class MainWindow

} // end of namespace ProjectEDDIE