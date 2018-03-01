/* 
 * File Name    : ServoController.h   
 * Version      : 2.0
 * Author       : Jon Mrowczynski 
 * 
 * This file contains all of the constants that are used in the file 
 * ServoController.c
 */

#ifndef _SERVOCONTROLLER_H_
#define _SERVOCONTROLLER_H_

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

#define ALL_OFF     0b00000000
#define ALL_OUTPUT  0b00000000
#define CLEAR_ALL   0b00000000
#define ON          1 
#define INPUT       1
#define HIGH        1
#define ENABLE      1
#define YES         1
#define EVER        1
#define DISABLE     0
#define LOW         0
#define NO          0
#define OFF         0
#define CLEAR       0    

extern volatile unsigned char part;
extern volatile unsigned char receiveCounter;
extern volatile unsigned char sawtoothCounter;

extern volatile unsigned char servo1SliderVal;
extern volatile unsigned char servo2SliderVal;
extern volatile unsigned char servo3SliderVal;
extern volatile unsigned char servo4SliderVal;
extern volatile unsigned char servo5SliderVal;
extern volatile unsigned char servo6SliderVal;

#endif