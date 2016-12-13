package canisius.jim;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import javafx.application.Application;
import javafx.stage.Stage;

public class MIDITest extends Application {
		
	public static final byte EYELIDS = RuppetControl.EYELIDS;
	
	public static void main(String[] args) throws InvalidMidiDataException { launch(args); } 
	
	private class ShorterMessage extends MidiMessage {
		
		private static final byte LENGTH = 2;
		
		public ShorterMessage(byte firstByte, int secondByte) {
			this(new byte[2]);
			data = new byte[2];
			if (firstByte <= (2 * Byte.MAX_VALUE - 1)) data[0] = firstByte;
			else throw new IllegalArgumentException("The first byte must be <= " + (2 * Byte.MAX_VALUE - 1));
			if (secondByte <= Byte.MAX_VALUE) data[1] = (byte) secondByte;
			else throw new IllegalArgumentException("The second byte must be <=" + Byte.MAX_VALUE);
		}
		
		protected ShorterMessage(byte[] bytes) {
			super(bytes);
		}
		
		/**
		 * Obtains the first byte of the MIDI message
		 * @return the first byte of the MIDI message
		 */
		
		public byte getFirstByte() {
			return data[0];
		}  
		
		/**
		 * Obtains the second byte of the MIDI message
		 * @return the second byte of the MIDI message
		 */
		
		public byte getSecondByte() {
			return data[1];
		}
		
		/**
		 * Returns the number of bytes that the ShorterMessage contains which will always be 2.
		 * @return the number of bytes in the ShorterMessage 
		 */
		
		public int getLength() {
			return LENGTH;
		}
		
		/*
		 * Creates a new object of the same class and with the same contents as the object.
		 * @return a clone of this instance.
		 */

		@Override
		public Object clone() {
			byte[] newData = new byte[length];
			System.arraycopy(data, 0, newData, 0, newData.length);
			ShorterMessage msg = new ShorterMessage(newData);
			return msg;
		}
		
	}

	@Override
	public void start(Stage arg0) throws Exception {
		
		MIDITest midiTest = new MIDITest();
		
		final byte EYELIDS = (byte) ShortMessage.NOTE_OFF;
		
		ShorterMessage msg1 = midiTest.new ShorterMessage(EYELIDS, 0);
		ShorterMessage msg2 = midiTest.new ShorterMessage(EYELIDS, 10);
		
		final byte EYELIDS_OLD = (byte) 0x4A;
		
		ShortMessage msg3 = new ShortMessage(RuppetControl.NOTE_ON, RuppetControl.CHAN_1, EYELIDS_OLD, 0);
		ShortMessage msg4 = new ShortMessage(RuppetControl.NOTE_ON, RuppetControl.CHAN_1, EYELIDS_OLD, 10);

		System.out.println(msg1.getLength());
		System.out.println(msg2.getLength());
		
		Connect.setupConnections();
		
		while(true) {
			Connect.getUSBReceiver().send(msg3, -1);
			RuppetControl.pause_ms(500);
			Connect.getUSBReceiver().send(msg4, -1);
			RuppetControl.pause_ms(500);
			System.out.println("Sent");
		}
	
	}
	
} // end of MIDITest
