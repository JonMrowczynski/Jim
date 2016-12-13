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

#define _XTAL_FREQ 20000000 // 20MHz quarts crystal resonator

// Set the configuration bits for the PIC

#pragma config CP       = ON    // Code protection on
#pragma config CPD      = ON    // Data memory code protection on
#pragma config LVP      = OFF   // Low-Voltage Programming Disabled
#pragma config BOREN    = ON    // Brown-out Reset Enabled
#pragma config MCLRE    = OFF   // Master Clear Disabled
#pragma config PWRTE    = ON    // Power Up Timer Enabled
#pragma config WDTE     = OFF   // Watchdog Timer Disabled
#pragma config FOSC     = HS    // Using an external 20Mhz crystal oscillator

// Declare constants that initialize some of the PIC's registers 

#define BRATE               39          // Set a midi baudrate to 31250
#define UENABLE             0b10100100  // USART enable
#define RCENABLE            0b10010000  // Receive enable
#define INTCON_INIT         0b11000000  // Enables global and peripheral interrupts
#define T2CON_INIT          0b00000101  // Enables Timer 2
#define ENABLE_INTERRUPTS   0b00100010  // Enable Timer 2 and Receive Interrupts
#define DISABLE_COMPARATORS 0b00000111  // Disable the PIC'S comparators

// Map some of the PIC output pins either to a servo motor or the lights 

#define SERVO1 RA0 // EYEBROW
#define SERVO2 RA1 // LEFT LIP CORNER
#define SERVO3 RA2 // RIGHT LIP CORNER
#define SERVO4 RA3 // LOWER JAW
#define SERVO5 RB0 // EYELIDS
#define SERVO6 RB3 // NOT USED
#define LIGHTS RB5 // EYE LIGHTS

// Associate a MIDI note with the servo motors and lights that are in parallel

#define SERVO1_MIDI_NOTE 0x3C   // C4 
#define SERVO2_MIDI_NOTE 0x3E   // D4 
#define SERVO3_MIDI_NOTE 0x40   // E4 
#define SERVO4_MIDI_NOTE 0x43   // G4 
#define SERVO5_MIDI_NOTE 0x45   // A5
#define SERVO6_MIDI_NOTE 0x48   // C5
#define LIGHTS_MIDI_NOTE 0x4A   // D5

// Declare constants related to the PIC's Timer 2 interrupts

#define MAX_VALUE           200                      // Need 200ms period
#define COUNTER_RESET       MAX_VALUE                // Reset sawtoothcounter every 200ms
#define MIN_SLIDER_VAL      180
#define MAX_SLIDER_VAL      190
#define position(velocity)  (MIN_SLIDER_VAL + (velocity)) // Allows for 11 unique angular positions
#define MIN_VELOCITY_VAL    0
#define MAX_VELOCITY_VAL    10
#define PR2_INIT            124 

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

#endif