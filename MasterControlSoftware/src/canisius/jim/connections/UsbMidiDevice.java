package canisius.jim.connections;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;

import javax.sound.midi.*;
import javax.sound.midi.MidiDevice.Info;

/**
 * {@code UsbMidiDevice} is a singleton class that allows one to make a connection to a USB MIDI device in order to
 * control the {@code Ruppet} through the transmission of {@code MidiMessage}s to the electronics.
 * 
 * @author Jon Mrowczynski
 */

public final class UsbMidiDevice extends MidiDeviceConnection<MidiDevice> {

	/**
	 * The singleton {@code UsbMidiDevice} instance.
	 */

	private static final UsbMidiDevice usbMidiDevice = new UsbMidiDevice();

	/**
	 * Returns the singleton {@code UsbMidiDevice} instance.
	 *
	 * @return the singleton {@code UsbMidiDevice} instance.
	 */

	public static UsbMidiDevice getInstance() { return usbMidiDevice; }

	/**
	 * Presents the user with an {@code Alert} dialog when a connection could not be formed to the
	 * {@code UsbMidiDevice} in order to determine what the user wants to do next.
	 */

	private static void errorConnectingToUsbMidiDevice() {
		final Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error Dialog");
		alert.setHeaderText("Error Connecting to USB MIDI Device");
		alert.setContentText("Make sure that the USB to MIDI cable is plugged in before retrying...");
		final ButtonType quitButtonType = new ButtonType("Quit", ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(new ButtonType("Retry"), quitButtonType);
		alert.showAndWait().ifPresent(theResult -> {
			if (theResult == quitButtonType) {
				Platform.exit();
				System.exit(0);
			}
		});
	}

	/**
	 * Presents the user with an {@code Alert} dialog when the {@code UsbMidiDevice} could not be opened in order to
	 * determine what the user wants to do next.
	 */

	private static void errorOpeningUsbMidiDevice() {
		final Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error Dialog");
		alert.setHeaderText("Error Opening USB to MIDI Device");
		alert.setContentText("Make sure that you close any programs that may be using the USB to MIDI device before retrying...");
		final ButtonType quitButtonType = new ButtonType("Quit");
		alert.getButtonTypes().setAll(new ButtonType("Retry"), quitButtonType);
		alert.showAndWait().ifPresent(theResult -> {
			if (theResult == quitButtonType) {
				Platform.exit();
				System.exit(0);
			}
		});
	}
	
	/**
	 * The {@code Receiver} of the acquired {@code UsbMidiDevice}.
	 */
	
	private Receiver usbMidiDeviceReceiver;
	
	/**
	 * Sets up a connection between the computer and the {@code UsbMidiDevice}'s {@code Receiver}.
	 * 
	 * Gets the USB MIDI device that is connected to the computer by checking to see if the name of the device contains
	 * the {@code String} "USB" and if the description of the device is "External MIDI Port". Otherwise, no
	 * {@code MidiDevice} will be acquired.
	 */

	@Override
	public final void connect() {
		do {
			for (final Info deviceInfo : MidiSystem.getMidiDeviceInfo()) {
				final boolean isUSBDevice = deviceInfo.getName().contains("USB");
				final boolean isExternalMIDIPort = deviceInfo.getDescription().equals("External MIDI Port");
				if (isUSBDevice && isExternalMIDIPort) {
					try {
						midiDevice = MidiSystem.getMidiDevice(deviceInfo);
						break;
					} catch (MidiUnavailableException e) { e.printStackTrace(); }
				}
			}	
			if (midiDevice == null) { errorConnectingToUsbMidiDevice(); }
		} while (midiDevice == null);
		do {
			if (!midiDevice.isOpen()) {
				try { midiDevice.open(); }
				catch (MidiUnavailableException e) { errorOpeningUsbMidiDevice(); }
			}
		} while (!midiDevice.isOpen());
		try { 
			if (usbMidiDeviceReceiver == null) { usbMidiDeviceReceiver = midiDevice.getReceiver(); }
		} catch (MidiUnavailableException e) { e.printStackTrace(); }
	}

	/**
	 * A convenience method used to send the {@code midiMessage} to the {@code Receiver} of the connected
	 * {@code MidiDevice} with a {@code timeStamp} of -1.
	 *
	 * @param midiMessage that is to be sent to the {@code Receiver} of the connected {@code MidiDevice}.
	 * @throws NullPointerException if the {@link #connect()} method of this class has not been called or failed.
	 * @see Receiver#send(MidiMessage, long)
	 */

	public final void send(final MidiMessage midiMessage) throws NullPointerException { usbMidiDeviceReceiver.send(midiMessage, -1); }

	/**
	 * Returns the {@code Receiver} of the {@code UsbMidiDevice} after {@code connect} has been called. Otherwise,
	 * {@code null} is returned.
	 *
	 * @return The {@code Receiver} to the {@code UsbMidiDevice}.
	 */

	public final Receiver getUsbReceiver() { return usbMidiDeviceReceiver; }
	
} // end of UsbMidiDevice class