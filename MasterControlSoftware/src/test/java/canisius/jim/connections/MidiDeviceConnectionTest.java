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
	
	@Test void connect() throws MidiUnavailableException {
		System.out.println("I am in super class");
		// Before a connection is made, the MidiDevice should not be open.
		assertFalse(midiDeviceConnection.getMidiDevice().isOpen());
		
		// After a connection is made, the MidiDevice should be open.
		midiDeviceConnection.connect();
		assertTrue(midiDeviceConnection.getMidiDevice().isOpen());
	}
	
	@Test void disconnect() {
		// The MidiDevice should start out not being open.
		assertFalse(midiDeviceConnection.getMidiDevice().isOpen());
		
		// After a connection is made, the MidiDevice should be open.
		midiDeviceConnection.connect();
		assertTrue(midiDeviceConnection.getMidiDevice().isOpen());
		
		// After a disconnect is performed, the MidiDevice should not be open.
		midiDeviceConnection.disconnect();
		assertFalse(midiDeviceConnection.getMidiDevice().isOpen());
	}
	
	@Test void getMidiDevice() {
		// getMidiDevice should return the midiDevice instance object.
		assertSame(midiDeviceConnection.midiDevice, midiDeviceConnection.getMidiDevice());
	}
}