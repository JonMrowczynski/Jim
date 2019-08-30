package canisius.jim.parts;

import canisius.jim.connections.SequencerConnection;
import canisius.jim.ruppet.Ruppet;

import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
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
 * A {@code Voice} uses the {@code lowerJaw} of the {@code Ruppet} to speak, rendering it unavailable for
 * {@code Emotion}al expressions.
 * 
 * @author Jon Mrowczynski
 */

public final class Voice {

	/**
	 * The {@code File} that contains the timing information for the mouth movements.
	 */

	private static final File mouthMovementTimesFile = new File("MouthMovementTimes.txt");

	/**
	 * The {@code File} that contains the audio that is to be played synchronously with the mouth movements.
	 */

	private static final File VoiceFile = new File("Voice.wav");

	/**
	 * The {@code Ruppet} that this {@code Voice} belongs to.
	 */

	private final Ruppet ruppet;

	/**
	 * Plays the audio file.
	 */

	private Clip clip = null;

	/**
	 * The {@code Track} that stores the timing information for the {@code Ruppet}'s mouth movements based on the timing
     * information gathered from the {@code List}s {@code mouthDownTimes} and {@code mouthUpTimes}.
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
	 * Constructs a {@code Voice} that can be used to make a {@code Ruppet} talk by setting up the audio file and the
	 * mouth movement timings {@code Track} for synchronous playing.
	 *
	 * @param ruppet that this {@code Voice} belongs to.
	 * @param actions that is used to create a {@code Track} that stores all of the timing information for the mouth
	 *                movements.
	 */

    public Voice(final Ruppet ruppet, final Sequence actions) {
		this.ruppet = ruppet;
		voiceTrack = actions.createTrack();
		readTimingInfoFromFile();
		openAudioFile();
		setupTimings();
	}

	/**
	 * Reads timing information from a {@code File} that contains {@code double}s representing when mouth movements
	 * should occur from start in seconds. These times are converted into ms and stored in {@code Integer} {@code List}s
	 * for later use in the {@code setupTimings} method.
	 *
	 * @see #setupTimings()
	 */

	private void readTimingInfoFromFile() {
		try (final Scanner reader = new Scanner(new FileReader(mouthMovementTimesFile))) {
			final int sec_to_ms_factor = 1000;
			while (reader.hasNextLine()) {
				String[] splitLine = reader.nextLine().split("\t");
				mouthDownTimes.add((int) Math.round(Double.parseDouble(splitLine[0]) * sec_to_ms_factor));
				splitLine = reader.nextLine().split("\t");
				mouthUpTimes.add((int) Math.round(Double.parseDouble(splitLine[0]) * sec_to_ms_factor));
			}
		} catch (FileNotFoundException e) { e.printStackTrace(); }
	}

	/**
	 * Uses the data stored in {@code mouthDownTimes} and {@code mouthUpTimes} to create {@code Track}s with
	 * {@code MidiEvents} such that the mouth movements can be sequenced to have a {@code Ruppet} run a script.
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
	 * Loads the audio file for later playback.
	 */

	private void openAudioFile() {
		try {
			clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(VoiceFile));
		} catch (LineUnavailableException | IOException e) { e.printStackTrace(); }
		  catch (UnsupportedAudioFileException e) {
			System.out.println("ERROR:");
			System.out.println("\n\"" + VoiceFile.getName() + "\"'s file type is not supported!");
			System.out.println("Make sure that you are using a .wav file!");
		}
    }

	/**
	 * Play the {@code Emotion}s and mouth movement {@code Track}s allowing the {@code Ruppet} to perform a script.
	 */

    public void givePresentation() {
		final int us_to_ms_factor = 1000;
		final Sequencer sequencer = SequencerConnection.getInstance().getMidiDevice();
		final List<Track> ruppetTracks = ruppet.getTracks();
		final Track emotionTrack = ruppet.getHeart().getEmotionTrack();
		sequencer.setTrackSolo(ruppetTracks.indexOf(emotionTrack), true);
		sequencer.setTrackSolo(ruppetTracks.indexOf(voiceTrack), true);
		clip.stop();
		sequencer.stop();
		clip.setMicrosecondPosition(0);
		sequencer.setMicrosecondPosition(0);
		clip.start();
		sequencer.start();
		Ruppet.pause_ms((int) (clip.getMicrosecondLength() / us_to_ms_factor));
		sequencer.setTrackSolo(ruppetTracks.indexOf(emotionTrack), false);
		sequencer.setTrackSolo(ruppetTracks.indexOf(voiceTrack), false);
	}

	/**
	 * Returns the {@code Track} that contains all of the {@code lowerJaw} movement timings.
	 *
	 * @return The {@code Track} that contains all of the {@code lowerJaw} movement timings.
	 */

	public final Track getVoiceTrack() { return voiceTrack; }
	
} // end of Voice