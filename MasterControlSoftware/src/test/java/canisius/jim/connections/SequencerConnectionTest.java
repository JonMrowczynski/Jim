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
	
	@Test void getInstance() {
		// The singleton instance should not be null.
		assertNotNull(SequencerConnection.getInstance());
	}
	
	@Test @Override void connect() throws MidiUnavailableException {
		super.connect();
		// The connected Sequencer should not be connected to anything.
		assertNull(SequencerConnection.getInstance().getMidiDevice().getTransmitter().getReceiver());
	}
	
	@Test void setReceiver() {
		// Setting the Receiver after connecting should yield back that Receiver. The MidiDevice might have more than
		// one Transmitter, so all of its Transmitters' Receivers are examined to determine if at least one of their
		// Receivers' reference matches that of receiver.
		final var receiver = new Receiver() {
			@Override public void send(final MidiMessage message, final long timeStamp) {
				System.out.println(message.toString());
			}
			
			@Override public void close() { }
		};
		SequencerConnection.getInstance().connect();
		SequencerConnection.getInstance().setReceiver(receiver);
		final var transmitters = SequencerConnection.getInstance().getMidiDevice().getTransmitters().stream();
		assertTrue(() -> transmitters.map(Transmitter::getReceiver).anyMatch(r -> r == receiver));
	}
}