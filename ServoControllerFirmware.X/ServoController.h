/*
 * File Name       : ServoController.h
 * Version         : 1.0
 * Author          : Jon Mrowczynski
 * Target          : PIC16F628A
 * Compiler        : XC8 v1.45 Free version
 * IDE             : MPLAB X IDE v4.10
 * Programmer      : PICKit3
 * Last Updated    : 3/1/2018
 */

#ifndef _SERVOCONTROLLER_H_
#define _SERVOCONTROLLER_H_

#include "pins.h"

// Map some of the PIC output pins either to a servo motor or the lights 

#define EYEBROW_SERVO           RA0
#define LEFT_LIP_CORNER_SERVO   RA1
#define RIGHT_LIP_CORNER_SERVO  RA2
#define LOWER_JAW_SERVO         RA3
#define EYELIDS_SERVO           RA4
#define UNUSED_SERVO            RB3
#define LIGHTS                  RB5

// Associate a MIDI note with the servo motors and lights that are in parallel

#define EYEBROW_MIDI_NOTE           0x3C   // C4 
#define LEFT_LIP_CORNER_MIDI_NOTE   0x3E   // D4 
#define RIGHT_LIP_CORNER_MIDI_NOTE  0x40   // E4 
#define LOWER_JAW_MIDI_NOTE         0x43   // G4 
#define EYELIDS_MIDI_NOTE           0x45   // A5
#define UNUSED_MIDI_NOTE            0x48   // C5
#define LIGHTS_MIDI_NOTE            0x4A   // D5

// The neutral positions of the movable body parts of the Ruppet 

#define EYEBROW_NEUTRAL_POSITION            MIN_SLIDER_VAL + 6
#define LEFT_LIP_CORNER_NEUTRAL_POSITION    MIN_SLIDER_VAL + 6
#define RIGHT_LIP_CORNER_NEUTRAL_POSITION   MIN_SLIDER_VAL + 7
#define LOWER_JAW_NEUTRAL_POSITION          MIN_SLIDER_VAL + 3
#define EYELIDS_NEUTRAL_POSITION            MIN_SLIDER_VAL + 5

// Some other miscellaneous constants that are used in the program to help
// with its readability 

#define CLEAR_ALL   0b00000000
#define ON          1 
#define HIGH        1
#define ENABLE      1
#define EVER        1
#define DISABLE     0
#define LOW         0
#define OFF         0

/*
 * The byte that represents the part of the Ruppet that the next byte from the 
 * MIDI message is to be associated with. 
 */
extern volatile unsigned char part;

/*
 * The byte that helps to keep track of which byte has been received by the 
 * microcontroller.
 */

extern volatile unsigned char receiveCounter;

/* 
 * This counter variable represents the current value of the discretely changing 
 * sawtooth function that is used to help the PIC implement the software PWM 
 * signals that controls the angular position of all six of the servo motors. 
 */

extern volatile unsigned char sawtoothCounter;

extern volatile unsigned char eyebrowSliderVal;
extern volatile unsigned char leftLipCornerSliderVal;
extern volatile unsigned char rightLipCornerSliderVal;
extern volatile unsigned char lowerJawSliderVal;
extern volatile unsigned char eyelidsSliderVal;
extern volatile unsigned char servo6SliderVal;

static inline void initServos() {
    TRISA0 = OUTPUT;
    TRISA1 = OUTPUT;
    TRISA2 = OUTPUT;
    TRISA3 = OUTPUT;
    TRISB0 = OUTPUT;
    TRISB3 = OUTPUT;
}

#endif