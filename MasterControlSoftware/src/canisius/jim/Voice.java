package canisius.jim;

import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Allows a {@code Ruppet} to talk by reading the timing information from a text file and storing 
 * the timing information into a {@code Track} with the corresponding MIDI events and plays the 
 * {@code Track} synced with a .wav audio file. The {@code Voice} class implements the use of the
 * lower jaw of the {@code Ruppet} to speak rendering it unavailable for emotional expressions.
 * 
 * @author Jon Mrowczynski
 */

final class Voice {

	/**
	 * Stores the name of the {@code File} that contains the timing information for the mouth movements.
	 */

	private static final File voiceSaveFile = new File("VoiceSaveFile.txt");

	/**
	 * Stores the name of the {@code File} that contains the audio that is to be played synchronously with the mouth
	 * movements.
	 */

	private static final File audioSaveFile = new File("AudioSaveFile.txt");

	/**
	 * The {@code Ruppet} that this {@code Voice} belongs to.
	 */

	private final Ruppet ruppet;

	/**
	 * Plays the audio file that has been pre-loaded.
	 */

	private Clip clip = null;

	/**
	 * The {@code Track} that stores the timing information for the {@code Ruppet}'s mouth movements based on the
	 * timing information gathered from the {@code List}s {@code down} and {@code up}.
	 */

	private final Track voiceTrack;
	
	/* *NOTE*: The timeOpen and timeClose ArrayLists are used to help with making the mouth
	   movements more continuous and less sudden by determining when to turn the motors off	
	   before turning them back on to move the mouth up or down. 
	   The method RuppetUtils.convergeTimes helps to converge the motor off time closer
	   to the motor on time so that it makes the Ruppet's mouth movements seem less robotic */

	/**
	 * Stores the timing information in ms for mouth down movements.
	 */

	private final List<Integer> down = new ArrayList<>();

	/**
	 * Stores the timing information in ms for mouth up movements.
	 */

	private final List<Integer> up = new ArrayList<>();

	/**
	 *
	 */

	private final List<Integer> timeOpen = new ArrayList<>();

	/**
	 *
	 */

	private final List<Integer> timeClose = new ArrayList<>();

	/**
	 * Constructs a {@code Voice} that can be used to make a {@code Ruppet} talk.
	 *
	 * @param ruppet that this {@code Voice} belongs to.
	 * @param actions {@code Track} that will contain timing information for the mouth movements.
	 */

	Voice (final Ruppet ruppet, final Sequence actions) {
		this.ruppet = ruppet;
		voiceTrack = actions.createTrack();
		readTimingInfoFromFile();
		openAudioFile();
		setupTimings();
	}

	/**
	 * Reads timing information from a {@code File} that contains {@code double}s representing mouth movements in
	 * seconds. These times are converted into ms and stored in {@code Integer} {@code List}s for later use in the
	 * setupTimings method.
	 */

	private void readTimingInfoFromFile() {
		try { RuppetUtils.checkFileExistence(voiceSaveFile); }
		catch (IOException e) { e.printStackTrace(); }
		try (final Scanner reader = new Scanner(new FileReader(voiceSaveFile))) {
			final int sec_to_ms_factor = 1000;
			while (reader.hasNext()) {
				down.add((int) Math.round(reader.nextDouble() * sec_to_ms_factor)); // read starting value
				reader.nextDouble();
				reader.nextLine();
				up.add((int) Math.round(reader.nextDouble() * sec_to_ms_factor));
				reader.nextDouble();
				reader.nextLine();
			}
		} catch (FileNotFoundException e) { e.printStackTrace(); }
		/* These times are in milliseconds in order to make the MidiEvents properly */
		for (int i = 0; i < down.size(); ++i) {
			timeOpen.add(up.get(i) - down.get(i));
			if (i + 1 < down.size()) { timeClose.add(down.get(i + 1) - up.get(i)); }
		}
		timeClose.add(2000);
	}

	/**
	 * Uses the data stored in the {@code List}s to create MIDI {@code Track}s with the corresponding
	 * {@code MidiEvents}.
	 */

	private void setupTimings() {
		final Movable mouth = ruppet.getLowerJaw();
		final int delay_end_of_seq = 10000;
		final ShortMessage[] mouthDown = mouth.getLowerBoundState();
		final ShortMessage[] mouthUp = mouth.getUpperBoundState();
		for (int i = 0; i < timeClose.size(); ++i) {
			mouth.addStateToTrack(voiceTrack, mouthDown, down.get(i));
			mouth.addStateToTrack(voiceTrack, mouthUp, up.get(i));
		}
		/*
		 * Add another two tracks to prevent one or more audio/visual blips at the end of the presentation. This
		 * prevents the sequence from being looped to early.
		 */
		mouth.addStateToTrack(voiceTrack, mouthDown, (down.get(down.size() - 1) + delay_end_of_seq));
		mouth.addStateToTrack(voiceTrack, mouthUp, (down.get(down.size() - 1) + delay_end_of_seq));
	}

	/**
	 * Pre-loads the user defined audio file.
	 */

	private void openAudioFile() {
		try { RuppetUtils.checkFileExistence(audioSaveFile); }
		catch (IOException e) { e.printStackTrace(); }
		try {
			clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(audioSaveFile));
		} catch (LineUnavailableException | IOException e) { e.printStackTrace(); }
		  catch (UnsupportedAudioFileException e) {
			System.out.println("ERROR:");
			System.out.println("\nFile: " + audioSaveFile.getName() + " is not supported!");
			System.out.println("Make sure that you are using a .wav file!");
			RuppetUtils.clearSaveFile(audioSaveFile);
		}
    }

	/**
	 * Play the emotions and mouth movement {@code Track}s allowing the {@code Ruppet} to speak.
	 */

	void givePresentation() {
		final int us_to_ms_factor = 1000;
		MidiConnection.getSequencer().setTrackSolo(ruppet.getTracks().indexOf(ruppet.getHeart().getEmotionTrack()), true);
		MidiConnection.getSequencer().setTrackSolo(ruppet.getTracks().indexOf(voiceTrack), true);
		clip.stop();
		MidiConnection.getSequencer().stop();
		clip.setMicrosecondPosition(0);
		MidiConnection.getSequencer().setMicrosecondPosition(0);
		clip.start();
		MidiConnection.getSequencer().start();
		RuppetUtils.pause_ms( (int) (clip.getMicrosecondLength() / us_to_ms_factor));
		MidiConnection.getSequencer().setTrackSolo(ruppet.getTracks().indexOf(ruppet.getHeart().getEmotionTrack()), false);
		MidiConnection.getSequencer().setTrackSolo(ruppet.getTracks().indexOf(voiceTrack), false);
	}

	/**
	 * Gets the {@code Track} that contains all of the {@code lowerJaw} movement timings.
	 *
	 * @return The {@code Track} that stores the {@code lowerJaw} movement timings.
	 */

	final Track getVoiceTrack() { return voiceTrack; }
	
} // end of Voice class