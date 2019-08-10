/*
 * File Name    : xbee.h
 * Version      : 1.0
 * Author       : Jon Mrowczynski
 * Target       : PIC18F24K40
 * Compiler     : XC8 v2.00 Free version
 * IDE          : MPLAB X IDE v5.05
 * Programmer   : PICkit3
 * Last Updated : 8/31/2018
 * 
 * Contains all of the constants required for the microcontroller to 
 * successfully interface with a router XBee in API mode.
 */

#ifndef _XBEE_H_
#define _XBEE_H_

// Constant to help with transmit request packet construction readability

#define PACKET_LENGTH                   20
#define START_BYTE                      0x7E
#define LENGTH_MSB                      0x00
#define LENGTH_LSB                      (PACKET_LENGTH - 4)
#define FRAME_TYPE                      0x10
#define FRAME_ID                        0x00
#define MY_ADDRESS_MSB                  0xFF
#define MY_ADDRESS_LSB                  0xFE
#define BROADCAST_RADIUS                0x00
#define OPTIONS                         0x00
#define TRANSMITTED_RF_DATA_BYTE1_INDEX (PACKET_LENGTH - 3)
#define TRANSMITTED_RF_DATA_BYTE2_INDEX (PACKET_LENGTH - 2)
#define TRANSMITTED_CHECKSUM_INDEX      (PACKET_LENGTH - 1)
#define PARTIAL_SUM                     (FRAME_TYPE + FRAME_ID + MY_ADDRESS_MSB + MY_ADDRESS_LSB + BROADCAST_RADIUS + OPTIONS)

// Constants to help with the received packets deconstruction readability

#define RECEIVED_PACKET_LENGTH      18
#define MSB_RECEIVED_PACKET_LENGTH  0
#define LSB_RECEIVED_PACKET_LENGTH  (RECEIVED_PACKET_LENGTH - 4) 

#define START_BYTE_INDEX                0
#define MSB_LENGTH_INDEX                1
#define LSB_LENGTH_INDEX                2
#define RECEIVED_RF_DATA_BYTE1_INDEX    (RECEIVED_PACKET_LENGTH - 3)
#define RECEIVED_RF_DATA_BYTE2_INDEX    (RECEIVED_PACKET_LENGTH - 2)
#define RECEIVED_CHECKSUM_INDEX         (RECEIVED_PACKET_LENGTH - 1)

#endif