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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import java.util.Arrays;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jon Mrowczynski
 */
class UsbMidiConnectionTest extends MidiDeviceConnectionTest {
	
	@BeforeAll static void init() { midiDeviceConnection = UsbMidiConnection.getInstance(); }
	
	@BeforeEach @Override void setUp() { UsbMidiConnection.getInstance().disconnect(); }
	
	@Test @Override void connect() throws MidiUnavailableException {
		super.connect();
		// The USB Receiver should also not be null.
		assertNotNull(UsbMidiConnection.getInstance().getUsbReceiver());
	}
	
	@Test @Override void disconnect() {
		super.disconnect();
		// The USB Receiver should start out null.
		assertNull(UsbMidiConnection.getInstance().getUsbReceiver());
		
		// After a connection, the USB Receiver should not be null.
		UsbMidiConnection.getInstance().connect();
		assertNotNull(UsbMidiConnection.getInstance().getUsbReceiver());
		
		// After a disconnect, the USB Receiver should be null.
		UsbMidiConnection.getInstance().disconnect();
		assertNull(UsbMidiConnection.getInstance().getUsbReceiver());
	}
	
	@Test void getInstance() {
		// The singleton instance should not be null.
		assertNotNull(UsbMidiConnection.getInstance());
	}
	
	@Test void doesUSBMidiDeviceExist() throws Exception {
		// Whether the USB MIDI device exists on the system or not, the Callable's result should match the result of
		// the
		// method.
		final Callable<Boolean> UsbMidiDeviceChecker =
				() -> Arrays.stream(MidiSystem.getMidiDeviceInfo()).map(MidiDevice.Info::getName)
						.anyMatch(name -> name.contains("USB") && name.contains("MIDIOUT"));
		assertEquals(UsbMidiDeviceChecker.call(), UsbMidiConnection.doesUSBMidiDeviceExist());
	}
	
	@Test void send() {
		// Sending a null MidiMessage should throw a NullPointerException
		assertThrows(NullPointerException.class, () -> UsbMidiConnection.getInstance().send(null));
		// The rest of the method is difficult to test using unit testing and should be tested with RuppetTester
		// instead.
	}
	
	@Test void getUsbReceiver() {
		// After a connection, the acquired USB Receiver, should be one of the MidiDevice's Receivers.
		// A MIDI In device can have more than one Receiver, so we are not necessarily guaranteed to get the same
		// Receiver Object from the getReceiver method. This is why the contains method is used.
		UsbMidiConnection.getInstance().connect();
		assertTrue(UsbMidiConnection.getInstance().getMidiDevice().getReceivers()
				           .contains(UsbMidiConnection.getInstance().getUsbReceiver()));
	}
}