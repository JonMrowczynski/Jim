/*
 * File Name    : configuration.h   
 * Version      : 1.0
 * Author       : Jon Mrowczynski 
 * Target       : PIC16F628A
 * Compiler     : XC8 v1.45 Free version
 * IDE          : MPLAB X IDE v4.10
 * Programmer   : PICKit3.5
 * Last Updated : 2/18/2018
 * 
 */

#ifndef _CONFIGURATION_H_
#define _CONFIGURATION_H_

// Set the configuration bits for the PIC

#pragma config CP       = ON    // Code protection on
#pragma config CPD      = ON    // Data memory code protection on
#pragma config LVP      = OFF   // Low-Voltage Programming Disabled
#pragma config BOREN    = ON    // Brown-out Reset Enabled
#pragma config MCLRE    = OFF   // Master Clear Disabled
#pragma config PWRTE    = ON    // Power Up Timer Enabled
#pragma config WDTE     = OFF   // Watchdog Timer Disabled
#pragma config FOSC     = HS    // Using an external 20Mhz quartz crystal resonator

#endif