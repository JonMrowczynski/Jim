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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sound.midi.MidiDevice;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests {@link MidiDeviceConnection}
 *
 * @author Jon Mrowczynski
 */
abstract class MidiDeviceConnectionTest<T extends MidiDeviceConnection<?>> {
	
	/**
	 * The {@link MidiDeviceConnection} used for testing.
	 */
	protected T midiDeviceConnection;
	
	/**
	 * Makes sure that the {@link MidiDeviceConnection} is disconnected after each test.
	 */
	@AfterEach void tearDown() {
		if (midiDeviceConnection == null) { return; }
		midiDeviceConnection.disconnect();
		midiDeviceConnection = null;
	}
	
	/**
	 * Assume that the {@link MidiDeviceConnection} is not {@code null} before performing any of the other tests.
	 */
	@BeforeEach void instanceNotNull() { assumeTrue(midiDeviceConnection != null); }
	
	/**
	 * Tests {@link MidiDeviceConnection#connect()} by making sure it can properly establish a connection to a
	 * {@link MidiDevice}. Before a connection is made, the {@link MidiDevice} should not be open. However, after a
	 * connection is made, the {@link MidiDevice} should be open.
	 */
	@Test void connect() {
		final var midiDevice = midiDeviceConnection.getMidiDevice();
		midiDevice.ifPresentOrElse(device -> assertFalse(device.isOpen()), Assertions::fail);
		midiDeviceConnection.connect();
		midiDevice.ifPresentOrElse(device -> assertTrue(device.isOpen()), Assertions::fail);
	}
	
	/**
	 * Tests {@link MidiDeviceConnection#disconnect()} by making sure that a connection can be successfully
	 * disconnected.
	 */
	@Test void disconnect() {
		final var midiDevice = midiDeviceConnection.getMidiDevice();
		// The MidiDevice should start out not being open.
		midiDevice.ifPresentOrElse(device -> assertFalse(device.isOpen()), Assertions::fail);
		
		// After a connection is made, the MidiDevice should be open.
		midiDeviceConnection.connect();
		midiDevice.ifPresentOrElse(device -> assertTrue(device.isOpen()), Assertions::fail);
		
		// After a disconnect is performed, the MidiDevice should not be open.
		midiDeviceConnection.disconnect();
		midiDevice.ifPresentOrElse(device -> assertFalse(device.isOpen()), Assertions::fail);
	}
	
	/**
	 * Tests {@link MidiDeviceConnection#getMidiDevice()} by ensuring it returns the {@link MidiDevice} instance.
	 */
	@Test void getMidiDevice() {
		midiDeviceConnection.getMidiDevice()
				.ifPresentOrElse(device -> assertSame(midiDeviceConnection.midiDevice, device), Assertions::fail);
	}
}