package canisius.jim.ruppet;

import canisius.jim.connections.MidiConnection;
import canisius.jim.parts.*;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Set;

/**
 * A {@code Ruppet} can be operated in four modes.
 *
 * The first mode is the manual demonstration mode were the user can input a number from the keyboard to get the
 * {@code Ruppet} to display the {@code Emotion} that corresponds to the number that was inputted.
 *
 * The second mode is the Facial Action Unit (FAS) mode where the user can input a number from the keyboard to get the
 * {@code Ruppet} to display the FAU that corresponds to the number that was inputted.
 *
 * The third mode allows the {@code Ruppet} to run the script that Steve was able to record where the {@code Ruppet}'s
 * {@code lowerJaw} movements are synchronized with Steve's voice.
 *
 * The fourth mode allows the {@code Ruppet} to mirror the emotion that it thinks the individual is expressing.
 *
 * The user also has the option to put the {@code Ruppet} to sleep and terminate the program.
 *	
 *	@author Jon Mrowczynski
 */

public final class Ruppet {

	/**
	 * The MIDI note that is associated with the servo motor that controls the {@code Ruppet}'s eyebrow.
	 */

	static final byte EYEBROW_MIDI_NOTE = 0x3C; // Servo 1 C4

	/**
	 * The MIDI note that is associated with the servo motor that controls the {@code Ruppet}'s left lip corner.
	 */

	static final byte LEFT_LIP_CORNER_MIDI_NOTE = 0x3E; // Servo 2 D4

	/**
	 * The MIDI note that is associated with the servo motor that controls the {@code Ruppet}'s right lip corner.
	 */

	static final byte RIGHT_LIP_CORNER_MIDI_NOTE = 0x40; // Servo 3 E4

	/**
	 * The MIDI note that is associated with the servo motor that controls the {@code Ruppet}'s lower jaw.
	 */

	public static final byte LOWER_JAW_MIDI_NOTE = 0x43; // Servo 4 G4

	/**
	 * The MIDI note that is associated with the servo motor that controls the {@code Ruppet}'s eyelids.
	 */

	static final byte EYELIDS_MIDI_NOTE = 0x45; // Servo 5 A4

	/**
	 * The MIDI note that is associated with the sixth servo motor. Currently this is not used in our {@code Ruppet}.
	 */

	static final byte SERVO6_MIDI_NOTE = 0x48; // Servo 6 C5

	/**
	 * The MIDI note that is associated with the eye lights of the {@code Ruppet}.
	 */

	static final byte LIGHTS_MIDI_NOTE = 0x4A; // Lights D5

	//static { System.loadLibrary("KinectEmotionDeterminer"); }

	/**
	 * Returns a {@code String} representation of the current detected human emotion.
	 *
	 * @return a {@code String} representing the current detected human emotion.
	 */

	public native String getCurrentEmotion();

	/**
	 * Returns a {@code boolean} representing whether the Kinect has detected a person's face.
	 *
	 * @return a {@code boolean} representing whether the Kinect has detected a person's face.
	 */
	
	public native boolean hasFace();

    /**
     * All of the {@code Ruppet}'s {@code Part}s. Each {@code Part} is added when it is initialized.
     */

    private final List<Part> parts = new ArrayList<>();

    /**
     * All of the {@code Ruppet}'s {@code Track}'s that are used to sequence commands to the {@code Ruppet}.
     */

    private final List<Track> tracks = new ArrayList<>();

    /**
     * The lower jaw of the {@code Ruppet}.
     */

    private final Movable lowerJaw = new Movable(parts, Ruppet.LOWER_JAW_MIDI_NOTE, (byte) 3, (byte) 8);

    /**
     * The right and left lip corners of the {@code Ruppet}.
     */

    private final Movable lipCorners = new Movable(parts, Ruppet.LEFT_LIP_CORNER_MIDI_NOTE, (byte) 1, (byte) 4, Ruppet.RIGHT_LIP_CORNER_MIDI_NOTE, "antiparallel");

    /**
     * The eyebrows of the {@code Ruppet}.
     */

    private final Movable eyebrows = new Movable(parts, Ruppet.EYEBROW_MIDI_NOTE, (byte) 4, (byte) 7);

    /**
     * The eyelids of the {@code Ruppet}.
     */

    private final Movable eyelids = new Movable(parts, Ruppet.EYELIDS_MIDI_NOTE, (byte) 3, (byte) 7);

    /**
     * The eye lights of the {@code Ruppet}.
     */

    private final Lights lights = new Lights(parts, Ruppet.LIGHTS_MIDI_NOTE);

	/**
	 * The component of the {@code Ruppet} that allows it to feel.
	 */

	private Heart heart;
	
	/**
	 * The component of the {@code Ruppet} that allows it to talk.
	 */
	
	private Voice voice;

    /**
     * Set everything up so that the {@code Ruppet} can have a successful life.
     */

    public Ruppet() {
		lowerJaw.setNeutral(8);
		lipCorners.setNeutral(2);
		eyebrows.setNeutral(5);
		eyelids.setNeutral(5);
		/*
		 * The values Sequence.PPQ, 160, and 375 were chosen based on the formula:
		 * ticksPerSecond = resolution * (currentTempoInBeatsPerMinute / 60.0)
		 * such that we would get 1 tick per ms.
		 * ticksPerSecond = 160 * (375 / 60.0) = 1,000[ticks/s] = 1[tick/ms]
		 */
        MidiConnection.establishConnection();
        MidiConnection.getSequencer().setTempoInBPM(375);
        MidiConnection.getSequencer().setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
        Sequence actions;
		try {
			actions = new Sequence(Sequence.PPQ, 160);
            heart = new Heart(this, actions);
            voice = new Voice(this, actions);
            final Track blinkingTrack = actions.createTrack();
            tracks.add(heart.getEmotionTrack());
            tracks.add(voice.getVoiceTrack());
            tracks.add(blinkingTrack);
            // Fill blinkingTrack with events that have been randomly chosen to give more of a "real" blinking effect.
            fillBlinkTrack(blinkingTrack);
            /*
             * Now that all of the Sequence's Tracks have been filled with MidiEvents, add the Sequence to the
             * Sequencer, otherwise, MidiEvents will not be stored in the Tracks.
             */
            MidiConnection.getSequencer().setSequence(actions);
            // Mute all of the tracks so that they are not unintentionally all playing at once
            RuppetUtils.muteAllTracks(tracks);
            RuppetUtils.deSoloAllTracks_ExceptEyes(tracks, blinkingTrack);
        } catch(InvalidMidiDataException e) { e.printStackTrace();}
		final ReleaseSoul releaseSoul = new ReleaseSoul();
		Runtime.getRuntime().addShutdownHook(releaseSoul);
	} // end of Ruppet constructor
	
	/**
	 * Allows the {@code Ruppet} to interact with people by asking for user input from the keyboard using a CLI.
	 */

	public final void live() {
		int choice = -1;
		lights.on();
		MidiConnection.getSequencer().start();
		//Connect.getSequencer().setTrackSolo(blinkingTrack.getTrackIndex(), true); // this is where the thing bugs
		do {
			RuppetUtils.deSoloAllTracks(tracks);
			System.out.println("\n");
			System.out.println("Would you like me to...");
			System.out.println("1. Go into manual emotion demo mode");
			System.out.println("2. Go into manual FAU demo mode");
			System.out.println("3. Run Steve Scripts");
			System.out.println("4. For mirror mode");
			System.out.println("5. Or go back to sleep?"); 
			System.out.print("\tChoice: ");
			try { 
				choice = RuppetUtils.reader.nextInt();
				switch(choice) {
					case 1: manualEmotionDemoMode();								break;
					case 2: manualFAUDemoMode();									break;
					case 3: runSteveScripts();										break;
					case 4: mirrorMode();											break;
					case 5: goToSleep();											break;
					default: System.out.println("Sorry, that's not an option.");	break;
				} 
			} catch(InputMismatchException e) { 
				System.out.println("\nI don't understand that input, make sure you type in an int!");
				RuppetUtils.reader.nextLine();
			}
		} while(choice != 3);
	}

	/**
	 * Allows the user to determine which {@code Emotion} the {@code Ruppet} should display using a CLI.
	 */

	private void manualEmotionDemoMode() {
		int emotionChoice;
		do {
			System.out.println();
			System.out.println("1. Neutral");
			System.out.println("2. Happy");
			System.out.println("3. Sad");
			System.out.println("4. Angry");
			System.out.println("5. Scared");
			System.out.println("6. Smile");
			System.out.println("9. To Exit");
			System.out.print("Choice: ");
			try { emotionChoice = RuppetUtils.reader.nextInt(); }
			catch(InputMismatchException e) {
				RuppetUtils.reader.nextLine();
				emotionChoice = -1;
			}
			System.out.println();
			switch(emotionChoice) {
				case 1: heart.feel(heart.getNeutral()); 				break;
				case 2: heart.feel(heart.getHappy());					break;
				case 3: heart.feel(heart.getSad());						break;
				case 4: heart.feel(heart.getAngry());					break;
				case 5: heart.feel(heart.getScared());					break;
				case 6: heart.feel(heart.getSmile());					break;
				case 9: System.out.println("Exited manual demo mode"); 	break;
				default: System.out.println("That is not an option.");	break;
			}
			// Display the specified emotion for a couple of seconds.
			if (emotionChoice != 9) { RuppetUtils.pause(); }
		} while(emotionChoice != 9);
	} // end of manualEmotionDemoMode

	/**
	 * Allows the user to determine which FAU they would like the {@code Ruppet} to display on command using a CLI.
	 */

	private void manualFAUDemoMode() {
		int fauChoice;
		do {
			System.out.println();
			System.out.println("1. Eyebrow up");
			System.out.println("2. Eyebrow neutral");
			System.out.println("3. Eyebrow down");
			System.out.println("4. Lower Jaw up");
			System.out.println("5. Lower Jaw neutral");
			System.out.println("6. Lower Jaw down");
			System.out.println("7. Lip Corners up");
			System.out.println("8. Lip Corners neutral");
			System.out.println("9. Lip Corners down");
			System.out.println("10. Eyelids up");
			System.out.println("11. Eyelids neutral");
			System.out.println("12. Eyelids down");
			System.out.println("20. To Go Back");
			System.out.print("Choice: ");
			try {
				fauChoice = RuppetUtils.reader.nextInt();
				switch(fauChoice) {
					case 1: eyebrows.toUpperBound();								break;
					case 2: eyebrows.toNeutral();									break;
					case 3: eyebrows.toLowerBound();								break;
					case 4: lowerJaw.toUpperBound();								break;
					case 5: lowerJaw.toNeutral();									break;
					case 6: lowerJaw.toLowerBound();								break;
					case 7: lipCorners.toUpperBound();								break;
					case 8: lipCorners.toNeutral();									break;
					case 9: lipCorners.toLowerBound();								break;
					case 10: eyelids.toLowerBound();								break;
					case 11: eyelids.toNeutral();									break;
					case 12: eyelids.toUpperBound();								break;
					default: System.out.println("That is not an available option");	break;
				} // end of switch
			} catch (InputMismatchException e) {
				RuppetUtils.reader.nextLine();
				fauChoice = -1;
			}
		} while (fauChoice != 20);
	} // end of manualFAUMode

	/**
	 * Runs the scripted demo that our friend Steve was so kind to record for us. :)
	 */

	private void runSteveScripts() {
		System.out.println();
		MidiConnection.getSequencer().stop();
		MidiConnection.getSequencer().setMicrosecondPosition(0);
		voice.givePresentation();
	}

	/**
	 * Allows the {@code Ruppet} to display the {@code Emotion} that it thinks that the human is emoting.
	 */

	private void mirrorMode() {
		do {
			switch(getCurrentEmotion()) {
				case "HAPPY":	heart.feel(heart.getHappy());	break;
				case "SAD": 	heart.feel(heart.getSad());		break;
				case "ANGRY": 	heart.feel(heart.getAngry());	break;
				default: 		heart.feel(heart.getNeutral());	break;
			}
		} while(hasFace());
	}

	/**
	 * Terminates the connections made with the USB {@code MidiDevice} and {@code Sequencer} while also terminating the
	 * program. The Shutdown Hook is always called when the program is terminated using {@code System.exit(0)}.
	 */

	private void goToSleep() {
		System.out.println("\nOkay, I was getting tired anyway.");
		RuppetUtils.pause();
		System.exit(0);
	}

	/**
	 * Gets all of this {@code Ruppet}'s {@code Track}s.
	 * 
	 * @return all of this {@code Ruppet}'s {@code Track}s.
	 */
	
	public final List<Track> getTracks() { return tracks; }

    /**
     * Gets this {@code Ruppet}'s {@code Heart}.
     *
     * @return This {@code Ruppet}'s {@code Heart}.
     */

	public final Heart getHeart() { return heart; }
	
	/**
	 * Gets this {@code Ruppet}'s {@code lowerJaw}.
	 * 
	 * @return This {@code Ruppet}'s {@code lowerJaw}.
	 */
	
	public final Movable getLowerJaw() { return lowerJaw; }
	
	/**
	 * Gets this {@code Ruppet}'s {@code lipCorners}.
	 * 
	 * @return This {@code Ruppet}'s {@code lipCorners}.
	 */
	
	public final Movable getLipCorners() { return lipCorners; }
	
	/**
	 * Gets this {@code Ruppet's }{@code eyebrows}
	 * 
	 * @return This {@code Ruppet}'s {@code eyebrows}.
	 */
	
	public final Movable getEyebrows() { return eyebrows; }
	
	/**
	 * Gets this {@code Ruppet}'s {@code eyelids}.
	 * 
	 * @return This {@code Ruppet}'s {@code eyelids}.
	 */
	
	public final Movable getEyelids() { return eyelids; }
	
	/**
	 * Gets all of this {@code Ruppet}'s {@code Part}s.
	 * 
	 * @return all of this {@code Ruppet}'s {@code Part}s.
	 */
	
	public final List<Part> getParts() { return parts; }
	
	/**
	 * Fills the blinking {@code Track} with MIDI data. The blinking effect is created by turning on and off the two
     * LED's in the {@code Ruppet}'s eyes.
	 * 
	 * @param blinkingTrack that contains the timings for the blinking.
	 */

	private void fillBlinkTrack(final Track blinkingTrack) {
		final int blink_length = 200;
		final int max_blink_wait = 3500;
		final Set<ShortMessage> eyelidsUp = this.eyelids.getUpperBoundState();
		final Set<ShortMessage> eyelidsDown = this.eyelids.getLowerBoundState();

		int prev_blink_time = 0;
		int next_blink_time;

		for(int i = 0; i < 500; ++i) {
			next_blink_time = RuppetUtils.getRandInt(prev_blink_time, prev_blink_time + max_blink_wait);
			eyelids.addStateToTrack(blinkingTrack, eyelidsDown, prev_blink_time);
			eyelids.addStateToTrack(blinkingTrack, eyelidsUp, next_blink_time);
			prev_blink_time = next_blink_time + blink_length;
		}
	}

	/**
     * If the program terminates and one or more motors are being operated, no note off MIDI messages will be sent to
     * shut off the motors and they can potentially strain themselves depending on their last state! Therefore, a
     * Shutdown Hook is implemented to prevent this from occurring.
	 *			
	 * The master reset button can be pressed to accomplish the same thing. However, why do something yourself when you
     * can get a computer to do it for you?
	*/

	private final class ReleaseSoul extends Thread {
		public final void run() {
			System.out.println();
			RuppetUtils.deSoloAllTracks(tracks);
			RuppetUtils.reader.close();
			parts.forEach(Part::toNeutral);
			MidiConnection.closeConnection();
		}
	}

} // end of Ruppet class