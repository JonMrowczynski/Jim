package canisius.jim;

import java.util.Optional;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Transmitter;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;

/**
 * This class acquires the computer's default {@code Sequencer} which is used to store and 
 * organize the MIDI data. It also acquires the {@code Receiver} to a USB MIDI device such 
 * that MIDI messages can be created and sent through the device.
 * <P>
 * Note that in order to only transmit MIDI messages stored in the system's default 
 * {@code Sequencer} to the USB MIDI device's {@code Receiver}, you have to disconnect 
 * the {@code Sequencer} from the system's default {@code Receiver} (such as the computer's 
 * speakers). This way, the MIDI messages are ONLY sent to the USB MIDI device's {@code Receiver}.
 * <P>
 * If the program is unable to acquire the the USB MIDI device's {@code Receiver}, (which may 
 * occur if some other program is using it) then the user is presented with an error message 
 * and then they are asked to make sure that any program that may be using the USB MIDI device's
 * {@code Receiver} is closed. Once that is done, the user can then tell the program to retry to get the 
 * USB MIDI device's {@code Receiver} or, if the user so desires, they can exit the program.
 * <P>
 * Currently, the USB MIDI device's {@code Receiver} is acquired by obtaining an {@code ArrayList}
 * of all of the MIDI devices that the computer can find. It then makes a connection with the 
 * USB MIDI device whose description matches the {@code String} "External MIDI port".
 * 
 * @author Jon Mrowczynski
 * @version 2.0
 */

public final class Connect {
	
	/**
	 * The USB MIDI device used to transmit MIDI message through.
	 */
	
	private static MidiDevice usb = null;
	
	/**
	 * The {@code Receiver} of the acquired USB to MIDI device.
	 */
	
	private static Receiver usbReceiver = null;
	
	/**
	 * The {@code Transmitter} of the acquired USB to MIDI device.
	 */
	
	private static Transmitter usbTransmitter = null;
	
	/**
	 * The default system's {@code Sequencer}.
	 */
	
	private static Sequencer sequencer = null;
	
	/**
	 * A {@code Connect} object should not be instantiated. 
	 * <P>
	 * This should always be the case to prevent attempts at acquiring the same 
	 * USB MIDI device. Otherwise an error will be presented stating that the device 
	 * is being used by some other application (which it will be).
	 */

	private Connect() { throw new AssertionError("Should not instantiate Connect object."); }
	
	/**
	 * Gets the {@code Receiver} of the USB to MIDI device.
	 * 
	 * @return The {@code Receiver} to the USB to MIDI device
	 */
	
	public static final Receiver getUSBReceiver() { return usbReceiver;	}
	
	/**
	 * Gets the {@code Transmitter} of the USB to MIDI device.
	 * 
	 * @return The {@code Transmitter} of the USB to MIDI device
	 */
	
	public static final Transmitter getUSBTransmitter() { return usbTransmitter; }
	
	/**
	 * Gets the system's default {@code Sequencer}.
	 * 
	 * @return The system's default {@code Sequencer}
	 */
	
	public static final Sequencer getSequencer() { return sequencer; }
	
	/**
	 * Sets up the necessary connections between the system's default {@code Sequencer},
	 * and the USB MIDI device's {@code Receiver}.
	 */
	
	public static final void setupConnections() {
		EstablishUSBConnection();
		EstablishSequencerConnection();
	}
	
	/**
	 * Makes sure that all of the connections that were formed when the method 
	 * {@code setupConnections()} was called are properly closed.
	 */

	public static final void closeConnections() {
		closeUSB();
		closeSequencer();
	}
	
	/**
	 * Sets up a connection between the computer and the USB MIDI device's {@code Receiver}.
	 * <P>
	 * If no USB MIDI device could be found, then an error message is presented and the 
	 * user has the option to have the program to look for the device again.
	 * <P>
	 * If a USB MIDI device was found, but some other program is using it, then a message 
	 * is presented to the user stating that they should close the program using the USB
	 * MIDI device before they ask the program to attempt to reopen it.
	 * <P>
	 * If either error is presented to the user, they have the option to quit the program.
	 */
	
	public static final void EstablishUSBConnection() {
		
		do {
			
			getUSBMIDIDevice();
					
			if (usb == null) ErrorConnectingUSB();
			
		} while (usb == null); 
		
		OpenUSBDevice();  
				
		try { 
			
			if (usbReceiver == null)  usbReceiver = usb.getReceiver(); 
			
			final MidiDevice.Info[] devices = MidiSystem.getMidiDeviceInfo();
			
			for (MidiDevice.Info deviceInfo : devices) {
				final String deviceName = deviceInfo.getName();
				final String deviceDescription = deviceInfo.getDescription();
				final String deviceVendor = deviceInfo.getVendor();
				final String deviceVersion = deviceInfo.getVersion();
				
				final boolean isUSB = deviceName.contains("USB");
				final boolean isUnknown = deviceDescription.equals("No details available");
				
				System.out.println("Device Name: " + deviceName);
				System.out.println("Device Descritpion: " + deviceDescription);
				System.out.println("Device Vendor: " + deviceVendor);
				System.out.println("Device Version: " + deviceVersion);
				System.out.println();
				
				if(isUSB && isUnknown) {
					MidiDevice device  = MidiSystem.getMidiDevice(deviceInfo);
					device.open();
					usbTransmitter = device.getTransmitter();
				}
				
			}
			
			//if (usbTransmitter == null) usbTransmitter = usb.getTransmitter();
			
		} catch (MidiUnavailableException ex) { ex.printStackTrace(); }
		
	} // end of EstablishUSBConnection
	
	
	/**
	 * Closes the USB MIDI device and states that it has been successfully closed.
	 * <P>
	 * If the USB MIDI device has already been closed, then print a message 
	 * to the console stating that it already has been closed.
	 */
	
	public static final void closeUSB() {
		
		if (usb.isOpen()) usb.close();
		
	} 
	
	/**
	 * Gets the USB MIDI device that is connected to the computer. It does this by checking
	 * to see if the name contains the {@code String}: "USB" and if the description of the 
	 * device is: "External MIDI Port". Otherwise, no MIDI device will be gotten. Once it is 
	 * found, a confirmation message as well as the name and description of the device is printed to the screen.
	 */
	
	private static final void getUSBMIDIDevice() {
	
		final MidiDevice.Info[] devices = MidiSystem.getMidiDeviceInfo();
		
		for (MidiDevice.Info deviceInfo : devices) {
			
			final String deviceName = deviceInfo.getName();
			final String deviceDescription = deviceInfo.getDescription();
			
			final boolean isUSBDevice = deviceName.contains("USB");
			final boolean isExternalMIDIPort = deviceDescription.equals("External MIDI Port");
			
			if (isUSBDevice && isExternalMIDIPort) {
				
				try {
					
					usb = MidiSystem.getMidiDevice(deviceInfo);
					break;
					
				} catch (MidiUnavailableException e) { e.printStackTrace(); }	
				
			} // end if
			
		} // end for
		
	} // end of getUSBMIDIDevice
	
	/**
	 * This method is only called when the USB MIDI device could not be found. 
	 * <P>
	 * It presents the user with the option to have the program try to find the 
	 * USB MIDI device again. If the user does not want this to happen, then they
	 * also have the option to terminate the program.
	 */
	
	private static final void ErrorConnectingUSB() {
		
		final Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error Dialog");
		alert.setHeaderText("Error Connecting to USB MIDI Device");
		alert.setContentText("Make sure that the USB to MIDI cable is plugged in before retrying...");
		
		final ButtonType retryButtonType = new ButtonType("Retry");
		final ButtonType quitButtonType = new ButtonType("Quit", ButtonData.CANCEL_CLOSE);
		
		alert.getButtonTypes().setAll(retryButtonType, quitButtonType);
		
		final Optional<ButtonType> result = alert.showAndWait();
		
		if (result.get() == quitButtonType) {
			Platform.exit();
			System.exit(0);
		}
		
	} // end of ErrorConnectingUSB
	
	/**
	 * Opens the USB MIDI device for usage. 
	 * <P>
	 * If the USB MIDI device cannot be used because another program is using it, 
	 * then print an error message stating that and that the program that is using 
	 * it must be closed in order for it to be used for this application.
	 * <P>
	 * if the USB MIDI device has already been opened by the application, then print 
	 * a message stating that.
	 */

	private static final void OpenUSBDevice() {
		
		do {
			
			if (!usb.isOpen())
				try { usb.open(); } 
				catch (MidiUnavailableException ex) { ErrorOpeningUSB(); }
			
		} while (!usb.isOpen());
	
	} 
	
	/**
	 * This method is called when another program is using the USB MIDI device before 
	 * this program attempted.
	 * <P>
	 * The user is prompted to close the program that may be using the USB MIDI device 
	 * before attempting to reopen it. If the user does not want to do this however, 
	 * they also have the option to close the program.
	 */
	
	private static final void ErrorOpeningUSB() {
		
		final Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error Dialog");
		alert.setHeaderText("Error Opening USB to MIDI Device");
		alert.setContentText("Make sure that you close any programs that may be using the " 
			+ "USB to MIDI device before retrying...");
		
		final ButtonType retryButtonType = new ButtonType("Retry");
		final ButtonType quitButtonType = new ButtonType("Quit");
		
		alert.getButtonTypes().setAll(retryButtonType, quitButtonType);
		
		final Optional<ButtonType> result = alert.showAndWait();
		
		if (result.get() == quitButtonType) {
			Platform.exit();
			System.exit(0);
		}

	} // end of ErrorOpeningUSB
	
	/**
	 * Acquires the system's default {@code Sequencer} and sets its {@code Transmitter}
	 * to send to only the USB MIDI device's {@code Receiver}.
	 */
	
	private static final void EstablishSequencerConnection() {
		
		// Disconnects the system's sequencer from the computer's speakers.
		
		final boolean DISCONNECT = false;
		
		try {
			
			sequencer = MidiSystem.getSequencer(DISCONNECT); 
			OpenSequencer();
			sequencer.getTransmitter().setReceiver(usbReceiver);
						
		} catch (MidiUnavailableException ex) { ex.printStackTrace(); }

	} // end of EstablishSequencerConnection
	
	/**
	 * Opens the system's default {@code Sequencer}. 
	 * <P>
	 * If it already has been opened by the application, then print a message stating that.
	 */
	
	private static final void OpenSequencer() {
		
		if (!sequencer.isOpen()) 
			try { sequencer.open(); } 
			catch (MidiUnavailableException ex) { ex.printStackTrace(); }
		
	} 
	
	/*
	 * Closes the system's default {@code Sequencer} and states that it has been successfully 
	 * close. Else, print a message to the console stating that it has already been closed.
	 */
	
	private static final void closeSequencer() {
		
		if (sequencer.isOpen()) sequencer.close();
		
	} 
	
} // end of class Connect


