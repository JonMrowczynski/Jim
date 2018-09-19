/*
 * File Name    : ServoController.c
 * Version      : 1.0
 * Author       : Jon Mrowczynski
 * Target       : PIC18F24K40
 * Compiler     : XC8 v2.00 Free version
 * IDE          : MPLAB X IDE v5.05
 * Programmer   : PICkit3
 * Last Updated : 8/31/2018
 * 
 * This firmware allows for up to six servo motors and two LEDs in parallel. 
 * TIMER2 match interrupts are used to create the software PWM signals with a 
 * period of 200ms that are necessary to control up to six servo motors.
 * 
 * For the servo motors that we are using (Tower Pro 9g servo), a full 
 * counterclockwise rotation requires a pulse width of 2.0ms, a full clockwise 
 * rotation requires a pulse width of 1.0ms, and the central or neutral position
 * requires a pulse width of 1.5ms.
 * 
 * To learn more information about how each module is specifically implemented,
 * check out the corresponding header file.
 */

#include <xc.h>
#include <stdbool.h>
#include "configuration.h"
#include "tmr2.h"
#include "usart.h"
#include "ServoController.h"

volatile unsigned char partMidiNote     = CLEAR;
volatile unsigned char velocity         = CLEAR;
volatile unsigned char receiveCounter   = CLEAR;
volatile unsigned char sawtoothCounter  = CLEAR;

/* 
 * For all servos, make each of them go to their neutral positions when the 
 * circuit is first turned on. This helps to make sure that the Ruppet forms its
 * neutral position on startup.
 */

volatile unsigned char eyebrowSawtoothThreshold         = EYEBROW_NEUTRAL_POSITION;
volatile unsigned char leftLipCornerSawtoothThreshold   = LEFT_LIP_CORNER_NEUTRAL_POSITION;
volatile unsigned char rightLipCornerSawtoothThreshold  = RIGHT_LIP_CORNER_NEUTRAL_POSITION;
volatile unsigned char lowerJawSawtoothThreshold        = LOWER_JAW_NEUTRAL_POSITION;
volatile unsigned char eyelidsSawtoothThreshold         = EYELIDS_NEUTRAL_POSITION;
//volatile unsigned char servo6SawtoothThreshold          = NEUTRAL_POSITION;

void __interrupt() isr(void) {
    if (TMR2IF) {
        ++sawtoothCounter;
        if (sawtoothCounter >= MIN_SAWTOOTH_THRESHOLD) {
            if (sawtoothCounter >= PWM_PERIOD) {
                EYEBROW_SERVO_OUTPUT           = LOW;
                LEFT_LIP_CORNER_SERVO_OUTPUT   = LOW;
                RIGHT_LIP_CORNER_SERVO_OUTPUT  = LOW;
                LOWER_JAW_SERVO_OUTPUT         = LOW;
                EYELIDS_SERVO_OUTPUT           = LOW;
                //SERVO6 = LOW;
                sawtoothCounter = CLEAR;
            } else {
                if (sawtoothCounter == eyebrowSawtoothThreshold)        EYEBROW_SERVO_OUTPUT           = HIGH;
                if (sawtoothCounter == leftLipCornerSawtoothThreshold)  LEFT_LIP_CORNER_SERVO_OUTPUT   = HIGH;
                if (sawtoothCounter == rightLipCornerSawtoothThreshold) RIGHT_LIP_CORNER_SERVO_OUTPUT  = HIGH;
                if (sawtoothCounter == lowerJawSawtoothThreshold)       LOWER_JAW_SERVO_OUTPUT         = HIGH;
                if (sawtoothCounter == eyelidsSawtoothThreshold)        EYELIDS_SERVO_OUTPUT           = HIGH;
                //if (sawtoothCounter == servo6SawtoothThreshold)         UNUSED_SERVO_OUTPUT            = HIGH;
            }
       }
        TMR2IF = CLEAR;
    }
    if (RCIF) {   
        ++receiveCounter;
        switch(receiveCounter) {
            case 1:
                RCREG = CLEAR;
                break;
            case 2:
                partMidiNote = RCREG;
                break;
            case 3:
                receiveCounter = CLEAR;
                velocity = RCREG;
                if (velocity > 10) velocity = 10;
                switch(partMidiNote) {
                    case EYEBROW_MIDI_NOTE:             eyebrowSawtoothThreshold        = position(velocity); break;
                    case LEFT_LIP_CORNER_MIDI_NOTE:     leftLipCornerSawtoothThreshold  = position(velocity); break;
                    case RIGHT_LIP_CORNER_MIDI_NOTE:    rightLipCornerSawtoothThreshold = position(velocity); break;
                    case LOWER_JAW_MIDI_NOTE:           lowerJawSawtoothThreshold       = position(velocity); break;
                    case EYELIDS_MIDI_NOTE:             eyelidsSawtoothThreshold        = position(velocity); break;
                   // case UNUSED_MIDI_NOTE:              servo6SawtoothThreshold         = position(velocity); break;
                    case LIGHTS_MIDI_NOTE:              LIGHTS                          = (velocity > 0) ? ON : OFF; break;
                }
                break;
            default:
                receiveCounter = CLEAR;
                RCREG = CLEAR;
                break;
        }
    }
}

void main(void) {
    
    // Set everything up
    
    initServos();
    initUSART();
    initTMR2();
    
    PEIE    = true;  // Enable peripheral interrupts
    GIE     = true;  // Enable global interrupts

    // In case the USART experiences an overrun error and/or a framing error, 
    // fix the encountered error. Otherwise, just wait to perform the interrupt 
    // service routine.
    
    while(true) {
        if (OERR)
            clearOverrunError();
        if(FERR)
            clearFramingError();
    }
    
}