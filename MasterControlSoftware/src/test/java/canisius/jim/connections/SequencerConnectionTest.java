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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sound.midi.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link SequencerConnection}.
 *
 * @author Jon Mrowczynski
 */
final class SequencerConnectionTest extends MidiDeviceConnectionTest<SequencerConnection> {
	
	/**
	 * Sets the {@link #midiDeviceConnection} to the singleton instance of {@link SequencerConnection}.
	 */
	@BeforeEach void setUp() { midiDeviceConnection = SequencerConnection.instance(); }
	
	/**
	 * Tests {@link SequencerConnection#connect()}. The default {@link Sequencer} should not be connected to anything.
	 */
	@Test @Override void connect() {
		super.connect();
		midiDeviceConnection.getMidiDevice().ifPresentOrElse(device -> {
			try { assertNull(device.getTransmitter().getReceiver()); }
			catch (MidiUnavailableException e) { fail(e); }
		}, Assertions::fail);
	}
	
	/**
	 * Tests {@link SequencerConnection#setReceiver(Receiver)}. Setting the {@link Receiver} after connecting should
	 * yield back that {@link Receiver}. The {@link MidiDevice} might have more than one {@link Transmitter}, so all
	 * its {@link Transmitter}s are examined to determine if at least one of their {@link Receiver}s matches that of
	 * set {@link Receiver}.
	 */
	@Test void setReceiver() {
		final var receiver = new Receiver() {
			@Override public void send(final @NotNull MidiMessage message, final long timeStamp) { }
			
			@Override public void close() { }
		};
		midiDeviceConnection.connect();
		midiDeviceConnection.setReceiver(receiver);
		midiDeviceConnection.getMidiDevice().ifPresentOrElse(device -> {
			final var receivers = device.getTransmitters().stream().map(Transmitter::getReceiver);
			assertTrue(receivers.anyMatch(r -> r == receiver));
		}, Assertions::fail);
	}
}