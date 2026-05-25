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

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sound.midi.*;
import javax.sound.midi.MidiDevice.Info;
import java.util.Arrays;
import java.util.Optional;

/**
 * {@code UsbMidiConnection} is a singleton class that allows one to make a connection to a USB {@link MidiDevice} in
 * and transmit {@link MidiMessage}s through them.
 *
 * @author Jon Mrowczynski
 */
public final class UsbMidiConnection extends MidiDeviceConnection<MidiDevice> {
	
	/**
	 *
	 */
	private static final String MIDI_MESSAGE_NAME = MidiMessage.class.getSimpleName();
	
	/**
	 *
	 */
	private static final String RECEIVER_NAME = Receiver.class.getSimpleName();
	
	/**
	 * The singleton {@code UsbMidiConnection} instance.
	 */
	private static final @NotNull UsbMidiConnection USB_MIDI_CONNECTION = new UsbMidiConnection();
	
	/**
	 * The {@link Receiver} of the acquired {@code UsbMidiConnection}.
	 */
	private @Nullable Receiver usbMidiDeviceReceiver;
	
	/**
	 * This is a singleton class.
	 */
	private UsbMidiConnection() { }
	
	/**
	 * Returns the singleton {@code UsbMidiConnection} instance.
	 *
	 * @return the singleton {@code UsbMidiConnection} instance
	 */
	public static @NotNull UsbMidiConnection instance() { return USB_MIDI_CONNECTION; }
	
	/**
	 * Returns a {@code boolean} indicating whether there exists a USB MIDI connection that can be used with this
	 * program.
	 *
	 * @return a {@code boolean} indicating whether a USB MIDI connection exists that can be used with this program.
	 */
	public static boolean doesUSBMidiDeviceExist() {
		final var midiDeviceNames = Arrays.stream(MidiSystem.getMidiDeviceInfo()).map(Info::getName);
		return midiDeviceNames.anyMatch(name -> name.contains("USB") && name.contains("MIDIOUT"));
	}
	
	/**
	 * Sets up a connection between the computer and the USB {@link MidiDevice}'s {@link Receiver} by first acquiring
	 * the USB {@link MidiDevice}. This is done by acquiring the {@link MidiDevice} whose name contains the
	 * {@link String} "USB" and "MIDIOUT". Otherwise, no {@link MidiDevice} or {@link Receiver} will be acquired.
	 */
	@Override public void connect() {
		do {
			for (final var info : MidiSystem.getMidiDeviceInfo()) {
				if (!info.getName().contains("USB") || !info.getName().contains("MIDIOUT")) { continue; }
				try {
					final var tempMidiDevice = MidiSystem.getMidiDevice(info);
					if (tempMidiDevice.getMaxReceivers() == 0) { continue; }
					midiDevice = tempMidiDevice;
					break;
				}
				catch (final MidiUnavailableException e) { e.printStackTrace(); }
			}
			if (midiDevice == null) {
				new UsbMidiConnectionAlert("Error Connecting to USB MIDI Device",
				                           "Make sure that the USB to MIDI cable is plugged in before retrying.");
			}
		} while (midiDevice == null);
		while (!midiDevice.isOpen()) {
			try { midiDevice.open(); }
			catch (final MidiUnavailableException e) {
				new UsbMidiConnectionAlert("Error opening USB to MIDI device",
				                           "Close any programs that may be using the USB to MIDI device before " +
						                           "retrying.");
			}
		}
		try { usbMidiDeviceReceiver = midiDevice.getReceiver(); }
		catch (final MidiUnavailableException e) { e.printStackTrace(); }
	}
	
	/**
	 * In addition to disconnecting from the {@code midiDevice}, also set {@code usbMidiDeviceReceiver} to
	 * {@code null}.
	 */
	@Override public void disconnect() {
		super.disconnect();
		usbMidiDeviceReceiver = null;
	}
	
	/**
	 * A convenience method used to send the {@code midiMessage} to the {@link Receiver} of the connected
	 * {@link MidiDevice} with a {@code timeStamp} of -1.
	 *
	 * @param midiMessage that is to be sent to the {@link Receiver} of the connected {@link MidiDevice}
	 */
	public void send(final @Nullable MidiMessage midiMessage) {
		if (midiMessage == null) {
			IO.println("Cannot send a null " + MIDI_MESSAGE_NAME);
			return;
		}
		getUsbReceiver().ifPresentOrElse(receiver -> receiver.send(midiMessage, -1), () -> System.err.println(
				"Cannot send a " + MIDI_MESSAGE_NAME + " to a null " + RECEIVER_NAME));
	}
	
	/**
	 * Returns the {@link Receiver} of the {@link #USB_MIDI_CONNECTION} after {@code connect} has been called.
	 *
	 * @return The {@link Receiver} to the {@link #USB_MIDI_CONNECTION} or {@code null}
	 */
	public @NotNull Optional<Receiver> getUsbReceiver() { return Optional.ofNullable(usbMidiDeviceReceiver); }
	
	/**
	 * Presents the user with an {@link AlertType#ERROR Alert Dialog} that allows them to either retry to form a
	 * {@code UsbMidiConnection} or close the program.
	 *
	 * @author Jon Mrowczynski
	 */
	private static final class UsbMidiConnectionAlert extends Alert {
		
		/**
		 * Constructs a new {@code UsbMidiConnectionAlert} and waits for user input.
		 *
		 * @param headerText  for the {@code UsbMidiConnectionAlert}
		 * @param contentText for the {@code UsbMidiConnectionAlert}
		 */
		UsbMidiConnectionAlert(final @Nullable String headerText, final @Nullable String contentText) {
			super(AlertType.ERROR);
			setTitle("Error Dialog");
			setHeaderText(headerText);
			setContentText(contentText);
			getButtonTypes().setAll(new ButtonType("Retry"), ButtonType.CLOSE);
			getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			showAndWait().ifPresent(result -> {
				if (result != ButtonType.CLOSE) { return; }
				Platform.exit();
				System.exit(0);
			});
		}
	}
}