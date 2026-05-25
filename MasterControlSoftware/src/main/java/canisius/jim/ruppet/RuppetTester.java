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

package canisius.jim.ruppet;

import canisius.jim.connections.UsbMidiConnection;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static canisius.jim.ruppet.Ruppet.MAX_VELOCITY;
import static canisius.jim.ruppet.Ruppet.MIN_VELOCITY;

/**
 * This class provides a GUI with {@code Control}s that assists with the interconnectivity testing and debugging of any
 * functionalities related to either the electronics, firmware, or software of the {@code Ruppet}.
 *
 * @author Jon Mrowczynski
 */
public class RuppetTester extends Application {
	
	private static final UsbMidiConnection usbMidiConnection = UsbMidiConnection.instance();
	
	@FXML private Button lightsOnButton;
	
	@FXML private Button lightsOffButton;
	
	@FXML private Spinner<Integer> velocitySelector;
	
	@FXML private Button sendMidiButton;
	
	/**
	 * A collection of MIDI notes that should be sent to the USB {@code MidiDevice}'s {@code Receiver}.
	 */
	private byte[] midiNotes;
	
	/**
	 * The velocity value that should be used when the {@code ShortMessage}s are sent to the USB {@code MidiDevice}'s
	 * {@code Receiver}.
	 */
	private byte currentVelocity;
	
	@Override public void start(final @NotNull Stage primaryStage) {
		final var loader = new FXMLLoader(getClass().getResource("/fxml/tester.fxml"));
		try { primaryStage.setScene(loader.load()); }
		catch (IOException e) { throw new RuntimeException(e); }
		
		primaryStage.setTitle("Ruppet Tester");
		primaryStage.sizeToScene();
		primaryStage.setResizable(false);
		primaryStage.setOnCloseRequest(_ -> usbMidiConnection.disconnect());
		primaryStage.show();
	}
	
	@FXML public void initialize() {
		velocitySelector.setValueFactory(new IntegerSpinnerValueFactory(MIN_VELOCITY, MAX_VELOCITY, MIN_VELOCITY));
		final var editor = velocitySelector.getEditor();
		/*
		 * If there is nothing in the Spinner's Editor, prevent a NullPointerException from being thrown if enter is
		 * pressed.
		 */
		editor.setOnAction(_ -> { if ("".equals(editor.getText())) { velocitySelector.cancelEdit(); } });
		/*
		 * Allow the user to increment or decrement the value in the Spinner's Editor, (the TextField) by using the up
		 * and down arrows respectively.
		 */
		editor.setOnKeyPressed(keyEvent -> {
			switch (keyEvent.getCode()) {
				case ENTER -> handleSendMidi();
				case UP -> velocitySelector.getValueFactory().increment(1);
				case DOWN -> velocitySelector.getValueFactory().decrement(1);
			}
		});
		/*
		 * Check to make sure that the user is entering in a valid value, specifically a value between the minimum
		 * velocity value and the maximum velocity value inclusive.
		 */
		editor.textProperty().addListener((_, oldValue, newValue) -> {
			if (newValue.isEmpty()) { return; }
			final var isNotNumber = !newValue.matches("\\d*");
			final var isNotValidNumber = newValue.charAt(0) == '0' && newValue.length() > 1;
			if (isNotNumber || isNotValidNumber) { editor.setText(oldValue); }
			else {
				final var newValueInt = Integer.parseInt(newValue);
				if (newValueInt > MAX_VELOCITY || newValueInt < MIN_VELOCITY) { editor.setText(oldValue); }
				currentVelocity = (byte) newValueInt;
			}
		});
		final var tooltip = new Tooltip("Min velocity: " + MIN_VELOCITY + "\nMax velocity: " + MAX_VELOCITY);
		velocitySelector.setTooltip(tooltip);
	}
	
	public void handleSendMidi() {
		try { makeMessages().forEach(usbMidiConnection::send); }
		catch (final InvalidMidiDataException e) { e.printStackTrace(); }
	}
	
	/**
	 * Creates a {@code Set} of {@code ShortMessage}s based on the {@code byte}s stored in {@code midiNotes} and the
	 * {@code currentVelocity} value.
	 *
	 * @return A {@code Set} of {@code ShortMessage}s that should be sent to the microcontroller
	 * @throws InvalidMidiDataException if any of the MIDI data is invalid
	 */
	private Set<ShortMessage> makeMessages() throws InvalidMidiDataException {
		final var messages = new HashSet<ShortMessage>();
		if (midiNotes.length >= 1) {
			messages.add(new ShortMessage(ShortMessage.NOTE_ON, 0, midiNotes[0], currentVelocity));
		}
		if (midiNotes.length == 2) {
			messages.add(new ShortMessage(ShortMessage.NOTE_ON, 0, midiNotes[1], MAX_VELOCITY - currentVelocity));
		}
		return messages;
	}
	
	public void handleSelectEyebrows() {
		sendMidiButton.setDisable(false);
		disableLightsButton(true);
		copyNotes(Ruppet.EYEBROW_MIDI_NOTE);
	}
	
	private void disableLightsButton(final boolean disable) {
		lightsOnButton.setDisable(disable);
		lightsOffButton.setDisable(disable);
	}
	
	private void copyNotes(final byte... notes) {
		midiNotes = new byte[notes.length];
		System.arraycopy(notes, 0, midiNotes, 0, midiNotes.length);
	}
	
	public void handleSelectLeftLipCorners() {
		sendMidiButton.setDisable(false);
		disableLightsButton(true);
		copyNotes(Ruppet.LEFT_LIP_CORNER_MIDI_NOTE);
	}
	
	public void handleSelectRightLipCorners() {
		sendMidiButton.setDisable(false);
		disableLightsButton(true);
		copyNotes(Ruppet.RIGHT_LIP_CORNER_MIDI_NOTE);
	}
	
	public void handleSelectLowerJaw() {
		sendMidiButton.setDisable(false);
		disableLightsButton(true);
		copyNotes(Ruppet.LOWER_JAW_MIDI_NOTE);
	}
	
	public void handleSelectEyelids() {
		sendMidiButton.setDisable(false);
		disableLightsButton(true);
		copyNotes(Ruppet.EYELIDS_MIDI_NOTE);
	}
	
	public void handleSelectLights() {
		sendMidiButton.setDisable(false);
		disableLightsButton(false);
		copyNotes(Ruppet.LIGHTS_MIDI_NOTE);
	}
	
	public void handleSelectLipCorners() {
		sendMidiButton.setDisable(false);
		disableLightsButton(true);
		copyNotes(Ruppet.LEFT_LIP_CORNER_MIDI_NOTE, Ruppet.RIGHT_LIP_CORNER_MIDI_NOTE);
	}
	
	public void handleOn() {
		try {
			final var msg = new ShortMessage(ShortMessage.NOTE_ON, 0, Ruppet.LIGHTS_MIDI_NOTE, MAX_VELOCITY);
			usbMidiConnection.send(msg);
		}
		catch (final InvalidMidiDataException e) { throw new RuntimeException(e); }
	}
	
	public void handleOff() {
		try {
			final var msg = new ShortMessage(ShortMessage.NOTE_ON, 0, Ruppet.LIGHTS_MIDI_NOTE, MIN_VELOCITY);
			usbMidiConnection.send(msg);
		}
		catch (final InvalidMidiDataException e) { throw new RuntimeException(e); }
	}
}