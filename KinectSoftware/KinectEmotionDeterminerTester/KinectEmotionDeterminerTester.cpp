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

#include "stdafx.h"
#include <iostream>

using namespace System;

// This file is used to test both the Kinect and wrapper code in C# and C++ respectively before attempting to interface
// them with the JNI to connect their functionalities to the master control software in Java.

int main(array<System::String ^> ^args) {
	KinectEmotionDeterminerWrapper^ kedw = gcnew KinectEmotionDeterminerWrapper();
	kedw->calibrate(); // Must perform a calibration on a tracked face to determine emotional expressions.
	while(true) {
		kedw->checkFAUs(kedw->getFacialData());
		if		(kedw->isHappy())	{ kedw->updateDisplayedEmotionText("Happy"); } 
		else if (kedw->isSad())		{ kedw->updateDisplayedEmotionText("Sad"); }
		else if (kedw->isAngry())	{ kedw->updateDisplayedEmotionText("Angry"); }
		else if (kedw->isNeutral()) { kedw->updateDisplayedEmotionText("Neutral"); }
		else						{ kedw->updateDisplayedEmotionText("Unknown"); }
	}
	std::cout << "Press Enter to Exit: ";
	std::cin.ignore();
	return 0;
}
