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
 * A baud rate (BRATE) of 31250 is needed to receive MIDI messages. To calculate
 * the SPBRG register value to generate that baud rate, the following formula, 
 * which only holds for high baud rates (BRGH == 1) is used:
 *
 * SPBRG = ( Fosc / ( 16 * Desired Baud Rate ) ) - 1
 *
 * Where Fosc is the frequency of the clock. In our case, with an external 
 * oscillator of 20MHz, the equation would yield:
 *
 * SPBRG = 20,000,000 / ( 16 * 31250 ) - 1 = 39
 * 
 * Note that that the theoretical baud rate error is 0%. However, There is going
 * to be some error introduced from error in the frequency of the quartz crystal 
 * resonator.
 * 
 * @author Jon Mrowczynski
 */

#ifndef _USART_H_
#define _USART_H_

#define BRATE 39  // Set baudrate to 31250 for MIDI communication

/*
 * Each of the servo motors and lights, which are in parallel, have an 
 * associated MIDI note.
 */

#define EYEBROW_MIDI_NOTE           0x3C   // C4 
#define LEFT_LIP_CORNER_MIDI_NOTE   0x3E   // D4 
#define RIGHT_LIP_CORNER_MIDI_NOTE  0x40   // E4 
#define LOWER_JAW_MIDI_NOTE         0x43   // G4 
#define EYELIDS_MIDI_NOTE           0x45   // A5
#define UNUSED_MIDI_NOTE            0x48   // C5
#define LIGHTS_MIDI_NOTE            0x4A   // D5

/**
 * Initializes the USART to allow the PIC to receive data.
 */

extern void initUSART(void);

/**
 * If a receive register overrun error occurs, reset the USART so that it can
 * receive messages again.
 */

extern void clearOverrunError(void);

/**
 * If a framing error occurs, reset the USART.
 */

extern void clearFramingError(void);

#endif