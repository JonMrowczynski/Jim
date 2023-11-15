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

import canisius.jim.connections.SequencerConnection;
import canisius.jim.connections.UsbMidiConnection;
import canisius.jim.parts.*;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;
import java.util.*;

/**
 * A {@code Ruppet} can be operated in four modes.
 * <p>
 * The first mode is the manual demonstration mode were the user can input a number from the keyboard to get the
 * {@code Ruppet} to display the {@code Emotion} that corresponds to the number that was inputted.
 * <p>
 * The second mode is the Facial Action Unit (FAS) mode where the user can input a number from the keyboard to get the
 * {@code Ruppet} to display the FAU that corresponds to the number that was inputted.
 * <p>
 * The third mode allows the {@code Ruppet} to run Steve's script where the {@code Ruppet}'s {@code lowerJaw} movements
 * are synchronized with Steve's voice.
 * <p>
 * The fourth mode allows the {@code Ruppet} to mirror the emotion that it thinks the individual is expressing.
 * <p>
 * The user also has the option to put the {@code Ruppet} to sleep and terminate the program.
 *
 * @author Jon Mrowczynski
 */
public final class Ruppet {
	
	/**
	 * The maximum MIDI velocity value.
	 */
	public static final byte MAX_VELOCITY = 10;
	
	/**
	 * The minimum MIDI velocity value.
	 */
	public static final byte MIN_VELOCITY = 0;
	
	/**
	 * The MIDI note that is associated with the servo motor that controls the {@code Ruppet}'s eyebrow.
	 */
	public static final byte EYEBROW_MIDI_NOTE = 0x3C; // Servo 1 C4
	
	/**
	 * The MIDI note that is associated with the servo motor that controls the {@code Ruppet}'s left lip corner.
	 */
	public static final byte LEFT_LIP_CORNER_MIDI_NOTE = 0x3E; // Servo 2 D4
	
	/**
	 * The MIDI note that is associated with the servo motor that controls the {@code Ruppet}'s right lip corner.
	 */
	public static final byte RIGHT_LIP_CORNER_MIDI_NOTE = 0x40; // Servo 3 E4
	
	/**
	 * The MIDI note that is associated with the servo motor that controls the {@code Ruppet}'s lower jaw.
	 */
	public static final byte LOWER_JAW_MIDI_NOTE = 0x43; // Servo 4 G4
	
	/**
	 * The MIDI note that is associated with the servo motor that controls the {@code Ruppet}'s eyelids.
	 */
	public static final byte EYELIDS_MIDI_NOTE = 0x45; // Servo 5 A4
	
	/**
	 * The MIDI note that is associated with the sixth servo motor. Currently, this is not used in our {@code Ruppet}.
	 */
	public static final byte SERVO6_MIDI_NOTE = 0x48; // Servo 6 C5
	
	/**
	 * The MIDI note that is associated with the eye lights of the {@code Ruppet}.
	 */
	public static final byte LIGHTS_MIDI_NOTE = 0x4A; // Lights D5
	
	/**
	 * Used to acquire input from the user when they are prompted by the CLI.
	 */
	private static final Scanner reader = new Scanner(System.in);
	
	//static { System.loadLibrary("KinectEmotionDeterminer"); }
	
	/**
	 * All of the {@code Ruppet}'s {@code Track}'s that are used to sequence commands to the {@code Ruppet}.
	 */
	private final List<Track> tracks = new ArrayList<>();
	
	/**
	 * All of the {@code Ruppet}'s {@code HardwarePart}s. Each {@code HardwarePart} is added when it is initialized.
	 */
	private final Set<HardwarePart> hardwareParts;
	
	/**
	 * The lower jaw of the {@code Ruppet}.
	 */
	private final Movable lowerJaw = new Movable(LOWER_JAW_MIDI_NOTE, 3, 8, 8);
	
	/**
	 * The right and left lip corners of the {@code Ruppet}.
	 */
	private final Movable lipCorners = new Movable(LEFT_LIP_CORNER_MIDI_NOTE, 1, 4, 2, RIGHT_LIP_CORNER_MIDI_NOTE,
	                                               Movable.Parallelism.ANTIPARALLEL);
	
	/**
	 * The eyebrows of the {@code Ruppet}.
	 */
	private final Movable eyebrows = new Movable(EYEBROW_MIDI_NOTE, 4, 7, 5);
	
	/**
	 * The eyelids of the {@code Ruppet}.
	 */
	private final Movable eyelids = new Movable(EYELIDS_MIDI_NOTE, 3, 7, 5);
	
	/**
	 * The eye lights of the {@code Ruppet}.
	 */
	private final Lights lights = new Lights(LIGHTS_MIDI_NOTE);
	
	/**
	 * All of the {@code Ruppet}'s {@code SoftwarePart}s. Each {@code SoftwarePart} is added when it is initialized.
	 */
	private Set<SoftwarePart> softwareParts;
	
	/**
	 * The component of the {@code Ruppet} that allows it to feel.
	 */
	private Heart heart;
	
	/**
	 * The component of the {@code Ruppet} that allows it to talk.
	 */
	private Voice voice;
	
	/**
	 * The {@code Track} that contains all the blinking {@code MidiEvent}s.
	 */
	private Track blinkingTrack;
	
	/**
	 * Set everything up so that the {@code Ruppet} can have a successful life.
	 */
	public Ruppet() {
		hardwareParts = Set.of(lowerJaw, lipCorners, eyebrows, eyelids, lights);
		/*
		 * The values Sequence.PPQ, 160, and 375 were chosen based on the formula:
		 * ticksPerSecond = resolution * (currentTempoInBeatsPerMinute / 60.0)
		 * such that we would get 1 tick per ms.
		 * ticksPerSecond = 160 * (375 / 60.0) = 1,000[ticks/s] = 1[tick/ms]
		 */
		SequencerConnection.getInstance().setReceiver(UsbMidiConnection.getInstance().getUsbReceiver());
		SequencerConnection.getInstance().getMidiDevice().setTempoInBPM(375);
		SequencerConnection.getInstance().getMidiDevice().setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
		final Sequence actions;
		try {
			actions = new Sequence(Sequence.PPQ, SequencerConnection.RESOLUTION);
			heart = new Heart(this, actions);
			voice = new Voice(this, actions);
			softwareParts = Set.of(heart, voice);
			tracks.add(heart.getTrack());
			tracks.add(voice.getTrack());
			blinkingTrack = actions.createTrack();
			tracks.add(blinkingTrack);
			fillBlinkTrack(blinkingTrack);
			/*
			 * Now that all the Sequence's Tracks have been filled with MidiEvents, add the Sequence to the
			 * Sequencer, otherwise, MidiEvents will not be stored in the Tracks.
			 */
			SequencerConnection.getInstance().getMidiDevice().setSequence(actions);
			muteAllTracks(blinkingTrack);
		}
		catch (final InvalidMidiDataException e) { e.printStackTrace(); }
		Runtime.getRuntime().addShutdownHook(new ReleaseSoul());
	}
	
	/**
	 * Fills {@code blinkingTrack} with MIDI data. The blinking effect is created by turning on and off the two
	 * LED's in
	 * the {@code Ruppet}'s eyes at bound, randomly chosen intervals.
	 *
	 * @param blinkingTrack that contains the timings for the blinking
	 */
	private void fillBlinkTrack(final Track blinkingTrack) {
		final var blink_length = 200;
		final var max_blink_wait = 3500;
		final var eyelidsUp = eyelids.getUpperBoundState();
		final var eyelidsDown = eyelids.getLowerBoundState();
		
		var prev_blink_time = 0;
		
		for (var i = 0; i < 500; ++i) {
			final var next_blink_time = getRandInt(prev_blink_time, prev_blink_time + max_blink_wait);
			eyelids.addStateToTrack(blinkingTrack, eyelidsDown, prev_blink_time);
			eyelids.addStateToTrack(blinkingTrack, eyelidsUp, next_blink_time);
			prev_blink_time = next_blink_time + blink_length;
		}
	}
	
	/**
	 * Mutes all of the {@code Track}s except for all of the {@code Track}s that are included in the optional argument
	 * {@code excludedTracks}.
	 *
	 * @param excludedTracks that are not to be muted
	 */
	public void muteAllTracks(final Track... excludedTracks) {
		tracks.forEach(track -> {
			final var sequencer = SequencerConnection.getInstance().getMidiDevice();
			final var trackIndex = tracks.indexOf(track);
			sequencer.setTrackMute(trackIndex,
			                       Arrays.stream(excludedTracks).noneMatch(excludedTrack -> excludedTrack == track));
		});
	}
	
	/**
	 * Returns a random {@code int} between {@code min} and {@code max} inclusive.
	 *
	 * @param min The maximum {@code int} value that can be randomly generated
	 * @param max The minimum {@code int} value that can be randomly generated
	 * @return A random {@code int} between {@code min} and {@code max} inclusive
	 */
	private static int getRandInt(final int min, final int max) {
		return (new Random()).nextInt((max - min) + 1) + min;
	}
	
	/**
	 * Allows the {@code Ruppet} to interact with people by asking for user input from the keyboard using a CLI.
	 */
	public void live() {
		var choice = -1;
		lights.on();
		SequencerConnection.getInstance().getMidiDevice().start();
		do {
			muteAllTracks(blinkingTrack);
			final var prompt = """
					Would you like to...
					1. Go into manual emotion demo mode?
					2. Go into manual FAU demo mode?
					3. Run Steve Scripts?
					4. Go into mirror mode?
					5. Go back to sleep?
						Choice: """;
			System.out.print(prompt);
			try {
				choice = reader.nextInt();
				switch (choice) {
					case 1 -> manualEmotionDemoMode();
					case 2 -> manualFAUDemoMode();
					case 3 -> runSteveScript();
					case 4 -> mirrorMode();
					case 5 -> goToSleep();
					default -> System.out.println("Sorry, that's not an option.");
				}
			}
			catch (final InputMismatchException e) {
				System.out.println("\nI don't understand that input, make sure you type in an int!");
				reader.nextLine();
			}
		} while (choice != 5);
	}
	
	/**
	 * Allows the user to determine which {@code Emotion} the {@code Ruppet} should display using a CLI.
	 */
	private void manualEmotionDemoMode() {
		var emotionChoice = -1;
		do {
			final var prompt = """
					1. Neutral
					2. Happy
					3. Sad
					4. Angry
					5. Scared
					6. Smile
					9. To Exit
						Choice: """;
			System.out.print(prompt);
			try { emotionChoice = reader.nextInt(); }
			catch (final InputMismatchException e) {
				reader.nextLine();
				emotionChoice = -1;
			}
			System.out.println();
			switch (emotionChoice) {
				case 1 -> heart.feel(heart.getNeutral());
				case 2 -> heart.feel(heart.getHappy());
				case 3 -> heart.feel(heart.getSad());
				case 4 -> heart.feel(heart.getAngry());
				case 5 -> heart.feel(heart.getScared());
				case 6 -> heart.feel(heart.getSmile());
				case 9 -> System.out.println("Exited manual demo mode");
				default -> System.out.println("That is not an option.");
			}
			// Display the specified emotion for a couple of seconds.
			if (emotionChoice != 9) { pause_ms(2000); }
		} while (emotionChoice != 9);
	}
	
	/**
	 * Allows the user to determine which FAU they would like the {@code Ruppet} to display using a CLI.
	 */
	private void manualFAUDemoMode() {
		var fauChoice = -1;
		do {
			final var prompt = """
					1. Eyebrow up
					2. Eyebrow neutral
					3. Eyebrow down
					4. Lower Jaw up
					5. Lower Jaw neutral
					6. Lower Jaw down
					7. Lip Corners up
					8. Lip Corners neutral
					9. Lip Corners down
					10. Eyelids up
					11. Eyelids neutral
					12. Eyelids down
					20. To Go Back
						Choice: """;
			System.out.print(prompt);
			try {
				fauChoice = reader.nextInt();
				switch (fauChoice) {
					case 1 -> eyebrows.toUpperBound();
					case 2 -> eyebrows.toNeutral();
					case 3 -> eyebrows.toLowerBound();
					case 4 -> lowerJaw.toUpperBound();
					case 5 -> lowerJaw.toNeutral();
					case 6 -> lowerJaw.toLowerBound();
					case 7 -> lipCorners.toUpperBound();
					case 8 -> lipCorners.toNeutral();
					case 9 -> lipCorners.toLowerBound();
					case 10 -> eyelids.toLowerBound();
					case 11 -> eyelids.toNeutral();
					case 12 -> eyelids.toUpperBound();
					default -> System.out.println("That is not an available option");
				} // end of switch
			}
			catch (final InputMismatchException e) {
				reader.nextLine();
				fauChoice = -1;
			}
		} while (fauChoice != 20);
	}
	
	/**
	 * Runs the scripted demo that our friend Steve was so kind to record for us. :)
	 */
	private void runSteveScript() {
		System.out.println();
		SequencerConnection.getInstance().getMidiDevice().stop();
		SequencerConnection.getInstance().getMidiDevice().setMicrosecondPosition(0);
		voice.givePresentation();
	}
	
	/**
	 * Allows the {@code Ruppet} to display the {@code Emotion} that it thinks that the human is emoting.
	 */
	private void mirrorMode() {
		do {
			switch (getCurrentEmotion()) {
				case "HAPPY" -> heart.feel(heart.getHappy());
				case "SAD" -> heart.feel(heart.getSad());
				case "ANGRY" -> heart.feel(heart.getAngry());
				default -> heart.feel(heart.getNeutral());
			}
		} while (hasFace());
	}
	
	/**
	 * Terminates the {@code UsbMidiConnection} and {@code SequencerConnection} if they were made while also
	 * terminating
	 * the program. This is done through the Shutdown Hook since it is always called when the program is terminated
	 * using {@code System.exit(0)}.
	 */
	private void goToSleep() {
		System.out.println("\nOkay, I was getting tired anyway.");
		pause_ms(2000);
		System.exit(0);
	}
	
	/**
	 * Halts the program for {@code ms} milliseconds.
	 *
	 * @param ms that the program should pause
	 */
	public static void pause_ms(final int ms) {
		try { Thread.sleep(ms); }
		catch (final InterruptedException e) { Thread.currentThread().interrupt(); }
	}
	
	/**
	 * Returns a {@code String} representation of the current detected human emotion.
	 *
	 * @return a {@code String} representing the current detected human emotion
	 */
	public native String getCurrentEmotion();
	
	/**
	 * Returns a {@code boolean} representing whether the Kinect has detected a person's face.
	 *
	 * @return a {@code boolean} representing whether the Kinect has detected a person's face
	 */
	public native boolean hasFace();
	
	/**
	 * Returns all of this {@code Ruppet}'s {@code SoftwarePart}s.
	 *
	 * @return all of this {@code Ruppet}'s {@code SoftwarePart}s
	 */
	public Set<SoftwarePart> getSoftwareParts() { return softwareParts; }
	
	/**
	 * Returns this {@code Ruppet}'s {@code Heart}.
	 *
	 * @return this {@code Ruppet}'s {@code Heart}
	 */
	public Heart getHeart() { return heart; }
	
	/**
	 * Returns this {@code Ruppet}'s {@code Voice}.
	 *
	 * @return this {@code Ruppet}'s {@code Voice}
	 */
	public Voice getVoice() { return voice; }
	
	/**
	 * Returns a {@code List} of all of this {@code Ruppet}'s {@code Track}s.
	 *
	 * @return a {@code List} of all of this {@code Ruppet}'s {@code Track}s
	 */
	public List<Track> getTracks() { return tracks; }
	
	/**
	 * Returns the {@code Track} that contains all of the blinking {@code MidiEvent}s.
	 *
	 * @return the {@code Track} that contains all of the blinking {@code MidiEvent}s
	 */
	public Track getBlinkingTrack() { return blinkingTrack; }
	
	/**
	 * Returns all of this {@code Ruppet}'s {@code HardwarePart}s.
	 *
	 * @return all of this {@code Ruppet}'s {@code HardwarePart}s
	 */
	public Set<HardwarePart> getHardwareParts() { return hardwareParts; }
	
	/**
	 * Returns this {@code Ruppet}'s {@code lowerJaw}.
	 *
	 * @return this {@code Ruppet}'s {@code lowerJaw}
	 */
	public Movable getLowerJaw() { return lowerJaw; }
	
	/**
	 * Returns this {@code Ruppet}'s {@code lipCorners}.
	 *
	 * @return this {@code Ruppet}'s {@code lipCorners}
	 */
	public Movable getLipCorners() { return lipCorners; }
	
	/**
	 * Returns this {@code Ruppet's }{@code eyebrows}.
	 *
	 * @return this {@code Ruppet}'s {@code eyebrows}
	 */
	public Movable getEyebrows() { return eyebrows; }
	
	/**
	 * Returns this {@code Ruppet}'s {@code eyelids}.
	 *
	 * @return this {@code Ruppet}'s {@code eyelids}
	 */
	public Movable getEyelids() { return eyelids; }
	
	/**
	 * Returns this {@code Ruppet}'s {@code lights}.
	 *
	 * @return this {@code Ruppet}'s {@code lights}
	 */
	public Lights getLights() { return lights; }
	
	/**
	 * If the program terminates and one or more {@code Part}s are being operated, no note off {@code MidiMessage}s
	 * will
	 * be sent to shut off those {@code Part}s and they can potentially strain the {@code Ruppet}'s mechanisms
	 * depending
	 * on their last state! Therefore, a Shutdown Hook is implemented to prevent this from occurring.
	 * <p>
	 * The master reset button can be pressed to accomplish the same thing. However, why do something yourself when you
	 * can get a computer to do it for you?
	 */
	private final class ReleaseSoul extends Thread {
		
		@Override public void run() {
			System.out.println();
			muteAllTracks();
			reader.close();
			hardwareParts.forEach(HardwarePart::toNeutral);
			UsbMidiConnection.getInstance().disconnect();
			SequencerConnection.getInstance().disconnect();
		}
	}
	
}