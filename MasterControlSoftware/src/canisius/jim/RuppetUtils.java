package canisius.jim;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

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
 */

final class RuppetUtils {
	
	/**
	 * The {@code byte} that represents channel one.
	 */
	
	static final byte CHAN_1 = 0;
	
	/**
	 * The maximum MIDI velocity value.
	 */
	
	static final byte MAX_VELOCITY = 10;
	
	/**
	 * The minimum MIDI velocity value.
	 */
	
	static final byte MIN_VELOCITY = 0;

    /**
     * Used to acquire input from the user when they are prompted by the CLI.
     */

    static final Scanner reader = new Scanner(System.in);
	
	/**
	 * {@code RuppetUtils} is a utility class.
	 */

	private RuppetUtils() { throw new AssertionError("Should not instantiate a RuppetUtils object"); }
	
	/**
	 * Halts the program for a user defined amount of time in milliseconds.
	 * 
	 * @param ms that the program should pause for.
	 */

	static void pause_ms(final int ms) {
		try { Thread.sleep(ms); } 
		catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
	}
	
	/**
	 * Halts the program for 2 seconds.
	 */

	static void pause() { pause_ms(2000); }
	
	/**
	 *
	 * 
	 * @param msg that is to be made into a {@code MidiEvent}.
	 * @param tick that {@code msg} is to be played.
	 * @return the constructed {@code MidiEvent}.
	 */

	static MidiEvent makeEvent(final ShortMessage msg, final int tick) { return new MidiEvent(msg, tick); }
	
	/**
	 * Gets the MIDI note that is associated with the given {@code ShortMessage}.
	 * 
	 * @param msg The {@code ShortMessage} whose MIDI note will be returned
	 * @return The MIDI note of the {@code ShortMessage}
	 */

	static byte getMidiNote (final ShortMessage msg) { return (byte) msg.getData1(); }
	
	/**
	 * Gets the velocity value associated with the passed in {@code ShortMessage}.
	 * 
	 * @param msg The {@code ShortMessage} whose velocity value will be returned
	 * @return The velocity value of the {@code ShortMessage}.
	 */
	
	static byte getVelocityVal (final ShortMessage msg) { return (byte) msg.getData2(); }
	
	/**
	 * A partial sum of a converging series is used to get the mouth movements closer together. Converges
	 * time {@code t1} to {@code t2}.
	 * 
	 * @param t1 The time that is to be converged with time {@code t2}.
	 * @param t2 The time that is to be converged to.
	 * @return The new converged time.
	 */

	public static int convergeTimes(final int t1, final int t2) { return t1 + (t2 / 2) + (t2 / 4) + (t2 / 8) + (t2 / 16); }
		
	/**
	 * Mutes every single {@code Track} that is stored in the {@code Ruppet}'s {@code Sequence}.
	 * 
	 * @param tracks The {@code Track}s that are to be muted
	 */

	static void muteAllTracks(final List<Track> tracks) {
	    tracks.forEach(track -> MidiConnection.getSequencer().setTrackMute(tracks.indexOf(track), true));
	}
		
	/**
	 * Sets all of the {@code Track}s in the {@code Ruppet}'s {@code Sequence} to not soloed.
	 * 
	 * @param tracks that are to be set to not soloed.
	 */

	static void deSoloAllTracks(final List<Track> tracks) {
	    tracks.forEach(track -> MidiConnection.getSequencer().setTrackSolo(tracks.indexOf(track), false));
	}
	
	/** 
	 * Sets all of the {@code Track}s in the {@code Ruppet}'s {@code Sequence} to not soloed except for the given
     * {@code soloTrack}.
	 * 
	 * @param tracks that are to be set to not soloed.
	 * @param soloTrack that is to be left alone.
	 */

	static void deSoloAllTracks_ExceptEyes(final List<Track> tracks, final Track soloTrack) {
		final int soloTrackIndex = tracks.indexOf(soloTrack);
		tracks.forEach(track -> {
            final int currentTrackIndex = tracks.indexOf(track);
            if (currentTrackIndex == soloTrackIndex) { MidiConnection.getSequencer().setTrackSolo(soloTrackIndex, true); }
            else { MidiConnection.getSequencer().setTrackMute(currentTrackIndex, true); }
        });
	}

	/**
     * Returns a random {@code int} between {@code min} and {@code max} inclusive.
	 * 
	 * @param min The maximum {@code int} value that can be randomly generated.
	 * @param max The minimum {@code int} value that can be randomly generated.
	 * @return A random {@code int} between {@code min} and {@code max} inclusive.
	 */

	static int getRandInt(final int min, final int max) { return (new Random()).nextInt((max - min) + 1) + min; }

    /**
     * Checks if a {@code File} exists. If it does not create the {@code File}.
     *
     * @param file whose existence is to be checked.
     * @throws IOException if creating a new {@code File} failed.
     */

	static void checkFileExistence(final File file) throws IOException {
		System.out.println();
		String saveFileName = file.getName();
		if(file.createNewFile()) {
			System.out.println("Save File: " + saveFileName + " not found...\n");
			System.out.println("\tCreated new Save File " + saveFileName + ".");
		} else { System.out.println("Save File: " + saveFileName + " found."); }
	}

    /**
     * Clears the contents of the given {@code File}.
     *
     * @param file that is to be cleared.
     */

    static void clearSaveFile(final File file) {
        try { Files.write(file.toPath(), "".getBytes()); }
        catch (IOException e) { e.printStackTrace(); }
    }
	
	/* This method currently reads the name of the data file from the save file
	  or asks the user to input the name of the data file if there is none stored */

    /**
     * This method reads the name of a file from
     *
     * @param file
     * @param saveFiles
     * @return
     */

	private static File getFile(final File file, final Hashtable<File, String> saveFiles) {
		File dataFile = null;
		System.out.println();
		if (file.length() == 0) {
			System.out.println("Please enter in the name of the file you wish to store in " + file.getName());
			System.out.print("\nName of Data File: ");
			dataFile = new File(reader.next().trim());
		} else {
			try { dataFile = new File(Files.readAllLines(file.toPath()).get(0).trim()); }
			catch (IOException e) { e.printStackTrace(); }
		}
		return checkForDataFile(dataFile, file, saveFiles);
	}
	
	/* Checks to see if the data file exists, if so return it, else return the File from another 
	   call from this method. (Pretty much keep on looping through this method until a File
	   with the name that the user specified is found and then return that File) */

    /**
     *
     *
     * @param dataFile
     * @param saveFile
     * @param saveFiles
     * @return
     */

	private static File checkForDataFile(File dataFile, final File saveFile, final Hashtable<File, String> saveFiles) {
		if (dataFile.exists() && getFileExtension(dataFile).equals(saveFiles.get(saveFile))) {
			System.out.println("\tData File: " + dataFile.getName() + " found.");
			saveFile(dataFile, saveFile);
			return dataFile;
		} else if (dataFile.exists() && !getFileExtension(dataFile).equals(saveFiles.get(saveFile))) {
			System.out.println("\n\tData File: " + dataFile.getName() + " found, but the extensions" + " don't match.");
			System.out.println("\nMake sure you are using a file with extension: " + saveFiles.get(saveFile));
			clearSaveFile(saveFile);
			return getFile(saveFile, saveFiles);
		} else {
			System.out.println("\n\tData File: " + dataFile.getName() + " not found...");
			System.out.print("\nPlease enter in the name of the Data File: ");
			dataFile = new File(reader.next().trim());
			return checkForDataFile(dataFile, saveFile, saveFiles);
		}
	}

    /**
     * Write the name of {@code file} to {@code saveFile}.
     *
     * @param file whose name is to be written to the {@code saveFile}.
     * @param saveFile who is to contain the name of {@code file}.
     */

	private static void saveFile(final File file, final File saveFile) {
        try { Files.write(saveFile.toPath(), file.getName().getBytes()); }
        catch (IOException e) { e.printStackTrace(); }
	}

    /**
     * Returns the {@code File}'s extension.
     *
     * @param file whose extension is to be returned.
     * @return a {@code String} of the {@code File}'s extension.
     */

	private static String getFileExtension(final File file) {
		final String fileName = file.getName();
		final int i = fileName.lastIndexOf('.');
		return i > 0 ? fileName.substring(i) : "";
	}

} // end of class RuppetUtils