/*
	Name: KinectEmotionDeterminerWrapper.cpp
	Date: 2/13/2016
	Author: Jon Mrowczynski
	
	This is the main C++ DLL file which will eventually be used to link the 
	Kinect v2 software to the Java master control software using the JNI
*/

#include "stdafx.h"
#include "KinectEmotionDeterminerWrapper.h"

using namespace std;
using namespace System;
using namespace Microsoft::Kinect::Face;
using namespace System::Threading;
using namespace KinectEmotionDeterminer;

/* Acquires the number of face shape animations that the Kinect v2 will be keeping 
   track of. For some reason the sizeof() approach was giving weird results for the 
   length of MainWindow's faceShapeAnimations array, so a getter method was implemented 
   which simply returns the value faceShapeAnimations.Length */

KinectEmotionDeterminerWrapper::KinectEmotionDeterminerWrapper(void) {

	numOfFaceShapeAnimations = this->mainWindow->getFaceShapeAnimationLength();
	averageFaceShapeAnimations = new double[numOfFaceShapeAnimations];
	guiThread = gcnew Thread(gcnew ThreadStart(this, &KinectEmotionDeterminerWrapper::runKinect));
	guiThread->SetApartmentState(ApartmentState::STA);
	guiThread->Start();

} // end of constructor

/* Pretty much the only thing that needs to be cleaned up when KinectEmotionDeterminerWrapper
   instances are destroyed is the pointer sine everything else is managed. */

KinectEmotionDeterminerWrapper::~KinectEmotionDeterminerWrapper(void) {

	delete[] averageFaceShapeAnimations;
	averageFaceShapeAnimations = NULL;

} // end of destructor

/* Acquires facial data from the user by collecting data every ten frames per second 
   for 1 second. Note this method communicates with the CollectFaceData method in 
   MainWindow through two boolean values: readyToReadData and readyToPassData. 
   There is nothing that this method directly does to collet the data, it only 
   determines when the CollectFaceData method shoulld collect facial data. 
   
   Ultimately collects 10 facial data points per second for each tracked FaceShapeAnimation.*/

double* KinectEmotionDeterminerWrapper::getFacialData(void) {

	MainWindow::readyToReadData = true;

	while (!mainWindow->isReadyToPassData());

	MainWindow::readyToReadData = false;

	double sum = 0;

	for (int faceShapeNum = 0; faceShapeNum < numOfFaceShapeAnimations; ++faceShapeNum) {

		for (int dataElement = 0; dataElement < MainWindow::dataCollectionFramesPerSecond; ++dataElement) {

			sum += MainWindow::faceData[faceShapeNum, dataElement];
			averageFaceShapeAnimations[faceShapeNum] = sum / MainWindow::dataCollectionFramesPerSecond;

		}

		sum = 0;

	} // end of outer for

	MainWindow::readyToPassData = false;

	return this->averageFaceShapeAnimations;

} // end of getFacialData

/* After facial data has been collected, this method compares all of the animation 
   units' double values to see if they have cleared some threshold. If they have then
   set the corresponding boolean value that represents the FAU to true, else set
   it to false. */

void KinectEmotionDeterminerWrapper::checkFAUs(double* faceShapeData) {

	if (faceShapeData[0] >= this->leftLipCornerPullerNeutral + this->deltaLeftLipCornerPuller) { 
		
		this->leftLipCornerPulled = true; 

	} else { this->leftLipCornerPulled = false; }

	if (faceShapeData[1] >= this->rightLipCornerPullerNeutral + this->deltaRightLipCornerPuller) { 
		
		this->rightLipCornerPulled = true; 
	
	} else { this->rightLipCornerPulled = false; }

	if (faceShapeData[2] >= this->leftLipCornerDepressorNeutral + this->deltaLeftLipCornerDepressor) { 
		
		this->leftLipCornerDepressed = true; 
	
	} else { leftLipCornerDepressed = false; }

	if (faceShapeData[3] >= this->rightLipCornerDepressorNeutral + this->deltaRightLipCornerDepressor) { 
		
		this->rightLipCornerDepressed = true; 
	
	} else { this->rightLipCornerDepressed = false; }

	if (faceShapeData[4] >= this->rightEyebrowNeutral + this->deltaRightEyebrowLowerer) {
		
		this->rightEyebrowLowered = true;
	
	} else { this->rightEyebrowLowered = false; }

	if (faceShapeData[4] <= this->rightEyebrowNeutral - this->deltaRightEyebrowRaiser) { 
		
		this->rightEyebrowRaised = true; 
	
	} else { this->rightEyebrowRaised = false; }
		 
	if (faceShapeData[5] >= this->leftEyebrowNeutral + this->deltaLeftEyebrowLowerer) { 
		
		this->leftEyebrowLowered = true;
	
	} else { leftEyebrowLowered = false; }

	if (faceShapeData[5] <= this->leftEyebrowNeutral - this->deltaLeftEyebrowRaiser) { 
		
		this->leftEyebrowRaised = true; 
	
	} else { this->leftEyebrowRaised = false; }

	this->updateMainWindowBooleans();

} // end of checkFAUs

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

		calibrationData = new double[this->numOfFaceShapeAnimations];

		for (int i = 0; i < this->numOfFaceShapeAnimations; ++i) { calibrationData[i] = 0; }

		cout << endl << "Calibrating. Make sure that the user keeps their neutral face." << endl << endl;

		for (int i = 0; i < secToCollectData; ++i) {

			facialData = this->getFacialData();

			for (int j = 0; j < this->numOfFaceShapeAnimations; ++j) { calibrationData[j] += facialData[j]; }

		} // end of outer for loop

		this->leftLipCornerPullerNeutral		= calibrationData[0] / secToCollectData;
		this->rightLipCornerPullerNeutral		= calibrationData[1] / secToCollectData;
		this->leftLipCornerDepressorNeutral		= calibrationData[2] / secToCollectData;
		this->rightLipCornerDepressorNeutral	= calibrationData[3] / secToCollectData;
		this->rightEyebrowNeutral				= calibrationData[4] / secToCollectData;
		this->leftEyebrowNeutral				= calibrationData[5] / secToCollectData;

		cout << "Your Neutral values are: "		<< endl << endl;
		cout << "LipCornerPullerLeft: "			<< this->leftLipCornerPullerNeutral << endl;
		cout << "LipCornerPullerRight: "		<< this->rightLipCornerPullerNeutral << endl;
		cout << "LipCornerDepressorLeft: "		<< this->leftLipCornerDepressorNeutral << endl;
		cout << "LipCornerDepressorsRight: "	<< this->rightLipCornerDepressorNeutral << endl;
		cout << "RightEyebrowLowerer: "			<< this->rightEyebrowNeutral << endl;
		cout << "LeftEyebrowLowerer: "			<< this->leftEyebrowNeutral << endl;

		cout << endl << "Was this a good calibration run?" << endl;
		cout << "(Y/N): ";
		choice = (char) getchar();

	} while ( choice != 'y');

	delete[] facialData;
	facialData = NULL;

	delete[] calibrationData;
	calibrationData = NULL;

	cout << endl << "Okay. Calibration completed." << endl << endl;

} // end of calibrate

/* Checks to see whether the user is expressing a happy face by seeing 
   if they are triggering certain FAUs and not triggering other FAUs */

bool KinectEmotionDeterminerWrapper::isHappy(void) {

	bool isHappy = false;

	if (this->leftLipCornerPulled	&&
		this->rightLipCornerPulled	&&
		!this->rightEyebrowLowered	&&
		!this->leftEyebrowLowered) { isHappy = true; }

	return isHappy;

}  // end of isHappy

/* Checks to see if the user is sad by seeing if they are triggering
   certain FAUs */

bool KinectEmotionDeterminerWrapper::isSad(void) {

	bool isSad = false;

	if (this->rightEyebrowRaised	 &&
		this->leftEyebrowRaised		 &&
		this->leftLipCornerDepressed &&
		this->rightLipCornerDepressed) { isSad = true; } 

	return isSad;

} // end of isSad

/* Checks to see if the user is expressing angry by checking to 
   see if they are triggering certain FAUs and not triggering 
   other FAUs */

bool KinectEmotionDeterminerWrapper::isAngry(void) {

	bool isAngry = false;

	if (this->rightEyebrowLowered	&&
		this->leftEyebrowLowered	&&
		!this->leftLipCornerPulled	&&
		!this->rightLipCornerPulled) { isAngry = true; }

	return isAngry;

} // end of isAngry

/* Checks to see if the user is expressing their neutral face by 
   making sure that they are not triggering any FAUs */

bool KinectEmotionDeterminerWrapper::isNeutral(void) {

	bool isNeutral = false;

	if (!this->leftLipCornerPulled		&&
		!this->rightLipCornerPulled		&&
		!this->leftLipCornerDepressed	&&
		!this->rightLipCornerDepressed	&&
		!this->rightEyebrowLowered		&&
		!this->leftEyebrowLowered		&&
		!this->rightEyebrowRaised		&&
		!this->leftEyebrowRaised) { isNeutral = true; } 

	return isNeutral;
	
} // end of isNeutral

/* Display the MainWindow which includes the textCanvas and the faceCanvas.
   The textCanvas contains the information about the FaceShapeAnimations of the user
   while the faceCanvas contains the high definition facial points */

void KinectEmotionDeterminerWrapper::runKinect(void) {

	mainWindow = gcnew MainWindow();
	mainWindow->ShowDialog();

} // end of runKinect

/* Allows communication between the KinectEmotionDeterminerWrapper and 
   the MainWindow such that the MainWindow can "see" what FAUs the person
   is triggering and recolor the text accordingly. */

void KinectEmotionDeterminerWrapper::updateMainWindowBooleans(void) {

	MainWindow::leftLipCornerPulled		= this->leftLipCornerPulled;
	MainWindow::rightLipCornerPulled	= this->rightLipCornerPulled;
	MainWindow::leftLipCornerDepressed	= this->leftLipCornerDepressed;
	MainWindow::rightLipCornerDepressed = this->rightLipCornerDepressed;
	MainWindow::rightEyebrowLowered		= this->rightEyebrowLowered;
	MainWindow::leftEyebrowLowered		= this->leftEyebrowLowered;
	MainWindow::rightEyebrowRaised		= this->rightEyebrowRaised;
	MainWindow::leftEyebrowRaised		= this->rightEyebrowRaised;

} // end of updateMainWindowBooleans

void KinectEmotionDeterminerWrapper::updateDisplayedEmotionText(String^ emotion) {

	MainWindow::currentEmotionDisplayed = emotion;

} // end of updateDisplayedEmotionText









