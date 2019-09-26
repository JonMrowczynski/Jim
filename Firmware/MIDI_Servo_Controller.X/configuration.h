/*
 * MIT License
 *
 * Copyright (c) 2013-2019 Jon Mrowczynski
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
 * 
 * @author Jon Mrowczynski
 */

#ifndef _CONFIGURATION_H_
#define _CONFIGURATION_H_

// Set the configuration bits for the PIC

#pragma config CP       = ON    // Code protection on
#pragma config CPD      = ON    // Data memory code protection on
#pragma config LVP      = OFF   // Low-Voltage Programming Disabled
#pragma config BOREN    = ON    // Brown-out Reset Enabled
#pragma config MCLRE    = OFF   // Master Clear Disabled
#pragma config PWRTE    = ON    // Power-up Timer Enabled
#pragma config WDTE     = OFF   // Watchdog Timer Disabled
#pragma config FOSC     = HS    // Using an external 20Mhz quartz crystal resonator

#endif