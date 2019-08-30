package canisius.jim.connections;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;

import javax.sound.midi.*;
import javax.sound.midi.MidiDevice.Info;

/**
 * {@code UsbMidiConnection} is a singleton class that allows one to make a connection to a USB {@code MidiDevice} in
 * order to control the {@code Ruppet} by transmitting {@code MidiMessage}s to the electronics.
 * 
 * @author Jon Mrowczynski
 */

public final class UsbMidiConnection extends MidiDeviceConnection<MidiDevice> {

	/**
	 * The singleton {@code UsbMidiConnection} instance.
	 */

	private static final UsbMidiConnection USB_MIDI_CONNECTION = new UsbMidiConnection();

	/**
	 * Returns the singleton {@code UsbMidiConnection} instance.
	 *
	 * @return the singleton {@code UsbMidiConnection} instance.
	 */

	public static UsbMidiConnection getInstance() { return USB_MIDI_CONNECTION; }
	
	/**
	 * The {@code Receiver} of the acquired {@code UsbMidiConnection}.
	 */
	
	private Receiver usbMidiDeviceReceiver;
	
	/**
	 * Sets up a connection between the computer and the USB {@code MidiDevice}'s {@code Receiver} by first acquiring
	 * the USB {@code MidiDevice}. This is done by acquiring the {@code MidiDevice} whose name contains the
	 * {@code String} "USB" and whose description is "External MIDI Port". Otherwise, no {@code MidiDevice} or
	 * {@code Receiver} will be acquired.
	 */

	@Override
	public final void connect() {
		do {
			for (final Info info : MidiSystem.getMidiDeviceInfo()) {
				if (info.getName().contains("USB") && info.getName().contains("MIDIOUT")) {
					try {
						final MidiDevice tempMidiDevice = MidiSystem.getMidiDevice(info);
						if (tempMidiDevice.getMaxReceivers() != 0) {
							midiDevice = tempMidiDevice;
							break;
						}
					} catch (MidiUnavailableException e) { e.printStackTrace(); }
				}
			}	
			if (midiDevice == null) {
				new UsbMidiConnectionAlert("Error Connecting to USB MIDI Device",
					"Make sure that the USB to MIDI cable is plugged in before retrying.");
			}
		} while (midiDevice == null);
		do {
			if (!midiDevice.isOpen()) {
				try { midiDevice.open(); }
				catch (MidiUnavailableException e) {
					new UsbMidiConnectionAlert("Error opening USB to MIDI device",
						"Close any programs that may be using the USB to MIDI device before retrying.");
				}
			}
		} while (!midiDevice.isOpen());
		try { if (usbMidiDeviceReceiver == null) { usbMidiDeviceReceiver = midiDevice.getReceiver(); } }
		catch (MidiUnavailableException e) { e.printStackTrace(); }
	}

	/**
	 * A convenience method used to send the {@code midiMessage} to the {@code Receiver} of the connected
	 * {@code MidiDevice} with a {@code timeStamp} of -1.
	 *
	 * @param midiMessage that is to be sent to the {@code Receiver} of the connected {@code MidiDevice}.
	 * @throws NullPointerException if the {@code connect()} method of this class has not been called or if it failed.
	 * @see Receiver#send(MidiMessage, long)
	 */

	public final void send(final MidiMessage midiMessage) throws NullPointerException { usbMidiDeviceReceiver.send(midiMessage, -1); }

	/**
	 * Returns the {@code Receiver} of the {@code USB_MIDI_CONNECTION} after {@code connect} has been called. Otherwise,
	 * {@code null} is returned.
	 *
	 * @return The {@code Receiver} to the {@code USB_MIDI_CONNECTION} or {@code null}.
	 */

	public final Receiver getUsbReceiver() { return usbMidiDeviceReceiver; }

	/**
	 * Presents the user with an {@code AlertType.ERROR Alert Dialog} that allows them to either retry to form a
	 * {@code UsbMidiConnection} or close the program.
	 *
	 * @author Jon Mrowczynski
	 */

	private static final class UsbMidiConnectionAlert extends Alert {

		/**
		 * Constructs a new {@code UsbMidiConnectionAlert} and waits for user input.
		 *
		 * @param headerText for the {@code UsbMidiConnectionAlert}.
		 * @param contentText for the {@code UsbMidiConnectionAlert}.
		 */

		UsbMidiConnectionAlert(final String headerText, final String contentText) {
			super(AlertType.ERROR);
			setTitle("Error Dialog");
			setHeaderText(headerText);
			setContentText(contentText);
			getButtonTypes().setAll(new ButtonType("Retry"), ButtonType.CLOSE);
			getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			showAndWait().ifPresent(theResult -> {
				if (theResult == ButtonType.CLOSE) {
					Platform.exit();
					System.exit(0);
				}
			});
		}
	} // end of UsbMidiConnectionAlert
	
} // end of UsbMidiConnection