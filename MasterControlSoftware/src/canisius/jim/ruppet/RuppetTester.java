package canisius.jim.ruppet;

import canisius.jim.connections.UsbMidiConnection;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides a GUI with {@code Control}s that assists with the testing and debugging of any functionalities
 * related to either the electronics, firmware, or software of the {@code Ruppet}.
 *
 * @author Jon Mrowczynski
 */

public class RuppetTester extends Application {
	
	/**
	 * The default font size that is used for the {@code RuppetTester} {@code Scene}.
	 */
	
	private static final Font FONT_SIZE = Font.font(14);

	/**
	 * Contains all of the {@code Text} and {@code Control}s related to the {@code Ruppet}'s {@code Part}s.
	 */

	private final GridPane partSelectGridPane = new GridPane();

	/**
	 * Contains all of the {@code Control}s that allows the user to modify the velocity value of the
	 * {@code ShortMessage}s and send {@code ShortMessage}s to the microcontroller.
	 */

	private final GridPane controlsGridPane = new GridPane();

	/**
	 * The {@code ToggleGroup} for the {@code RadioButton}s.
	 */

	private final ToggleGroup radioButtonToggleGroup = new ToggleGroup();

	/**
	 * Turns the lights on when pressed.
	 */

	private final Button lightsOn = setupButton("On", 0, 10);

	/**
	 * Turns the lights off when pressed.
	 */

	private final Button lightsOff = setupButton("Off", 1, 0);
	
	/**
	 * A collection of MIDI notes that should be sent to the USB {@code MidiDevice}'s {@code Receiver}.
	 */
	
	private byte[] midiNotes = null;

	/**
	 * The velocity value that should be used when the {@code ShortMessage}s are sent to the USB {@code MidiDevice}'s
	 * {@code Receiver}.
	 */
	
	private byte currentVelocity = 0;
	
	public static void main(final String[] args) { launch(args); }

	@Override
	public void start(final Stage primaryStage) { manualOperationSceneSetup(primaryStage); }
	
	/**
	 * Construct the manual operation {@code Scene} add it to the primary {@code Stage} and show the {@code Stage}.
	 */
	
	private void manualOperationSceneSetup(final Stage primaryStage) {
		primaryStage.setTitle("Ruppet Tester");
		partSelectGridPane.setAlignment(Pos.CENTER);
		partSelectGridPane.setHgap(20);
		partSelectGridPane.setVgap(10);
		controlsGridPane.setAlignment(Pos.CENTER);
		controlsGridPane.setHgap(20);
		controlsGridPane.setVgap(10);
		partSelectSetup();
		servoNumSetup();
		PICPinNumberSetup();
		PICPinNameSetup();
		MIDISelectSetup(primaryStage);
		final VBox container = new VBox(partSelectGridPane, controlsGridPane);
		container.setSpacing(20);
		container.setAlignment(Pos.CENTER);
		container.setPadding(new Insets(25));
		final Scene manualControlScene = new Scene(container, 500, 350);
		primaryStage.setScene(manualControlScene);
		primaryStage.sizeToScene();
		primaryStage.setResizable(false);
		primaryStage.setOnCloseRequest(windowEvent -> UsbMidiConnection.getInstance().disconnect());
		primaryStage.show();
	} // end of manualOperationSceneSetup
	
	/**
	 * Constructs the left most column of the GUI where the user is able to select which {@code Ruppet Part} they would
	 * like to manually operate.
	 */
	
	private void partSelectSetup() {
		setupText("Part Select", 0, 0);
		setupRadioButton(true, "Eyebrows", 1, Ruppet.EYEBROW_MIDI_NOTE);
		setupRadioButton(true, "Left Lip Corner", 2, Ruppet.LEFT_LIP_CORNER_MIDI_NOTE);
		setupRadioButton(true, "Right Lip Corner", 3, Ruppet.RIGHT_LIP_CORNER_MIDI_NOTE);
		setupRadioButton(true, "Lower Jaw", 4, Ruppet.LOWER_JAW_MIDI_NOTE);
		setupRadioButton(true, "EyeLids", 5, Ruppet.EYELIDS_MIDI_NOTE);
		setupRadioButton(false, "Lights", 6, Ruppet.LIGHTS_MIDI_NOTE);
		setupRadioButton(true, "Lip Corners", 7, Ruppet.LEFT_LIP_CORNER_MIDI_NOTE, Ruppet.RIGHT_LIP_CORNER_MIDI_NOTE);
	} // end of partSelectSetup

	/**
	 * Returns a {@code Button} that can control a component of the {@code Ruppet}.
	 *
	 * @param label that the {@code Button} should have.
	 * @param rowIndex that the {@code Button} should have in {@code controlsGridPane}.
	 * @param velocity of the {@code ShortMessage} that is sent when the {@code Button} is pressed.
	 * @return the constructed {@code Button}.
	 */

	private Button setupButton(final String label, final int rowIndex, final int velocity) {
		final Button button = new Button(label);
		button.setDisable(true);
		button.setFont(FONT_SIZE);
		controlsGridPane.add(button, 0, rowIndex);
		GridPane.setHalignment(button, HPos.LEFT);
		try {
			final ShortMessage msg = new ShortMessage(ShortMessage.NOTE_ON, 0, Ruppet.LIGHTS_MIDI_NOTE, velocity);
			button.setOnAction(actionEvent -> UsbMidiConnection.getInstance().send(msg));
		} catch (InvalidMidiDataException e) { e.printStackTrace(); }
		return button;
	}

	/**
	 * Constructs a {@code RadioButton} that can control a component of the {@code Ruppet} and adds it to the
	 * {@code partsSelectGridPane}.
	 *
	 * @param setLightsDisabled a {@code boolean} indicating whether the lights on/off {@code Button}s should be
	 *                          disabled upon the selection of the {@code RadioButton}.
	 * @param label that the {@code RadioButton} should have.
	 * @param rowIndex that the {@code RadioButton} should have in {@code partSelectGridPane}.
	 * @param midiNotes that should be sent when the {@code RadioButton} is pressed.
	 */

	private void setupRadioButton(final boolean setLightsDisabled, final String label, final int rowIndex, final byte... midiNotes) {
		final RadioButton radioButton = new RadioButton();
		radioButton.setText(label);
		radioButton.setFont(FONT_SIZE);
		radioButton.setToggleGroup(radioButtonToggleGroup);
		radioButton.setOnAction(actionEvent -> {
			lightsOn.setDisable(setLightsDisabled);
			lightsOff.setDisable(setLightsDisabled);
			this.midiNotes = new byte[midiNotes.length];
			System.arraycopy(midiNotes, 0, this.midiNotes, 0, midiNotes.length);
		});
		partSelectGridPane.add(radioButton, 0, rowIndex);
	}
	
	/**
	 * Constructs and adds {@code Text} to the {@code Scene} that displays information about which servo numbers are
	 * associated with each {@code Ruppet Part}.
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
	 * A helper method used to add a {@code String} to {@code partSelectGridPane} as {@code Text} at the specified
	 * {@code column} and {@code row}.
	 *
	 * @param label of the {@code Text}.
	 * @param column that the {@code Text} should be placed in.
	 * @param row that the {@code Text} should be placed in.
	 */
	
	private void setupText(final String label, final int column, final int row) {
		final Text text = new Text(label);
		text.setFont(FONT_SIZE);
		partSelectGridPane.add(text, column, row);
		GridPane.setHalignment(text, HPos.CENTER);
	}
	
	/**
	 * Constructs a {@code Spinner} that allows the user to enter in a velocity value for the {@code ShortMessage}s and
	 * a {@code Button} that allows the user to send the {@code ShortMessage}s to the electronics.
	 * 
	 * @param primaryStage The primary {@code Stage} that contains all of the {@code Text} and {@code Control}s.
	 */
	
	private void MIDISelectSetup(final Stage primaryStage) {
		final Button sendMIDIBtn = new Button("Send MIDI");
		sendMIDIBtn.setOnAction(actionListener -> {	
			if (midiNotes == null) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.initOwner(primaryStage);
				alert.setTitle("Invalid Part Selection");
				alert.setHeaderText("No Ruppet Part has been Selected");
				alert.setContentText("Please select a Ruppet Part to operate");
				alert.showAndWait();
			} else {
				try { makeMessages().forEach(msg -> UsbMidiConnection.getInstance().send(msg)); }
				catch (InvalidMidiDataException e) { e.printStackTrace(); }
				actionListener.consume();
			}
		});
		controlsGridPane.add(sendMIDIBtn, 1, 1);

		final Spinner<Integer> spinner = new Spinner<>(Ruppet.MIN_VELOCITY, Ruppet.MAX_VELOCITY, Ruppet.MIN_VELOCITY);
		spinner.setEditable(true);
		spinner.setPrefWidth(55);
		/*
		 * If there is nothing in the Spinner's Editor, prevent a NullPointerException from being thrown if enter is
		 * pressed.
		 */
		spinner.getEditor().setOnAction(actionEvent -> { if (spinner.getEditor().getText().equals("")) { spinner.cancelEdit(); } });
		/*
		 * Allow the user to increment or decrement the value in the Spinner's Editor, (the TextField) by using the up
		 * and down arrows respectively.
		 */
		spinner.getEditor().setOnKeyPressed(keyEvent -> {
			switch (keyEvent.getCode()) {
				case ENTER: sendMIDIBtn.fire(); 							break;
				case UP: 	spinner.getValueFactory().increment(1); 	break;
				case DOWN: 	spinner.getValueFactory().decrement(1); 	break;
			}
		});
		/*
		 * Check to make sure that the user is entering in a valid value, specifically a value between the minimum
		 * velocity value and the maximum velocity value inclusive.
		 */
		spinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.equals("")) {
				final boolean isNotNumber = !newValue.matches("\\d*");
				final boolean isNotValidNumber = newValue.charAt(0) == '0' && newValue.length() > 1;
				if (isNotNumber || isNotValidNumber) { spinner.getEditor().setText(oldValue); }
				else {
					final int newValueInt = Integer.parseInt(newValue);
					if (newValueInt > Ruppet.MAX_VELOCITY || newValueInt < Ruppet.MIN_VELOCITY) { spinner.getEditor().setText(oldValue); }
					currentVelocity = (byte) newValueInt;
				}
			}				
		});
		final Tooltip tooltip = new Tooltip("Min velocity: " + Ruppet.MIN_VELOCITY + "\n" + "Max velocity: " + Ruppet.MAX_VELOCITY);
		spinner.setTooltip(tooltip);

		final Text velocityText = new Text("Velocity:");
		velocityText.setFont(FONT_SIZE);
		final HBox spinnerContainer = new HBox(velocityText, spinner);
		spinnerContainer.setSpacing(10);
		spinnerContainer.setAlignment(Pos.CENTER);

		controlsGridPane.add(spinnerContainer, 1, 0);
	} // end of MIDISelectSetup
	
	/**
	 * Creates a {@code Set} of {@code ShortMessage}s based on the {@code byte}s stored in {@code midiNotes} and the
	 * {@code currentVelocity} value.
	 * 
	 * @return A {@code Set} of {@code ShortMessage}s that should be sent to the microcontroller.
	 * @throws InvalidMidiDataException if any of the MIDI data is invalid.
	 */
	
	private Set<ShortMessage> makeMessages() throws InvalidMidiDataException {
		final Set<ShortMessage> messages = new HashSet<>();
		if (midiNotes.length >= 1) { messages.add(new ShortMessage(ShortMessage.NOTE_ON, 0, midiNotes[0], currentVelocity)); }
		if (midiNotes.length == 2) { messages.add(new ShortMessage(ShortMessage.NOTE_ON, 0, midiNotes[1], Ruppet.MAX_VELOCITY - currentVelocity)); }
		return messages;
	}
	
} // end of RuppetTester



