/*
 * File Name    : usart.h   
 * Version      : 1.0
 * Author       : Jon Mrowczynski 
 * Target       : PIC16F628A
 * Compiler     : XC8 v1.45 Free version
 * IDE          : MPLAB X IDE v4.10
 * Programmer   : PICKit3
 * Last Updated : 3/1/2018
 * 
 * We need a baud rate (BRATE) of 31250 to work with MIDI. In order to calculate
 * the value that we need to set the SPBRG register to in order to get a baud 
 * rate of 31250, we use the following formula which only holds for high baud 
 * rates (BRGH == 1):
 *
 * SPBRG = ( Fosc / ( 16 * Desired Baud Rate ) ) - 1
 *
 * Therefore, in our case, with a desired baud rate of 31250 and a Fosc of 20Mhz,
 * the equation would yield:
 *
 * SPBRG = 20,000,000 / ( 16 * 31250 ) - 1 = * 39 *
 * 
 * *NOTE*: Because of the integer division in the position macro, the minimum
 * change in the angular position of the servo motors is 9 degrees. This is 
 * due to the ones place of the velocity value being neglected when the 
 * integer division is performed. Therefore the velocity value 50 and 55 would 
 * yield the same angular position value: 185, since
 * 
 *        180 + (55 / 10) = 180 + 5.5 (but the .5 gets cut off so)
 *                        = 180 + 5 = 180 + (50 / 10) = 185
 */

#ifndef _USART_H_
#define _USART_H_

#include <pic.h>
#include <stdbool.h>
#include "pins.h"

// Associate a MIDI note with the servo motors and lights that are in parallel

#define EYEBROW_MIDI_NOTE           0x3C   // C4 
#define LEFT_LIP_CORNER_MIDI_NOTE   0x3E   // D4 
#define RIGHT_LIP_CORNER_MIDI_NOTE  0x40   // E4 
#define LOWER_JAW_MIDI_NOTE         0x43   // G4 
#define EYELIDS_MIDI_NOTE           0x45   // A5
#define UNUSED_MIDI_NOTE            0x48   // C5
#define LIGHTS_MIDI_NOTE            0x4A   // D5

#define MIN_VELOCITY_VAL    0
#define MAX_VELOCITY_VAL    10

// Declare constants that initialize some of the PIC's registers 

#define BRATE               39          // Set a midi baudrate to 31250

/**
 * Initializes the USART to allow the PIC to receive data. This currently does 
 * not allow the PIC to transmit data.
 */

static inline void initUSART() {
    TRISB1  = INPUT;
    SPBRG   = BRATE;    // Set the MIDI baud rate to 31250
    BRGH    = SET;      // Use high speed baud rate
    CREN    = true;     // Enable continuous receive
    SPEN    = true;     // Enable serial port
}

/**
 * In case of an receive register overrun error, reset the EUSART to be able to
 * receive messages again. This should be called in any loop where it is 
 * required for the EUSART to be able to receive data in order to exit the loop.
 */

static inline void clearOverrunError() {
    static unsigned char temp = 0; 
    do {                
        temp = RCREG;  
        temp = RCREG;  
        CREN = false; 
        CREN = true;  
    } while(OERR); 
}

/**
 * A framing error can occur if there is any noise on the signal lines, or if 
 * the baud rates of the communicating devices are different. Of course, there 
 * could potentially be a sufficient amount of noise on the signal lines causes
 * by a variety of things. It is also the case that the baud rate of the XBee
 * is not going to be exactly the same as the baud rate of the PIC.
 */

static inline void clearFramingError() {
    static unsigned char temp = 0;
    do {
        temp = RCREG;
        temp = RCREG;
        SPEN = false;
        SPEN = true;
    } while (FERR);
}

#endif