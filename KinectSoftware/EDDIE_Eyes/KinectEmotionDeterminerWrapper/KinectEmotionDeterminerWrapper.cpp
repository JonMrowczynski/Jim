/**	
	This is the main C++ DLL file which will eventually be used to link the 
	Kinect v2 software to the Java master control software using the JNI.

	@author Jon Mrowczynski
*/

#include "stdafx.h"
#include "KinectEmotionDeterminerWrapper.h"

using namespace std;
using namespace System;
using namespace Microsoft::Kinect::Face;
using namespace System::Threading;
using namespace KinectEmotionDeterminer;

KinectEmotionDeterminerWrapper::KinectEmotionDeterminerWrapper(void) {
	guiThread = gcnew Thread(gcnew ThreadStart(this, &KinectEmotionDeterminerWrapper::runKinect));
	guiThread->SetApartmentState(ApartmentState::STA);
	guiThread->Start();
}

void KinectEmotionDeterminerWrapper::runKinect(void) {
	mainWindow = gcnew MainWindow();
	mainWindow->ShowDialog();
}

cli::array<double>^ KinectEmotionDeterminerWrapper::getFacialData(void) {
	double sum = 0;
	double* averageFaceShapeAnimations = new double[MainWindow::faceShapeAnimations->Length];
	mainWindow->ReadyToReadData = true;
	while (!mainWindow->ReadyToPassData);
	mainWindow->ReadyToReadData = false;
	cli::array<double>^ facialData = mainWindow->faceData;
	return facialData;
}

void KinectEmotionDeterminerWrapper::checkFAUs(cli::array<double>^ faceShapeData) {
	leftLipCornerPulled = faceShapeData[0] >= leftLipCornerPullerNeutral + deltaLeftLipCornerPuller ? true : false;
	rightLipCornerPulled = faceShapeData[1] >= rightLipCornerPullerNeutral + deltaRightLipCornerPuller ? true : false;
	leftLipCornerDepressed = faceShapeData[2] >= leftLipCornerDepressorNeutral + deltaLeftLipCornerDepressor ? true : false;
	rightLipCornerDepressed = faceShapeData[3] >= rightLipCornerDepressorNeutral + deltaRightLipCornerDepressor ? true : false;
	rightEyebrowLowered = faceShapeData[4] >= rightEyebrowNeutral + deltaRightEyebrowLowerer ? true : false;
	rightEyebrowRaised = faceShapeData[4] <= rightEyebrowNeutral - deltaRightEyebrowRaiser ? true : false;
	leftEyebrowLowered = faceShapeData[5] >= leftEyebrowNeutral + deltaLeftEyebrowLowerer ? true : false;
	leftEyebrowRaised = faceShapeData[5] <= leftEyebrowNeutral - deltaLeftEyebrowRaiser ? true : false;
}

void KinectEmotionDeterminerWrapper::calibrate(void) {

	const int numOfDataPoints = 100;
	cli::array<double>^ facialDataSums = gcnew cli::array<double>(MainWindow::faceShapeAnimations->Length);
	char choice = '\0';

	cout << "Press Enter when you are ready to calibrate: ";
	cin.ignore();

	do {
		fflush(stdin); // clear the input buffer to get rid of the newlines

		cout << endl << "Calibrating. Make sure that the user keeps their neutral face." << endl << endl;

		for (int i = 0; i < numOfDataPoints; ++i) {
			cli::array<double>^ facialData = getFacialData();
			for (int j = 0; j < MainWindow::faceShapeAnimations->Length; ++j) { facialDataSums[j] += facialData[j]; }
		}

		leftLipCornerPullerNeutral		= facialDataSums[0] / numOfDataPoints;
		rightLipCornerPullerNeutral		= facialDataSums[1] / numOfDataPoints;
		leftLipCornerDepressorNeutral	= facialDataSums[2] / numOfDataPoints;
		rightLipCornerDepressorNeutral	= facialDataSums[3] / numOfDataPoints;
		rightEyebrowNeutral				= facialDataSums[4] / numOfDataPoints;
		leftEyebrowNeutral				= facialDataSums[5] / numOfDataPoints;

		cout << "Your Neutral values are: "		<< endl << endl;
		cout << "LipCornerPullerLeft: "			<< leftLipCornerPullerNeutral << endl;
		cout << "LipCornerPullerRight: "		<< rightLipCornerPullerNeutral << endl;
		cout << "LipCornerDepressorLeft: "		<< leftLipCornerDepressorNeutral << endl;
		cout << "LipCornerDepressorsRight: "	<< rightLipCornerDepressorNeutral << endl;
		cout << "RightEyebrowLowerer: "			<< rightEyebrowNeutral << endl;
		cout << "LeftEyebrowLowerer: "			<< leftEyebrowNeutral << endl;

		cout << endl << "Was this a good calibration run?" << endl;
		cout << "(Y/N): ";
		choice = (char) getchar();

	} while (choice != 'y');

	cout << endl << "Okay. Calibration completed." << endl << endl;

}

bool KinectEmotionDeterminerWrapper::isHappy(void) {
	return leftLipCornerPulled && rightLipCornerPulled && !rightEyebrowLowered && !leftEyebrowLowered;
}

bool KinectEmotionDeterminerWrapper::isSad(void) {
	return rightEyebrowRaised && leftEyebrowRaised && leftLipCornerDepressed && rightLipCornerDepressed;
}

bool KinectEmotionDeterminerWrapper::isAngry(void) {
	return rightEyebrowLowered && leftEyebrowLowered && !leftLipCornerPulled && !rightLipCornerPulled;
}

bool KinectEmotionDeterminerWrapper::isNeutral(void) {
	return !leftLipCornerPulled && !rightLipCornerPulled && !leftLipCornerDepressed && !rightLipCornerDepressed && 
		!rightEyebrowLowered && !leftEyebrowLowered && !rightEyebrowRaised && !leftEyebrowRaised;
}

void KinectEmotionDeterminerWrapper::updateDisplayedEmotionText(String^ emotion) {
	mainWindow->CurrentDisplayedEmotion = emotion;
}