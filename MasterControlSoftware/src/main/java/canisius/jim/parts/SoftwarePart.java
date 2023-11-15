/*
 * MIT License
 *
 * Copyright (c) 2013-2023 Jon Mrowczynski
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

import canisius.jim.ruppet.Ruppet;

import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.io.File;
import java.util.Objects;

/**
 * A {@code SoftwarePart} allows for a {@code Ruppet} to time specific actions to carry out during the running of a
 * script.
 *
 * @author Jon Mrowczynski
 */
public abstract class SoftwarePart {
	
	/**
	 * The {@code Track} that stores the timing information for the {@code Ruppet}'s mouth movements based on the
	 * timing
	 * information gathered from the {@code List}s {@code mouthDownTimes} and {@code mouthUpTimes}.
	 */
	protected final Track track;
	
	/**
	 * The name of the {@code File} that contains the timing information for this {@code SoftwarePart} to run for the
	 * script.
	 */
	protected final String fileName;
	
	/**
	 * The {@code File} that contains the timing information for the mouth movements.
	 */
	protected final File transitionTimesFile;
	
	/**
	 * Constructs a {@code SoftwarePart} that can be used to time specific types of actions for the execution of a
	 * script.
	 *
	 * @param ruppet   that this {@code SoftwarePart} belongs to
	 * @param actions  that is used to create a {@code Track} that stores all the timing information
	 * @param fileName of the {@code File} that contains all the timing information for this {@code SoftwarePart}
	 * @throws NullPointerException if {@code ruppet}, {@code actions} or {@code fileName} is null;
	 */
	SoftwarePart(final Ruppet ruppet, final Sequence actions, final String fileName) throws NullPointerException {
		Objects.requireNonNull(ruppet,
		                       "Cannot initialize a " + SoftwarePart.class.getSimpleName() + " with a null ruppet");
		track = Objects.requireNonNull(actions, "Cannot initialize a " + SoftwarePart.class.getSimpleName() +
				" with null actions").createTrack();
		this.fileName = fileName;
		transitionTimesFile = new File(fileName);
	}
	
	/**
	 * Reads timing information from a {@code File} that will be used to set up the timed actions that will be carried
	 * out upon the execution of a script.
	 */
	protected abstract void readTimingInfoFromFile();
	
	/**
	 * Sets up the {@code SoftwarePart} such that it will carry out specific timed actions during the running of a
	 * script.
	 */
	protected abstract void setupTimings();
	
	/**
	 * Returns an {@code int} representing the number of transitions that have been read from the {@code File}.
	 *
	 * @return an {@code int} representing the number of transitions that have been read from the {@code File}
	 */
	protected abstract int getNumberOfTransitions();
	
	/**
	 * Returns the {@code Track} that contains all of the {@code MidiEvent} timings.
	 *
	 * @return The {@code Track} that contains all of the {@code MidiEvent} timings
	 */
	public final Track getTrack() { return track; }
	
}