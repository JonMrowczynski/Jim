/*
 * File Name    : tmr2.h   
 * Version      : 1.0
 * Author       : Jon Mrowczynski 
 * Target       : PIC16F628A
 * Compiler     : XC8 v1.45 Free version
 * IDE          : MPLAB X IDE v4.10
 * Programmer   : PICKit3
 * Last Updated : 3/1/2018
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
 * For our purposes, these values are set to:
 * t_interrupt = 0.1ms
 * TMR2 = 0
 * Tosc = (1 / 20,000,000) [s]
 * Prescaler => 1:4 = 4
 * Postscaler => 1:1 = 1
 *
 * Which yields a PR2 value of 124.
 */

#ifndef _TMR2_H_
#define _TMR2_H_

#include <pic.h>

// Declare constants related to the PIC's Timer 2 interrupts

#define MAX_VALUE           200                             // Need 200ms period
#define COUNTER_RESET       MAX_VALUE                       // Reset sawtoothcounter every 200ms
#define MIN_SLIDER_VAL      180
#define MAX_SLIDER_VAL      190
#define position(velocity)  (MIN_SLIDER_VAL + (velocity))   // Allows for 11 unique angular positions
#define MIN_VELOCITY_VAL    0
#define MAX_VELOCITY_VAL    10
#define PR2_INIT            124 
#define T2CON_INIT          0b00000101  // Enables Timer 2
#define ENABLE_INTERRUPTS   0b00100010  // Enable Timer 2 and Receive Interrupts

static inline void initTMR2(void) {
    T2CON   = T2CON_INIT;           // Setup Timer2     
    PR2     = PR2_INIT;             // Set initial value for Timer2's PR2 register
    PIE1    = ENABLE_INTERRUPTS;    // Enable Timer2 and Receive interrupts
}

#endif