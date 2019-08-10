/*
 * File Name    : configuration.h   
 * Version      : 1.0
 * Author       : Jon Mrowczynski 
 * Target       : PIC18F24K40
 * Compiler     : XC8 v2.00 Free version
 * IDE          : MPLAB X IDE v5.05
 * Programmer   : PICkit3
 * Last Updated : 8/31/2018
 */

#ifndef _CONFIGURATION_H_
#define _CONFIGURATION_H_

// Set the configuration bits for the PIC

// Configuration Word 1: Oscillators

#pragma config FCMEN    = ON                // Fail Safe Clock Monitor Enabled
#pragma config CSWEN    = OFF               // NOSC and NDIC bits can't be changed by software
#pragma config CLKOUTEN = OFF               // Disable CLKOUT function; I/O functoin on OSC2
#pragma config RSTOSC   = HFINTOSC_64MHZ    // Using high frequency internal clock
#pragma config FEXTOSC  = OFF               // DOn't use an external oscialltor

// Configuration Word 2: Supervisor

#pragma config XINST    = OFF       // Extended Instruction Set and Indexed Addressing disabled (must be until it is supported by the compiler)
#pragma config DEBUG    = OFF       // Background debugger disabled
#pragma config STVREN   = ON        // Stack Full/Underflow Reset enabled
#pragma config PPS1WAY  = ON        // PPS registers can only be set once
#pragma config ZCD      = OFF       // ZCD disabled
#pragma config BORV     = VBOR_285  // Brown-out Reset Voltage set to 2.85V
#pragma config BOREN    = SBORDIS   // Brown-out enabled, SBOREN bit is ignored
#pragma config LPBOREN  = OFF       // ULPBOR disabled
#pragma config PWRTE    = ON        // Power up timer enabled
#pragma config MCLRE    = INTMCLR   // MCLR pin function is port defined function

// Configuration Word 3: Windowed Watchdog Timer

#pragma config WDTCCS   = SC        // Software controlled watchdog clock selector
#pragma config WDTCWS   = WDTCWS_7  // Window always open; software controled; keyed acces not required
#pragma config WDTE     = OFF       // Disabled Watchdog timer
#pragma config WDTCPS   = WDTCPS_31 // Divider ratio 1:65536; software control of WDTPS

// Configuration Word 4: Memory Write Protection

#pragma config LVP      = OFF   // Disable low voltage programming disabled
#pragma config SCANE    = ON    // Scanner module is available for use
#pragma config WRTD     = ON    // Data EEPROM write-protected
#pragma config WRTB     = ON    // Boot Block write-protected
#pragma config WRTC     = ON    // Configuration registers write-protected
#pragma config WRT1     = ON    // Block 1 write-protected
#pragma config WRT0     = ON    // Block 0 write-protected

// Configuration Word 5: Code Protection

#pragma config CPD      = ON    // Data NVM code protection enabled
#pragma config CP       = ON    // User NVM code protection enabled

// Configuration Word 6: Memory Read Protection

#pragma config EBTRB    = ON    // Boot Block protected from table reads executed in other blocks
#pragma config EBTR1    = ON    // Block 1 protected from table reads executed in other blocks
#pragma config EBTR0    = ON    // Block 0 protected from table reads executed in other blocks

#endif