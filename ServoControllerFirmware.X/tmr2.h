/*
 * File Name    : tmr2.h   
 * Version      : 1.0
 * Author       : Jon Mrowczynski 
 * Target       : PIC16F628A
 * Compiler     : XC8 v1.45 Free version
 * IDE          : MPLAB X IDE v4.10
 * Programmer   : PICKit3.5
 * Last Updated : 3/1/2018
 * 
 */

#ifndef _TMR2_H_
#define _TMR2_H_

// Declare constants related to the PIC's Timer 2 interrupts

#define MAX_VALUE           200                             // Need 200ms period
#define COUNTER_RESET       MAX_VALUE                       // Reset sawtoothcounter every 200ms
#define MIN_SLIDER_VAL      180
#define MAX_SLIDER_VAL      190
#define position(velocity)  (MIN_SLIDER_VAL + (velocity))   // Allows for 11 unique angular positions
#define MIN_VELOCITY_VAL    0
#define MAX_VELOCITY_VAL    10
#define PR2_INIT            124 

#endif