using Microsoft.Kinect;
using Microsoft.Kinect.Face;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Globalization;
using System.Windows;
using System.Windows.Media;

namespace Test
{
    /// <summary>
    /// Interaction logic for Test
    /// </summary>
    
    public partial class FaceRecognitionTester : Window, INotifyPropertyChanged
    {
        /// <summary>
        /// Thickness of the rectagle that bounds each face as well as the thickness of the face points.
        /// </summary>
        
        private const double faceShapeThickness = 8;

        /// <summary>
        /// Font size used for the text indicating that a face could not be found.
        /// </summary>
         
        private const double faceNotTrackedTextFontSize = 100;

        /// <summary>
        /// Font size of the face property text.
        /// </summary>
         
        private const double textFontSize = 30;

        /// <summary>
        /// The radius of each face point circle.
        /// </summary>
         
        private const double facePointRadius = 1.0;

        /// <summary>
        /// Text layout x offset.
        /// </summary>

        private const float textLayoutXOffset = -0.1f;

        /// <summary>
        /// Text layout y offset.
        /// </summary>

        private const float textLayoutYOffset = -0.15f;

        /// <summary>
        /// Face rotation display angle increment in degrees.
        /// </summary>

        private const double FaceRotationIncrementInDegrees = 5.0;

        /// <summary>
        /// Formatted text to indicate that there are no bodies/faces tracked in the FOV.
        /// </summary>

        private readonly FormattedText faceNotTrackedText = new FormattedText(
                        "             No faces are being tracked. :( \n\n" + "  Don't be shy, come on over! I don't byte!",
                        CultureInfo.GetCultureInfo("en-us"),
                        FlowDirection.LeftToRight,
                        new Typeface("Georgia"),
                        faceNotTrackedTextFontSize,
                        Brushes.White);

        /// <summary>
        /// The origin for the no face tracked message.
        /// </summary>

        private readonly Point faceNotTrackedTextOrigin = new Point(10.0, 400.0);

        /// <summary>
        /// Drawing group for body rendering output.
        /// </summary>

        private readonly DrawingGroup drawingGroup;

        /// <summary>
        /// Drawing image that we will display.
        /// </summary>

        private readonly DrawingImage imageSource;

        /// <summary>
        /// Coordinate mapper to map one type of point to another.
        /// </summary>

        private readonly CoordinateMapper coordinateMapper = null;

        /// <summary>
        /// Array to store bodies.
        /// </summary>

        private readonly Body[] bodies = null;

        /// <summary>
        /// The maximum number of bodies that can be tracked by the Kinect sensor.
        /// </summary>

        private readonly int maxBodyCount;

        /// <summary>
        /// Array of FaceFrameSources, one for each body.
        /// </summary>

        private readonly FaceFrameSource[] faceFrameSources = null;

        /// <summary>
        /// Array of FaceFrameReaders, one for each body.
        /// </summary>

        private readonly FaceFrameReader[] faceFrameReaders = null;

        /// <summary>
        /// Array of FaceFameResults, one for each body.
        /// </summary>

        private readonly FaceFrameResult[] faceFrameResults = null;

        /// <summary>
        /// The width of the display (color space).
        /// </summary>

        private readonly int displayWidth;

        /// <summary>
        /// The height of the display (color space).
        /// </summary>

        private readonly int displayHeight;

        /// <summary>
        /// Array of Burshes to color each tracked face a different color.
        /// </summary>

        private readonly Brush[] faceBrush;

        /// <summary>
        /// The Kinect sensor.
        /// </summary>

        private KinectSensor kinectSensor = null;

        /// <summary>
        /// Reader for body frames
        /// </summary>

        private BodyFrameReader bodyFrameReader = null;

        /// <summary>
        /// The display rectangle.
        /// </summary>

        private Rect displayRect;

        /// <summary>
        /// Current status text to display.
        /// </summary>

        private string statusText = null;

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
                    // Notify any bound elements that the text has changed
                    PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(StatusText)));
                }
            }
        }

        /// <summary>
        /// Initializes a new instance of the FaceRecognitionTester class.
        /// </summary>
        
        public FaceRecognitionTester()
        {
            kinectSensor = KinectSensor.GetDefault();                                           // Get the Kinect sensor
            coordinateMapper = kinectSensor.CoordinateMapper;                                   // Get the coordinate mapper
            FrameDescription frameDescription = kinectSensor.ColorFrameSource.FrameDescription; // Get the color frame details

            // Set the display specifics
            displayWidth = frameDescription.Width;
            displayHeight = frameDescription.Height;
            displayRect = new Rect(0.0, 0.0, displayWidth, displayHeight);

            bodyFrameReader = kinectSensor.BodyFrameSource.OpenReader();    // Open the reader for the body frames
            bodyFrameReader.FrameArrived += Reader_BodyFrameArrived;        // Wire handler for body frame arrival
            maxBodyCount = kinectSensor.BodyFrameSource.BodyCount;          // Set the maximum number of bodies that would be tracked by Kinect
            bodies = new Body[maxBodyCount];                                // Allocate storage to store body objects

            // specify the required face frame results
            FaceFrameFeatures faceFrameFeatures =
                FaceFrameFeatures.BoundingBoxInColorSpace
                | FaceFrameFeatures.PointsInColorSpace
                | FaceFrameFeatures.RotationOrientation
                | FaceFrameFeatures.FaceEngagement
                | FaceFrameFeatures.Glasses
                | FaceFrameFeatures.Happy
                | FaceFrameFeatures.LeftEyeClosed
                | FaceFrameFeatures.RightEyeClosed
                | FaceFrameFeatures.LookingAway
                | FaceFrameFeatures.MouthMoved
                | FaceFrameFeatures.MouthOpen;

            // Create a face frame source and reader to track each face in the FOV.
            faceFrameSources = new FaceFrameSource[maxBodyCount];
            faceFrameReaders = new FaceFrameReader[maxBodyCount];
            for (int i = 0; i < maxBodyCount; i++)
            {
                faceFrameSources[i] = new FaceFrameSource(kinectSensor, 0, faceFrameFeatures);
                faceFrameReaders[i] = faceFrameSources[i].OpenReader();
            }
            faceFrameResults = new FaceFrameResult[maxBodyCount]; // Allocate storage to store face frame results for each face in the FOV

            // Populate face result colors - one for each face index
            faceBrush = new Brush[] { Brushes.White, Brushes.Orange, Brushes.Green, Brushes.Red, Brushes.LightBlue, Brushes.Yellow };

            kinectSensor.IsAvailableChanged += Sensor_IsAvailableChanged;   // Set IsAvailableChanged event notifier
            kinectSensor.Open();                                            // Open the sensor

            // Set the status text
            StatusText = kinectSensor.IsAvailable ? Properties.Resources.RunningStatusText : Properties.Resources.NoSensorStatusText;

            drawingGroup = new DrawingGroup();              // Create the drawing group we'll use for drawing
            imageSource = new DrawingImage(drawingGroup);   // Create an image source that we can use in our image control
            DataContext = this;                             // Use the window object as the view model in this simple example
            InitializeComponent();                          // Initialize the components (controls) of the window
        }

        /// <summary>
        /// Execute start up tasks.
        /// </summary>
        /// <param name="sender">object sending the event</param>
        /// <param name="e">event arguments</param>

        private void FaceRecognitionTester_Loaded(object sender, RoutedEventArgs e)
        {
            for (int i = 0; i < maxBodyCount; i++)
            {
                if (faceFrameReaders[i] != null)
                {
                    faceFrameReaders[i].FrameArrived += Reader_FaceFrameArrived; // Wire handler for face frame arrival
                }
            }

            if (bodyFrameReader != null)
            {
                bodyFrameReader.FrameArrived += Reader_BodyFrameArrived; // Wire handler for body frame arrival
            }
        }

        /// <summary>
        /// Execute shutdown tasks.
        /// </summary>
        /// <param name="sender">object sending the event</param>
        /// <param name="e">event arguments</param>

        private void FaceRecognitionTester_Closing(object sender, CancelEventArgs e)
        {
            for (int i = 0; i < maxBodyCount; ++i)
            {
                // FaceFrameReader and FaceFrameSource are IDisposable
                faceFrameReaders[i]?.Dispose();
                faceFrameSources[i]?.Dispose();
                faceFrameReaders[i] = null;
                faceFrameSources[i] = null;
            }

            // BodyFrameReader is IDisposable
            bodyFrameReader?.Dispose();
            bodyFrameReader = null;

            kinectSensor?.Close();
            kinectSensor = null;
        }

        /// <summary>
        /// Converts rotation quaternion to Euler angles and then maps them to a specified range of values to control the refresh rate.
        /// </summary>
        /// <param name="rotQuaternion">face rotiation</param>
        /// <param name="pitch">rotation about the x-axis</param>
        /// <param name="yaw">rotation about the y-axis</param>
        /// <param name="roll">rotation about the z-axis</param>

        private static void ExtractFaceRotationInDegrees(Vector4 rotQuaternion, out int pitch, out int yaw, out int roll)
        {
            float x = rotQuaternion.X;
            float y = rotQuaternion.Y;
            float z = rotQuaternion.Z;
            float w = rotQuaternion.W;

            // Convert face rotation quaternion to Euler angles in degrees
            double yawD, pitchD, rollD;
            pitchD = Math.Atan2(2 * ((y * z) + (w * x)), (w * w) - (x * x) - (y * y) + (z * z)) / Math.PI * 180.0;
            yawD = Math.Asin(2 * ((w * y) - (x * z))) / Math.PI * 180.0;
            rollD = Math.Atan2(2 * ((x * y) + (w * z)), (w * w) + (x * x) - (y * y) - (z * z)) / Math.PI * 180.0;

            // Clamp the values to a multiple of the specified increment to control the refresh rate
            double increment = FaceRotationIncrementInDegrees;
            pitch = (int) (Math.Floor((pitchD + (increment / 2.0 * (pitchD > 0 ? 1.0 : -1.0))) / increment) * increment);
            yaw = (int) (Math.Floor((yawD + (increment / 2.0 * (yawD > 0 ? 1.0 : -1.0))) / increment) * increment);
            roll = (int) (Math.Floor((rollD + (increment / 2.0 * (rollD > 0 ? 1.0 : -1.0))) / increment) * increment);
        }

        /// <summary>
        /// Handles the face frame data arriving from the sensor.
        /// </summary>
        /// <param name="sender">object sending the event</param>
        /// <param name="e">event arguments</param>

        private void Reader_FaceFrameArrived(object sender, FaceFrameArrivedEventArgs e)
        {
            using (FaceFrame faceFrame = e.FrameReference.AcquireFrame())
            {
                if (faceFrame != null)
                {
                    int index = GetFaceSourceIndex(faceFrame.FaceFrameSource); // Get the index of the face source from the face source array

                    // Check if this face frame has valid face frame results
                    faceFrameResults[index] = ValidateFaceBoxAndPoints(faceFrame.FaceFrameResult) ? faceFrame.FaceFrameResult : null;
                }
            }
        }

        /// <summary>
        /// Returns the index of the face frame source.
        /// </summary>
        /// <param name="faceFrameSource">the face frame source</param>
        /// <returns>the index of the face source in the face source array</returns>

        private int GetFaceSourceIndex(FaceFrameSource faceFrameSource)
        {
            int index = -1;
            for (int i = 0; i < maxBodyCount; i++)
            {
                if (faceFrameSources[i] == faceFrameSource)
                {
                    index = i;
                    break;
                }
            }
            return index;
        }

        /// <summary>
        /// Handles the body frame data arriving from the sensor.
        /// </summary>
        /// <param name="sender">object sending the event</param>
        /// <param name="e">event arguments</param>

        private void Reader_BodyFrameArrived(object sender, BodyFrameArrivedEventArgs e)
        {
            using (BodyFrame bodyFrame = e.FrameReference.AcquireFrame())
            {
                if (bodyFrame != null)
                {
                    bodyFrame.GetAndRefreshBodyData(bodies); // Update body data

                    using (DrawingContext dc = drawingGroup.Open())
                    {
                        dc.DrawRectangle(Brushes.Black, null, displayRect); // Draw the dark background

                        bool drawFaceResult = false;

                        for (int i = 0; i < maxBodyCount; i++) // Iterate through each face source
                        {
                            if (faceFrameSources[i].IsTrackingIdValid) // Check if a valid face is tracked in this face source
                            {
                                if (faceFrameResults[i] != null) // Check if we have valid face frame results
                                {
                                    DrawFaceFrameResults(i, faceFrameResults[i], dc); // Draw face frame results
                                    drawFaceResult = true;
                                }
                            }
                            else if (bodies[i].IsTracked) // Check if the corresponding body is tracked 
                            {
                                faceFrameSources[i].TrackingId = bodies[i].TrackingId; // Update the face frame source to track this body
                            }
                        }

                        if (!drawFaceResult)
                        {
                            /*
                             * If no faces were drawn then this indicates one of the following:
                             * A body was not tracked
                             * A body was tracked but the corresponding face was not tracked
                             * A body and the corresponding face was tracked though the face box or the face points were not valid
                             */
                            dc.DrawText(faceNotTrackedText, faceNotTrackedTextOrigin);
                        }
                        drawingGroup.ClipGeometry = new RectangleGeometry(displayRect);
                    }
                }
            }
        }

        /// <summary>
        /// Draw face frame results.
        /// </summary>
        /// <param name="faceIndex">the index of the face frame corresponding to a specific body in the FOV</param>
        /// <param name="faceResult">container of all face frame results</param>
        /// <param name="drawingContext">drawing context to render to</param>

        private void DrawFaceFrameResults(int faceIndex, FaceFrameResult faceResult, DrawingContext drawingContext)
        {
            // Choose the brush based on the face index
            Brush drawingBrush = faceIndex < maxBodyCount ? faceBrush[faceIndex] : faceBrush[0];
            Pen drawingPen = new Pen(drawingBrush, faceShapeThickness);

            // Draw the face bounding box
            RectI faceBoxSource = faceResult.FaceBoundingBoxInColorSpace;
            Rect faceBox = new Rect(faceBoxSource.Left, faceBoxSource.Top, faceBoxSource.Right - faceBoxSource.Left, faceBoxSource.Bottom - faceBoxSource.Top);
            drawingContext.DrawRectangle(null, drawingPen, faceBox);

            if (faceResult.FacePointsInColorSpace != null)
            {
                // Draw each face point
                foreach (PointF pointF in faceResult.FacePointsInColorSpace.Values)
                {
                    drawingContext.DrawEllipse(null, drawingPen, new Point(pointF.X, pointF.Y), facePointRadius, facePointRadius);
                }
            }

            string faceText = string.Empty;

            // Extract each face property information and store it in faceText
            if (faceResult.FaceProperties != null)
            {
                foreach (KeyValuePair<FaceProperty, DetectionResult> item in faceResult.FaceProperties)
                {
                    faceText += item.Key.ToString() + " : ";

                    // Consider a "maybe" as a "no" to restrict the detection result refresh rate
                    faceText += item.Value == DetectionResult.Maybe ? DetectionResult.No + "\n" : item.Value.ToString() + "\n";
                }
            }

            // Extract face rotation in degrees as Euler angles
            if (faceResult.FaceRotationQuaternion != null)
            {
                ExtractFaceRotationInDegrees(faceResult.FaceRotationQuaternion, out int pitch, out int yaw, out int roll);
                faceText += "FaceYaw : " + yaw + "\n" +
                            "FacePitch : " + pitch + "\n" +
                            "FacenRoll : " + roll + "\n";
            }

            // Render the face property and face rotation information
            if (GetFaceTextPositionInColorSpace(faceIndex, out Point faceTextLayout))
            {
                drawingContext.DrawText(
                        new FormattedText(
                            faceText,
                            CultureInfo.GetCultureInfo("en-us"),
                            FlowDirection.LeftToRight,
                            new Typeface("Georgia"),
                            textFontSize,
                            drawingBrush),
                        faceTextLayout);
            }
        }

        /// <summary>
        /// Computes the face result text position by adding an offset to the corresponding body's head joint in camera space and then 
        /// by projecting it to the screen space.
        /// </summary>
        /// <param name="faceIndex">the index of the face frame corresponding to a specific body in the FOV</param>
        /// <param name="faceTextLayout">the text layout position in screen space</param>
        /// <returns>a boolean indicating success or failure</returns>

        private bool GetFaceTextPositionInColorSpace(int faceIndex, out Point faceTextLayout)
        {
            faceTextLayout = new Point();
            bool isLayoutValid = false;
            Body body = bodies[faceIndex];
            if (body.IsTracked)
            {
                CameraSpacePoint headJoint = body.Joints[JointType.Head].Position;
                CameraSpacePoint textPoint = new CameraSpacePoint()
                {
                    X = headJoint.X + textLayoutXOffset,
                    Y = headJoint.Y + textLayoutYOffset,
                    Z = headJoint.Z
                };
                ColorSpacePoint textPointInColor = coordinateMapper.MapCameraPointToColorSpace(textPoint);
                faceTextLayout.X = textPointInColor.X;
                faceTextLayout.Y = textPointInColor.Y;
                isLayoutValid = true;
            }
            return isLayoutValid;
        }

        /// <summary>
        /// Validate face bounding box and face points to be within screen space.
        /// </summary>
        /// <param name="faceResult">the face frame result containing face box and points</param>
        /// <returns>a boolean indicating success or failure</returns>

        private bool ValidateFaceBoxAndPoints(FaceFrameResult faceResult)
        {
            bool isFaceValid = faceResult != null;

            if (isFaceValid)
            {
                RectI faceBox = faceResult.FaceBoundingBoxInColorSpace;
                if (faceBox != null)
                {
                    // Check if we have a valid rectangle within the bounds of the screen space
                    isFaceValid = (faceBox.Right - faceBox.Left) > 0 &&
                                  (faceBox.Bottom - faceBox.Top) > 0 &&
                                  faceBox.Right <= displayWidth &&
                                  faceBox.Bottom <= displayHeight;

                    if (isFaceValid)
                    {
                        IReadOnlyDictionary<FacePointType, PointF> facePoints = faceResult.FacePointsInColorSpace;
                        if (facePoints != null)
                        {
                            foreach (PointF pointF in facePoints.Values)
                            {
                                // Check if we have a valid face point within the bounds of the screen space
                                bool isFacePointValid = pointF.X > 0.0f && pointF.Y > 0.0f && pointF.X < displayWidth && pointF.Y < displayHeight;
                                if (!isFacePointValid)
                                {
                                    isFaceValid = false;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            return isFaceValid;
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
                // On failure, set the status text
                StatusText = kinectSensor.IsAvailable ? Properties.Resources.RunningStatusText : Properties.Resources.SensorNotAvailableStatusText;
            }
        }
    }
}