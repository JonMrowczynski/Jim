/*
	Name: KinectEmotionDeterminerWrapper.h
	Date: 2/13/2016
	Author: Jon Mrowczynski

   The class structure for the C# wrapper class. It contains the necessary fields and 
   methods to handle intermediate processes such that the information gathered from 
   the Kinect can be transfered to either another C++ program, or to a Java program 
   using Java's JNI. 
   
   Note that this class is currently marked as sealed which means that none of the 
   methods should be overridden 
*/

#pragma once

#include <iostream>

public ref class KinectEmotionDeterminerWrapper sealed {

	private:

		// The amount of change from the neutral position of the human's left lip corner puller for the Kinect to register the FAU.
		const double deltaLeftLipCornerPuller = 0.12;

		// The amount of change from the netrual position of the human's right lip corner puller for the Kinect to register the FAU.
		const double deltaRightLipCornerPuller = 0.12;

		// The amount of change from the neutral position of the human's left lip corner depressor for the Kinect to register the FAU.
		const double deltaLeftLipCornerDepressor = 0.15;

		// The amount of change from the neutral position of the human's right lip corner depressor for the Kinect to register the FAU.
		const double deltaRightLipCornerDepressor = 0.15;

		// The amount of change from the neutral position of the human's right eyebrow for the Kinect to register the FAU.
		const double deltaRightEyebrowLowerer = 0.12;

		// The amount of change from the netrual position of the human's left eyebrow for the Kinect to register the FAU.
		const double deltaLeftEyebrowLowerer = 0.12;

		// The amount of change from the neutral position of the human's right eyebrow for the Kinect to register the FAU.
		const double deltaRightEyebrowRaiser = 0.15;

		// The amount of change from the neutral position of the human's left eyebrow for the Kinect to register the FAU.
		const double deltaLeftEyebrowRaiser = 0.15;

		// The neutral state of the left lip corner puller of the human assigned after calibration.
		double leftLipCornerPullerNeutral;

		// The neutral state of the right lip corner puller of the human assigned after calibration.
		double rightLipCornerPullerNeutral;

		// The neutral state of the left lip corner depressor of the human assigned after calibration.
		double leftLipCornerDepressorNeutral;

		// The netrual state of the right lip corner depressor of the human assigned after calibration.
		double rightLipCornerDepressorNeutral;

		// The netrual state of the right eyebrow of the human assigned after calibration.
		double rightEyebrowNeutral;

		// The neutral state of the left eyebrow of the human assigned after calibration.
		double leftEyebrowNeutral;

		// This function is given to the guiThread such that a single STA thread runs the gui for the MainWindow.
		void runKinect(void);

	public:

		// The object that is wrapped by this class to allow for the Kinect C# code to be used with the JNI.
		KinectEmotionDeterminer::MainWindow^ mainWindow;

		// The STA Thread that runs the Kinect code.
		System::Threading::Thread^ guiThread;

		// A boolean indicating whether the human is expressing the left lip corner pulled FAU.
		bool leftLipCornerPulled = false;

		// A boolean indicating wheather the human is expressing the right lip corner pulled FAU.
		bool rightLipCornerPulled = false;

		// A boolean indicating wheather the human is expressing the left lip corner depressed FAU.
		bool leftLipCornerDepressed = false;

		// A boolean indicating wheather the human is expressing the right lip corner depressed FAU.
		bool rightLipCornerDepressed = false;

		// A boolean indicating wheather the human is expressing the left eyebrow lowered FAU.
		bool leftEyebrowLowered = false;

		// A boolean indicating wheather the human is expressing the right eyebrow lowered FAU.
		bool rightEyebrowLowered = false;

		// A boolean indicating wheather the human is expressing the right left eyebrow raised FAU.
		bool leftEyebrowRaised = false;

		// A boolean indicating wheather the human is expressing the right right eyebrow raised FAU.
		bool rightEyebrowRaised = false;

		// The constructor that sets up the GUI thread that runs the runKinect method.
		KinectEmotionDeterminerWrapper();

		// Returns a snapshot of the human's facial data.
		cli::array<double>^ getFacialData(void);

		// Determines if the individual is expressing any of the facial action units after calibration.
		void checkFAUs(cli::array<double>^);

		// Calibrates the neutral FAU values for the human to acquire relative (rather than absolute) threshold values.
		// If the calibration data is determined to be invalid, the user can rerun the calibration routine. 
		// Multi-threading practices need to be carried out since the GUI (Kinect) thread needs to communicate with 
		// the main (console) thread.
		void calibrate(void);

		// Updates the text that indicates which emotion the human is displaying.
		void updateDisplayedEmotionText(System::String^ emotion);

		// Returns a boolean indicating whether the human is expressing all of the FAUs of a happy face.
		bool isHappy(void);

		// Returns a boolean indicating whether the human is expressing all of the FAUS of a sad face.
		bool isSad(void);

		// Returns a boolean indicating whether the human is expressing all of the FAUS of an angry face.
		bool isAngry(void);

		// Returns a boolean indicating whether the human is expressing all of the FAUS of their neutral face.
		bool isNeutral(void);

};