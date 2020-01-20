package canisius.jim.connections;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jon Mrowczynski
 */
class SequencerConnectionTest extends MidiDeviceConnectionTest {

    @BeforeAll static void init() { midiDeviceConnection = SequencerConnection.getInstance(); }

    @Test
    void getInstance() {
        // The singleton instance should not be null.
        assertNotNull(SequencerConnection.getInstance());
    }

    @Test @Override
    void connect() throws MidiUnavailableException {
        super.connect();
        // The connected Sequencer should not be connected to anything.
        assertNull(SequencerConnection.getInstance().getMidiDevice().getTransmitter().getReceiver());
    }

    @Test
    void setReceiver() {
        // Setting the Receiver after connecting should yield back that Receiver. The MidiDevice might have more than
        // one Transmitter, so all of its Transmitters' Receivers are examined to determine if at least one of their
        // Receivers' reference matches that of receiver.
        final var receiver = new Receiver() {
            @Override public void send(MidiMessage message, long timeStamp) { System.out.println(message.toString()); }
            @Override public void close() { }
        };
        SequencerConnection.getInstance().connect();
        SequencerConnection.getInstance().setReceiver(receiver);
        final var transmitters = SequencerConnection.getInstance().getMidiDevice().getTransmitters().stream();
        assertTrue(() -> transmitters.map(Transmitter::getReceiver).anyMatch(r -> r == receiver));
    }
}