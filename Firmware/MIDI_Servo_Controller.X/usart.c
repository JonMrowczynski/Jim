/*
 * Author: Jon Mrowczynski
 */

#include <pic.h>
#include <stdbool.h>
#include "pins.h"
#include "usart.h"

void initUSART() {
    TRISB1  = INPUT;
    SPBRG   = BRATE;    // Set the MIDI baud rate to 31250
    BRGH    = SET;      // Use high speed baud rate
    CREN    = true;     // Enable continuous receive
    SPEN    = true;     // Enable serial port
}

void clearOverrunError() {
    static unsigned char temp = 0; 
    do {                
        temp = RCREG;  
        temp = RCREG;  
        CREN = false; 
        CREN = true;  
    } while(OERR); 
}

void clearFramingError() {
    static unsigned char temp = 0;
    do {
        temp = RCREG;
        temp = RCREG;
        SPEN = false;
        SPEN = true;
    } while (FERR);
}