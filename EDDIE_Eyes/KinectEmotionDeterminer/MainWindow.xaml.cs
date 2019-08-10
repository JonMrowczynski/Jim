/* 
 * File Name: MainWindow.xaml.cs
 * Version: 1.0
 * Author: Jon Mrowczynski
 * Last Date Edited: 1/28/2016
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
using System.Windows.Documents;
using System.Windows.Media;
using System.Windows.Shapes;

namespace KinectEmotionDeterminer {

    public partial class MainWindow : Window, INotifyPropertyChanged, IDisposable {

        /* The INotifyPropertyChange  event is to allow the window controls to bind to changeable data */

        public event PropertyChangedEventHandler PropertyChanged;

        /*  This array contains all of the FaceShapeAnimations that we want to keep track of */

        public static FaceShapeAnimations[] faceShapeAnimations = {
            FaceShapeAnimations.LipCornerPullerLeft,
            FaceShapeAnimations.LipCornerPullerRight,
            FaceShapeAnimations.LipCornerDepressorLeft,
            FaceShapeAnimations.LipCornerDepressorRight,
            FaceShapeAnimations.RighteyebrowLowerer,
            FaceShapeAnimations.LefteyebrowLowerer,
        };

        /* The reference to the Kinect v2 sensor */

        private KinectSensor kinectSensor = null;

        /* Array to store bodies */

        private Body[] bodies = null;

        /* The currently tracked body */

        private Body currentTrackedBody = null;

        /* The number of bodies tracked */

        private int bodyCount;

        /* The offsets used to dynamically place text underneath the tracked face */

        private const float TextLayoutOffsetX = -0.2f;
        private const float TextLayoutOffsetY = -0.15f;

        /* Acquire and read the body frame data */

        private BodyFrameSource bodyFrameSource = null;  
        private BodyFrameReader bodyFrameReader = null; 

        /* Acquires the HD face data */

        private HighDefinitionFaceFrameSource hdFaceFrameSource = null;  
        private HighDefinitionFaceFrameReader hdFaceFrameReader = null;

        /* This allows us to map 3D point to the 2D window */

        private CoordinateMapper coordinateMapper = null; 

        /* Gain access to the face verticies as well as the face model points */
 
        private FaceAlignment faceAlignment = null; 
        private FaceModel faceModel = null; 

        /* Use Ellipses to display the facial points in the MainWindow */

        private List<Ellipse> points = new List<Ellipse>(); 

        /* Display the face information as well as draw the image that is displayed  */

        private DrawingGroup drawingGroup; 
        private DrawingImage imageSource; 

        /* The current status text to display */

        private string statusText = null; 

        /* Dynamically display the AU values of the tracked face */

        private TextBlock textBlock = new TextBlock(); 

        /* The Text that is displayed in the left hand column of the grid */

        private string faceInfoText = String.Empty; 

        /* Only display the facial data every certain amount of frames per second */

        private static int faceDisplayOffsetCounter = 0;
        private static int dataCollectionOffsetCounter = 0;

        private static readonly int framesPerSecond = 30;
        private static readonly int textFramesPerSecond = 5;

        public static readonly int dataCollectionFramesPerSecond = 10;

        private static readonly int textDisplayerOffset = framesPerSecond / textFramesPerSecond;
        private static readonly int dataCollectionOffset = framesPerSecond / dataCollectionFramesPerSecond;
        
        /* Stores the floating point value for each corresponding FaceShapeAnimation */

        private IReadOnlyDictionary<FaceShapeAnimations, float> animationUnits = null;

        /* The one and only constructor */
 
        public MainWindow() {

            this.DataContext = this;
            this.InitializeComponent();

        } // end of MainWindow constructor

        public static bool readyToPassData = false;
        public static bool readyToReadData = false;

        public static float[,] faceData = new float[faceShapeAnimations.Length , dataCollectionFramesPerSecond];

        private static int faceDataCounter = 0;

        public void CollectFaceData() {

            if (!readyToPassData && readyToReadData) {

                for (int i = 0; i < faceShapeAnimations.Length; ++i) {

                    faceData[i, faceDataCounter] = this.animationUnits[faceShapeAnimations[i]];

                } // end of for

                ++faceDataCounter;

                if (faceDataCounter >= dataCollectionFramesPerSecond - 1) {

                    readyToPassData = true;
                    faceDataCounter = 0;

                } else { readyToPassData = false; }

            } // end of if

        } // end of GetFaceData

        public bool isReadyToPassData() { return readyToPassData; }

        public int getFaceShapeAnimationLength() { return faceShapeAnimations.Length; }

        /* Gets the bitmap to display */

        public ImageSource ImageSource {

            get { return this.imageSource; }

        } // end of ImageSource

        /* Gets or sets the current status text to display */

        public string StatusText {

            get { return this.statusText; } 

            set {

                if (this.statusText != value) {

                    this.statusText = value;

                    /* Notify any bound elements that the text has changed */

                    if (this.PropertyChanged != null) {

                        this.PropertyChanged(this, new PropertyChangedEventArgs("StatusText"));

                    } // end of if

                } // end of outer if

            } // end of set

        } // end of StatusText

        /* Called when this object is no longer needed in the program (when it shutsdown) */

        public void Dispose() {

            this.Dispose(true);
            GC.SuppressFinalize(this);

        } // end of Dispose

        protected virtual void Dispose(bool disposing) {

            if (disposing && this.faceModel != null) {

                this.faceModel.Dispose();
                this.faceModel = null;

            } // end of if

        } // end of Dispose 

        private void MainWindow_Loaded(object sender, RoutedEventArgs e) {

            /* Currently the Kinect v2 is the only sensor supported so simply get that */

            this.kinectSensor = KinectSensor.GetDefault();

            if (kinectSensor != null) {

                this.coordinateMapper = this.kinectSensor.CoordinateMapper;

                /* Listen for the body data */

                this.bodyFrameSource = this.kinectSensor.BodyFrameSource;
                this.bodyFrameReader = this.kinectSensor.BodyFrameSource.OpenReader();
                this.bodyFrameReader.FrameArrived += Reader_BodyFrameArrived;

               /* Set the maximum number of bodies that wouldbe tracked by the Kinect */

                this.bodyCount = this.kinectSensor.BodyFrameSource.BodyCount;

                /* Allocate storage to store body objects */

                this.bodies = new Body[this.bodyCount];

                /* Listen for HD face data */

                this.hdFaceFrameSource = new HighDefinitionFaceFrameSource(this.kinectSensor);
                this.hdFaceFrameReader = this.hdFaceFrameSource.OpenReader();
                this.hdFaceFrameReader.FrameArrived += Reader_FaceFrameArrived;

                this.faceModel = new FaceModel();
                this.faceAlignment = new FaceAlignment();

                this.kinectSensor.IsAvailableChanged += this.Sensor_IsAvailableChanged;

                this.kinectSensor.Open();

            } // end of if

            this.drawingGroup = new DrawingGroup();
            this.imageSource = new DrawingImage(this.drawingGroup);

        } // end of MainWindow_Loaded

        /* If the MainWindow is closed for whatever reason, make sure that stuff gets cleaned up */

        private void MainWindow_Closing(object sender, CancelEventArgs e) {

            if (this.hdFaceFrameReader != null) { this.hdFaceFrameReader.Dispose(); }

            if (this.bodyFrameReader != null) { this.bodyFrameReader.Dispose(); }

            if (this.kinectSensor != null) {

                this.kinectSensor.Close();
                this.kinectSensor = null;

            }

        } // end of MainWindow_Closing

        /* We connect a body to a face by setting the TrackingId property of the Face source */

        private void Reader_BodyFrameArrived(object sender, BodyFrameArrivedEventArgs e) {

            using (var frame = e.FrameReference.AcquireFrame()) {

                /* The chance to acquire the frame may have been missed, so only execute this if 
                 * a frame has been acquired */

                if (frame != null) {

                    frame.GetAndRefreshBodyData(this.bodies);
                    Body body = bodies.Where(b => b.IsTracked).FirstOrDefault();

                    if (!this.hdFaceFrameSource.IsTrackingIdValid && body != null) {

                        this.hdFaceFrameSource.TrackingId = body.TrackingId;

                    }

                } // end of outer if 

            } // end of using 

        } // end of Reader_BodyFrameArrived

        private Body FindBodyWithTrackingId(BodyFrame bodyFrame, ulong trackingId) {
            
            Body result = null;

            bodyFrame.GetAndRefreshBodyData(this.bodies);

            foreach (var body in bodies) {

                if (body.IsTracked) {

                    if (body.TrackingId == trackingId) {

                        result = body;
                        break;
                    
                    }
                
                } // end of if
            
            } // end of foreach

            return result;

        } // end of FindBodyWithTrackingId

        /* Finds the body that is closest to the Kinect V2 sensor by calculating the distances between
         * the Kinect v2 sensor and the tracked bodies and then returning the Body whose distance from
         * the Kinect v2 is the smallest */

        private Body FindClosestBody(BodyFrame bodyFrame) {

            Body result = null;

            double closestBodyDistance = double.MaxValue;

            bodyFrame.GetAndRefreshBodyData(this.bodies);

            foreach (var body in bodies) {

                if (body.IsTracked) {

                    var currentLocation = body.Joints[JointType.SpineBase].Position;

                    var currentDistance = VectorLength(currentLocation);

                    if (result == null || currentDistance < closestBodyDistance) {

                        result = body;
                        closestBodyDistance = currentDistance;

                    }

                } // end of if

            } // end of foreach

            return result;

        } // end of FindClosestBody

        /* Calculates the distance between the Kinect v2's camera and a CameraSpacePoint */

        private double VectorLength(CameraSpacePoint point) {

            var result = Math.Pow(point.X, 2) + Math.Pow(point.Y, 2) + Math.Pow(point.Z, 2);

            result = Math.Sqrt(result);

            return result;

        } // end of VectorLength

        TextBlock faceTextBlock = new TextBlock();

        public static string currentEmotionDisplayed = string.Empty;

        private void DrawFaceEmotion(int faceIndex) {

            using (DrawingContext dc = this.drawingGroup.Open()) {

                this.faceTextBlock.Text = "Emotion: " + currentEmotionDisplayed;
                this.faceTextBlock.FontSize = 20;
                this.faceTextBlock.TextWrapping = TextWrapping.Wrap;
                this.faceTextBlock.Foreground = new SolidColorBrush(Colors.White);
                Canvas.SetLeft(this.faceTextBlock, this.faceCanvas.Height / 2 - 50);
                Canvas.SetTop(this.faceTextBlock, 350);

                if (!faceCanvas.Children.Contains(this.faceTextBlock)) { faceCanvas.Children.Add(this.faceTextBlock); }

            } // end of using 

        } // end of DrawFaceResults

        private bool GetFaceTextPositionInColorSpace(int faceIndex, out Point faceTextLayout) {

            faceTextLayout = new Point();

            bool isLayoutValid = false;

            Body body = this.bodies[faceIndex];
            
            if (body.IsTracked) {

                var headJoint = body.Joints[JointType.Head].Position;
                
                CameraSpacePoint textPoint = new CameraSpacePoint() {

                    X = headJoint.X + TextLayoutOffsetX,
                    Y = headJoint.Y + TextLayoutOffsetY,
                    Z = headJoint.Z

                };

                ColorSpacePoint textPointInColor = this.coordinateMapper.MapCameraPointToColorSpace(textPoint);

                faceTextLayout.X = textPointInColor.X;
                faceTextLayout.Y = textPointInColor.Y;
                isLayoutValid = true;
            
            } // end of if

            return isLayoutValid;
        
        } // end of GetFaceTextPositionInColorSpace

        /* We need to check two conditions first in order to access the face point data.
         * 1. Check that the frame is not null
         * 2. Ensure that the frame has at least one tracked face.
         * Once those two conditions are satisfied, we can call the method GetAndRefreshFaceAlignmentResult
         * so that the facial points and properties are updated.
         * *NOTE*: That the facial points are represented by verticies (3D points) which are all stored in an array
         * These verticies are of triangles such that a 3D triangular mesh can be constructed using 
         * these verticies to display the face. */

        private void Reader_FaceFrameArrived(object sender, HighDefinitionFaceFrameArrivedEventArgs hde) {

            using (var frame = hde.FrameReference.AcquireFrame()) {

                if (frame != null && frame.IsFaceTracked) {

                    frame.GetAndRefreshFaceAlignmentResult(this.faceAlignment);

                    /* Only update the ShapeAnimationUnit information to the screen every 
                     * certain number of frames per second */

                    if (faceDisplayOffsetCounter >= textDisplayerOffset) {

                        this.UpdateFaceInformationText();
                        faceDisplayOffsetCounter = 0;

                    } else { ++faceDisplayOffsetCounter; }

                    if (dataCollectionOffsetCounter >= dataCollectionOffset - 1) { // fires every 3 frames

                        this.CollectFaceData();
                        dataCollectionOffsetCounter = 0;

                    } else { ++dataCollectionOffsetCounter; }

                    this.UpdateFacePoints();

                    this.DrawFaceEmotion(0);

                } // end of outer if

            } // end of using 

        } // end of Reader_FaceFrameArrived

        /* We have a list of CamerSpacePoint objects and a list of Ellipse objets which we will add
         * to the canvas and specify their x and y position.
         * *NOTE*: That the x, y, and z coordinate are measured in meters. In order to properly find the corresponding
         * pixel values, we use a Coordinate Mapper which is a built in mechanism that converts between 3D space positions 
         * to 2D screen positions. */

        private void UpdateFacePoints() {

            /* If there is no face being tracked, simply return */

            if (this.faceModel == null) return;

            /* Get the facial points */

            var verticies = this.faceModel.CalculateVerticesForAlignment(this.faceAlignment);

            if (verticies.Count > 0) {

                if (this.points.Count == 0) {

                    for (int i = 0; i < verticies.Count; ++i) {

                        Ellipse ellipse = new Ellipse {

                            Width = 1.0, 
                            Height = 1.0,
                            Fill = new SolidColorBrush(Colors.White)

                        };

                        this.points.Add(ellipse);

                    } // end of for

                    foreach (Ellipse ellipse in this.points) {

                        faceCanvas.Children.Add(ellipse);

                    } // end of foreach

                } // end of inner if

                /* For all of the facial points, add them to the face canvas */

                for (int i = 0; i < verticies.Count; ++i) {

                    CameraSpacePoint vertex = verticies[i];
                  
                    DepthSpacePoint point = this.coordinateMapper.MapCameraPointToDepthSpace(vertex);

                    /* If the points are off of the screen, do not try to put them on the screen */

                    if (float.IsInfinity(point.X) || float.IsInfinity(point.Y)) return;

                    /* Else, put them on the faceCanvas */

                    Ellipse ellipse = this.points[i];
                    Canvas.SetLeft(ellipse, point.X);
                    Canvas.SetTop(ellipse, point.Y);

                } // end of for 

            } // end of verticies.Count if

        } // end of UpdateFacePoints

        /* This method updates the text that is displayed in the Grid's left column
         * which shows how much a person's face is in the given states (FaceShapeAnimation units) */

        public static bool leftLipCornerPulled = false;
        public static bool rightLipCornerPulled = false;
        public static bool leftLipCornerDepressed = false;
        public static bool rightLipCornerDepressed = false;
        public static bool rightEyebrowLowered = false;
        public static bool leftEyebrowLowered = false;
        public static bool rightEyebrowRaised = false;
        public static bool leftEyebrowRaised = false;
        
        private TextBlock lipCornerPullerLeftTextBlock = new TextBlock();
        private TextBlock lipCornerPullerRightTextBlock = new TextBlock();
        private TextBlock lipCornerDepressorLeftTextBlock = new TextBlock();
        private TextBlock lipCornerDepressorRightTextBlock = new TextBlock();
        private TextBlock rightEyebrowTextBlock = new TextBlock();
        private TextBlock leftEyebrowTextBlock = new TextBlock();
        
        //private TextBlock FAUTextBlock = new TextBlock();

        private void UpdateFaceInformationText() {

            /* If there is a face being tracked... */

            if (faceAlignment != null) {

                /* Get the face's AnimationUnits and for each FaceShapeAnimation, store the name of the AnimationUnit 
                 * as well as its associated floating point value in the faceInfoText string */
                /*
                FAUTextBlock.FontSize = 14;
                FAUTextBlock.FontWeight = FontWeights.Bold;
                */
                this.animationUnits = this.faceAlignment.AnimationUnits;

                string lipCornerPullerLeftText = faceShapeAnimations[0].ToString() + ": " + animationUnits[faceShapeAnimations[0]].ToString() + "\n";
                string lipCornerPullerRightText = faceShapeAnimations[1].ToString() + ": " + animationUnits[faceShapeAnimations[1]].ToString() + "\n";
                string lipCornerDepressorLeftText = faceShapeAnimations[2].ToString() + ": " + animationUnits[faceShapeAnimations[2]].ToString() + "\n";
                string lipCornerDepressorRightText = faceShapeAnimations[3].ToString() + ": " + animationUnits[faceShapeAnimations[3]].ToString() + "\n";
                string rightEyebrowText = faceShapeAnimations[4].ToString() + ": " + animationUnits[faceShapeAnimations[4]].ToString() + "\n";
                string leftEyebrowText = faceShapeAnimations[5].ToString() + ": " + animationUnits[faceShapeAnimations[5]].ToString() + "\n";
                
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
                
                /*
                if (leftLipCornerPulled) { FAUTextBlock.Inlines.Add(new Run(lipCornerPullerLeftText) { Foreground = Brushes.Green }); }
                else { FAUTextBlock.Inlines.Add(new Run(lipCornerPullerLeftText) { Foreground = Brushes.Black }); }

                if (rightLipCornerPulled) { FAUTextBlock.Inlines.Add(new Run(lipCornerPullerRightText) { Foreground = Brushes.Green }); }
                else { FAUTextBlock.Inlines.Add(new Run(lipCornerPullerRightText) { Foreground = Brushes.Black }); }
                */
                
                if (leftLipCornerPulled) { lipCornerPullerLeftTextBlock.Foreground = new SolidColorBrush(Colors.Blue); }
                else { lipCornerPullerLeftTextBlock.Foreground = new SolidColorBrush(Colors.Black); }

                if (rightLipCornerPulled) { lipCornerPullerRightTextBlock.Foreground = new SolidColorBrush(Colors.Blue); }
                else { lipCornerPullerRightTextBlock.Foreground = new SolidColorBrush(Colors.Black); }

                if (leftLipCornerDepressed) { lipCornerDepressorLeftTextBlock.Foreground = new SolidColorBrush(Colors.Blue); }
                else { lipCornerDepressorLeftTextBlock.Foreground = new SolidColorBrush(Colors.Black); }

                if (rightLipCornerDepressed) { lipCornerDepressorRightTextBlock.Foreground = new SolidColorBrush(Colors.Blue); }
                else { lipCornerDepressorRightTextBlock.Foreground = new SolidColorBrush(Colors.Black); }

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
                
                if (!textCanvas.Children.Contains(lipCornerDepressorLeftTextBlock)) {

                    this.textCanvas.Children.Add(lipCornerPullerLeftTextBlock);
                    this.textCanvas.Children.Add(lipCornerPullerRightTextBlock);
                    this.textCanvas.Children.Add(lipCornerDepressorLeftTextBlock);
                    this.textCanvas.Children.Add(lipCornerDepressorRightTextBlock);
                    this.textCanvas.Children.Add(rightEyebrowTextBlock);
                    this.textCanvas.Children.Add(leftEyebrowTextBlock);

                }
                 
                //if (!this.textCanvas.Children.Contains(FAUTextBlock)) { this.textCanvas.Children.Add(FAUTextBlock); }

            } // end if

        } // end of UpdateFaceInformationText

        /* In case the sensor becomes unavailable (E.g. paused, closed, unplugged) handle that event. */

        private void Sensor_IsAvailableChanged(object sender, IsAvailableChangedEventArgs e) {

            if (this.kinectSensor != null) {

                this.StatusText = this.kinectSensor.IsAvailable ? Properties.Resources.RunningStatusText
                                                                : Properties.Resources.SensorNotAvailableStatusText;

            } 

        } // end of Sensor_IsAvailableChanged

    } // end of partial class MainWindow

} // end of namespace ProjectEDDIE