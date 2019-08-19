package canisius.jim;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import java.util.Arrays;

/**
 * This class provides a GUI with {@code Control}s that allow for one to easily test and debug any functionalities
 * related to either the electronics, firmware, or software.
 * 
 * @author Jon Mrowczynski
 */

public class RuppetTester extends Application {
	
	/**
	 * The default font size that is used for the Ruppet Tester {@code Scene}.
	 */
	
	private static final Font FONT_SIZE = Font.font(14);

	/**
	 * Contains all of the {@code Text} and {@code Control}s.
	 */

	private final GridPane gridPane = new GridPane();

	/**
	 * The {@code ToggleGroup} for the {@code RadioButton}s.
	 */

	private final ToggleGroup radioButtonToggleGroup = new ToggleGroup();

	/**
	 * Turns the lights on when pressed.
	 */

	private final Button lightsOn = setupButton("On", 8, 10);

	/**
	 * Turns the lights off when pressed.
	 */

	private final Button lightsOff = setupButton("Off", 9, 0);
	
	/**
	 * A collection of MIDI notes that should be sent to the USB MIDI {@code Receiver}.
	 */
	
	private byte[] midiNotes = null;

	/**
	 * The velocity value that should be used when the {@code ShortMessage}s are sent to the USB {@code MidiDevice}'s
	 * {@code Receiver}.
	 */
	
	private byte currentVelocity = 0;
	
	public static void main(final String[] args) { launch(args); }

	@Override
	public void start(final Stage stage) { manualOperationSceneSetup(stage); }
	
	/**
	 * Construct the manual operation scene.
	 */
	
	private void manualOperationSceneSetup(final Stage stage) {
		
		//Connect.EstablishUSBConnection();
		
		stage.setTitle("Ruppet Tester");
		
		gridPane.setAlignment(Pos.CENTER);
		gridPane.setHgap(20);
		gridPane.setVgap(10);
		gridPane.setPadding(new Insets(25, 25, 25, 25));
		
		partSelectSetup();
		servoNumSetup();
		PICPinNumberSetup();
		PICPinNameSetup();
		MIDISelectSetup(stage);

		final Scene manualControlScene = new Scene(gridPane, 500, 350);
				
		stage.setScene(manualControlScene);
		stage.sizeToScene();
		stage.setResizable(false);
		stage.setOnCloseRequest(windowEvent -> MidiConnection.closeConnection());
		stage.show();
		
	} // end of manualOperationSceneSetup
	
	/**
	 * Constructs the left most column of the GUI where the user is able to select which {@code Ruppet} part they would
	 * like to manually operate.
	 */
	
	private void partSelectSetup() {
		setupText("Part Select", 0, 0);
		setupRadioButton(false, "Eyebrows", 1, RuppetControl.EYEBROW);
		setupRadioButton(false, "Left Lip Corner", 2, RuppetControl.LEFT_LIP_CORNER);
		setupRadioButton(false, "Right Lip Corner", 3, RuppetControl.RIGHT_LIP_CORNER);
		setupRadioButton(false, "Lower Jaw", 4, RuppetControl.LOWER_JAW);
		setupRadioButton(false, "EyeLids", 5, RuppetControl.EYELIDS);
		setupRadioButton(true, "Lights", 6, RuppetControl.LIGHTS);
		setupRadioButton(false, "Lip Corners", 7, RuppetControl.LEFT_LIP_CORNER, RuppetControl.RIGHT_LIP_CORNER);
	} // end of partSelectSetup

	/**
	 * Constructs and returns a {@code Button} that can control some aspect of the {@code Ruppet}.
	 *
	 * @param label that the {@code Button} should have.
	 * @param rowIndex that the {@code Button} should have in {@code gridPane}.
	 * @param velocity of the {@code ShortMessage} that is sent when the {@code Button} is pressed.
	 * @return the constructed {@code Button}.
	 */

	private Button setupButton(final String label, final int rowIndex, final int velocity) {
		final Button button = new Button(label);
		button.setVisible(false);
		button.setFont(FONT_SIZE);
		gridPane.add(button, 0, rowIndex);
		GridPane.setHalignment(button, HPos.LEFT);
		try {
			final ShortMessage msg = new ShortMessage(ShortMessage.NOTE_ON, RuppetControl.CHAN_1, RuppetControl.LIGHTS, velocity);
			button.setOnAction(actionEvent -> MidiConnection.getUsbReceiver().send(msg, -1));
		} catch (InvalidMidiDataException e) { e.printStackTrace(); }
		return button;
	}

	/**
	 * Constructs a {@code RadioButton} that can control some aspect of the {@code Ruppet}.
	 *
	 * @param setLightsVisible a {@code boolean} indicating whether the lights on/off {@code Control}s should be visible
	 *                         upon the selection of the constructed {@code RadioButton}.
	 * @param label that the {@code RadioButton} should have.
	 * @param rowIndex that the {@code Button} should have in {@code gridPane}
	 * @param notes that should be sent when the {@code Button} is pressed.
	 */

	private void setupRadioButton(final boolean setLightsVisible, final String label, final int rowIndex, final byte... notes) {
		final RadioButton radioButton = new RadioButton();
		radioButton.setText(label);
		radioButton.setFont(FONT_SIZE);
		radioButton.setToggleGroup(radioButtonToggleGroup);
		radioButton.setOnAction(actionEvent -> {
			lightsOn.setVisible(setLightsVisible);
			lightsOff.setVisible(setLightsVisible);
			midiNotes = new byte[notes.length];
			System.arraycopy(notes, 0, midiNotes, 0, notes.length);
		});
		gridPane.add(radioButton, 0, rowIndex);
	}
	
	/**
	 * Constructs and adds {@code Text} to the {@code Scene} that displays information about the which servo number(s)
	 * is/are associated with which {@code Ruppet Part}(s).
	 */
	
	private void servoNumSetup() {
		setupText("Servo", 1, 0);
		setupText("1", 1, 1);
		setupText("2", 1, 2);
		setupText("3", 1, 3);
		setupText("4", 1, 4);
		setupText("5", 1, 5);
		setupText("-", 1, 6);
		setupText("2 and 3", 1, 7);
	}
	
	/**
	 * Constructs the {@code Text} that associates the PIC pin number that outputs the PWM signal with the servo that
	 * that signal gets sent to.
	 */
	
	private void PICPinNumberSetup() {
		setupText("PIC Pin Number(s)", 2, 0);
		setupText("17", 2, 1);
		setupText("18", 2, 2);
		setupText("1", 2, 3);
		setupText("2", 2, 4);
		setupText("6", 2, 5);
		setupText("11", 2, 6);
		setupText("18 and 1", 2, 7);
	}
	
	/**
	 * Constructs the {@code Text} that associates all of the PIC pin numbers with their names.
	 */
	
	private void PICPinNameSetup() {
		setupText("PIC Pin Name(s)", 3, 0);
		setupText("RA0", 3, 1);
		setupText("RA1", 3, 2);
		setupText("RA2", 3, 3);
		setupText("RA3", 3, 4);
		setupText("RB0", 3, 5);
		setupText("RB5", 3, 6);
		setupText("RA1 and RA2", 3, 7);
	}

	/**
	 * A helper method used to add a {@code String} to the given {@code gridPane} as {@code Text} at the specified
	 * {@code column} and {@code row}.
	 *
	 * @param label of the {@code Text}.
	 * @param column that the {@code Text} should be placed in.
	 * @param row that the {@code Text} should be placed in.
	 */
	
	private void setupText(final String label, final int column, final int row) {
		final Text text = new Text(label);
		text.setFont(FONT_SIZE);
		gridPane.add(text, column, row);
		GridPane.setHalignment(text, HPos.CENTER);
	}
	
	/**
	 * Constructs a {@code Spinner} that allows the user to enter in a velocity value for the {@code MidiMessage}s that
	 * will be sent to the electronics and a {@code Button} that allows the user to send the {@code MidiMessage} to the
	 * electronics.
	 * 
	 * @param stage The primary {@code Stage} that contains all of the {@code Text} and {@code Control}s.
	 */
	
	private void MIDISelectSetup(final Stage stage) {
		
		final Button sendMIDIBtn = new Button("Send MIDI");
		sendMIDIBtn.setOnAction(actionListener -> {	
			if (midiNotes == null) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.initOwner(stage);
				alert.setTitle("Invalid Part Selection");
				alert.setHeaderText("No Ruppet Part has been Selected");
				alert.setContentText("Please select a Ruppet Part to operate");
				alert.showAndWait();
				return;
			}
			try { Arrays.stream(makeMessages()).forEach(msg -> MidiConnection.getUsbReceiver().send(msg, -1)); }
			catch (InvalidMidiDataException e) { e.printStackTrace(); }
			actionListener.consume();
		});
		gridPane.add(sendMIDIBtn, 0, 9);
		GridPane.setHalignment(sendMIDIBtn, HPos.RIGHT);
		
		final Spinner<Integer> spinner = new Spinner<>(RuppetControl.MIN_VELOCITY, RuppetControl.MAX_VELOCITY, RuppetControl.MIN_VELOCITY);
		spinner.setEditable(true);
		spinner.setPrefWidth(75);
		
		/*
		 * Allow the user to increment or decrement the value in the Spinner's Editor, (the TextField) by using the up
		 * and down arrows respectively.
		 */
		
		spinner.getEditor().setOnKeyPressed(keyEvent -> {
			if (keyEvent.getCode().equals(KeyCode.ENTER)) {
				sendMIDIBtn.fire();
				return;
			}						
			if (keyEvent.getCode().equals(KeyCode.UP)) { spinner.getValueFactory().increment(1); }
			else if (keyEvent.getCode().equals(KeyCode.DOWN)) { spinner.getValueFactory().decrement(1); }
		});
		
		/*
		 * Make sure that if there is nothing in the Spinner Editor (the TextField) that a NullPointerException is not
		 * thrown when the user presses enter.
		 */

		spinner.getEditor().setOnAction( actionEvent -> {
			final TextField editor = spinner.getEditor();
			final SpinnerValueFactory<Integer> factory = spinner.getValueFactory();
			final StringConverter<Integer> converter = factory.getConverter();
			final Integer value = converter.fromString(editor.getText());
			if (value == null) { editor.setText(converter.toString(factory.getValue())); }
			actionEvent.consume();
		});
		
		/*
		 * Check to make sure that the user is entering in a valid value, specifically a value between the minimum
		 * velocity value and the maximum velocity value inclusive.
		 */

		spinner.getEditor().textProperty().addListener( (observable, oldValue, newValue) -> {
			if (!newValue.equals("")) {
				final boolean isNotNumber = !newValue.matches("\\d*");
				final boolean isNotValidNumber = (newValue.charAt(0) == '0' && newValue.length() > 1);
				if (isNotNumber || isNotValidNumber) {
					spinner.getEditor().setText(oldValue);
					return;
				}
				final int newValueInt = Integer.parseInt(newValue);
				if (newValueInt > RuppetControl.MAX_VELOCITY || newValueInt < RuppetControl.MIN_VELOCITY) { spinner.getEditor().setText(oldValue); }
				currentVelocity = (byte) newValueInt;
			}				
		});
			
		final Tooltip tooltip = new Tooltip("Min velocity: " + RuppetControl.MIN_VELOCITY + "\n" + "Max velocity: " + RuppetControl.MAX_VELOCITY);
		
		spinner.setTooltip(tooltip);
		spinner.getEditor().setTooltip(tooltip);
		
		gridPane.add(spinner, 0, 8);
		GridPane.setHalignment(spinner, HPos.RIGHT);
		
	} // end of MIDISelectSetup
	
	/**
	 * Creates {@code ShortMessage}s based on the MIDI notes stored in the {@code midiNotes} array and the
	 * {@code currentVelocity} value.
	 * 
	 * @return An array of {@code ShortMessage}s which are all of the messages that should be sent to the PIC.
	 * @throws InvalidMidiDataException if any of the MIDI data is invalid.
	 */
	
	private ShortMessage[] makeMessages() throws InvalidMidiDataException {
		final ShortMessage[] messages = new ShortMessage[midiNotes.length];
		if (midiNotes.length >= 1) { messages[0] = new ShortMessage(ShortMessage.NOTE_ON, RuppetControl.CHAN_1, midiNotes[0], currentVelocity); }
		if (midiNotes.length == 2) { messages[1] = new ShortMessage(ShortMessage.NOTE_ON, RuppetControl.CHAN_1, midiNotes[1], RuppetControl.MAX_VELOCITY - currentVelocity); }
		return messages;
	}
	
} // end of class ManualOperationScene



