/*******************************************************************************
File Name       : ServoController.c
Version         : 2.0
Author          : Jon Mrowczynski
Target          : PIC16F628A
Compiler        : XC8 v1.37 Free version
IDE             : MPLAB X IDE v3.35
Programmer      : PICKit3.5
Last Updated    : 1/25/2016

 * Description:
 * 
 * This firmware allows a PIC16F628A MCU to control up to six servo motors as
 * well as a set of two LEDs in parallel. TIMER2 match interrupts are used to 
 * create the PWM signals necessary to control the six servo motors.
 * 
 *                   *** TIMER 2 INTERRUPT INFORMATION ***
 *
 * In order to get the necessary 0.1ms TIMER2 interrupt period resolution 
 * (t_interrupt), the equation below is used to determine the value for the 
 * PR2 register:
 *
 * t_interrupt [s] = [(PR2 - TMR2) + 1] * 4 * Tosc * Prescaler * Postscaler [s]
 *
 * Where TMR2 is the value stored in the TMR2 register, Tosc is the period of
 * the (internal/external) clock, PR2 is the value stored in the TIMER2 period
 * register, Prescaler and Postscaler are the scales that are set in the TIMER2
 * configuration register.
 *
 * For our purposes, these values are/have been set to:
 * t_interrupt = 0.1ms
 * TMR2 = 0
 * Tosc = (1 / 20,000,000) seconds
 * Prescaler => 1:4
 * Postscaler => 1:1
 *
 * Which would yield a PR2 value of *124*.
 *
 * The velocity of the note will determine the angular position of the servo motor arm.
 * For our servo motors, a counterclockwise rotation requires a pulse width of
 * 2.0ms, a clockwise rotation requires a pulse width of 1.0ms and the central
 * or neutral position requires a pulse width of 1.5ms.
 * 
 *                        *** MIDI INFORMATION ***
 * 
 * The notes that are now associated with the motors and the lights have been
 * changed to match the tones of a C4 pentatonic scale, which are C4, D4, E4, 
 * G4, A4, C5 and D5
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
 * 
 ******************************************************************************/

#include <pic.h>
#include "configuration.h"
#include "ServoController.h"

/* This byte represents the part that the other byte from the MIDI message 
   should be associated with */

volatile unsigned char part = CLEAR;

/* This counter variable helps keep track of what part of the MIDI message the 
   PIC just received. */

volatile unsigned char receiveCounter = CLEAR;

/* This counter variable represents the value of the discretely changing 
   sawtooth function that helps the PIC implement the PWM signals that 
   controls the angular position of all six servo motors */

volatile unsigned char sawtoothCounter = CLEAR;

/* For all servos, make each of them go to their neutral positions when 
   the circuit is first turned on. */

volatile unsigned char servo1SliderVal = EYEBROW_NEUTRAL_POSITION;
volatile unsigned char servo2SliderVal = LEFT_LIP_CORNER_NEUTRAL_POSITION;
volatile unsigned char servo3SliderVal = RIGHT_LIP_CORNER_NEUTRAL_POSITION;
volatile unsigned char servo4SliderVal = LOWER_JAW_NEUTRAL_POSITION;
volatile unsigned char servo5SliderVal = EYELIDS_NEUTRAL_POSITION;
//volatile unsigned char servo6Threshold = NEUTRAL_POSITION;

static void interrupt isr(void) {
    if (TMR2IF) {
        ++sawtoothCounter;
        if (sawtoothCounter >= MIN_SLIDER_VAL) {
            if (sawtoothCounter >= COUNTER_RESET) {
                SERVO1 = LOW;
                SERVO2 = LOW;
                SERVO3 = LOW;
                SERVO4 = LOW;
                SERVO5 = LOW;
                //SERVO6 = LOW;
                sawtoothCounter = CLEAR;
            } else {
                if (sawtoothCounter == servo1SliderVal) SERVO1 = HIGH;
                if (sawtoothCounter == servo2SliderVal) SERVO2 = HIGH;
                if (sawtoothCounter == servo3SliderVal) SERVO3 = HIGH;
                if (sawtoothCounter == servo4SliderVal) SERVO4 = HIGH;
                if (sawtoothCounter == servo5SliderVal) SERVO5 = HIGH;
                //if (sawtoothCounter == servo6Threshold) SERVO6 = HIGH;
            }
       } // end of inner if
        TMR2IF = CLEAR;
    } // end of TMR2IF if
    if (RCIF) {   
        ++receiveCounter;
        switch(receiveCounter) {
            case 1:
                RCREG = CLEAR;
                break;
            case 2:
                part = RCREG;
                break;
            case 3:
                receiveCounter = CLEAR;
                switch(part) {
                    case SERVO1_MIDI_NOTE: servo1SliderVal = position(RCREG); break;
                    case SERVO2_MIDI_NOTE: servo2SliderVal = position(RCREG); break;
                    case SERVO3_MIDI_NOTE: servo3SliderVal = position(RCREG); break;
                    case SERVO4_MIDI_NOTE: servo4SliderVal = position(RCREG); break;
                    case SERVO5_MIDI_NOTE: servo5SliderVal = position(RCREG); break;
                    //case SERVO6_MIDI_NOTE: servo6Threshold = position(RCREG); break;
                    case LIGHTS_MIDI_NOTE: LIGHTS = (RCREG > 0) ? ON : OFF; break;
                } // end of inner switch
                break;
        } // end of outer switch  
    } // end of RCIF if
} // end of isr

void transmit(void) {
    while(!TRMT);
    TXREG = 0x90;
    while(!TRMT);   
    TXREG = SERVO1_MIDI_NOTE;
    while(!TRMT);
    TXREG = 0x64;
    __delay_ms(250);
    while(!TRMT);
    TXREG = 0x80;
    while(!TRMT);
    TXREG = SERVO1_MIDI_NOTE;
    while(!TRMT);
    TXREG = 0x64;
    __delay_ms(250);
}

void main(void) {
    
    // Perform the setup operations
    
    PORTA   = ALL_OFF;              // Set all port A pins to low.
    PORTB   = ALL_OFF;              
    SPBRG   = BRATE;                // Set the MIDI baud rate to 31250
    TXSTA   = UENABLE;              // Enable the USART
    RCSTA   = RCENABLE;             // Enable the reception of MIDI data
    CMCON   = DISABLE_COMPARATORS;  // Disable comparators
    TRISA   = ALL_OUTPUT;           // Set all of port A's pins to output mode
    TRISB   = 0b00000110;           // This line is questionable
    T2CON   = T2CON_INIT;           // Setup Timer2     
    PR2     = PR2_INIT;             // Set initial value for Timer2's PR2 register
    PIR1    = CLEAR_ALL;            // Clear all of the interrupt flags
    PIE1    = ENABLE_INTERRUPTS;    // Enable Timer2 and Receive interrupts
    INTCON  = INTCON_INIT;          // Enable global and peripheral interrupts

    // Wait to perform the interrupts forever  
    
    for(;EVER;);
    
} // end of main