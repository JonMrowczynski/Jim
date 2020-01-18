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

import canisius.jim.Emotion;
import canisius.jim.connections.UsbMidiConnection;
import canisius.jim.ruppet.Ruppet;

import javax.sound.midi.Sequence;
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
public final class Heart extends SoftwarePart {

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
		super(ruppet, actions, "EmotionTransitionTimes.txt");

		final var lowerJaw   = ruppet.getLowerJaw();
		final var lipCorners = ruppet.getLipCorners();
		final var eyebrow 	 = ruppet.getEyebrows();
		final var eyelid 	 = ruppet.getEyelids();

		neutral = new Emotion(ruppet, lowerJaw.getNeutralHardwarePartState(), lipCorners.getNeutralHardwarePartState(), eyebrow.getNeutralHardwarePartState(), eyelid.getNeutralHardwarePartState());
		happy = new Emotion(ruppet, lowerJaw.getUpperBoundHardwarePartState(), lipCorners.getUpperBoundHardwarePartState());
		sad = new Emotion(ruppet, lowerJaw.getUpperBoundHardwarePartState(), lipCorners.getLowerBoundHardwarePartState(), eyebrow.getUpperBoundHardwarePartState());
		angry = new Emotion(ruppet, lowerJaw.getUpperBoundHardwarePartState(), lipCorners.getLowerBoundHardwarePartState(), eyebrow.getLowerBoundHardwarePartState());
		scared = new Emotion(ruppet, lowerJaw.getUpperBoundHardwarePartState(), lipCorners.getLowerBoundHardwarePartState(), eyebrow.getLowerBoundHardwarePartState());
		smile = new Emotion(ruppet, lipCorners.getUpperBoundHardwarePartState());

		readTimingInfoFromFile();
		setupTimings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readTimingInfoFromFile() {
		try {
			Files.readAllLines(transitionTimesFile.toPath()).stream().map(line -> line.split("\t"))
					.forEach(splitLine -> emotionTimingsMap.put((int) Math.round(Double.parseDouble(splitLine[0]) * 1000), splitLine[1].trim()));
		} catch (IOException e) { e.printStackTrace(); }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setupTimings() {
		for (final var entry : emotionTimingsMap.entrySet()) {
			final var emotion = entry.getValue();
			final var emotionTransitionTime = entry.getKey();
			switch (emotion.toUpperCase()) {
				case "HAPPY": 	happy.addEmotionToTrack(track, emotionTransitionTime);       	break;
				case "SAD": 	sad.addEmotionToTrack(track, emotionTransitionTime);           	break;
				case "ANGRY": 	angry.addEmotionToTrack(track, emotionTransitionTime);       	break;
				case "SCARED": 	scared.addEmotionToTrack(track, emotionTransitionTime);     	break;
				case "NEUTRAL": neutral.addEmotionToTrack(track, emotionTransitionTime);   		break;
				case "SMILE": 	smile.addEmotionToTrack(track, emotionTransitionTime);       	break;
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
	 * Display {@code emotion}.
	 * 
	 * @param emotion that the {@code Ruppet} is to feel
	 * @throws NullPointerException if {@code emotion} is {@code null}
	 */
	public final void feel(final Emotion emotion) throws NullPointerException {
		Objects.requireNonNull(emotion, "Cannot feel a null emotion").getAttributes().forEachRemaining(msg -> UsbMidiConnection.getInstance().send(msg));
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

} // end of Heart