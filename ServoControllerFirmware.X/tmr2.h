/*
 * File Name    : tmr2.h   
 * Version      : 1.0
 * Author       : Jon Mrowczynski 
 * Target       : PIC16F628A
 * Compiler     : XC8 v1.45 Free version
 * IDE          : MPLAB X IDE v4.15
 * Programmer   : PICKit3
 * Last Updated : 3/3/2018
 *
 * In order to get the necessary 0.1ms TIMER2 interrupt period resolution 
 * (t_interrupt), the equation below is used to determine the value for the 
 * PR2 register:
 *
 * t_interrupt [s] = [(PR2 - TMR2) + 1] * 4 * Tosc * Prescaler * Postscaler [s]
 *
 * where TMR2 is the value stored in the TMR2 register, Tosc is the period of
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
 * which yields a PR2 value of 124.
 */

#ifndef _TMR2_H_
#define _TMR2_H_

#include <pic.h>
#include <stdbool.h>
#include "pins.h"

#define PR2_INIT    124 

#define PWM_PERIOD              200 // 200ms PWM period
#define MIN_SAWTOOTH_THRESHOLD  180

static inline void initTMR2(void) {
    PR2     = PR2_INIT;
    T2CON   = 0b00000001;   // Set a 1:4 Prescaler value
    TMR2IF  = CLEAR;
    TMR2IE  = true;
    TMR2ON  = true;
}

#endif