/**	
	This is the main C++ DLL file which will eventually be used to link the 
	Kinect v2 software to the Java master control software using the JNI

	@author Jon Mrowczynski
*/

#include "stdafx.h"
#include "KinectEmotionDeterminerWrapper.h"

using namespace std;
using namespace System;
using namespace Microsoft::Kinect::Face;
using namespace System::Threading;
using namespace KinectEmotionDeterminer;

/* Acquires the number of face shape animations that the Kinect v2 will be keeping track of. */

KinectEmotionDeterminerWrapper::KinectEmotionDeterminerWrapper(void) {
	numOfFaceShapeAnimations = mainWindow->GetFaceShapeAnimationLength();
	averageFaceShapeAnimations = new double[numOfFaceShapeAnimations];
	guiThread = gcnew Thread(gcnew ThreadStart(this, &KinectEmotionDeterminerWrapper::runKinect));
	guiThread->SetApartmentState(ApartmentState::STA);
	guiThread->Start();
}

/* Pretty much the only thing that needs to be cleaned up when KinectEmotionDeterminerWrapper
   instances are destroyed is the pointer since everything else is managed. */

KinectEmotionDeterminerWrapper::~KinectEmotionDeterminerWrapper(void) {
	delete[] averageFaceShapeAnimations;
	averageFaceShapeAnimations = NULL;
}

/* Acquires facial data from the user by collecting data every ten frames per second 
   for 1 second. Note this method communicates with the CollectFaceData method in 
   MainWindow through two boolean values: readyToReadData and readyToPassData. 
   There is nothing that this method directly does to collet the data, it only 
   determines when the CollectFaceData method shoulld collect facial data. 
   
   Ultimately collects 10 facial data points per second for each tracked FaceShapeAnimation.*/

double* KinectEmotionDeterminerWrapper::getFacialData(void) {
	MainWindow::readyToReadData = true;
	while (!mainWindow->IsReadyToPassData());
	MainWindow::readyToReadData = false;
	double sum = 0;
	for (int faceShapeNum = 0; faceShapeNum < numOfFaceShapeAnimations; ++faceShapeNum) {
		for (int dataElement = 0; dataElement < MainWindow::dataCollectionFramesPerSecond; ++dataElement) {
			sum += MainWindow::faceData[faceShapeNum, dataElement];
			averageFaceShapeAnimations[faceShapeNum] = sum / MainWindow::dataCollectionFramesPerSecond;
		}
		sum = 0;
	}
	MainWindow::readyToPassData = false;
	return averageFaceShapeAnimations;
}

/* After facial data has been collected, this method compares all of the animation 
   units' double values to see if they have cleared some threshold. If they have then
   set the corresponding boolean value that represents the FAU to true, else set
   it to false. */

void KinectEmotionDeterminerWrapper::checkFAUs(double* faceShapeData) {
	leftLipCornerPulled = faceShapeData[0] >= leftLipCornerPullerNeutral + deltaLeftLipCornerPuller ? true : false;
	rightLipCornerPulled = faceShapeData[1] >= rightLipCornerPullerNeutral + deltaRightLipCornerPuller ? true : false;
	leftLipCornerDepressed = faceShapeData[2] >= leftLipCornerDepressorNeutral + deltaLeftLipCornerDepressor ? true : false;
	rightLipCornerDepressed = faceShapeData[3] >= rightLipCornerDepressorNeutral + deltaRightLipCornerDepressor ? true : false;
	rightEyebrowLowered = faceShapeData[4] >= rightEyebrowNeutral + deltaRightEyebrowLowerer ? true : false;
	rightEyebrowRaised = faceShapeData[4] <= rightEyebrowNeutral - deltaRightEyebrowRaiser ? true : false;
	leftEyebrowLowered = faceShapeData[5] >= leftEyebrowNeutral + deltaLeftEyebrowLowerer ? true : false;
	leftEyebrowRaised = faceShapeData[5] <= leftEyebrowNeutral - deltaLeftEyebrowRaiser ? true : false;
	updateMainWindowBooleans();
}

/* Calibraates the face by recording facial data for 3 seconds (which in total means
   that 40 data values will be taken) and then those values are averaged to get the 
   corresponding neutral animation unit value for the associated facial part */

void KinectEmotionDeterminerWrapper::calibrate(void) {

	const int secToCollectData = 4;
	const int totalNumOfDataPointsCollected = MainWindow::dataCollectionFramesPerSecond * secToCollectData;

	double* facialData = NULL;
	double* calibrationData = NULL;

	char choice = '\0';

	cout << "Press Enter when you are ready to calibrate: ";
	cin.ignore();

	do {
		fflush(stdin); // clear the input buffer to get rid of the newlines
		calibrationData = new double[numOfFaceShapeAnimations];
		for (int i = 0; i < numOfFaceShapeAnimations; ++i) { calibrationData[i] = 0; }

		cout << endl << "Calibrating. Make sure that the user keeps their neutral face." << endl << endl;

		for (int i = 0; i < secToCollectData; ++i) {
			facialData = getFacialData();
			for (int j = 0; j < numOfFaceShapeAnimations; ++j) { calibrationData[j] += facialData[j]; }
		}

		leftLipCornerPullerNeutral		= calibrationData[0] / secToCollectData;
		rightLipCornerPullerNeutral		= calibrationData[1] / secToCollectData;
		leftLipCornerDepressorNeutral	= calibrationData[2] / secToCollectData;
		rightLipCornerDepressorNeutral	= calibrationData[3] / secToCollectData;
		rightEyebrowNeutral				= calibrationData[4] / secToCollectData;
		leftEyebrowNeutral				= calibrationData[5] / secToCollectData;

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

	delete[] facialData;
	facialData = NULL;

	delete[] calibrationData;
	calibrationData = NULL;

	cout << endl << "Okay. Calibration completed." << endl << endl;

} // end of calibrate

/* Checks to see whether the user is expressing a happy face by seeing 
   if they are triggering certain FAUs and not triggering other FAUs */

bool KinectEmotionDeterminerWrapper::isHappy(void) {
	return leftLipCornerPulled && rightLipCornerPulled && !rightEyebrowLowered && !leftEyebrowLowered;
}

/* Checks to see if the user is sad by seeing if they are triggering
   certain FAUs */

bool KinectEmotionDeterminerWrapper::isSad(void) {
	return rightEyebrowRaised && leftEyebrowRaised && leftLipCornerDepressed && rightLipCornerDepressed;
}

/* Checks to see if the user is expressing angry by checking to 
   see if they are triggering certain FAUs and not triggering 
   other FAUs */

bool KinectEmotionDeterminerWrapper::isAngry(void) {
	return rightEyebrowLowered && leftEyebrowLowered && !leftLipCornerPulled && !rightLipCornerPulled;
}

/* Checks to see if the user is expressing their neutral face by 
   making sure that they are not triggering any FAUs */

bool KinectEmotionDeterminerWrapper::isNeutral(void) {
	return !leftLipCornerPulled && !rightLipCornerPulled && !leftLipCornerDepressed && !rightLipCornerDepressed && 
		!rightEyebrowLowered && !leftEyebrowLowered && !rightEyebrowRaised && !leftEyebrowRaised;
}

/* Display the MainWindow which includes the textCanvas and the faceCanvas.
   The textCanvas contains the information about the FaceShapeAnimations of the user
   while the faceCanvas contains the high definition facial points */

void KinectEmotionDeterminerWrapper::runKinect(void) {
	mainWindow = gcnew MainWindow();
	mainWindow->ShowDialog();
}

/* Allows communication between the KinectEmotionDeterminerWrapper and 
   the MainWindow such that the MainWindow can "see" what FAUs the person
   is triggering and recolor the text accordingly. */

void KinectEmotionDeterminerWrapper::updateMainWindowBooleans(void) {
	MainWindow::leftLipCornerPulled		= leftLipCornerPulled;
	MainWindow::rightLipCornerPulled	= rightLipCornerPulled;
	MainWindow::leftLipCornerDepressed	= leftLipCornerDepressed;
	MainWindow::rightLipCornerDepressed = rightLipCornerDepressed;
	MainWindow::rightEyebrowLowered		= rightEyebrowLowered;
	MainWindow::leftEyebrowLowered		= leftEyebrowLowered;
	MainWindow::rightEyebrowRaised		= rightEyebrowRaised;
	MainWindow::leftEyebrowRaised		= rightEyebrowRaised;
}

void KinectEmotionDeterminerWrapper::updateDisplayedEmotionText(String^ emotion) {
	MainWindow::currentEmotionDisplayed = emotion;
}