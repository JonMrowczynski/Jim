// TestKinectEmotionDeterminer.cpp : main project file.

#include "stdafx.h"
#include <iostream>

using namespace std;
using namespace System;

int main(array<System::String ^> ^args) {
	
	KinectEmotionDeterminerWrapper^ kedw = gcnew KinectEmotionDeterminerWrapper();

	kedw->calibrate();
	
	while(true) {

		kedw->checkFAUs(kedw->getFacialData());

		if		(kedw->isHappy())	{ kedw->updateDisplayedEmotionText("Happy"); } 
		else if (kedw->isSad())		{ kedw->updateDisplayedEmotionText("Sad"); }
		else if (kedw->isAngry())	{ kedw->updateDisplayedEmotionText("Angry"); }
		else if (kedw->isNeutral()) { kedw->updateDisplayedEmotionText("Neutral"); }
		else						{ kedw->updateDisplayedEmotionText("Unknown"); }

	} // end of while
	
	cout << "Press Enter to Exit: ";
	cin.ignore();

	return 0;

} // end of main
