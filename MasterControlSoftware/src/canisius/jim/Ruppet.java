package canisius.jim;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;

/**
 *	This class holds all of the pertinent information that directly deals with the {@code Ruppet}.
 *	The {@code live} method is the methods that allows the {@code Ruppet} to interact with people. 
 *	<P>
 *	Currently we have two modes and a third option: a manual demonstration mode were the user can 
 *	input a number from the keyboard to get the {@code Ruppet} to display a certain emotion that 
 *	corresponds to the number that was inputted. The second mode allows the {@code Ruppet} to run
 *	the script that Steve was able to record where the Ruppet's lowerJaw movements are synchronized with 
 *	Steve's voice. The final option simply puts the {@code Ruppet} to sleep and terminates the program.
 *	<P>
 *	Note that in every instance where the program terminates, it should always close all of the 
 *	connections that were made in the beginning of the program. This is accomplished by 
 *	calling the method Connection.closeConnections(). The Shutdown Hook at the bottom of this 
 * 	file is the part of the program that makes sure that his always happens even if the 
 *	program is terminated unexpectedly. 
 *	
 *	@author Jon Mrowczynski
 */

final class Ruppet {

	//static { System.loadLibrary("KinectEmotionDeterminer"); }

	public native String getCurrentEmotion();
	
	public native boolean hasFace();

    /**
     * Stores all of the {@code Ruppet}'s {@code Part}s. Each {@code Part} is added when it is initialized.
     */

    private final List<Part> parts = new ArrayList<>();

    /**
     * Stores all of the {@code Ruppet}'s {@code Track}'s that are used to sequence commands to the {@code Ruppet}.
     */

    private final List<Track> tracks = new ArrayList<>();

    /**
     * The lower jaw of the {@code Ruppet} which is a {@code Movable Part}
     */

    private final Movable lowerJaw = new Movable(parts, RuppetControl.LOWER_JAW, (byte) 3, (byte) 8);

    /**
     * The right and left lip corners of the {@code Ruppet} which is a {@code Movable Part}
     */

    private final Movable lipCorners = new Movable(parts, RuppetControl.LEFT_LIP_CORNER, (byte) 1, (byte) 4, RuppetControl.RIGHT_LIP_CORNER, "antiparallel");

    /**
     * The eyebrows of the {@code Ruppet} which is a {@code Movable Part}
     */

    private final Movable eyebrows = new Movable(parts, RuppetControl.EYEBROW, (byte) 4, (byte) 7);

    /**
     * The eyelids of the {@code Ruppet} which is a {@code Movable Part}
     */

    private final Movable eyelids = new Movable(parts, RuppetControl.EYELIDS, (byte) 3, (byte) 7);

    /**
     * The eye lights of the {@code Ruppet} which is a {@code Lights Part}
     */

    private final Lights lights = new Lights(parts, RuppetControl.LIGHTS);

	/**
	 * The component of the {@code Ruppet} that allows it to feel.
	 */

	private Heart heart;
	
	/**
	 * The component of the {@code Ruppet} that allows it to speak.
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
            RuppetControl.muteAllTracks(tracks);
            RuppetControl.deSoloAllTracks_ExceptEyes(tracks, blinkingTrack);
        } catch(InvalidMidiDataException e) { e.printStackTrace();}
		final ReleaseSoul releaseSoul = new ReleaseSoul();
		Runtime.getRuntime().addShutdownHook(releaseSoul);
	} // end of Ruppet constructor
	
	/**
	 * Allows the {@code Ruppet} to interact with people by asking for user input from the keyboard using a CLI.
	 */

	final void live() {
		int choice = -1;
		lights.on();
		MidiConnection.getSequencer().start();
		//Connect.getSequencer().setTrackSolo(blinkingTrack.getTrackIndex(), true); // this is where the thing bugs
		do {
			RuppetControl.deSoloAllTracks(tracks); 
			System.out.println("\n\nWould you like me to...");
			System.out.println("1. Go into manual emotion demo mode");
			System.out.println("2. Go into manual FAU demo mode");
			System.out.println("3. Run Steve Scripts");
			System.out.println("4. For mirror mode");
			System.out.println("5. Or go back to sleep?"); 
			System.out.print("\tChoice: ");
			try { 
				choice = RuppetControl.reader.nextInt();
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
				RuppetControl.reader.nextLine();
			}
		} while(choice != 3);
	}

	/**
	 * Gets the {@code List} that contains all of the {@code Ruppet}'s {@code Track}'s.
	 * 
	 * @return The {@code List} that contains all of the {@code Ruppet}'s {@code Track}'s
	 */
	
	final List<Track> getTracks() { return tracks; }

    /**
     * Returns the {@code Heart} of this {@code Ruppet}.
     *
     * @return the {@code Heart} of this {@code Ruppet}.
     */

	final Heart getHeart() { return heart; }
	
	/**
	 * Gets the {@code lowerJaw Part} of the {@code Ruppet}.
	 * 
	 * @return The {@code lowerJaw Part} of the {@code Ruppet}.
	 */
	
	final Movable getLowerJaw() { return lowerJaw; }
	
	/**
	 * Gets the {@code lipCorners Part} of the {@code Ruppet}. 
	 * 
	 * @return The {@code lipCorners Part} of the {@code Ruppet}.
	 */
	
	final Movable getLipCorners() { return lipCorners; }
	
	/**
	 * Gets the {@code eyebrows Part] of the {@code Ruppet}.
	 * 
	 * @return The {@code eyebrows Part] of the {@code Ruppet}.
	 */
	
	final Movable getEyebrows() { return eyebrows; }
	
	/**
	 * Gets the {@code eyelids Part} of the {@code Ruppet}.
	 * 
	 * @return The {@code eyelids Part} of the {@code Ruppet}.
	 */
	
	final Movable getEyelids() { return eyelids; }
	
	/**
	 * Gets the {@code List} that contains all of the {@code Part}s of the {@code Ruppet}.
	 * 
	 * @return The {@code List} that contains all of the {@code Part}s of the {@code Ruppet}.
	 */
	
	final List<Part> getParts() { return parts; }
		
	/**
	 * Allows the user to determine which {@code Emotion} the {@code Ruppet} should display using a CLI.
	 */

	private void manualEmotionDemoMode() {
		int emotionChoice;
		do {
			System.out.println("\n1. Neutral");
			System.out.println("2. Happy");
			System.out.println("3. Sad");
			System.out.println("4. Angry");
			System.out.println("5. Scared");
			System.out.println("6. Smile");
			System.out.println("9. To Exit");
			System.out.print("Choice: ");
			try { emotionChoice = RuppetControl.reader.nextInt(); } 
			catch(InputMismatchException e) { 
				RuppetControl.reader.nextLine();
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
			if (emotionChoice != 9) { RuppetControl.pause(); }
		} while(emotionChoice != 9);
	} // end of manualEmotionDemoMode
	
	/**
	 * Allows the user to determine which FAU they would like the {@code Ruppet} to display on command using a CLI.
	 */

	private void manualFAUDemoMode() {
		int fauChoice;
		do {
			System.out.println("\n1. Eyebrow up");
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
				fauChoice = RuppetControl.reader.nextInt();
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
				RuppetControl.reader.nextLine();
				fauChoice = -1;
			}
		} while (fauChoice != 20); 
	} // end of manualFAUMode
	
	/**
	 * Runs the scripted demo that our friend Steve was so kind to help us with. :)
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
		RuppetControl.pause();
		System.exit(0);
	} 
	
	/**
	 * Fills the blinking {@code Track} with MIDI data. The blinking effect is created by turning on and off the two
     * LED's in the {@code Ruppet}'s eyes.
	 * 
	 * @param blinkingTrack that contains the timings for the blinking.
	 */

	private void fillBlinkTrack(final Track blinkingTrack) {
		final int blink_length = 200;
		final int max_blink_wait = 3500;
		final ShortMessage[] eyelidsUp = this.eyelids.getUpperBoundState();
		final ShortMessage[] eyelidsDown = this.eyelids.getLowerBoundState();

		int prev_blink_time = 0;
		int next_blink_time;

		for(int i = 0; i < 500; ++i) {
			next_blink_time = RuppetControl.getRandInt(prev_blink_time, prev_blink_time + max_blink_wait);
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
	 *	The master reset button can be pressed to accomplish the same thing. However, why do something yourself when you
     *	can get a computer to do it for you?
	*/

	private final class ReleaseSoul extends Thread {
		public final void run() {
			System.out.println();
			RuppetControl.deSoloAllTracks(tracks);
			RuppetControl.reader.close();
			parts.forEach(part -> {
                if (part instanceof Movable) { ((Movable) part).toNeutral(); }
                else if (part instanceof Lights) { ((Lights) part).off(); }
            });
			MidiConnection.closeConnection();
		}
	}

} // end of Ruppet class