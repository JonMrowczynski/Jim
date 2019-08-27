package canisius.jim.parts;

import canisius.jim.connections.MidiSequencer;
import canisius.jim.ruppet.Ruppet;
import canisius.jim.ruppet.RuppetUtils;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * Allows a {@code Ruppet} to talk by reading timing information from a TXT file and storing that information into a
 * {@code Track} with the corresponding {@code MidiEvent}s and synchronously plays the {@code Track} with a WAV file.
 * A {@code Voice} uses the {@code lowerJaw} of the {@code Ruppet} to speak rendering it unavailable for emotional
 * expressions.
 * 
 * @author Jon Mrowczynski
 */

public final class Voice {

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
	 * The {@code Track} that stores the timing information for the {@code Ruppet}'s mouth movements based on the timing
     * information gathered from the {@code List}s {@code down} and {@code up}.
	 */

	private final Track voiceTrack;

	/**
	 * Stores the timing information in ms for mouth down movements.
	 */

	private final List<Integer> mouthDownTimes = new LinkedList<>();

	/**
	 * Stores the timing information in ms for mouth up movements.
	 */

	private final List<Integer> mouthUpTimes = new LinkedList<>();

	/**
	 * Constructs a {@code Voice} that can be used to make a {@code Ruppet} talk.
	 *
	 * @param ruppet that this {@code Voice} belongs to.
	 * @param actions {@code Track} that will contain timing information for the mouth movements.
	 */

    public Voice(final Ruppet ruppet, final Sequence actions) {
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
				mouthDownTimes.add((int) Math.round(reader.nextDouble() * sec_to_ms_factor)); // read starting value
				reader.nextDouble();
				reader.nextLine();
				mouthUpTimes.add((int) Math.round(reader.nextDouble() * sec_to_ms_factor));
				reader.nextDouble();
				reader.nextLine();
			}
		} catch (FileNotFoundException e) { e.printStackTrace(); }
	}

	/**
	 * Uses the data stored in the {@code List}s to create MIDI {@code Track}s with the corresponding
	 * {@code MidiEvents}.
	 */

	private void setupTimings() {
		final Movable mouth = ruppet.getLowerJaw();
		final int delay_end_of_seq = 10000;
		final Set<ShortMessage> mouthDown = mouth.getLowerBoundState();
		final Set<ShortMessage> mouthUp = mouth.getUpperBoundState();
		mouthDownTimes.forEach(time -> mouth.addStateToTrack(voiceTrack, mouthDown, time));
		mouthUpTimes.forEach(time -> mouth.addStateToTrack(voiceTrack, mouthUp, time));
		/*
		 * Add a buffer to prevent one or more audio/visual blips at the end of the presentation. This prevents the
		 * sequence from being looped to early.
		 */
		final int latestTime = mouthUpTimes.get(mouthUpTimes.size() - 1);
		mouth.addStateToTrack(voiceTrack, mouthUp, latestTime + delay_end_of_seq);
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
			System.out.println("\nFile type \"" + RuppetUtils.getFileExtension(audioSaveFile) + "\" is not supported!");
			System.out.println("Make sure that you are using a .wav file!");
		}
    }

	/**
	 * Play the emotions and mouth movement {@code Track}s allowing the {@code Ruppet} to speak.
	 */

    public void givePresentation() {
		final int us_to_ms_factor = 1000;
		MidiSequencer.getInstance().getMidiDevice().setTrackSolo(ruppet.getTracks().indexOf(ruppet.getHeart().getEmotionTrack()), true);
		MidiSequencer.getInstance().getMidiDevice().setTrackSolo(ruppet.getTracks().indexOf(voiceTrack), true);
		clip.stop();
		MidiSequencer.getInstance().getMidiDevice().stop();
		clip.setMicrosecondPosition(0);
		MidiSequencer.getInstance().getMidiDevice().setMicrosecondPosition(0);
		clip.start();
		MidiSequencer.getInstance().getMidiDevice().start();
		RuppetUtils.pause_ms((int) (clip.getMicrosecondLength() / us_to_ms_factor));
		MidiSequencer.getInstance().getMidiDevice().setTrackSolo(ruppet.getTracks().indexOf(ruppet.getHeart().getEmotionTrack()), false);
		MidiSequencer.getInstance().getMidiDevice().setTrackSolo(ruppet.getTracks().indexOf(voiceTrack), false);
	}

	/**
	 * Gets the {@code Track} that contains all of the {@code lowerJaw} movement timings.
	 *
	 * @return The {@code Track} that stores the {@code lowerJaw} movement timings.
	 */

	public final Track getVoiceTrack() { return voiceTrack; }
	
} // end of Voice class