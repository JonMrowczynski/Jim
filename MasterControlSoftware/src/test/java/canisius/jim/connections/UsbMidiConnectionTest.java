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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link UsbMidiConnection}.
 *
 * @author Jon Mrowczynski
 */
final class UsbMidiConnectionTest extends MidiDeviceConnectionTest<UsbMidiConnection> {
	
	/**
	 * Sets the {@link #midiDeviceConnection} to the singleton instance of {@link SequencerConnection}.
	 */
	@BeforeEach void setUp() { midiDeviceConnection = UsbMidiConnection.instance(); }
	
	/**
	 * Tests {@link UsbMidiConnection#connect()}. The USB {@link Receiver} should not be {@code null}.
	 */
	@Test @Override void connect() {
		super.connect();
		assertNotNull(midiDeviceConnection.getUsbReceiver());
	}
	
	/**
	 * Tests {@link UsbMidiConnection#disconnect()}. The USB {@link Receiver} should start out {@code null} then
	 * become not {@code null} after connecting. After disconnected, it should become {@code null} again.
	 */
	@Test @Override void disconnect() {
		super.disconnect();
		assertTrue(midiDeviceConnection.getUsbReceiver().isEmpty());
		midiDeviceConnection.connect();
		assertTrue(midiDeviceConnection.getUsbReceiver().isPresent());
		midiDeviceConnection.disconnect();
		assertTrue(midiDeviceConnection.getUsbReceiver().isEmpty());
	}
	
	/**
	 * Tests {@link UsbMidiConnection#doesUSBMidiDeviceExist()} Whether the USB {@link MidiDevice} exists on the
	 * system or not, the {@link Callable}'s results should match the result of the
	 */
	@Test void doesUSBMidiDeviceExist() {
		final Callable<Boolean> UsbMidiDeviceChecker = () -> {
			final var names = Arrays.stream(MidiSystem.getMidiDeviceInfo()).map(MidiDevice.Info::getName);
			return names.anyMatch(name -> name.contains("USB") && name.contains("MIDIOUT"));
		};
		try { assertEquals(UsbMidiDeviceChecker.call(), UsbMidiConnection.doesUSBMidiDeviceExist()); }
		catch (final Exception e) { fail(e); }
	}
	
	/**
	 * Tests {@link UsbMidiConnection#send(MidiMessage)} by Sending a {@code null} {@link MidiMessage} shouldn't
	 * throw anything.
	 */
	@Test void sendNullMessage() { assertDoesNotThrow(() -> midiDeviceConnection.send(null)); }
	
	/**
	 * Tests {@link UsbMidiConnection#getUsbReceiver()}. After a connection, the acquired USB {@link Receiver} should
	 * be one of the {@link MidiDevice}'s {@link Receiver}s. A MIDI In device can have more than one {@link Receiver},
	 * so we are not necessarily guaranteed to get the same {@link Receiver} from the
	 * {@link MidiDevice#getReceiver()} method. Therefore, the {@link Stream#anyMatch(Predicate)} is used.
	 */
	@Test void getUsbReceiver() {
		midiDeviceConnection.connect();
		midiDeviceConnection.getUsbReceiver().ifPresentOrElse(receiver -> {
			final var allReceivers = midiDeviceConnection.getMidiDevice().map(MidiDevice::getReceivers).stream();
			assertTrue(allReceivers.anyMatch(receivers -> receivers.stream().anyMatch(r -> r == receiver)));
		}, Assertions::fail);
	}
}