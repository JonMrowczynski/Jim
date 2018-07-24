package canisius.jim;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 *	This class holds all of the pertinent information that directly deals with the {@code Ruppet}.
 *	The {@code live} method is the methods that allows the {@code Ruppet} to interact with people. 
 *	<P>
 *	Currently we have two modes and a third option: a manual demonstration mode were the user can 
 *	input a number from the keyboard to get the {@code Ruppet} to display a certain emotion that 
 *	corresponds to the number that was inputed. The second mode allows the {@code Ruppet} to run
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
 *	@version 1.2
 */

public final class Ruppet {

	//static { System.loadLibrary("KinectEmotionDeterminer"); }

	public native String getCurrentEmotion();
	public native boolean hasFace();

	/**
	 * The emotional center of the {@code Ruppet}.
	 */

	private Heart heart = null;
	
	/**
	 * The vocal center of the {@code Ruppet}.
	 */
	
	private Voice voice = null;
	
	/**
	 * The {@code Sequencer} that stores all of the {@code Track}s which will be played by the system's {@code Sequencer}.
	 */

	private Sequence actions = null;
	
	/**
	 * The {@code Track} that stores all of the randomly generated times for the {@code Ruppet} to blink.
	 */

	private Track blinkingTrack = null;
	
	/**
	 * The {@code Track} that stores all of the {@code Emotion} transition timings.
	 */
	
	private Track emotionTrack = null;
	
	/**
	 * The {@code Track{ that stores all of the {@code lowerJaw} movements to allow the {@code Ruppet} to talk.
	 */
	
	private Track voiceTrack = null;
	
	/* This ArrayList stores wrapper AugmentedTrack classes. Currently there are only 3 AugmentedTracks
	   that are stored which are the ones listed above */

	private final List<Track> tracks = new ArrayList<>();

	/* An ArrayList of Ruppet Parts. Currently the Parts that our Ruppet uses are the ones that 
	   are listed below this ArrayList */

	private final List<Part> parts = new ArrayList<>();

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
	
	private final Lights lights = new Lights(parts, RuppetControl.LIGHTS, (byte) 0, (byte) 10); 
	
	/* This constructor sets everything up in order for the Ruppet to have a successful life. */

	public Ruppet() {
		
		lowerJaw.setNeutral( (byte) 8);
		lipCorners.setNeutral( (byte) 2);
		eyebrows.setNeutral( (byte) 5);
		eyelids.setNeutral( (byte) 5);

		RuppetControl.initConnections(); // Setup the USB and Sequencer connections

		try {
			
			/* 
			 * Sequence.PPQ == pulses (ticks) per quarter note
			 * 160 == 160 pulses per quarter note
			 * Tempo in beats per minute: 375 (From RuppetControl)
			 * such that ticksPerSecond == 160 * (375 / 60) == 1000 
			 * So we get one tick every 1ms 
			 */

			actions = new Sequence(Sequence.PPQ, 160);

		} catch(InvalidMidiDataException ex) { ex.printStackTrace(); }
		
		emotionTrack = actions.createTrack();
		blinkingTrack = actions.createTrack();
		voiceTrack = actions.createTrack();
		
		tracks.add(emotionTrack);
		tracks.add(blinkingTrack);
		tracks.add(voiceTrack);
		( (ArrayList<Track>) tracks).trimToSize();

		/* Get the save files to find where all of the data for movement is kept */

		RuppetControl.initSaveFiles();

		/* Initialize the Ruppet's Heart and Voice */

		heart = new Heart(this, emotionTrack);
		voice = new Voice(this, voiceTrack);

		/* Fill the blinkingTrack with events that have been randomly chosen to give 
		   more of a random blinking effect */

		fillBlinkTrack(blinkingTrack);

		/* Want to set the Sequence after all the MidiEvents have been stored in the Tracks, 
		   otherwise, the MidiEvents will not be stored in the Tracks if they are added 
		   after setting the Sequence. */

		try{ MidiConnection.getSequencer().setSequence(actions); } 
		catch(InvalidMidiDataException ex) { ex.printStackTrace(); }	

		/* Mute all of the tracks so that they are not unintentionally 
		   all playing at once.  */

		RuppetControl.MuteAllTracks(tracks);  
		RuppetControl.DeSoloAll_ExceptEyes(tracks, blinkingTrack);

		final ReleaseSoul releaseSoul = new ReleaseSoul();
		Runtime.getRuntime().addShutdownHook(releaseSoul);

	} // end of Ruppet constructor
	
	/**
	 * This method allows the {@code Ruppet} tp interact with people by asking
	 * for user input from the keyboard using a CLI.
	 * 
	 * @param heart The {@code Heart} associated with the {@code Ruppet}
	 * @param voice The {@code Voice} associated with the {@code Ruppet}.
	 */

	public final void live() {

		byte choice = -1;
		
		/* Set everything up */

		lights.on();
		
		MidiConnection.getSequencer().start();

		//Connect.getSequencer().setTrackSolo(blinkingTrack.getTrackIndex(), true); // this is where the thing bugs

		do { // live

			RuppetControl.DeSoloAllTracks(tracks); 

			System.out.println("\n\nWould you like me to...");
			System.out.println("1. Go into manual emotion demo mode");
			System.out.println("2. Go into manual FAU demo mode");
			System.out.println("3. Run Steve Scripts");
			System.out.println("4. For mirror mode");
			System.out.println("5. Or go back to sleep?"); 
			System.out.print("\tChoice: ");

			try { 

				choice = RuppetControl.reader.nextByte();

				switch(choice) {

					case 1:
						manualEmotionDemoMode();
						break;

					case 2:
						manualFAUDemoMode();
						break;

					case 3:
						runSteveScripts();
						break;

					case 4:
						mirrorMode();
						break;

					case 5:
						goToSleep();
						break;

					default:

						System.out.println("Sorry, that's not an option.");
						break;

				} 

			} catch(InputMismatchException ex) { 
				System.out.println("\nI don't understand that input, make sure you type in an int!");
				RuppetControl.reader.nextLine();
			}

		} while(choice != 3); // end of do while loop
		
	} // end of live
	
	/**
	 * Gets the {@code Heart} associated with the {@code Ruppet}.
	 * 
	 * @return The {Ruppet}'s {@code Heart}.
	 */

	public final Heart getHeart() { return heart; }
	
	/**
	 * Gets the {@code Voice} associated with the {@code Ruppet}.
	 * 
	 * @return The {@code Ruppet}'s {@code Voice}.
	 */
	
	public final Voice getVoice() { return voice; }
	
	/**
	 * Gets the {@code Sequence} which contains and can play all of the stored {@code Tracks}.
	 * 
	 * @return The {@code Ruppet}'s {@code Sequence}
	 */
	
	public final Sequence getSequence() { return actions; }
	
	/**
	 * Gets the {@code Track} that contains all of the blinking timings.
	 * 
	 * @return The {@code Ruppet}'s {@code Track} that stores blink timing information.
	 */
	
	public final Track getBlinkingTrack() { return blinkingTrack; }
	
	/**
	 * Gets the {@code Track} that contains all of the {@code Emotion} transition timings.
	 * 
	 * @return The {@code Ruppet}'s {@code Track} that stores the {@code Emotion} transition timings
	 */
	
	public final Track getEmotionTrack() { return emotionTrack; }
	
	/**
	 * Gets the {@code Track} that contains all of the {@code lowerJaw} movement timings.
	 * 
	 * @return the {@code Ruppet}'s {@code Track} that stores the {@code lowerJaw} movement timings
	 */
	
	public final Track getVoiceTrack() { return voiceTrack; }
	
	/**
	 * Gets the {@code ArrayList} tha contains all of the {@code Track}'s associated with the {@code Ruppet}.
	 * 
	 * @return An {@code ArrayList} that contains all of the {@code Ruppet}'s {@code Track}'s 
	 */
	
	public final List<Track> getTracks() { return tracks; }
	
	/**
	 * Gets the {@code lowerjaw Part} of the {@code Ruppet}. 
	 * 
	 * @return The {@code lowerJaw Part} of the {@code Ruppet}
	 */
	
	public final Movable getLowerJaw() { return lowerJaw; }
	
	/**
	 * Gets the {@code lipCorners Part} of the {@code Ruppet}. 
	 * 
	 * @return The {@code lipCorners Part} of the {@code Ruppet}
	 */
	
	public final Movable getLipCorners() { return lipCorners; }
	
	/**
	 * Gets the {@code eyebrows Part] of the {@code Ruppet}.
	 * 
	 * @return The {@code eyebrows Part] of the {@code Ruppet}
	 */
	
	public final Movable getEyebrows() { return eyebrows; }
	
	/**
	 * Gets the {@code eyelids Part} of the {@code Ruppet}.
	 * 
	 * @return The {@code eyelids Part} of the {@code Ruppet}
	 */
	
	public final Movable getEyelids() { return eyelids; }
	
	/**
	 * Gets the {@code Lights} of the {@code Ruppet} which are currently only eye lights.
	 * 
	 * @return The eye lights of the {@code Ruppet}
	 */
	
	public final Lights getLights() { return lights; }
	
	/**
	 * Gets the {@code ArrayList} that contains all of the {@code Part}s of the {@code Ruppet}.
	 * 
	 * @return The {@code ArrayList} that contains all of the {@code Part}s of the {@code Ruppet}
	 */
	
	public final List<Part> getParts() { return parts; }
		
	/**
	 * Allows the user to determine which {@code Emotion} the {@code Ruppet} should display 
	 * using a CLI.
	 * 
	 * @param heart The {@code Heart} associated with the {@code Ruppet}
	 */

	private final void manualEmotionDemoMode() {

		final int exitChoice = 9;
		
		int emotionChoice = -1;
		
		do {

			System.out.println("\n1. Neutral");
			System.out.println("2. Happy");
			System.out.println("3. Sad");
			System.out.println("4. Angry");
			System.out.println("5. Scared");
			System.out.println("6. Smile");
			System.out.println("9. To Exit");
			System.out.print("Choice: ");

			try {
				emotionChoice = RuppetControl.reader.nextInt();
			} catch(InputMismatchException ex) { 
				RuppetControl.reader.nextLine();
				emotionChoice = -1;
			} 
			
			System.out.println();

			switch(emotionChoice) {
				case 1:
					heart.feel(heart.getNeutral());
					break;
				case 2:
					heart.feel(heart.getHappy());
					break;
				case 3:
					heart.feel(heart.getSad());
					break;
				case 4:
					heart.feel(heart.getAngry());
					break;
				case 5:
					heart.feel(heart.getScared());
					break;
				case 6:
					heart.feel(heart.getSmile());
					break;
				case exitChoice:
					System.out.println("Exited manual demo mode");
					break;
				default:
					System.out.println("That is not an option.");
					break;
			}

			if (emotionChoice != exitChoice)
				RuppetControl.pause(); // Display the specified emotion for a couple of seconds

		} while(emotionChoice != 9); // end of do while loop 

	} // end of manualEmotionDemoMode
	
	/**
	 * Allows the user to determine which FAU they would like the {@code Ruppet} to display 
	 * on command using a CLI.
	 */

	private final void manualFAUDemoMode() {
		
		int fauChoice = -1;

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
				fauChoice = RuppetControl.reader.nextInt();
				switch(fauChoice) {
					case 1:
						eyebrows.toUpperBound();
						break;
					case 2:
						eyebrows.toNeutral();
						break;
					case 3:
						eyebrows.toLowerBound();
						break;
					case 4:
						lowerJaw.toUpperBound();
						break;
					case 5:
						lowerJaw.toNeutral();
						break;
					case 6:
						lowerJaw.toLowerBound();
						break;
					case 7:
						lipCorners.toUpperBound();
						break;
					case 8:
						lipCorners.toNeutral();
						break;
					case 9:
						lipCorners.toLowerBound();
						break;
					case 10:
						eyelids.toLowerBound(); // The LowerBound and UpperBound are switched for the eyelids due to servo orientation.
						break;
					case 11:
						eyelids.toNeutral();
						break;
					case 12:
						eyelids.toUpperBound();
						break;
					default:
						System.out.println("That is not an available option");
						break;
				} // end of switch
			} catch (InputMismatchException ex) {
				RuppetControl.reader.nextLine();
				fauChoice = -1;
			}
		} while (fauChoice != 20); 

	} // end of manualFAUMode
	
	/**
	 * Runs the scripted demo that our friend Steve was so kind to help us with. :)
	 * 
	 * @param voice The {@code Voice} object that allows the {@code Ruppet} to talk.
	 */

	private final void runSteveScripts() {
		System.out.println();
		MidiConnection.getSequencer().stop();
		MidiConnection.getSequencer().setMicrosecondPosition(0);
		voice.givePresentation();
	} // end of runSteveScript
	
	/**
	 * Allows the {@code Ruppet} to display the {@code Emotion} that it thinks that the human is displaying.
	 * 
	 * @param heart The {@code Heart} associated with the {@code Ruppet}.
	 */

	private final void mirrorMode() {
		do {
			switch(getCurrentEmotion()) {
				case "NEUTRAL":
					heart.feel(heart.getNeutral());
					break;
				case "HAPPY":
					heart.feel(heart.getHappy());
					break;
				case "SAD":
					heart.feel(heart.getSad());
					break;
				case "ANGRY":
					heart.feel(heart.getAngry());
					break;
				default:
					heart.feel(heart.getNeutral());
					break;
			} // end of switch
		} while(hasFace()); // end of do while loop
	} // end of mirrorMode
	
	/**
	 * Terminates the connections made with the USB MIDI device and {@code Sequencer} while also
	 * terminating the program. Note that the Shutdown Hookd is always called when the 
	 * program is terminated using System.exit(0);.
	 */

	private final void goToSleep() {
		System.out.println("\nOkay, I was getting tired anyway.");
		RuppetControl.pause();
		System.exit(0);
	} 
	
	/**
	 * Fills the blinking {@code Track} with MIDI data. The blinking effect is currently created by
	 * turning on and off the two white LED's in the {@Ruppet}'s eyes.
	 * 
	 * @param blinkingTrack The {@code Track} that contains the timings for the blinking.
	 */

	private final void fillBlinkTrack(final Track blinkingTrack) {

		final int blink_length = 200;
		final int max_blink_wait = 3500;
		final ShortMessage[] eyelidsUp = this.eyelids.getUpperBoundState();
		final ShortMessage[] eyelidsDown = this.eyelids.getLowerBoundState();

		int prev_blink_time = 0;
		int next_blink_time;
		
		for(int i = 0; i < 500; ++i) {
			next_blink_time = RuppetControl.randInt(prev_blink_time, prev_blink_time + max_blink_wait);
		
			eyelids.addStateToTrack(blinkingTrack, eyelidsDown, prev_blink_time);
			eyelids.addStateToTrack(blinkingTrack, eyelidsUp, next_blink_time);
				
			prev_blink_time = next_blink_time + blink_length;
		} 

	} // end of fillBlinkTrack

	/**
	 *	A ShutdownHook is implemented to prevent the motors from potentially straining themselves. 
	 *	If one or more of the motors are being operated and the program gets unexpectedly canceled, 
	 *	there are no note off MIDI messages to shut them off! Therefore they just keep on running until a 
	 *	note off message is received. This class makes sure that no matter what happens 
	 *	everything gets shut off when the program ends. 
	 *			
	 *	*NOTE*: The master reset button for the PIC can be pressed to accomplish the same thing,
	 *	however	the ShutdownHook prevents the need for that. And why do something yourself when	
	 *	you can get a computer to do it for you?
	*/

	private final class ReleaseSoul extends Thread {

		public final void run() {
			System.out.println();
			RuppetControl.DeSoloAllTracks(tracks);
			RuppetControl.reader.close();
			for (Part part : parts) {
				if (part instanceof Movable)
					( (Movable) part ).toNeutral();
				else if (part instanceof Lights)
					( (Lights) part ).off();
			}
			MidiConnection.closeUsbMidiDevice();
			MidiConnection.closeSequencer();
		}

	} // end of ReleaseSoul

} // end of Ruppet class