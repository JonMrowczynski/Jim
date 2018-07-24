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
	public void start(final Stage stage) { manualOperationSceneSetup(stage); }
	
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
		stage.setOnCloseRequest( windowEvent -> { MidiConnection.closeUsbMidiDevice(); } );
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
		
		final ShortMessage on = new ShortMessage(ShortMessage.NOTE_ON, RuppetControl.CHAN_1, RuppetControl.LIGHTS, 10);
		
		final Button lightsOn = new Button("On");	
		lightsOn.setVisible(false);
		lightsOn.setFont(FONT_SIZE);
		lightsOn.setOnAction( actionEvent -> { MidiConnection.getUsbReceiver().send(on, -1); } );
		grid.add(lightsOn, 0, 8);
		GridPane.setHalignment(lightsOn, HPos.LEFT);
		
		final ShortMessage off = new ShortMessage(ShortMessage.NOTE_ON, RuppetControl.CHAN_1, RuppetControl.LIGHTS, 0);
		
		final Button lightsOff = new Button("Off");
		lightsOff.setVisible(false);
		lightsOff.setFont(FONT_SIZE);
		lightsOff.setOnAction( actionEvent -> { MidiConnection.getUsbReceiver().send(off, -1); } );
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
		setupText(grid, "Servo", 1, 0);
		setupText(grid, "1", 1, 1);
		setupText(grid, "2", 1, 2);
		setupText(grid, "3", 1, 3);
		setupText(grid, "4", 1, 4);
		setupText(grid, "5", 1, 5);
		setupText(grid, "-", 1, 6);
		setupText(grid, "2 and 3", 1, 7);
	}
	
	/**
	 * Sets up the {@code Text} that associates the PIC pin number that outputs the 
	 * PWM signal with the servo that that signal gets sent to.
	 * 
	 * @param grid The {@code GridPane} that contains and lays out all of the GUI elements
	 */
	
	private final void PICPinNumberSetup(final GridPane grid) {
		setupText(grid, "PIC Pin Number(s)", 2, 0);
		setupText(grid, "17", 2, 1);
		setupText(grid, "18", 2, 2);
		setupText(grid, "1", 2, 3);
		setupText(grid, "2", 2, 4);
		setupText(grid, "6", 2, 5);
		setupText(grid, "11", 2, 6);
		setupText(grid, "18 and 1", 2, 7);
	}
	
	/**
	 * Sets up the {@code Text} that associates all of the PIC pin numbers with their 
	 * names.
	 * 
	 * @param grid The {@code GridPane} that contains and lays out all of the GUi elements
	 */
	
	private final void PICPinNameSetup(final GridPane grid) {
		setupText(grid, "PIC Pin Name(s)", 3, 0);
		setupText(grid, "RA0", 3, 1);
		setupText(grid, "RA1", 3, 2);
		setupText(grid, "RA2", 3, 3);
		setupText(grid, "RA3", 3, 4);
		setupText(grid, "RB0", 3, 5);
		setupText(grid, "RB5", 3, 6);
		setupText(grid, "RA1 and RA2", 3, 7);
	}
	
	private final void setupText(final GridPane gridPane, final String label, final int column, final int row) {
		final Text text = new Text(label);
		text.setFont(FONT_SIZE);
		gridPane.add(text, 1, row);
		GridPane.setHalignment(text, HPos.CENTER);
	}
	
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
			try { 
				final ShortMessage[] messages = makeMessages(); 
				for (byte i = 0; i < messages.length; ++i)
					MidiConnection.getUsbReceiver().send(messages[i], -1);
			} 
			catch (InvalidMidiDataException e) { e.printStackTrace(); }
			actionListener.consume();
		});
		grid.add(sendMIDIBtn, 0, 9);
		GridPane.setHalignment(sendMIDIBtn, HPos.RIGHT);
		
		final Spinner<Integer> spinner = new Spinner<>(RuppetControl.MIN_VELOCITY, RuppetControl.MAX_VELOCITY, RuppetControl.MIN_VELOCITY);
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
			
		final Tooltip tooltip = new Tooltip("Min velocity: " + RuppetControl.MIN_VELOCITY + "\n" + "Max velocity: " + RuppetControl.MAX_VELOCITY);
		
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
			messages[0] = new ShortMessage(ShortMessage.NOTE_ON, RuppetControl.CHAN_1, midiNotes[0], currentVelocity);
		if (midiNotes.length == 2) 
			messages[1] = new ShortMessage(ShortMessage.NOTE_ON, RuppetControl.CHAN_1, midiNotes[1], RuppetControl.MAX_VELOCITY - currentVelocity);
		return messages;
	}
	
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
					MidiConnection.getUsbReceiver().send(on, -1);
					Thread.sleep(100);
					MidiConnection.getUsbReceiver().send(off, -1);
					Thread.sleep(100);
				} catch (InterruptedException e) { }
			}
		}
		public final void flash() { isFlashing = true; }
		public final void halt() { isFlashing = false; }
	}
	
	/*********************************************************************************/
	
} // end of class ManualOperationScene



