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

// Map some of the PIC output pins either to a servo motor or to the lights 

#define EYEBROW_SERVO_OUTPUT           RA0
#define LEFT_LIP_CORNER_SERVO_OUTPUT   RA1
#define RIGHT_LIP_CORNER_SERVO_OUTPUT  RA2
#define LOWER_JAW_SERVO_OUTPUT         RA3
#define EYELIDS_SERVO_OUTPUT           RA4
#define UNUSED_SERVO_OUTPUT            RB3
#define LIGHTS                         RB5

// The neutral positions of the movable body parts of the Ruppet 

#define EYEBROW_NEUTRAL_POSITION            MIN_SLIDER_VAL + 6
#define LEFT_LIP_CORNER_NEUTRAL_POSITION    MIN_SLIDER_VAL + 6
#define RIGHT_LIP_CORNER_NEUTRAL_POSITION   MIN_SLIDER_VAL + 7
#define LOWER_JAW_NEUTRAL_POSITION          MIN_SLIDER_VAL + 3
#define EYELIDS_NEUTRAL_POSITION            MIN_SLIDER_VAL + 5

// Some other miscellaneous constants that are used in the program to help
// with its readability 

#define ON          1 
#define OFF         0

#define position(velocity)  (MIN_SLIDER_VAL + (velocity))   // Allows for 11 unique angular positions

/*
 * The byte that represents the received MIDI note that is associated with a 
 * part of the Ruppet.
 */
extern volatile unsigned char partMidiNote;

/*
 * Helps to keep track of which MIDI byte has been received by the 
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
//extern volatile unsigned char servo6SliderVal;

/**
 * Sets all of the pins that will produce a software PWM signal to a 
 * corresponding servo to be an output pin.
 */

static inline void initServos(void) {
    TRISA0 = OUTPUT;
    TRISA1 = OUTPUT;
    TRISA2 = OUTPUT;
    TRISA3 = OUTPUT;
    TRISB0 = OUTPUT;
    TRISB3 = OUTPUT;
}

#endif