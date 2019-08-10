/* 
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

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using System.ComponentModel;
using System.Globalization;
using Microsoft.Kinect;
using Microsoft.Kinect.Face;

namespace ProjectEDDIE {

    public partial class MainWindow : Window, INotifyPropertyChanged, IDisposable {

        // The INotifyPropertyChange  event is to allow the window controls to bind to changeable data
        public event PropertyChangedEventHandler PropertyChanged;

        private KinectSensor kinectSensor = null; // The reference to the Kinect v2 sensor
        private BodyFrameSource bodyFrameSource = null; // 
        private BodyFrameReader bodyFrameReader = null; // Reads the body frame data
        private CoordinateMapper coordinateMapper = null; // This allows us to map a 3D point to the 2D window.
        private HighDefinitionFaceFrameSource hdFaceFrameSource = null;  // Acquires the HD face data
        private HighDefinitionFaceFrameReader hdFaceFrameReader = null; // Reads the HD face data
        private FaceAlignment faceAlignment = null; // This is required to access the face verticies
        private FaceModel faceModel = null; // This is required to access the face model points
        private List<Ellipse> points = new List<Ellipse>(); // Used to display all the facial points in the MainWindow
        private string statusText = null; // The current status text to display
        private DrawingGroup drawingGroup; // Drawing group for displaying face information
        private DrawingImage imageSource; // Drawing image that we will display 
        private TextBlock textBlock = new TextBlock(); // Used to dynamically display the AU values of the tracked face
        
        // Stores the floating point value for each corresponding FaceShapeAnimation
        private IReadOnlyDictionary<FaceShapeAnimations, float> animationUnits = null; 

        // This array contains all of the FaceShapeAnimations that we want to keep track of
        private FaceShapeAnimations[] faceShapeAnimations = {
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

        // The Constructor 
        public MainWindow() {

            // Currently the Kinect v2 is the only sensor supported so simply get that
            this.kinectSensor = KinectSensor.GetDefault();

            if (kinectSensor != null){

                this.coordinateMapper = this.kinectSensor.CoordinateMapper;

                // Listen for the body data
                this.bodyFrameSource = this.kinectSensor.BodyFrameSource;
                this.bodyFrameReader = this.kinectSensor.BodyFrameSource.OpenReader();
                this.bodyFrameReader.FrameArrived += Reader_BodyFrameArrived;

                // Listen for HD face data.
                this.hdFaceFrameSource = new HighDefinitionFaceFrameSource(this.kinectSensor);
                this.hdFaceFrameReader = this.hdFaceFrameSource.OpenReader();
                this.hdFaceFrameReader.FrameArrived += Reader_FaceFrameArrived;

                this.faceModel = new FaceModel();
                this.faceAlignment = new FaceAlignment();

                this.kinectSensor.IsAvailableChanged += this.Sensor_IsAvailableChanged;

                this.kinectSensor.Open();

                this.StatusText = this.kinectSensor.IsAvailable ? Properties.Resources.RunningStatusText
                                                                : Properties.Resources.NoSensorStatusText;
            }
            this.drawingGroup = new DrawingGroup();
            this.imageSource = new DrawingImage(this.drawingGroup);
            this.DataContext = this;
            this.InitializeComponent();
        }

        // Gets the bitmap to display
        public ImageSource ImageSource {
            get { return this.imageSource; }
        }

        // Gets or sets the current status text to display
        public string StatusText {
            get { return this.statusText; }
            set {
                if (this.statusText != value) {
                    this.statusText = value;

                    // notify any bound elements that the text has changed
                    if (this.PropertyChanged != null) {
                        this.PropertyChanged(this, new PropertyChangedEventArgs("StatusText"));
                    }
                }
            }
        }

        // Called when this object is no longer needed in the program (when it shutsdown).
        public void Dispose() {
            this.Dispose(true);
            GC.SuppressFinalize(this);
        }

        protected virtual void Dispose(bool disposing) {
            if (disposing && this.faceModel != null) {
                this.faceModel.Dispose();
                this.faceModel = null;
            }
        }

        // We connect a body to a face by setting the TrackingId property of the Face source
        private void Reader_BodyFrameArrived(object sender, BodyFrameArrivedEventArgs e) {
            using (var frame = e.FrameReference.AcquireFrame()) {
                if (frame != null) {
                    Body[] bodies = new Body[frame.BodyCount];
                    frame.GetAndRefreshBodyData(bodies);
                    Body body = bodies.Where(b => b.IsTracked).FirstOrDefault();
                    if (!this.hdFaceFrameSource.IsTrackingIdValid && body != null) {
                        this.hdFaceFrameSource.TrackingId = body.TrackingId;
                    }
                }
            }
        }

        // We need to check two conditions first in order to access the face point data.
        // 1. Check that the frame is not null
        // 2. Ensure that the frame has at least one tracked face.
        // Once those two conditions are satisfied, we can call the method GetAndRefreshFaceAlignmentResult
        // so that the facial points and properties are updated.
        // *NOTE*: That the facial points are represented by verticies (3D points) which are all stored in an array
        // These verticies are of triangles such that a 3D triangular mesh can be constructed using 
        // these verticies to display the face.

        private int framesPerSecond = 30;
        private int wantedFramesPerSecond = 3;
        private int counter = 0;

        private void Reader_FaceFrameArrived(object sender, HighDefinitionFaceFrameArrivedEventArgs e) {
            using (var frame = e.FrameReference.AcquireFrame()) {
                if (frame != null && frame.IsFaceTracked) {
                    frame.GetAndRefreshFaceAlignmentResult(this.faceAlignment);
                    if (counter == framesPerSecond / wantedFramesPerSecond) {
                        this.UpdateFaceInformationText();
                        counter = 0;
                    } else {
                        ++counter;
                    }
                    this.UpdateFacePoints();  
                }
            }
        }

        // We have a list of CamerSpacePoint objects and a list of Ellipse objets which we will add
        // to the canvas and specify their x and y position.
        // *NOTE*: That the x, y, and z coordinate are measured in meters. In order to properly find the corresponding
        // pixel values, we use a Coordinate Mapper which is a built in mechanism that converts between 3D space positions 
        // to 2D screen positions.
        private void UpdateFacePoints() {
            if (this.faceModel == null) return;
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
                    }
                    foreach (Ellipse ellipse in this.points) {
                        faceCanvas.Children.Add(ellipse);
                    }
                }
                for (int i = 0; i < verticies.Count; ++i) {
                    CameraSpacePoint vertex = verticies[i];
                    DepthSpacePoint point = this.coordinateMapper.MapCameraPointToDepthSpace(vertex);
                    if (float.IsInfinity(point.X) || float.IsInfinity(point.Y)) return;
                    Ellipse ellipse = this.points[i];
                    Canvas.SetLeft(ellipse, point.X);
                    Canvas.SetTop(ellipse, point.Y);
                }
            }
        }

        private void UpdateFaceInformationText() {
            string faceInfoText = string.Empty;
            if (faceAlignment != null) {
                this.animationUnits = this.faceAlignment.AnimationUnits;
                foreach (FaceShapeAnimations faceShapeAnimation in faceShapeAnimations) {
                    faceInfoText += faceShapeAnimation.ToString() + " : ";
                    faceInfoText += animationUnits[faceShapeAnimation].ToString() + "\n\n";
                }
            } 
            this.textBlock.Text = faceInfoText;
            this.textBlock.FontSize = 11;
            this.textBlock.TextWrapping = TextWrapping.Wrap;
            this.textBlock.Foreground = new SolidColorBrush(Colors.Black);
            Canvas.SetLeft(this.textBlock, 0.0);
            Canvas.SetTop(this.textBlock, 10.0);
            if (!textCanvas.Children.Contains(this.textBlock)) {
                textCanvas.Children.Add(this.textBlock);
            }
        }

        // In case the sensor becomes unavailable (E.g. paused, closed, unplugged) handle that event.
        private void Sensor_IsAvailableChanged(object sender, IsAvailableChangedEventArgs e) {
            if (this.kinectSensor != null) {
                this.StatusText = this.kinectSensor.IsAvailable ? Properties.Resources.RunningStatusText
                                                                : Properties.Resources.SensorNotAvailableStatusText;
            }
        }
    }
}
