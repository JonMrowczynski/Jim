package canisius.jim.connections;

import javax.sound.midi.*;

/**
 * {@code MidiSequencer} is a singleton class that allows one to make a connection to the system's default
 * {@code Sequencer} in order to store sequenced commands for the {@code Ruppet} in the form of {@code MidiEvent}s.
 * This allows the {@code Ruppet} to run scripts.
 *
 * @author Jon Mrowczynski
 */

public class MidiSequencer extends MidiDeviceConnection<Sequencer> {

    /**
     * The singleton {@code MidiSequencer} instance.
     */

    private static final MidiSequencer midiSequencer = new MidiSequencer();

    /**
     * Returns the singleton {@code MidiSequencer} instance.
     *
     * @return the singleton {@code MidiSequencer} instance.
     */

    public static MidiSequencer getInstance() { return midiSequencer; }

    /**
     * {@inheritDoc}
     *
     * Also disconnects the system's default {@code Sequencer} from the default device (which is usually the computer's
     * speakers).
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
     * @param receiver that should receive {@code MidiMessage}s from this {@code MidiSequencer}.
     * @see javax.sound.midi.Transmitter#setReceiver(Receiver)
     */

    public void setReceiver(final Receiver receiver) {
        try { midiDevice.getTransmitter().setReceiver(receiver); }
        catch (MidiUnavailableException e) { e.printStackTrace(); }
    }

} // end of class MidiSequencer
