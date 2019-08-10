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

	public:

		/* The constructor and destructor for the class */

		KinectEmotionDeterminerWrapper();
		~KinectEmotionDeterminerWrapper();

		/* This method acquires the user's facial data over a period of time*/

		double* getFacialData(void);

		/* Calibrates the neutral values for the user such that we can acquire relative 
		   rather than absolute thresholding values. If, for whatever reason the calibration 
		   data is determined to be invalid, the user can decide to run the callibration
		   process again. */

		void calibrate(void);

		/* Checks to see if the individual is expressing any of the facial actions units */

		void checkFAUs(double*);

		/* Updates MainWindow's boolean fields (the same boolean fields that this class
		   has, so that the MainWindow can know to redraw the text a different color 
		   when the user is showing one or more of the FAUs. */

		void updateMainWindowBooleans(void);

		/* Updates the string which */

		void updateDisplayedEmotionText(System::String^ emotion);

		/* Checks to see whether the user is expressing the corresponding emotion */

		bool isHappy(void);
		bool isSad(void);
		bool isAngry(void);
		bool isNeutral(void);

		/* The MainWindow object that this class wraps up so that this code can all eventually 
		   be linked to Java through the JNI */

		KinectEmotionDeterminer::MainWindow^ mainWindow;

		/* The STA Thread that runs MainWindow's window */

		System::Threading::Thread^ guiThread;

		/* The number of face shappe animations that we currently are tracking */

		int numOfFaceShapeAnimations;

		/* Represents whether the user has triggered the corresponding FAU. These are 
		   used to determine whether the individual is expressing some emotion as well
		   as formatting the displayed text differently */

		bool leftLipCornerPulled = false;
	    bool rightLipCornerPulled = false;
		bool leftLipCornerDepressed = false;
		bool rightLipCornerDepressed = false;
		bool rightEyebrowLowered = false;
		bool leftEyebrowLowered = false;
		bool rightEyebrowRaised = false;
		bool leftEyebrowRaised = false;

	private:

		/* The changes in the user's neutral position that requires the Kinect 
		   to register that the usser is expressing the FAUs */

		const double deltaLeftLipCornerPuller = 0.12;
		const double deltaRightLipCornerPuller = 0.12;
		const double deltaLeftLipCornerDepressor = 0.15;
		const double deltaRightLipCornerDepressor = 0.15;
		const double deltaRightEyebrowLowerer = 0.12;
		const double deltaLeftEyebrowLowerer = 0.12;
		const double deltaRightEyebrowRaiser = 0.15;
		const double deltaLeftEyebrowRaiser = 0.15;

		/* These represent the neutral position of the user. These variables are all
		   assigned a value after the calibration routine has been run */

		double leftLipCornerPullerNeutral;		
		double rightLipCornerPullerNeutral;	
		double leftLipCornerDepressorNeutral;
		double rightLipCornerDepressorNeutral;
		double rightEyebrowNeutral;
		double leftEyebrowNeutral;

		/* This is the function that is given to the guiThread so that this Thread 
		   has some task to run when it is started. This is required because we 
		   need a multi-threaded program, but we also need a single STA thread 
		   that only runs the gui for the MainWindow */
		
		void runKinect(void);

		double* averageFaceShapeAnimations = NULL;

}; // end of wrapper class KinectEmotionDeterminerWrapper

