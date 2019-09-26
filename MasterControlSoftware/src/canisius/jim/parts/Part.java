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

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class provides the framework for the components of the {@code Ruppet} that are operated by the microcontroller.
 *
 * The {@code List} {@code states} contains {@code Set}s of {@code ShortMessage}s where each {@code Set} represents a
 * state of this {@code Part}.
 *
 * For the parts that move due to changes in the angular position of one or more servo motors, or {@code Movable}s,
 * {@code states} represents the possible angular positions of those servo motors.
 *
 * For {@code Lights}, {@code states} represents the brightness levels. Currently the lights are digitally operated so
 * the only meaningful states would be the first and last elements in the {@code List} {@code states}.
 *
 *	@author Jon Mrowcsynski
 */

public abstract class Part {
	
	/**
	 * The highest velocity value that represents one bounding state for this {@code Part}.
	 */

    private final int upperBound;
	
	/**
	 * The lowest velocity value that represents the other bounding state for this {@code Part}.
	 */

    private final int lowerBound;

    /**
     * The neutral velocity value that represents a "middle" or "resting" state for this {@code Part}.
     */

    private int neutral;

	/**
	 * The {@code List} that contains the available states that a {@code Part} can go to. If there is a {@code Part}
     * that requires n servo motors to operate properly, then n {@code ShortMessage}s can be added to the {@code Set}s.
	 */

	final List<Set<ShortMessage>> states = new ArrayList<>();
	
	/**
	 * Initializes all of the states that this {@code Part} can go to. It also adds this {@code Part} to the
     * {@code Ruppet}'s {@code Part}'s {@code List} and sets {@code neutral} to the average of {@code lowerBound} and
     * {@code upperBound}.
	 * 
	 * @param ruppetParts of all of the {@code Part}s of a {@code Ruppet} that this {@code Part} is to be added to.
	 * @param midiNote The corresponding MIDI note that is used to operate this {@code Part}.
     * @throws InvalidParameterException if {@code lowerBound <= upperBound} or if either are not valid values.
	 */

	Part(final List<Part> ruppetParts, final int midiNote, final int lowerBound, final int upperBound) throws InvalidParameterException {
        if (lowerBound <= upperBound) {
            if (validVelocity(lowerBound)) { this.lowerBound = lowerBound; }
            else { throw new InvalidParameterException("for lowerBound. Boundary values could not be set."); }
            if (validVelocity(upperBound)) { this.upperBound = upperBound; }
            else { throw new InvalidParameterException("for upperBound. Boundary values could not be set."); }
        } else { throw new InvalidParameterException("Invalid boundary values: " +
                "\nlowerBound: " + lowerBound +
                "\nupperBound: " + upperBound +
                "\nCould not instantiate Movable Part.");
        }
        neutral = (upperBound + lowerBound) / 2;
        for (int i = 0; i < (this.upperBound - this.lowerBound) + 1; ++i) {
			try { states.add(new HashSet<>(Set.of(new ShortMessage(ShortMessage.NOTE_ON, 0, midiNote, i + lowerBound)))); }
			catch (InvalidMidiDataException e) { e.printStackTrace(); }
		}
        ruppetParts.add(this);
    }

    /**
     * Sets the neutral velocity value of this {@code Part}.
     *
     * @param newNeutral velocity value of this {@code Part}.
     * @throws InvalidParameterException if {@code newNeutral} value is invalid.
     */

    public final void setNeutral(final int newNeutral) throws InvalidParameterException {
        if (newNeutral >= lowerBound && newNeutral <= upperBound) { neutral = newNeutral; }
        else { throw new InvalidParameterException("Cannot set the neutral state value to: " + newNeutral
                + "\nThe provided value is not within the defined acceptable range of values: "
                + lowerBound + "-" + upperBound); }
    }
	
	/**
     * Sets the state of this {@code Part} to the state that corresponds to the {@code velocity} value, but only if that
     * {@code velocity} value is valid for this {@code Part}.
	 * 
	 * @param velocity value that is to be converted to a {@code stateIndex}.
     * @see #velocityToStateIndex(int)
	 */

	final void toState(final int velocity) {
		if (validVelocity(velocity)) { states.get(velocityToStateIndex(velocity)).forEach(msg -> UsbMidiConnection.getInstance().send(msg)); }
	}
	
	/**
	 * Adds the passed in state (which is represented by a {@code Set} of {@code ShortMessage}s) to {@code track} at a
     * specified time ({@code tick}). However, this is only done if the passed in state is valid for this {@code Part}.
	 * 
	 * @param track that is to have {@code ShortMessage}s added to it.
	 * @param messages that are to be added to the {@code Track}.
	 * @param tick of that the {@code ShortMessage}s should be played at from the start of the {@code Track}.
	 */

	public final void addStateToTrack(final Track track, final Set<ShortMessage> messages, final int tick) {
		if (validShortMessages(messages)) { messages.forEach(msg -> track.add(new MidiEvent(msg, tick))); }
	}

    /**
     * Transitions the {@code {Part}} to one of its bounding states.
     */

    public final void toUpperBound() { toState(upperBound); }

    /**
     * Transitions the {@code Part} to its other bounding state.
     */

    public final void toLowerBound() { toState(lowerBound); }

    /**
     * Transitions the {@code Part} to its neutral state.
     */

    public void toNeutral() { toState(neutral); }

    /**
     * Returns the state that represents one bounding state of this {@code Part}.
     *
     * @return The state that represents one bounding state of this {@code Part}.
     */

    public final Set<ShortMessage> getUpperBoundState() { return getState(upperBound); }

    /**
     * Returns the state the represents the other bounding state of this {@code Part}.
     *
     * @return The state the represents the other bounding state of this {@code Part}.
     */

    public final Set<ShortMessage> getLowerBoundState() { return getState(lowerBound); }

    /**
     * Returns the state the represents the neutral state of this {@code Part}.
     *
     * @return The state that represents the neutral state of this {@code Part}.
     */

    final Set<ShortMessage> getNeutralState() { return getState(neutral); }

    /**
     * Returns the {@code upperBound} {@code PartState} of this {@code Part}.
     *
     * @return The {@code upperBound} {@code PartState} of this {@code Part}.
     */

    final PartState getUpperBoundPartState() { return getPartState(upperBound); }

    /**
     * Returns the {@code lowerBound} {@code PartState} of this {@code Part}.
     *
     * @return The {@code lowerBound} {@code PartState} of this {@code Part}.
     */

    final PartState getLowerBoundPartState() { return getPartState(lowerBound); }

    /**
     * Returns the {@code neutral} {@code PartStat} of this {@code Part}.
     *
     * @return The {@code neutral} {@code PartState} of this {@code Part}.
     */

    final PartState getNeutralPartState() { return new PartState(this, getState(neutral)); }

    /**
     * Returns the corresponding {@code PartState} associated with the {@code velocity} if {@code velocity} is a valid
     * value.
     *
     * @param velocity value that maps to the returned {@code PartState}.
     * @return The {@code PartState} that corresponds to {@code velocity}.
     * @throws InvalidParameterException if {@code velocity} is a invalid.
     */

    private PartState getPartState(final int velocity) throws InvalidParameterException {
        if (validVelocity(velocity) && velocity >= lowerBound && velocity <= upperBound) { return new PartState(this, getState(velocity)); }
        else { throw new InvalidParameterException("Cannot retrieve the PartState associated with the velocity value: " + velocity); }
    }
	
	/**
	 * Converts {@code velocity} into an index in {@code states}.
	 *  
	 * @param velocity value that maps to an index in {@code states}.
	 * @return An {@code int} representing an index in {@code states}.
	 */

	private int velocityToStateIndex(final int velocity) { return velocity - lowerBound; }

    /**
     * Returns a {@code boolean} representing whether {@code velocity} is valid for this {@code Part}.
     *
     * @param velocity value that is to be checked.
     * @return A {@code boolean} representing whether {@code velocity} is valid.
     */

    private boolean validVelocity(final int velocity) {
        if (velocity >= Ruppet.MIN_VELOCITY && velocity <= Ruppet.MAX_VELOCITY) { return true; }
        else {
            System.out.println("Invalid velocity value: " + velocity);
            return false;
        }
    }

    /**
     * Returns a {@code boolean} representing whether {@code messages} are contained in {@code states} for this
     * {@code Part}. If any of them are not, then {@code messages} are not valid. Otherwise, {@code messages} are valid.
     *
     * @param messages The {@code Set} of {@code ShortMessage}s that are to be checked for validity.
     * @return a {@code boolean} representing whether the given {@code Set} of {@code ShortMessage}s is a valid group of
     *         {@code ShortMessage}s for this {@code Part}.
     */

    private boolean validShortMessages(final Set<ShortMessage> messages) {
        final Set<ShortMessage> state = states.get(velocityToStateIndex(getVelocityVal(messages.iterator().next())));
        return state.containsAll(messages);
    }

    /**
     * Returns this {@code Part}'s state based on the velocity value iff that velocity value is valid for this
     * {@code Part}.
     *
     * @param velocity value that is to be checked for validity.
     * @return A {@code Set} of {@code ShortMessage}s that represents the state associated with the given velocity
     *         value.
     * @throws InvalidParameterException if the value of {@code velocity} is invalid.
     */

    private Set<ShortMessage> getState(final int velocity) throws InvalidParameterException {
        if (validVelocity(velocity)) { return states.get(velocityToStateIndex(velocity)); }
        else { throw new InvalidParameterException(" for velocityToStateIndex conversion. No State can be returned."); }
    }

    /**
     * Returns the velocity value associated with {@code msg}.
     *
     * @param msg whose velocity value will be returned.
     * @return The velocity value of {@code msg} as an {@code int}.
     */

    private static int getVelocityVal(final ShortMessage msg) { return msg.getData2(); }

} // end of Part


