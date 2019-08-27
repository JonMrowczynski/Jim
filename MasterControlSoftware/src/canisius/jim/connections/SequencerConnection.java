package canisius.jim.connections;

import javax.sound.midi.*;

/**
 * {@code SequencerConnection} is a singleton class that allows one to make a connection to the system's default
 * {@code Sequencer} in order to store sequenced commands for the {@code Ruppet} in the form of {@code MidiEvent}s.
 * This allows the {@code Ruppet} to automatically run scripts.
 *
 * @author Jon Mrowczynski
 */

public class SequencerConnection extends MidiDeviceConnection<Sequencer> {

    /**
     * The singleton {@code SequencerConnection} instance.
     */

    private static final SequencerConnection SEQUENCER_CONNECTION = new SequencerConnection();

    /**
     * Returns the singleton {@code SequencerConnection} instance.
     *
     * @return the singleton {@code SequencerConnection} instance.
     */

    public static SequencerConnection getInstance() { return SEQUENCER_CONNECTION; }

    /**
     * Connects to the system's default {@code Sequencer} and disconnects it from the default {@code Synthesizer}. This
     * prevents the {@code MidiMessage}s from being played by the system's speakers.
     *
     * @see MidiSystem#getSequencer(boolean)
     * @see MidiDevice#isOpen()
     * @see MidiDevice#open()
     */

    public void connect() {
        try {
            // Disconnects the system's sequencer from the default device (the computer's speakers).
            midiDevice = MidiSystem.getSequencer(false);
            if (!midiDevice.isOpen()) { midiDevice.open(); }
        } catch (MidiUnavailableException e) { e.printStackTrace(); }
    }

    /**
     * Sets the {@code Receiver} that should receive the sequenced {@code MidiMessage}s.
     *
     * @param receiver that should receive {@code MidiMessage}s from this {@code SequencerConnection}.
     * @see javax.sound.midi.Transmitter#setReceiver(Receiver)
     */

    public void setReceiver(final Receiver receiver) {
        try { midiDevice.getTransmitter().setReceiver(receiver); }
        catch (MidiUnavailableException e) { e.printStackTrace(); }
    }

} // end of SequencerConnection
