/*
 * MIT License
 *
 * Copyright (c) 2013-2019 Jon Mrowczynski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * The equation below is used to determine the value for the PR2 register that 
 * is required to achieve a 0.1ms TIMER2 interrupt period resolution 
 * (t_interrupt):
 *
 * t_interrupt [s] = [(PR2 - TMR2) + 1] * 4 * Tosc * Prescaler * Postscaler [s]
 *
 * where TMR2 is the value stored in the TMR2 register, Tosc is the period of
 * the clock, PR2 is the value stored in the TIMER2 period register, Prescaler 
 * and Postscaler are the scales that are set in the TIMER2 configuration 
 * register.
 *
 * For our purposes, these values are set to:
 * t_interrupt = 0.1ms
 * TMR2 = 0
 * Tosc = (1 / 20,000,000) [s]
 * Prescaler => 1:4 = 4
 * Postscaler => 1:1 = 1
 *
 * which yields a PR2 value of 124.
 * 
 * @author Jon Mrowczynski
 */

#ifndef _TMR2_H_
#define _TMR2_H_

#include <pic.h>
#include <stdbool.h>
#include "pins.h"

#define PR2_INIT 124 

#define PWM_PERIOD              200 // in ms
#define MIN_SAWTOOTH_THRESHOLD  180

static inline void initTMR2(void) {
    PR2     = PR2_INIT;
    T2CON   = 0b00000001;   // Set a 1:4 Prescaler value
    TMR2IF  = CLEAR;
    TMR2IE  = true;
    TMR2ON  = true;
}

#endif