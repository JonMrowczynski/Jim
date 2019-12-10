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

import canisius.jim.connections.UsbMidiConnection;
import canisius.jim.ruppet.Ruppet;

import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 *	The {@code Heart} mainly deals with the {@code Emotion}al states of the {@code Ruppet} that can either be expressed
 *	on command or added to a {@code Track} to be sequenced.
 *	
 *	@author Jon Mrowczynski
 */
public final class Heart {
	
	/**
	 * The {@code File} that contains the times and {@code String}s that represent the {@code Emotion}s that should be
	 * transitioned to at the corresponding times.
	 */
	private static final File emotionTransitionTimesFile = new File("EmotionTransitionTimes.txt");

	/**
	 * The neutral {@code Emotion} state of the {@code Ruppet}.
	 */
	private final Emotion neutral;
	
	/**
	 * The happy {@code Emotion} state of the {@code Ruppet}.
	 */
	private final Emotion happy;
	
	/**
	 * The sad {@code Emotion} state of the {@code Ruppet}.
	 */
	private final Emotion sad;
	
	/**
	 * The angry {@code Emotion} state of the {@code Ruppet}.
	 */
	private final Emotion angry;
	
	/**
	 * The scared {@code Emotion} state of the {@code Ruppet}.
	 */
	private final Emotion scared;
	
	/**
	 * The smile "{@code Emotion}" state of the {@code Ruppet}.
	 */
	private final Emotion smile;
		
	/**
	 * The {@code Track} that stores all of the {@code Emotion} expression timings.
	 */
	private final Track emotionTrack;

    /**
     * Maps {@code Integer} times in ms to a {@code String} that represents the {@code Emotion} that should be
	 * transitioned to at the corresponding time.
     */
	private final Map<Integer, String> emotionTimingsMap = new LinkedHashMap<>();

	/**
	 * Sets up each {@code Emotion} using the {@code Part}s of {@code ruppet}. {@code emotionTrack} is also setup
	 * so that {@code Emotion}s can be transitioned to as part of a script.
	 * 
	 * @param ruppet that this {@code Heart} object belongs to
	 * @param actions that is used to create a {@code Track} that stores all of the {@code Emotion} expression timings
	 * @throws NullPointerException if {@code ruppet} or {@code actions} is {@code null}
	 */
    public Heart(final Ruppet ruppet, final Sequence actions) throws NullPointerException {
		Objects.requireNonNull(ruppet, "Cannot initialize a " + Heart.class.getSimpleName() + " with a null ruppet");
		Objects.requireNonNull(actions, "Cannot initialize a " + Heart.class.getSimpleName() + " with null actions");

		final var lowerJaw   = ruppet.getLowerJaw();
		final var lipCorners = ruppet.getLipCorners();
		final var eyebrow 	 = ruppet.getEyebrows();
		final var eyelid 	 = ruppet.getEyelids();

		neutral = new Emotion(ruppet, lowerJaw.getNeutralPartState(), lipCorners.getNeutralPartState(), eyebrow.getNeutralPartState(), eyelid.getNeutralPartState());
		happy = new Emotion(ruppet, lowerJaw.getUpperBoundPartState(), lipCorners.getUpperBoundPartState());
		sad = new Emotion(ruppet, lowerJaw.getUpperBoundPartState(), lipCorners.getLowerBoundPartState(), eyebrow.getUpperBoundPartState());
		angry = new Emotion(ruppet, lowerJaw.getUpperBoundPartState(), lipCorners.getLowerBoundPartState(), eyebrow.getLowerBoundPartState());
		scared = new Emotion(ruppet, lowerJaw.getUpperBoundPartState(), lipCorners.getLowerBoundPartState(), eyebrow.getLowerBoundPartState());
		smile = new Emotion(ruppet, lipCorners.getUpperBoundPartState());

		emotionTrack = actions.createTrack();
		readTimingLabels();
		setupTimings();
	}
	
	/**
	 * Display {@code emotion}.
	 * 
	 * @param emotion that the {@code Ruppet} is to feel
	 * @throws NullPointerException if {@code emotion} is {@code null}
	 */
	public final void feel(final Emotion emotion) throws NullPointerException{
		Objects.requireNonNull(emotion, "Cannot feel a null emotion").getAttributes()
				.forEachRemaining(msg -> UsbMidiConnection.getInstance().send(msg));
	}

	/**
	 * Returns the {@code Ruppet}'s neutral {@code Emotion}.
	 * 
	 * @return The {@code Ruppet}'s neutral {@code Emotion}
	 */
	public final Emotion getNeutral() { return neutral; }
	
	/**
	 * Returns the {@code Ruppet}'s happy {@code Emotion}.
	 * 
	 * @return The {@code Ruppet}'s happy {@code Emotion}
	 */
	public final Emotion getHappy() { return happy;   }
	
	/**
	 * Returns the {@code Ruppet}'s sad {@code Emotion}.
	 * 
	 * @return The {@code Ruppet}'s sad {@code Emotion}
	 */
	public final Emotion getSad() { return sad; }
	
	/**
	 * Returns the {@code Ruppet}'s angry {@code Emotion}.
	 * 
	 * @return The {@code Ruppet}'s angry {@code Emotion}
	 */
	public final Emotion getAngry() { return angry; }
	
	/**
	 * Returns the {@code Ruppet}'s scared {@code Emotion}.
	 * 
	 * @return The {@code Ruppet}'s scared {@code Emotion}
	 */
	public final Emotion getScared() { return scared; }
	
	/**
	 * Returns the {@code Ruppet}'s smile "{@code Emotion}".
	 * 
	 * @return The {@code Ruppet}'s smile "{@code Emotion}"
	 */
	public final Emotion getSmile() { return smile; }
	
	/**
	 * Reads the {@code Emotion} states and corresponding transition timings from a text file and stores that data in
	 * {@code emotionTimingsMap}.
	 */
	private void readTimingLabels() {
		try {
			Files.readAllLines(emotionTransitionTimesFile.toPath()).stream().map(line -> line.split("\t"))
					.forEach(splitLine -> emotionTimingsMap.put((int) Math.round(Double.parseDouble(splitLine[0]) * 1000), splitLine[1].trim()));
		} catch (IOException e) { e.printStackTrace(); }
	}
		
	/**
	 * Sets up all of the {@code Emotion} timings for a prerecorded script.
	 */
	private void setupTimings() {
	    for (final var entry : emotionTimingsMap.entrySet()) {
	        final var emotion = entry.getValue();
	        final var emotionTransitionTime = entry.getKey();
            switch (emotion.toUpperCase()) {
                case "HAPPY": happy.addEmotionToTrack(emotionTrack, emotionTransitionTime);       break;
                case "SAD": sad.addEmotionToTrack(emotionTrack, emotionTransitionTime);           break;
                case "ANGRY": angry.addEmotionToTrack(emotionTrack, emotionTransitionTime);       break;
                case "SCARED": scared.addEmotionToTrack(emotionTrack, emotionTransitionTime);     break;
                case "NEUTRAL": neutral.addEmotionToTrack(emotionTrack, emotionTransitionTime);   break;
                case "SMILE": smile.addEmotionToTrack(emotionTrack, emotionTransitionTime);       break;
                default:
                    System.out.println("\nEMOTION DEFINITION ERROR:");
                    System.out.println("\tEmotion: \"" + emotion + "\" is not currently defined.");
                    System.out.println("\n\tPlease define what to do for \"" + emotion + "\" before operating ruppet.");
                    System.out.println("\nTerminating Program.");
                    System.exit(1);
                    break;
            }
        }
	}

	/**
	 * Returns the {@code Track} that contains all of the {@code Emotion} transition timings.
	 *
	 * @return The {@code Track} that contains all of the {@code Emotion} transition timings
	 */
	public final Track getEmotionTrack() { return emotionTrack; }

} // end of Heart