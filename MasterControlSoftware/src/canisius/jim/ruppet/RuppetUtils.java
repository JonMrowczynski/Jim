package canisius.jim.ruppet;

import canisius.jim.connections.MidiSequencer;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.io.File;
import java.io.IOException;
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

public final class RuppetUtils {
	
	/**
	 * The {@code byte} that represents channel one.
	 */
	
	public static final byte CHAN_1 = 0;
	
	/**
	 * The maximum MIDI velocity value.
	 */
	
	public static final byte MAX_VELOCITY = 10;
	
	/**
	 * The minimum MIDI velocity value.
	 */
	
	public static final byte MIN_VELOCITY = 0;

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

	public static void pause_ms(final int ms) {
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

	public static MidiEvent makeEvent(final ShortMessage msg, final int tick) { return new MidiEvent(msg, tick); }
	
	/**
	 * Gets the MIDI note that is associated with the given {@code ShortMessage}.
	 * 
	 * @param msg The {@code ShortMessage} whose MIDI note will be returned
	 * @return The MIDI note of the {@code ShortMessage}
	 */

	public static byte getMidiNote(final ShortMessage msg) { return (byte) msg.getData1(); }
	
	/**
	 * Gets the velocity value associated with the passed in {@code ShortMessage}.
	 * 
	 * @param msg The {@code ShortMessage} whose velocity value will be returned
	 * @return The velocity value of the {@code ShortMessage}.
	 */
	
	public static byte getVelocityVal(final ShortMessage msg) { return (byte) msg.getData2(); }
		
	/**
	 * Mutes every single {@code Track} that is stored in the {@code Ruppet}'s {@code Sequence}.
	 * 
	 * @param tracks The {@code Track}s that are to be muted
	 */

	static void muteAllTracks(final List<Track> tracks) {
	    tracks.forEach(track -> MidiSequencer.getInstance().getMidiDevice().setTrackMute(tracks.indexOf(track), true));
	}
		
	/**
	 * Sets all of the {@code Track}s in the {@code Ruppet}'s {@code Sequence} to not soloed.
	 * 
	 * @param tracks that are to be set to not soloed.
	 */

	static void deSoloAllTracks(final List<Track> tracks) {
	    tracks.forEach(track -> MidiSequencer.getInstance().getMidiDevice().setTrackSolo(tracks.indexOf(track), false));
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
            if (currentTrackIndex == soloTrackIndex) { MidiSequencer.getInstance().getMidiDevice().setTrackSolo(soloTrackIndex, true); }
            else { MidiSequencer.getInstance().getMidiDevice().setTrackMute(currentTrackIndex, true); }
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

	public static void checkFileExistence(final File file) throws IOException {
		System.out.println();
		String saveFileName = file.getName();
		if(file.createNewFile()) {
			System.out.println("Save File: " + saveFileName + " not found...\n");
			System.out.println("\tCreated new Save File " + saveFileName + ".");
		} else { System.out.println("Save File: " + saveFileName + " found."); }
	}

    /**
     * Returns the {@code File}'s extension.
     *
     * @param file whose extension is to be returned.
     * @return a {@code String} of the {@code File}'s extension.
     */

	public static String getFileExtension(final File file) {
		final String fileName = file.getName();
		final int i = fileName.lastIndexOf('.');
		return i > 0 ? fileName.substring(i) : "";
	}

} // end of class RuppetUtils