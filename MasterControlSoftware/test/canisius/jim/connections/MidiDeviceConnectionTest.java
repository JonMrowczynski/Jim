package canisius.jim.connections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sound.midi.MidiUnavailableException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jon Mrowczynski
 */
class MidiDeviceConnectionTest {

    static MidiDeviceConnection<?> midiDeviceConnection;

    @BeforeEach void setUp() { midiDeviceConnection.disconnect(); }

    @Test
    void connect() throws MidiUnavailableException {
        System.out.println("I am in super class");
        // Before a connection is made, the MidiDevice should not be open.
        assertFalse(midiDeviceConnection.getMidiDevice().isOpen());

        // After a connection is made, the MidiDevice should be open.
        midiDeviceConnection.connect();
        assertTrue(midiDeviceConnection.getMidiDevice().isOpen());
    }

    @Test
    void disconnect() {
        // The MidiDevice should start out not being open.
        assertFalse(midiDeviceConnection.getMidiDevice().isOpen());

        // After a connection is made, the MidiDevice should be open.
        midiDeviceConnection.connect();
        assertTrue(midiDeviceConnection.getMidiDevice().isOpen());

        // After a disconnect is performed, the MidiDevice should not be open.
        midiDeviceConnection.disconnect();
        assertFalse(midiDeviceConnection.getMidiDevice().isOpen());
    }

    @Test
    void getMidiDevice() {
        // getMidiDevice should return the midiDevice instance object.
        assertSame(midiDeviceConnection.midiDevice, midiDeviceConnection.getMidiDevice());
    }
}