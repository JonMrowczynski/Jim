#include <xc.h>
#include "eeprom.h"

static unsigned char dataEEReadByte(unsigned char address) {
    NVMADRL = (address & 0xFF);
    NVMCON1bits.NVMREG = 0;
    NVMCON1bits.RD = 1;
    NOP();  // NOPs may be required for latency at high frequencies
    NOP();
    return (NVMDAT);
}

static void dataEEWriteByte(const unsigned char address, const unsigned char value) {
    unsigned char GIEBitValue = INTCONbits.GIE;   // Save interrupt enable
    NVMADRL = (address & 0xFF);
    NVMDAT = value;
    NVMCON1bits.NVMREG = 0;
    NVMCON1bits.WREN = 1;
    INTCONbits.GIE = 0; // Disable interrupts
    NVMCON2 = 0x55;
    NVMCON2 = 0xAA;
    NVMCON1bits.WR = 1;
    // Wait for write to complete
    while (NVMCON1bits.WR);
    NVMCON1bits.WREN = 0;
    INTCONbits.GIE = GIEBitValue;   // Restore interrupt enable
}

void saveSetting(const unsigned char address, const unsigned char value) {
    static unsigned char EEPROMValue; 
    EEPROMValue = dataEEReadByte(address);
    if (EEPROMValue != value)
        dataEEWriteByte(address, value);
}

unsigned char loadSettings(const unsigned char address) { return dataEEReadByte(address); }