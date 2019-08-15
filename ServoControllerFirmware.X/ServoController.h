/*
 * File Name    : ServoController.h
 * Version      : 1.0
 * Author       : Jon Mrowczynski
 * Target       : PIC16F628A
 * Compiler     : XC8 v2.0 Free version
 * IDE          : MPLAB X IDE v5.0
 * Programmer   : PICkit3
 * Last Updated : 8/10/2018
 */

#ifndef _SERVOCONTROLLER_H_
#define _SERVOCONTROLLER_H_

#include "pins.h"

// Map some of the PIC output pins either to a servo motor or to the lights. 
// Note that the lights are in parallel, so that only one output pin needs to be
// mapped to the lights.

#define EYEBROW_SERVO_OUTPUT           RA0
#define LEFT_LIP_CORNER_SERVO_OUTPUT   RA1
#define RIGHT_LIP_CORNER_SERVO_OUTPUT  RA2
#define LOWER_JAW_SERVO_OUTPUT         RA3
#define EYELIDS_SERVO_OUTPUT           RB0
#define UNUSED_SERVO_OUTPUT            RB3
#define LIGHTS                         RB5

// The neutral positions of the movable body parts of the Ruppet.

#define EYEBROW_NEUTRAL_POSITION            MIN_SAWTOOTH_THRESHOLD + 6
#define LEFT_LIP_CORNER_NEUTRAL_POSITION    MIN_SAWTOOTH_THRESHOLD + 6
#define RIGHT_LIP_CORNER_NEUTRAL_POSITION   MIN_SAWTOOTH_THRESHOLD + 7
#define LOWER_JAW_NEUTRAL_POSITION          MIN_SAWTOOTH_THRESHOLD + 3
#define EYELIDS_NEUTRAL_POSITION            MIN_SAWTOOTH_THRESHOLD + 5
//#define NEUTRAL_POSITION                    MIN_SAWTOOTH_THRESHOLD + 5

/*
 * Sets the position of the servo motor by converting the received velocity 
 * value into a sawtooth threshold value. This allows for the PWM pulse width
 * to take on 11 different widths (since the velocity value ranges from 0-11)
 * thus allowing for up to 11 unique angular positions per servo motor.
 * 
 * Note that if the velocity value does happen to take on a larger value, then 
 * it is as if the velocity value is set to 10. This can be seen in the 
 * interrupt service routine.
 */

#define position(velocity)  (MIN_SAWTOOTH_THRESHOLD + (velocity))

/*
 * The byte that represents the received MIDI note that is associated with a 
 * part of the Ruppet.
 */

extern volatile unsigned char partMidiNote;

/*
 * The velocity of the received MIDI note.
 */

extern volatile unsigned char velocity;

/*
 * Helps to keep track of which MIDI byte has been received by the 
 * microcontroller.
 */

extern volatile unsigned char receiveCounter;

/* 
 * This counter variable keeps track of the current value of the discretely 
 * changing sawtooth function that is used to help the PIC implement the 
 * software PWM signals that controls the angular position of all six of the 
 * servo motors. It is incremented whenever a TMR2 interrupt occurs.
 */

extern volatile unsigned char sawtoothCounter;

/*
 * Each part (or servo motor) has a corresponding sawtooth threshold value that 
 * creates different pulse widths based on the value. To see how this is
 * specifically done, take a look at the interrupt service routine.
 */

extern volatile unsigned char eyebrowSawtoothThreshold;
extern volatile unsigned char leftLipCornerSawtoothThreshold;
extern volatile unsigned char rightLipCornerSawtoothThreshold;
extern volatile unsigned char lowerJawSawtoothThreshold;
extern volatile unsigned char eyelidsSawtoothThreshold;
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