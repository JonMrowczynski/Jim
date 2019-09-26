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

#include <pic.h>
#include <stdbool.h>
#include "pins.h"
#include "usart.h"

void initUSART() {
    TRISB1  = INPUT;
    SPBRG   = BRATE;    // Set the MIDI baud rate to 31250
    BRGH    = SET;      // Use high speed baud rate
    CREN    = true;     // Enable continuous receive
    SPEN    = true;     // Enable serial port
}

void clearOverrunError() {
    static unsigned char temp = 0; 
    do {                
        temp = RCREG;  
        temp = RCREG;  
        CREN = false; 
        CREN = true;  
    } while(OERR); 
}

void clearFramingError() {
    static unsigned char temp = 0;
    do {
        temp = RCREG;
        temp = RCREG;
        SPEN = false;
        SPEN = true;
    } while (FERR);
}