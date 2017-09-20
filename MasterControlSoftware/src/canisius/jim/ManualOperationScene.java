package canisius.jim;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * This class provides a GUI for the ServoTest class to make the debugging process
 * more user friendly.
 * 
 * @author Jon Mrowczynski
 */

public class ManualOperationScene extends Application {
	
	/**
	 * The default font size that is used for the Ruppet Calibration Scene
	 */
	
	private final Font FONT_SIZE = Font.font(14);
	
	/**
	 * A collection of MIDI notes that should be sent to the USB MIDI receiver.
	 */
	
	private byte[] midiNotes = null;
	
	/**
	 * The current velocity value that the collection of MIDI notes should be associated with.
	 */
	
	private byte currentVelocity = 0;
	
	/**
	 * Launch the GUI, currently this is only the manual operation Scene, but it will be able 
	 * to be switched to eventually when the main Scene is created.
	 * 
	 * @param args The command line arguments
	 */
	
	public static void main(final String[] args) { launch(args); }

	@Override
	public void start(final Stage stage) {

		manualOperationSceneSetup(stage);
		
	} // end of start
	
	/**
	 * Setup the manual operation scene, eventually will be public
	 */
	
	private final void manualOperationSceneSetup(final Stage stage) {
		
		//Connect.EstablishUSBConnection();
		
		stage.setTitle("Ruppet Calibrator");
		
		final GridPane gridPane = new GridPane();
		gridPane.setAlignment(Pos.CENTER);
		gridPane.setHgap(20);
		gridPane.setVgap(10);
		gridPane.setPadding(new Insets(25, 25, 25, 25));
		
		try { partSelectSetup(gridPane); } 
		catch (InvalidMidiDataException e) { e.printStackTrace(); } // Should never happen
		
		servoNumSetup(gridPane);
		
		PICPinNumberSetup(gridPane);
		
		PICPinNameSetup(gridPane);
			
		MIDISelectSetup(gridPane, stage);
		
		// This will eventually be returned to the main GUI
		
		final Scene manualControlScene = new Scene(gridPane, 500, 350);
				
		stage.setScene(manualControlScene);
		stage.sizeToScene();
		stage.setResizable(false);
		stage.setOnCloseRequest( windowEvent -> { Connect.closeUSB(); } );
		stage.show();
		
	} // end of manualOperationSceneSetup
	
	/**
	 * Sets up the left most column of the GUI where the user is able to select which 
	 * Ruppet part they would like to manually operate.
	 * 
	 * @param grid The {@code GridPane} that contains and lays out all of the GUI elements
	 * @throws InvalidMidiDataException 
	 */
	
	private final void partSelectSetup(final GridPane grid) throws InvalidMidiDataException {
		
		final Text partSelectText = new Text("Part Select");
		partSelectText.setFont(FONT_SIZE);
		grid.add(partSelectText, 0, 0);
		GridPane.setHalignment(partSelectText, HPos.CENTER);
		
		final ShortMessage on = new ShortMessage(RuppetControl.NOTE_ON, RuppetControl.CHAN_1, RuppetControl.LIGHTS, 10);
		
		final Button lightsOn = new Button("On");	
		lightsOn.setVisible(false);
		lightsOn.setFont(FONT_SIZE);
		lightsOn.setOnAction( actionEvent -> { Connect.getUSBReceiver().send(on, RuppetControl.TIMESTAMP); } );
		grid.add(lightsOn, 0, 8);
		GridPane.setHalignment(lightsOn, HPos.LEFT);
		
		final ShortMessage off = new ShortMessage(RuppetControl.NOTE_ON, RuppetControl.CHAN_1, RuppetControl.LIGHTS, 0);
		
		final Button lightsOff = new Button("Off");
		lightsOff.setVisible(false);
		lightsOff.setFont(FONT_SIZE);
		lightsOff.setOnAction( actionEvent -> { Connect.getUSBReceiver().send(off, RuppetControl.TIMESTAMP); } );
		grid.add(lightsOff, 0, 9);
		GridPane.setHalignment(lightsOff, HPos.LEFT);
		
		/********************** For my purposes only ********************************/
		
		final FlashLights flashingLights = new FlashLights(on, off);
		
		final Button flashLights = new Button("Flash Lights");
		flashLights.setVisible(false);
		flashLights.setFont(FONT_SIZE);
		flashLights.setOnMouseClicked( mouseEvent -> {
			if (flashLights.getText().equals("Flash Lights")) {
				final Thread flashingLightsThread = new Thread(flashingLights);
				flashingLights.flash();
				flashingLightsThread.start();
				flashLights.setText("Stop");
			} else if (flashLights.getText().equals("Stop")) {
				flashingLights.halt();
				flashLights.setText("Flash Lights");
			}
			mouseEvent.consume();
		});
		grid.add(flashLights, 3, 9);
		GridPane.setHalignment(flashLights, HPos.CENTER);
		
		/*************************************************************************/
		
		final ToggleGroup partSelected = new ToggleGroup();
		
		final RadioButton servo1Select = new RadioButton();
		servo1Select.setText("Eyebrows");
		servo1Select.setFont(FONT_SIZE);
		servo1Select.setToggleGroup(partSelected);
		servo1Select.setOnAction( actionEvent -> {
			lightsOn.setVisible(false);
			lightsOff.setVisible(false);
			flashLights.setVisible(false);
			midiNotes = new byte[1];
			midiNotes[0] = RuppetControl.EYEBROW;
			actionEvent.consume();
		});
		grid.add(servo1Select, 0, 1);
		
		final RadioButton servo2Select = new RadioButton();
		servo2Select.setText("Left Lip Corner");
		servo2Select.setFont(FONT_SIZE);
		servo2Select.setToggleGroup(partSelected);
		servo2Select.setOnAction( actionEvent -> {
			lightsOn.setVisible(false);
			lightsOff.setVisible(false);
			flashLights.setVisible(false);
			midiNotes = new byte[1];
			midiNotes[0] = RuppetControl.LEFT_LIP_CORNER;
			actionEvent.consume();
		});
		grid.add(servo2Select, 0, 2);
		
		final RadioButton servo3Select = new RadioButton();
		servo3Select.setText("Right Lip Corner");
		servo3Select.setFont(FONT_SIZE);
		servo3Select.setToggleGroup(partSelected);
		servo3Select.setOnAction( actionEvent -> {
			lightsOn.setVisible(false);
			lightsOff.setVisible(false);
			flashLights.setVisible(false);
			midiNotes = new byte[1];
			midiNotes[0] = RuppetControl.RIGHT_LIP_CORNER;
			actionEvent.consume();
		});
		grid.add(servo3Select, 0, 3);
		
		final RadioButton servo4Select = new RadioButton();
		servo4Select.setText("Lower Jaw");
		servo4Select.setFont(FONT_SIZE);
		servo4Select.setToggleGroup(partSelected);
		servo4Select.setOnAction( actionEvent -> {
			lightsOn.setVisible(false);
			lightsOff.setVisible(false);
			flashLights.setVisible(false);
			midiNotes = new byte[1];
			midiNotes[0] = RuppetControl.LOWER_JAW;
			actionEvent.consume();
		});
		grid.add(servo4Select, 0, 4);
		
		final RadioButton servo5Select = new RadioButton();
		servo5Select.setText("Eyelids");
		servo5Select.setFont(FONT_SIZE);
		servo5Select.setToggleGroup(partSelected);
		servo5Select.setOnAction( actionEvent -> {
			lightsOn.setVisible(false);
			lightsOff.setVisible(false);
			flashLights.setVisible(false);
			midiNotes = new byte[1];
			midiNotes[0] = RuppetControl.EYELIDS;
			actionEvent.consume();
		});
		grid.add(servo5Select, 0, 5);
		
		final RadioButton lightsSelect = new RadioButton();
		lightsSelect.setText("Lights");
		lightsSelect.setFont(FONT_SIZE);
		lightsSelect.setToggleGroup(partSelected);
		lightsSelect.setOnAction( actionEvent -> {
			lightsOn.setVisible(true);
			lightsOff.setVisible(true);
			flashLights.setVisible(true);
			midiNotes = new byte[1];
			midiNotes[0] = RuppetControl.LIGHTS;
			actionEvent.consume();
		});
		grid.add(lightsSelect, 0, 6);
		
		final RadioButton dualServoSelect = new RadioButton();
		dualServoSelect.setText("Lip Corners");
		dualServoSelect.setFont(FONT_SIZE);
		dualServoSelect.setToggleGroup(partSelected);
		dualServoSelect.setOnAction( actionEvent -> {
			lightsOn.setVisible(false);
			lightsOff.setVisible(false);
			flashLights.setVisible(false);
			midiNotes = new byte[2];
			midiNotes[0] = RuppetControl.LEFT_LIP_CORNER;
			midiNotes[1] = RuppetControl.RIGHT_LIP_CORNER;
			actionEvent.consume();
		});
		grid.add(dualServoSelect, 0, 7);
		
	} // end of partSelectSetup
	
	/**
	 * Sets up and adds {@code Text} to the {@code Scene} that displays information about the 
	 * which servo number(s) is/are associated with which {@code Ruppet Part}(s).
	 * 
	 * @param grid The {@code GridPane} that the text is added to
	 */
	
	private final void servoNumSetup(final GridPane grid) {
		
		final Text servoText = new Text("Servo");
		servoText.setFont(FONT_SIZE);
		grid.add(servoText, 1, 0);
		GridPane.setHalignment(servoText, HPos.CENTER);
		
		final Text servo1Text = new Text("1");
		servo1Text.setFont(FONT_SIZE);
		grid.add(servo1Text, 1, 1);
		GridPane.setHalignment(servo1Text, HPos.CENTER);
		
		final Text servo2Text = new Text("2");
		servo2Text.setFont(FONT_SIZE);
		grid.add(servo2Text, 1, 2);
		GridPane.setHalignment(servo2Text, HPos.CENTER);
		
		final Text servo3Text = new Text("3");
		servo3Text.setFont(FONT_SIZE);
		grid.add(servo3Text, 1, 3);
		GridPane.setHalignment(servo3Text, HPos.CENTER);
		
		final Text servo4Text = new Text("4");
		servo4Text.setFont(FONT_SIZE);
		grid.add(servo4Text, 1, 4);
		GridPane.setHalignment(servo4Text, HPos.CENTER);
		
		final Text servo5Text = new Text("5");
		servo5Text.setFont(FONT_SIZE);
		grid.add(servo5Text, 1, 5);
		GridPane.setHalignment(servo5Text, HPos.CENTER);
		
		final Text lightsText = new Text("-");
		lightsText.setFont(FONT_SIZE);
		grid.add(lightsText, 1, 6);
		GridPane.setHalignment(lightsText, HPos.CENTER);
		
		final Text dualServosText = new Text("2 and 3");
		dualServosText.setFont(FONT_SIZE);
		grid.add(dualServosText, 1, 7);
		GridPane.setHalignment(dualServosText, HPos.CENTER);
		
	} // end of servoNumSetup
	
	/**
	 * Sets up the {@code Text} that associates the PIC pin number that outputs the 
	 * PWM signal with the servo that that signal gets sent to.
	 * 
	 * @param grid The {@code GridPane} that contains and lays out all of the GUI elements
	 */
	
	private final void PICPinNumberSetup(final GridPane grid) {
		
		final Text pinNumberText = new Text("PIC Pin Number(s)");
		pinNumberText.setFont(FONT_SIZE);
		grid.add(pinNumberText, 2, 0);
		GridPane.setHalignment(pinNumberText, HPos.CENTER);
		
		final Text outputPin1 = new Text("17");
		outputPin1.setFont(FONT_SIZE);
		grid.add(outputPin1, 2, 1);
		GridPane.setHalignment(outputPin1, HPos.CENTER);
		
		final Text outputPin2 = new Text("18");
		outputPin2.setFont(FONT_SIZE);
		grid.add(outputPin2, 2, 2);
		GridPane.setHalignment(outputPin2, HPos.CENTER);
		
		final Text outputPin3 = new Text("1");
		outputPin3.setFont(FONT_SIZE);
		grid.add(outputPin3, 2, 3);
		GridPane.setHalignment(outputPin3, HPos.CENTER);
		
		final Text outputPin4 = new Text("2");
		outputPin4.setFont(FONT_SIZE);
		grid.add(outputPin4, 2, 4);
		GridPane.setHalignment(outputPin4, HPos.CENTER);
		
		final Text outputPin5 = new Text("6");
		outputPin5.setFont(FONT_SIZE);
		grid.add(outputPin5, 2, 5);
		GridPane.setHalignment(outputPin5, HPos.CENTER);
		
		final Text outputPin6 = new Text("11");
		outputPin6.setFont(FONT_SIZE);
		grid.add(outputPin6, 2, 6);
		GridPane.setHalignment(outputPin6, HPos.CENTER);
		
		final Text outputPins = new Text("18 and 1");
		outputPins.setFont(FONT_SIZE);
		grid.add(outputPins, 2, 7);
		GridPane.setHalignment(outputPins, HPos.CENTER);
		
	} // end of PICPinNumberSetup
	
	/**
	 * Sets up the {@code Text} that associates all of the PIC pin numbers with their 
	 * names.
	 * 
	 * @param grid The {@code GridPane} that contains and lays out all of the GUi elements
	 */
	
	private final void PICPinNameSetup(final GridPane grid) {
		
		final Text pinNameText = new Text("PIC Pin Name(s)");
		pinNameText.setFont(FONT_SIZE);
		grid.add(pinNameText, 3, 0);
		GridPane.setHalignment(pinNameText, HPos.CENTER);
		
		final Text outputPin1 = new Text("RA0");
		outputPin1.setFont(FONT_SIZE);
		grid.add(outputPin1, 3, 1);
		GridPane.setHalignment(outputPin1, HPos.CENTER);
		
		final Text outputPin2 = new Text("RA1");
		outputPin2.setFont(FONT_SIZE);
		grid.add(outputPin2, 3, 2);
		GridPane.setHalignment(outputPin2, HPos.CENTER);
		
		final Text outputPin3 = new Text("RA2");
		outputPin3.setFont(FONT_SIZE);
		grid.add(outputPin3, 3, 3);
		GridPane.setHalignment(outputPin3, HPos.CENTER);
		
		final Text outputPin4 = new Text("RA3");
		outputPin4.setFont(FONT_SIZE);
		grid.add(outputPin4, 3, 4);
		GridPane.setHalignment(outputPin4, HPos.CENTER);
		
		final Text outputPin5 = new Text("RB0");
		outputPin5.setFont(FONT_SIZE);
		grid.add(outputPin5, 3, 5);
		GridPane.setHalignment(outputPin5, HPos.CENTER);
		
		final Text outputPin6 = new Text("RB5");
		outputPin6.setFont(FONT_SIZE);
		grid.add(outputPin6, 3, 6);
		GridPane.setHalignment(outputPin6, HPos.CENTER);
		
		final Text outputPins = new Text("RA1 and RA2");
		outputPins.setFont(FONT_SIZE);
		grid.add(outputPins, 3, 7);
		GridPane.setHalignment(outputPins, HPos.CENTER);
		
	} // end of PICPinNameSetup
	
	/**
	 * Sets up the GUI elements that accept input from that user which constructs the 
	 * MIDI messages that will be eventually sent to the PIC microcontroller. Specifically 
	 * a {@code Spinner} is used for the user to enter in velocity values and a {@code Button}
	 * that allows the user to send the MIDI messages to the PIC.
	 * 
	 * @param grid The {@code GridPane} that contains and lays out all of the GUI elements
	 * @param stage The primary {@code Stage} that contains all of the GUI elements
	 */
	
	private final void MIDISelectSetup(final GridPane grid, final Stage stage) {
		
		final Button sendMIDIBtn = new Button("Send MIDI");
		sendMIDIBtn.setOnAction( actionListener -> {	
			if (midiNotes == null) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.initOwner(stage);
				alert.setTitle("Invalid Part Selection");
				alert.setHeaderText("No Ruppet Part has been Selected");
				alert.setContentText("Please select a Ruppet Part to operate");
				alert.showAndWait();
				return;
			}
			try { 
				final ShortMessage[] messages = makeMessages(); 
				for (byte i = 0; i < messages.length; ++i)
					Connect.getUSBReceiver().send(messages[i], RuppetControl.TIMESTAMP);
			} 
			catch (InvalidMidiDataException e) { e.printStackTrace(); }
			actionListener.consume();
		});
		grid.add(sendMIDIBtn, 0, 9);
		GridPane.setHalignment(sendMIDIBtn, HPos.RIGHT);
		
		final Spinner<Integer> spinner = new Spinner<>(
			RuppetControl.MIN_VELOCITY, 
			RuppetControl.MAX_VELOCITY, 
			RuppetControl.MIN_VELOCITY
		);
		spinner.setEditable(true);
		spinner.setPrefWidth(75);
		
		// Allow the user to increment or decrement the value in the Spinner's Editor, (the TextField)
		// by using the up and down arrows respectively.
		
		spinner.getEditor().setOnKeyPressed( keyEvent -> {
			if (keyEvent.getCode().equals(KeyCode.ENTER)) {
				sendMIDIBtn.fire();
				return;
			}						
			if (keyEvent.getCode().equals(KeyCode.UP)) 
				spinner.getValueFactory().increment(1);
			else if (keyEvent.getCode().equals(KeyCode.DOWN)) 
				spinner.getValueFactory().decrement(1);
		});
		
		// Make sure that if there is nothing in the Spinner Editor (the TextField) that a 
		// NullPointerException is not thrown when the user presses enter.
		
		spinner.getEditor().setOnAction( actionEvent -> {
			final TextField editor = spinner.getEditor();
			final SpinnerValueFactory<Integer> factory = spinner.getValueFactory();
			final StringConverter<Integer> converter = factory.getConverter();
			final Integer value = converter.fromString(editor.getText());
			if (value == null)
				editor.setText(converter.toString(factory.getValue()));
			actionEvent.consume();
		});
		
		// Check to make sure that the user is entering in a valid value, specifically a value
		// between the minimum velocity value and the maximum velocity value inclusive. 
		
		spinner.getEditor().textProperty().addListener( (observable, oldValue, newValue) -> {
			if (!newValue.equals("")) {
				final boolean isNotNumber = !newValue.matches("\\d*");
				final boolean isNotValidNumber = (newValue.charAt(0) == '0' && newValue.length() > 1);
				if (isNotNumber || isNotValidNumber) {
					spinner.getEditor().setText(oldValue);
					return;
				}
				final int newValueInt = Integer.parseInt(newValue);
				if (newValueInt > RuppetControl.MAX_VELOCITY || newValueInt < RuppetControl.MIN_VELOCITY)
					spinner.getEditor().setText(oldValue);
				currentVelocity = (byte) newValueInt;
			}				
		});
			
		final Tooltip tooltip = new Tooltip(
			"Min velocity: " + RuppetControl.MIN_VELOCITY + "\n" +
			"Max velocity: " + RuppetControl.MAX_VELOCITY
		);
		
		spinner.setTooltip(tooltip);
		spinner.getEditor().setTooltip(tooltip);
		
		grid.add(spinner, 0, 8);	
		GridPane.setHalignment(spinner, HPos.RIGHT);
		
	} // end of MIDISelectSetup
	
	/**
	 * Creates MIDI messages based on the MIDI notes stored in the {@code midiNotes} array 
	 * and the {@code currentVelocity} value.
	 * 
	 * @return An array of {@code ShortMessages} which are all of the messages that should be sent to the PIC.
	 * @throws InvalidMidiDataException Thrown if any of the MIDI data is invalid
	 */
	
	private final ShortMessage[] makeMessages() throws InvalidMidiDataException {
				
		final ShortMessage[] messages = new ShortMessage[midiNotes.length];
		
		if (midiNotes.length >= 1) 
			messages[0] = new ShortMessage(RuppetControl.NOTE_ON, RuppetControl.CHAN_1, midiNotes[0], currentVelocity);
		
		if (midiNotes.length == 2) 
			messages[1] = new ShortMessage(RuppetControl.NOTE_ON, RuppetControl.CHAN_1, midiNotes[1], RuppetControl.MAX_VELOCITY - currentVelocity);
		
		return messages;
		
	} // end of makeMessages
	
	/*************************** For my purposes only *********************************************/
	
	private class FlashLights implements Runnable {
		
		private volatile boolean isFlashing = true;
		
		private ShortMessage on = null;
		private ShortMessage off = null;
		
		public FlashLights(ShortMessage on, ShortMessage off) {
			this.on = on;
			this.off = off;
		}
		
		@Override
		public void run() {
			while (isFlashing) {
				try {
					Connect.getUSBReceiver().send(on, RuppetControl.TIMESTAMP);
					Thread.sleep(100);
					Connect.getUSBReceiver().send(off, RuppetControl.TIMESTAMP);
					Thread.sleep(100);
				} catch (InterruptedException e) { }
			}
		}
		
		public final void flash() { isFlashing = true; }
		
		public final void halt() { isFlashing = false; }
	
	}
	
	/*********************************************************************************/
	
} // end of class ManualOperationScene



