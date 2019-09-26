/*
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
	Author: Jon Mrowczynski
*/

#pragma once

#include <iostream>

/*
	KinectEmotionDeterminerWrapper is a C++ wrapper class that is designed to help interface the Kinect code, which is
	in C#, with the Java master control software via the JNI.
*/

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
		KinectEmotionDeterminerNS::KinectEmotionDeterminer^ kinectEmotionDeterminer;

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

		// Returns a boolean indicating whether the human is expressing all of the FAUs of a sad face.
		bool isSad(void);

		// Returns a boolean indicating whether the human is expressing all of the FAUs of an angry face.
		bool isAngry(void);

		// Returns a boolean indicating whether the human is expressing all of the FAUs of their neutral face.
		bool isNeutral(void);
};