package canisius.jim;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 *	This class contains the constants that are required to make the MidiEvents that fill the 
 *	various Tracks. These Midi Events are made out of a ShortMessage along with a timestamp. 
 *	However, for our current purposes, we do not need to worry about implementing a timestamp. 
 *	This class also contains some of the helper methods that allow the other classes and their 
 *	corresponding objects to function properly.
 *
 *	Currently this class also contains methods that allow the retrieval and manipulation of timing 
 *	data from text files as well as .wav files. This is currently kind of sloppy and in its
 *	current form is somewhat difficult to expand on easily. I was thinking of either storing 
 *	the information of the Files such as their names in a either a CSV file or use something
 *	similar to an SQL database. That way, other data can be stored such as the list of MIDI 
 *	notes that are being used to control the servo motors of the robot.
 *	
 *	@author Jon Mrowczynski
 *	@version 1.2
 */

public class RuppetControl {
	
	/**
	 * A {@code byte} that represents the channel number. In our case, we use channel one to
	 * send MIDI message to the {@code Ruppet}.
	 */
	
	public static final byte CHAN_1 = 0; //For channel 1
	
	/**
	 * The maximum velocity value that the PIC is able to interpret successfully.
	 */
	
	public static final byte MAX_VELOCITY = 10; 
	
	/**
	 * The minimum velocity value that the PIC is able to interpret successfully.
	 */
	
	public static final byte MIN_VELOCITY = 0;
	
	/**
	 * The MIDI note that is associated with the servo motor that controls the {@code Ruppet}'s eyebrow.
	 */

	public static final byte EYEBROW = 0x3C; // Servo 1 C4
	
	/**
	 * The MIDI note that is associated with the servo motor that controls the {@code Ruppet}'s left lip corner.
	 */
	
	public static final byte LEFT_LIP_CORNER = 0x3E; // Servo 2 D4
	
	/**
	 * The MIDI note that is associated with the servo motor that controls the {@code Ruppet}'s right lip corner.
	 */
	
	public static final byte RIGHT_LIP_CORNER = 0x40; // Servo 3 E4
	
	/**
	 * The MIDI note that is associated with the servo motor that controls the {@code Ruppet}'s lower jaw.
	 */
	
	public static final byte LOWER_JAW = 0x43; // Servo 4 G4
	
	/**
	 * The MIDI note that is associated with the servo motor that controls the {@code Ruppet}'s eyelids.
	 */
	
	public static final byte EYELIDS = 0x45; // Servo 5 A4
	
	/**
	 * The MIDI note that is associated with the sixth servo motor. Currently this is not used in our {@code Ruppet}.
	 */
	
	public static final byte SERVO6 = 0x48; // Servo 6 C5
	
	/**
	 * The MIDI note that is associated with the eye lights of the {@code Ruppet}.
	 */
	
	public static final byte LIGHTS = 0x4A; // Lights D5
	
	/**
	 * The ending point in the PWM period where the PIC is able to set a servo output pin to high.
	 */

	public static final short MAX_SERVO_VAL = 190;
	
	/**
	 * The starting point in the PWM period where the PIC is able to set a servo output pin to high.
	 */
	
	public static final short MIN_SERVO_VAL = 180;
	
	/**
	 * The table that associated a save file with its name.
	 */

	public static final Hashtable<File, String> saveFiles = new Hashtable<>();
	
	public static final Scanner reader = new Scanner(System.in);
	
	/**
	 * The RuppetControl class in not designed to be instantiated.
	 */

	private RuppetControl() { throw new AssertionError("Should not instantiate a RuppetControl object"); }
	
	/* Sets the Sequencer up such that one can start to add Tracks to the Sequence.
		Note that the number of ticks the values Sequence.PPQ, 160, and 375 (the first
		two values of which can be found in the Ruppet class's constructor) were 
		chosen based on the formula:

			ticksPerSecond = resolution * (currentTempoInBeatsPerMinute / 60.0)

		such that we would get 1 tick per ms.

			ticksPerSecond = 160 * (375 / 60.0) = 1,000(ticks/s) = 1(tick/ms)
	*/

	public static final void initConnections() {
		MidiConnection.establishUsbMidiConnection();
		MidiConnection.establishSequencerConnection();
		MidiConnection.getSequencer().setTempoInBPM(375);
		MidiConnection.getSequencer().setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
	}
	
	/* Makes sure that all of the saves files are there so that data can be read from such
	   that Midi Events can be created and added to the corresponding Tracks */

	public static final void initSaveFiles() {
		saveFiles.put(Heart.emoteSaveFile, ".txt");
		saveFiles.put(Voice.voiceSaveFile, ".txt");
		saveFiles.put(Voice.audioSaveFile, ".wav");
	}	

	/* Halts the program for a user defined amount of time in milliseconds */
	
	/**
	 * Halts the program for a user defined amount of time in milliseconds.
	 * 
	 * @param ms The amount of time that the program should pause for in milliseconds
	 */

	public static final void pause_ms(final int ms) {
		try { Thread.sleep(ms); } 
		catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
	}
	
	/**
	 * Halts the program for 2 seconds.
	 */

	public static void pause() { pause_ms(2000); }
		
	/**
	 * Halts the program for a user defined amount of time in seconds.
	 * 
	 * @param sec The amount of time that the program should pause for in seconds
	 */

	public static void pause(final double sec){
		final long ms = (Math.round(sec * 1000));
		try { Thread.sleep(ms); } 
		catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
	}
	
	/**
	 * 
	 * @param msg
	 * @param tick
	 * @return
	 */

	public static final MidiEvent makeEvent(final ShortMessage msg, final int tick) { return new MidiEvent(msg, tick); }
	
	/**
	 * Gets the MIDI note that is associated with the passed in {@code ShortMessage}.
	 * 
	 * @param msg The {@code ShortMessage} whose MIDI note will be returned
	 * @return The MIDI note of the {@code ShortMessage}
	 */

	public static final byte getMidiNote (final ShortMessage msg) { return (byte) msg.getData1(); }
	
	/**
	 * Gets the velocity value aassociated with the passed in {@code ShortMessage}.
	 * 
	 * @param msg The {@code ShortMessage} whose velocity value will be returned
	 * @return The velocity value of the {@code ShortMessage}.
	 */
	
	public static final byte getVelocityVal (final ShortMessage msg) { return (byte) msg.getData2(); }

	/* Add a state to an array list by adding all of the ShortMessages contained within
	   the state to the ArrayList */
	
	/**
	 * Adds a state to a {@code List} by adding all of the {@code ShortMessage}s contained 
	 * within the state to the {@code ArrayList}.
	 * 
	 * @param states The {@code ArrayList} that (a) {@code ShortMessage}(s) will be added to
	 * @param state The array of {@code ShortMessages} that will be added to the {@code ArrayList}.
	 */

	public static final void addStateToList(final List<ShortMessage> states, final ShortMessage[] state) {
		for (final ShortMessage msg : state) states.add(msg);
	}

	/* Uses a partial sum of a converging series to get the mouth movements closer together. 
	   Converges time 1 to time 2 */
	
	/**
	 * A partial sum of a converging series is used to get the mouth movements closer together. Converges
	 * time {@code t1} to {@code t2}.
	 * 
	 * @param t1 The time that is to be converged with time {@code t2}
	 * @param t2 The time that is to be converged to.
	 * @return The new converged time
	 */

	public static final int convergeTimes(final int t1, final int t2) { return t1 + (t2 / 2) + (t2 / 4) + (t2 / 8) + (t2 / 16); }
		
	/**
	 * Mutes every single {@code Track} that is stored in the {@code Ruppet}'s {@code Sequence}.
	 * 
	 * @param tracks The {@code Track}s that are to be muted
	 */

	public static final void MuteAllTracks(final List<Track> tracks) {
		for (final Track track : tracks) 
			MidiConnection.getSequencer().setTrackMute(tracks.indexOf(track), true); 
	}
		
	/**
	 * Makes sure that all of the {@code Track}s in the {@code Ruppet}'s {@code Sequence} is not soloed
	 * 
	 * @param tracks The {@code Track}s that are to be set to not soloed.
	 */

	public static final void DeSoloAllTracks(final List<Track> tracks) {
		for (final Track track : tracks) 
			MidiConnection.getSequencer().setTrackSolo(tracks.indexOf(track), false); 
	} 
	
	/*  Has a similar functionality as the DeSoloAllTracks method, however this method keeps the 
	   eye Track soloed */
	
	/** 
	 * Makes sure that all of the {@code Track}s in the {@code Ruppet}'s {@code Sequence} is not soloed
	 * except for one specified {@code Track}.
	 * 
	 * @param tracks The {@code Track}s that are to be set to not soloed
	 * @param soloTrack The one {@code Track} that is to be left alone
	 */

	public static final void DeSoloAll_ExceptEyes(final List<Track> tracks, final Track soloTrack) {
		final int soloTrackIndex = tracks.indexOf(soloTrack);
		int currentTrackIndex;
		for(final Track track : tracks) {
			currentTrackIndex = tracks.indexOf(track);
			if (currentTrackIndex == soloTrackIndex) 
				MidiConnection.getSequencer().setTrackSolo(soloTrackIndex, true); 
			else 
				MidiConnection.getSequencer().setTrackMute(currentTrackIndex, true);
		}
	}

	/**
	 * Creates a random {@code int} based on the given upper and lower bounds. This method is 
	 * mostly used to create the blinking effect. 
	 * <P>
	 * *NOTE*: That {@code nextInt} excludes the upper bound, so you must add 1 if you want to include that 
	 * in the set of randomly generated {@code int}s.
	 * 
	 * @param min The maximum {@code int} value that should be randomly generated
	 * @param max The minimum {@code int} value that should be randomly generated
	 * @return A randomly generated {@code int} value between the values {@code min} and {@code max} inclusive
	 */

	public static final int randInt(final int min, final int max) { return ( new Random() ).nextInt( (max - min) + 1 ) + min; }
		
	/* Checks to see if the save file exists, if it does, read in the information in
	   the file, otherwise, create the file and ask for a file name to store in the save file */

	public static final void checkSaveFile(final File saveFile) throws IOException {
		System.out.println();
		String saveFileName = saveFile.getName();
		if(saveFile.createNewFile()) {
			System.out.println("Save File: " + saveFileName + " not found...\n");
			System.out.println("\tCreated new Save File " + saveFileName + ".");
		} else 
			System.out.println("Save File: " + saveFileName + " found.");
	}
	
	/* This method currently reads the name of the data file from the save file
	  or asks the user to input the name of the data file if there is none stored */

	public static final File getDataFile(final File saveFile, final Hashtable<File, String> saveFiles) {
		File dataFile = null;
		System.out.println();
		if (saveFile.length() == 0) {
			System.out.println("Please enter in the name of the file you wish to store in " + saveFile.getName());
			System.out.print("\nName of Data File: ");
			dataFile = new File(reader.next().trim());
		} else {
			try (final Scanner autoReader = new Scanner(new FileReader(saveFile))) { dataFile = new File(autoReader.next().trim()); }
			catch (FileNotFoundException ex) { ex.printStackTrace(); }
		}
		return checkForDataFile(dataFile, saveFile, saveFiles);
	}
	
	/**
	 * 
	 * 
	 * @param saveFile
	 */
	
	public static final void clearSaveFile(final File saveFile) {
		try (final PrintWriter writer = new PrintWriter(saveFile.getName())) { writer.print(""); } 
		catch (FileNotFoundException ex) { ex.printStackTrace(); }
	}
	
	/* Checks to see if the data file exists, if so return it, else return the File from another 
	   call from this method. (Pretty much keep on looping through this method until a File
	   with the name that the user specified is found and then return that File) */

	private static final File checkForDataFile(File dataFile, final File saveFile, final Hashtable<File, String> saveFiles) {
		if(dataFile.exists() && (getFileExtension(dataFile).equals(saveFiles.get(saveFile)))) {
			System.out.println("\tData File: " + dataFile.getName() + " found.");
			saveDataFile(dataFile, saveFile);
			return dataFile;
		} else if(dataFile.exists() && (!getFileExtension(dataFile).equals(saveFiles.get(saveFile)))) {
			System.out.println("\n\tData File: " + dataFile.getName() + " found, but the extensions" + " don't match.");
			System.out.println("\nMake sure you are using a file with extension: " + saveFiles.get(saveFile));
			clearSaveFile(saveFile);
			return getDataFile(saveFile, saveFiles);
		} else {
			System.out.println("\n\tData File: " + dataFile.getName() + " not found...");
			System.out.print("\nPlease enter in the name of the Data File: ");
			dataFile = new File(reader.next().trim());
			return checkForDataFile(dataFile, saveFile, saveFiles);
		}
	}
	
	/* Write the name of the data file to the corresponding save file */

	private static final void saveDataFile(final File dataFile, final File saveFile) {
		try (final PrintWriter writer = new PrintWriter(saveFile.getName())) { writer.print(dataFile.getName()); }
		catch (FileNotFoundException ex) { ex.printStackTrace(); }
	}
	
	/* Gets the file extension of the file. */

	public static final String getFileExtension(final File file) {
		final String fileName = file.getName();
		final int i = fileName.lastIndexOf('.');
		return i > 0 ? fileName.substring(i) : "";
	}

} // end of class RuppetControl