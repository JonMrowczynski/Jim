/*
 * Copyright (c) 2018-2019 Jon Mrowczynski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
