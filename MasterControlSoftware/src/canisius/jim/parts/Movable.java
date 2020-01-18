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

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import java.security.InvalidParameterException;
import java.util.stream.IntStream;

/**
 * A {@code Movable} is a component of the {@code Ruppet} that can be controlled by one or more servo motors. If the
 * {@code Movable} requires two servo motors to be operated, then the states for the second servo motor depends on
 * whether the motion of the servo motors will be in parallel or antiparallel with one another. If their motion is in
 * parallel, then the velocity values will be the same as the first servo motors'. If the motion is antiparallel, then
 * the velocity values will be inversely related.
 *
 * @author Jon Mrowczynski
 */
public final class Movable extends HardwarePart {

	/**
	 * Represents the type of parallelism that two servo motor setups can take.
	 */
	public enum Parallelism {
		PARALLEL,
		ANTIPARALLEL
	}

	/**
	 * If the {@code Movable} only requires one servo motor to be operated, the the superclass's constructor is simply
	 * called. This constructor also sets the neutral value of the
	 *
	 * @param midiNote associated with the servo that is to operate this {@code Movable}
	 * @param lowerBound that the servo arm can move to
	 * @param upperBound that the servo arm can move to
	 * @param neutral position of the {@code Movable}
	 * @throws InvalidParameterException if {@code lowerBound <= upperBound} or if either are not valid values
	 */
	public Movable(final int midiNote, final int lowerBound, final int upperBound, final int neutral) throws InvalidParameterException {
    	super(midiNote, lowerBound, upperBound, neutral);
	}

	/**
	 * If the {@code Movable} requires two servo motors to be operated, then it first calls the superclass's constructor
	 * to create the states for the first servo motor and then it creates the states for the second servo motor. The
	 * neutral position of the {@code Movable} is also set.
	 *
	 * @param midiNote1 that is associated with the first servo motor
	 * @param lowerBound that the servo arm can move to
	 * @param upperBound that the servo arm can move to
	 * @param neutral position of the {@code Movable}
	 * @param midiNote2 that is associated with the second servo motor
	 * @param parallelism represents whether the servo motors are to be operated in parallel or anti-parallel relative
	 *                    to one another
	 * @throws InvalidParameterException if {@code lowerBound <= upperBound} or if either are not valid values
	 */
	public Movable(final int midiNote1, final int lowerBound, final int upperBound, final int neutral, final int midiNote2, final Parallelism parallelism) throws InvalidParameterException {
    	this(midiNote1, lowerBound, upperBound, neutral);
    	setupSecondServoStates(lowerBound, upperBound, midiNote2, parallelism);
	}

	/**
	 * Sets up all of the states of this {@code Movable}.
	 *
	 * @param lowerBound that the servo arm can move to
	 * @param upperBound that the servo arm can move to
	 * @param midiNote2 that is associated with the second servo motor
	 * @param parallelism represents whether the servo motors are to be operated in parallel or anti-parallel relative
	 *                    to one another
	 */
	private void setupSecondServoStates(final int lowerBound, final int upperBound, final int midiNote2, final Parallelism parallelism) {
		IntStream.range(0, states.size()).forEach(i -> {
			try {
				switch(parallelism) {
					case PARALLEL:
						states.get(i).add(new ShortMessage(ShortMessage.NOTE_ON, 0, midiNote2, i + lowerBound));
						break;
					case ANTIPARALLEL:
						states.get(i).add(new ShortMessage(ShortMessage.NOTE_ON, 0, midiNote2, ((upperBound + lowerBound) - (i + lowerBound))));
						break;
				}
			} catch(InvalidMidiDataException ex) { ex.printStackTrace(); }
		});
	}
		
} // end of Movable