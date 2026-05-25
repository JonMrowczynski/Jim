/*
 * MIT License
 *
 * Copyright (c) 2013-2023 Jon Mrowczynski
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sound.midi.*;

/**
 * {@code SequencerConnection} is a singleton class that allows one to make a connection to the system's default
 * {@link Sequencer} in order to store sequenced commands in the form of {@link MidiEvent}s.
 *
 * @author Jon Mrowczynski
 */
public final class SequencerConnection extends MidiDeviceConnection<Sequencer> {
	
	/**
	 * The resolution of the {@link Sequence} that is to be used by the {@code Sequencer}.
	 */
	public static final int RESOLUTION = 160;
	
	/**
	 * The singleton {@code SequencerConnection} instance.
	 */
	private static final @NotNull SequencerConnection SEQUENCER_CONNECTION = new SequencerConnection();
	
	/**
	 * This is a singleton class.
	 */
	private SequencerConnection() { }
	
	/**
	 * Returns the singleton {@code SequencerConnection} instance.
	 *
	 * @return the singleton {@code SequencerConnection} instance
	 */
	public static @NotNull SequencerConnection instance() { return SEQUENCER_CONNECTION; }
	
	/**
	 * Connects to the system's default {@link Sequencer} and disconnects it from the default {@link Synthesizer}. This
	 * prevents the {@link MidiMessage}s from being played by the system's speakers.
	 */
	@Override public void connect() {
		try {
			// Disconnects the system's sequencer from the default device (the computer's speakers).
			midiDevice = MidiSystem.getSequencer(false);
			if (!midiDevice.isOpen()) { midiDevice.open(); }
		}
		catch (final MidiUnavailableException e) { e.printStackTrace(); }
	}
	
	/**
	 * Sets the {@link Receiver} that should receive the sequenced {@link MidiMessage}s.
	 *
	 * @param receiver that should receive {@link MidiMessage}s from this {@code SequencerConnection}
	 */
	public void setReceiver(final @Nullable Receiver receiver) {
		if (midiDevice == null || !midiDevice.isOpen()) { return; }
		try { midiDevice.getTransmitter().setReceiver(receiver); }
		catch (final MidiUnavailableException e) { e.printStackTrace(); }
	}
}