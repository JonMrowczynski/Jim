/*
 * Author: Jon Mrowczynski
 */

#ifndef _SERVOCONTROLLER_H_
#define _SERVOCONTROLLER_H_

#include "pins.h"

/* 
 * Map some of the PIC output pins either to a servo motor or to the lights. 
 * Only one output pin needs to be mapped to the lights since they are in 
 * parallel.
 */

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
#define UNUSED_NEUTRAL_POSITION             MIN_SAWTOOTH_THRESHOLD + 5

#define MAX_VELOCITY 10

/*
 * Sets the position of the servo motor by converting the received velocity 
 * value into a sawtooth threshold value. This allows for the PWM pulse width
 * to take on MAX_VELOCITY + 1 different widths (since the velocity value ranges
 * from 0-MAX_VELOCITY inclusive) thus allowing for up to MAX_VELOCITY + 1 
 * unique angular positions per servo motor.
 * 
 * If the velocity value takes on a larger value, then it should be set to 
 * MAX_VELOCITY.
 */

#define position(velocity)  (MIN_SAWTOOTH_THRESHOLD + (velocity))

/*
 * The byte that represents the received MIDI note that is associated with a 
 * Ruppet part.
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
 * Keeps track of the current value of the discretely changing sawtooth function
 * that is used to help the PIC implement the software PWM signals that controls
 * the angular position of all six of the servo motors. It is incremented 
 * whenever a TMR2 interrupt occurs.
 */

extern volatile unsigned char sawtoothCounter;

/*
 * Each part (or servo motor) has a corresponding sawtooth threshold value that 
 * creates different pulse widths based on the value.
 */

extern volatile unsigned char eyebrowSawtoothThreshold;
extern volatile unsigned char leftLipCornerSawtoothThreshold;
extern volatile unsigned char rightLipCornerSawtoothThreshold;
extern volatile unsigned char lowerJawSawtoothThreshold;
extern volatile unsigned char eyelidsSawtoothThreshold;
extern volatile unsigned char unusedSawtoothThreshold;

/**
 * Sets all of the pins that will produce a software PWM signal to corresponding
 * servos to be output pins.
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