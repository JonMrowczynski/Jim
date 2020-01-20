/*
 * MIT License
 *
 * Copyright (c) 2013-2019 Jon Mrowczynski
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

package canisius.jim.parts;

import canisius.jim.connections.SequencerConnection;
import canisius.jim.ruppet.Ruppet;

import javax.sound.midi.Sequence;
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

/**
 * Allows a {@code Ruppet} to talk by reading timing information from a TXT file and storing that information into a
 * {@code Track} with the corresponding {@code MidiEvent}s and synchronously plays the {@code Track} with a WAV file.
 * A {@code Voice} uses the {@code lowerJaw} of the {@code Ruppet} to speak, rendering it unavailable for
 * {@code Emotion}al expressions.
 * 
 * @author Jon Mrowczynski
 */
public final class Voice extends SoftwarePart {

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
	 * @param ruppet that this {@code Voice} belongs to
	 * @param actions that is used to create a {@code Track} that stores all of the timing information for the mouth
	 *                movements
	 * @throws NullPointerException if {@code ruppet} or {@code actions} is {@code null}
	 */
    public Voice(final Ruppet ruppet, final Sequence actions) throws NullPointerException {
    	super(ruppet, actions, "MouthMovementTimes.txt");
    	this.ruppet = ruppet;
		readTimingInfoFromFile();
		openAudioFile();
		setupTimings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void readTimingInfoFromFile() {
		try (final var reader = new Scanner(new FileReader(transitionTimesFile))) {
			final var sec_to_ms_factor = 1000;
			while (reader.hasNextLine()) {
				var splitLine = reader.nextLine().split("\t");
				mouthDownTimes.add((int) Math.round(Double.parseDouble(splitLine[0]) * sec_to_ms_factor));
				splitLine = reader.nextLine().split("\t");
				mouthUpTimes.add((int) Math.round(Double.parseDouble(splitLine[0]) * sec_to_ms_factor));
			}
		} catch (FileNotFoundException e) { e.printStackTrace(); }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void setupTimings() {
		final var mouth = ruppet.getLowerJaw();
		final var delay_end_of_seq = 10000;
		final var mouthDown = mouth.getLowerBoundState();
		final var mouthUp = mouth.getUpperBoundState();
		mouthDownTimes.forEach(time -> mouth.addStateToTrack(track, mouthDown, time));
		mouthUpTimes.forEach(time -> mouth.addStateToTrack(track, mouthUp, time));
		/*
		 * Add a buffer to prevent one or more audio/visual blips at the end of the presentation. This prevents the
		 * sequence from being looped to early.
		 */
		final var latestTime = mouthUpTimes.get(mouthUpTimes.size() - 1);
		mouth.addStateToTrack(track, mouthUp, latestTime + delay_end_of_seq);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final int getNumberOfTransitions() { return mouthDownTimes.size() + mouthUpTimes.size(); }

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
		final var us_to_ms_factor = 1000;
		final var sequencer = SequencerConnection.getInstance().getMidiDevice();
		ruppet.muteAllTracks(ruppet.getHeart().getTrack(), track);
		clip.stop();
		sequencer.stop();
		clip.setMicrosecondPosition(0);
		sequencer.setMicrosecondPosition(0);
		clip.start();
		sequencer.start();
		Ruppet.pause_ms((int) (clip.getMicrosecondLength() / us_to_ms_factor));
		ruppet.muteAllTracks(ruppet.getBlinkingTrack());
	}

} // end of Voice