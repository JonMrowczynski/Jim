/*
 * File Name       : ServoController.c
 * Version         : 1.0
 * Author          : Jon Mrowczynski
 * Target          : PIC16F628A
 * Compiler        : XC8 v1.45 Free version
 * IDE             : MPLAB X IDE v4.15
 * Programmer      : PICKit3
 * Last Updated    : 3/3/2018
 * 
 * This firmware allows a PIC16F628A microcontroller to control up to six 
 * servo motors and two LEDs in parallel. TIMER2 match interrupts are used to 
 * create the software PWM signals with a period of 200ms that are necessary to 
 * control up to six servo motors.
 * 
 * For the servo motors that we are using (Tower Pro 9g servo), a full 
 * counterclockwise rotation requires a pulse width of 2.0ms, a full clockwise 
 * rotation requires a pulse width of 1.0ms, and the central or neutral position
 * requires a pulse width of 1.5ms.
 * 
 * MIDI is used as the communication protocol to control the servo motors. The 
 * velocity of the MIDI note is used to determine the angular position of a 
 * corresponding servo motor arm.
 * 
 * The notes that are associated with the motors and the lights match the tones 
 * of a C4 pentatonic scale, which are C4, D4, E4, G4, A4, C5 and D5.
 * 
 * To learn more information about how each module is specifically implemented,
 * check out the corresponding header file.
 */

#include <pic.h>
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

void interrupt isr(void) {
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