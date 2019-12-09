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
import java.util.List;

/**
 * A {@code Movable} is a component of the {@code Ruppet} that can be controlled by one or more servo motors. If the
 * {@code Movable} requires two servo motors to be operated, then the states for the second servo motor depends on
 * whether the motion of the servo motors will be in parallel or antiparallel with one another. If their motion is in
 * parallel, then the velocity values will be the same as the first servo motors'. If the motion is antiparallel, then
 * the velocity values will be inversely related.
 *
 * @author Jon Mrowczynski
 */
public final class Movable extends Part {
	
	/**
	 * If the {@code Movable} only requires one servo motor to be operated, then the superclass's constructor is simply
	 * called.
	 * 
	 * @param ruppetParts of a {@code Ruppet}
	 * @param midiNote associated with the servo(s) that is/are to operate this {@code Movable}
	 * @param lowerBound that the servo arm can move to
	 * @param upperBound that the servo arm can move to
	 * @throws InvalidParameterException if {@code lowerBound <= upperBound} or if either are not valid values
	 */
    public Movable(final List<Part> ruppetParts, final int midiNote, final int lowerBound, final int upperBound) throws InvalidParameterException {
		super(ruppetParts, midiNote, lowerBound, upperBound);
	}
	
	/**
	 * If the {@code Movable} requires two servo motors to be operated, then it first calls the superclass's constructor
	 * to create the state for the first servo motor and then it creates the state for the second servo motor.
	 * 
	 * @param ruppetParts of a {@code Ruppet}
	 * @param midiNote1 that is associated with the first servo motor
	 * @param lowerBound that the servo arms can move to
	 * @param upperBound that the servo arms can move to
	 * @param midiNote2 that is associated with the second servo motor
	 * @param parallelism Represents whether the servo motors are to be operated in parallel or anti-parallel relative
	 *                    to one another
	 * @throws InvalidParameterException if {@code lowerBound <= upperBound} or if either are not valid values or if
	 * 									 {@code parallelism} is not "parallel" or "antiparallel"
	 */
    public Movable(final List<Part> ruppetParts, final int midiNote1, final int lowerBound, final int upperBound, final int midiNote2, final String parallelism)
			throws InvalidParameterException {
	    this(ruppetParts, midiNote1, lowerBound, upperBound);
		for (var i = 0; i < states.size(); ++i) {
			try {
				switch(parallelism.toLowerCase()) {
                    case "parallel":
                        states.get(i).add(new ShortMessage(ShortMessage.NOTE_ON, 0, midiNote2, i + lowerBound));
                        break;
                    case "antiparallel":
                        states.get(i).add(new ShortMessage(ShortMessage.NOTE_ON, 0, midiNote2, ((upperBound + lowerBound) - (i + lowerBound))));
                        break;
                    default:
                        throw new InvalidParameterException("The String: " + parallelism + " is not defined for this constructor.");
                }
			} catch(InvalidMidiDataException ex) { ex.printStackTrace(); }
		}
	}
		
} // end of Movable