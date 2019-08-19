package canisius.jim;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;

import javax.sound.midi.*;
import javax.sound.midi.MidiDevice.Info;

/**
 * {@code MidiConnection} is a utility class that allows one to make a connection to a USB MIDI device to send MIDI
 * messages to the electronics in order to control the {@code Ruppet}.
 * It also allows one to make a connection to the system's default {@code Sequencer} in order for the {@code Ruppet} to
 * be able to run scripts.
 *
 * This class is meant to be used with a JavaFX {@code Application} since calls to {@code Control}s are made in order to
 * present errors to the user and allow for them to decide how to handle them.
 * 
 * @author Jon Mrowczynski
 */

final class MidiConnection {
	
	/**
	 * The USB MIDI device used to transmit MIDI messages.
	 */
	
	private static MidiDevice usbMidiDevice = null;
	
	/**
	 * The {@code Receiver} of the acquired USB MIDI device.
	 */
	
	private static Receiver usbMidiDeviceReceiver = null;
	
	/**
	 * The default system's {@code Sequencer}.
	 */
	
	private static Sequencer sequencer = null;
	
	/**
	 * A {@code MidiConnection} object should not be instantiated.
	 */

	private MidiConnection() { throw new AssertionError("Should not instantiate " + MidiConnection.class.getSimpleName() + " object."); }
	
	/**
	 * Gets the {@code Receiver} of the USB MIDI device but only after {@code establishUsbMidiConnection} has been
	 * called.
	 * 
	 * @return The {@code Receiver} to the USB MIDI device.
	 */
	
	static Receiver getUsbReceiver() { return usbMidiDeviceReceiver; }
	
	/**
	 * Gets the system's default {@code Sequencer} but only after {@code establishSequencerConnection} has been called.
	 * 
	 * @return The system's default {@code Sequencer}.
	 */
	
	static Sequencer getSequencer() { return sequencer; }

	/**
	 * Establish a connection to a USB MIDI device such that MIDI messages can be sent to the electronics to control the
	 * {@code Ruppet}.
	 *
	 * @see #establishUsbMidiConnection()
	 * @see #establishSequencerConnection()
	 */

	static void establishConnection() {
		establishUsbMidiConnection();
		establishSequencerConnection();
	}

	/**
	 * Closes the connection to the USB MIDI device such that MIDI messages can no longer be sent to the electronics
	 * to control the {@code Ruppet}. However, the connection can be reestablished by calling
	 * {@code establishConnection}.
	 *
	 * @see #closeUsbMidiDevice()
	 * @see #closeSequencer()
	 * @see #establishConnection()
	 */

	static void closeConnection() {
		closeUsbMidiDevice();
		closeSequencer();
	}
	
	/**
	 * Sets up a connection between the computer and the USB MIDI device's {@code Receiver}.
	 * 
	 * Gets the USB MIDI device that is connected to the computer by checking to see if the name of the device contains
	 * the {@code String} "USB" and if the description of the device is "External MIDI Port". Otherwise, no MIDI device
	 * will be acquired.
	 */
	
	private static void establishUsbMidiConnection() {
		do {
			for (final Info deviceInfo : MidiSystem.getMidiDeviceInfo()) {
				final boolean isUSBDevice = deviceInfo.getName().contains("USB");
				final boolean isExternalMIDIPort = deviceInfo.getDescription().equals("External MIDI Port");
				if (isUSBDevice && isExternalMIDIPort) {
					try {
						usbMidiDevice = MidiSystem.getMidiDevice(deviceInfo);
						break;
					} catch (MidiUnavailableException e) { e.printStackTrace(); }
				}
			}	
			if (usbMidiDevice == null) { errorConnectingToUsbMidiDevice(); }
		} while (usbMidiDevice == null); 
		do {
			if (!usbMidiDevice.isOpen()) {
				try { usbMidiDevice.open(); }
				catch (MidiUnavailableException e) { errorOpeningUsbMidiDevice(); }
			}
		} while (!usbMidiDevice.isOpen());  
		try { 
			if (usbMidiDeviceReceiver == null) { usbMidiDeviceReceiver = usbMidiDevice.getReceiver(); }
		} catch (MidiUnavailableException e) { e.printStackTrace(); }
	}
	
	/**
	 * Closes the USB MIDI device.
	 */
	
	private static void closeUsbMidiDevice() { if (usbMidiDevice != null && usbMidiDevice.isOpen()) { usbMidiDevice.close(); } }
	
	/**
	 * Disconnects the system's default {@code Sequencer} from the default device and sets the {@code Sequencer}'s
	 * {@code Transmitter} to send to the USB MIDI device's {@code Receiver} instead.
	 * 
	 * @throws NullPointerException if a connection has not been established to a USB MidiDevice.
	 */
	
	private static void establishSequencerConnection() throws NullPointerException {
		if (usbMidiDeviceReceiver == null) {
			throw new NullPointerException("Must acquire a USB MIDI device before establishing a connection to the system's default Sequencer");
		}
		try {
			sequencer = MidiSystem.getSequencer(false); // Disconnects the system's sequencer from the default device (the computer's speakers).
			if (!sequencer.isOpen()) { sequencer.open(); }
			sequencer.getTransmitter().setReceiver(usbMidiDeviceReceiver);			
		} catch (MidiUnavailableException e) { e.printStackTrace(); }
	}

	/**
	 * Closes the system's default {@code Sequencer}.
	 */
	
	private static void closeSequencer() { if (sequencer != null && sequencer.isOpen()) { sequencer.close(); } }

	/**
	 * Presents the user with an {@code Alert} dialog when the USB MIDI device could not be connected to in order to
	 * determine what to do next.
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
	 * Presents the user with an {@code Alert} dialog when the USB MIDI device could not be opened in order to determine
	 * what to do next.
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
	
} // end of MidiConnection class