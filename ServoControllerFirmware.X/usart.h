/*
 * File Name    : usart.h   
 * Version      : 1.0
 * Author       : Jon Mrowczynski 
 * Target       : PIC16F628A
 * Compiler     : XC8 v2.0 Free version
 * IDE          : MPLAB X IDE v5.0
 * Programmer   : PICkit3
 * Last Updated : 8/10/2018
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
 * SPBRG = 20,000,000 / ( 16 * 31250 ) - 1 = 39
 * 
 * Note that that the theoretical baud rate error is 0%. However, There is going
 * to be some error introduced from error in the frequency of the quartz crystal 
 * resonator.
 */

#ifndef _USART_H_
#define _USART_H_

#include <pic.h>
#include <stdbool.h>
#include "pins.h"

#define BRATE   39  // Set baudrate to 31250 for MIDI communication

// Each of the servo motors and lights (which are in parallel) have a MIDI note
// that is associated with them.

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

static inline void initUSART() {
    TRISB1  = INPUT;
    SPBRG   = BRATE;    // Set the MIDI baud rate to 31250
    BRGH    = SET;      // Use high speed baud rate
    CREN    = true;     // Enable continuous receive
    SPEN    = true;     // Enable serial port
}

/**
 * In case of a receive register overrun error, reset the USART to be able to
 * receive messages again.
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
 * the baud rates of the communicating devices are different. There is the 
 * potential for a lot of noise in the circuit due to the number of PWM signals,
 * the close proximity of some of the signal lines, as well as noise from each 
 * of the servo motors.
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