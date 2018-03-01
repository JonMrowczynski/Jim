/*
 * File Name    : usart.h   
 * Version      : 1.0
 * Author       : Jon Mrowczynski 
 * Target       : PIC16F628A
 * Compiler     : XC8 v1.45 Free version
 * IDE          : MPLAB X IDE v4.10
 * Programmer   : PICKit3.5
 * Last Updated : 3/1/2018
 * 
 */

#ifndef _USART_H_
#define _USART_H_

// Declare constants that initialize some of the PIC's registers 

#define BRATE               39          // Set a midi baudrate to 31250
#define UENABLE             0b10100100  // USART enable
#define RCENABLE            0b10010000  // Receive enable
#define INTCON_INIT         0b11000000  // Enables global and peripheral interrupts
#define T2CON_INIT          0b00000101  // Enables Timer 2
#define ENABLE_INTERRUPTS   0b00100010  // Enable Timer 2 and Receive Interrupts
#define DISABLE_COMPARATORS 0b00000111  // Disable the PIC'S comparators

#endif