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
import java.util.*;
import java.util.stream.IntStream;

/**
 * This class provides the framework for the components of the {@code Ruppet} that are operated by the microcontroller.
 *
 * The {@code List} {@code states} contains {@code Set}s of {@code ShortMessage}s where each {@code Set} represents a
 * state of this {@code HardwarePart}.
 *
 * For the parts that move due to changes in the angular position of one or more servo motors, or {@code Movable}s,
 * {@code states} represents the possible angular positions of those servo motors.
 *
 * For {@code Lights}, {@code states} represents the brightness levels. Currently the lights are digitally operated so
 * the only meaningful states would be the first and last elements in the {@code List} {@code states}.
 *
 *	@author Jon Mrowcsynski
 */
public abstract class HardwarePart {

    /**
     * The absolute minimum bound.
     */
    public static final int MIN_BOUND = 0;

    /**
     * The absolute maximum bound.
     */
    public static final int MAX_BOUND = 10;
	
	/**
	 * The highest velocity value that represents one bounding state for this {@code HardwarePart}.
	 */
    private final int upperBound;
	
	/**
	 * The lowest velocity value that represents the other bounding state for this {@code HardwarePart}.
	 */
    private final int lowerBound;

    /**
     * The neutral velocity value that represents a "middle" or "resting" state for this {@code HardwarePart}.
     */
    private int neutral;

	/**
	 * The {@code List} that contains the available states that a {@code HardwarePart} can go to. If there is a
     * {@code HardwarePart} that requires n servo motors to operate properly, then n {@code ShortMessage}s can be added
     * to the {@code Set}s.
	 */
	final List<Set<ShortMessage>> states = new ArrayList<>();
	
	/**
	 * Initializes all of the states that this {@code HardwarePart} can go to. It also sets {@code neutral} to the
     * average of {@code lowerBound} and {@code upperBound}.
	 * 
	 * @param midiNote the corresponding MIDI note that is used to operate this {@code HardwarePart}
     * @param lowerBound that the {@code HardwarePart} can move to
     * @param upperBound that the {@code HardwarePart} can move to
     * @throws InvalidParameterException if {@code lowerBound <= upperBound} or if either are not valid values
	 */
	HardwarePart(final int midiNote, final int lowerBound, final int upperBound) throws InvalidParameterException {
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
        IntStream.range(0, upperBound - lowerBound + 1).forEach(i -> {
            try { states.add(new HashSet<>(Set.of(new ShortMessage(ShortMessage.NOTE_ON, 0, midiNote, i + lowerBound)))); }
            catch (InvalidMidiDataException e) { e.printStackTrace(); }
        });
    }

    /**
     * Initializes all of the states that this {@code HardwarePart} can go to. It also sets {@code this.neutral} to
     * {@code neutral}.
     *
     * @param midiNote the corresponding MIDI note that is used to operate this {@code HardwarePart}
     * @param lowerBound that the {@code HardwarePart} can move to
     * @param upperBound that the {@code HardwarePart} can move to
     * @param neutral value that should be used instead of the average of {@code lowerBound} and {@code upperBound}
     * @throws InvalidParameterException if {@code lowerBound <= upperBound} or if either are not valid values
     */
    HardwarePart(final int midiNote, final int lowerBound, final int upperBound, final int neutral) throws InvalidParameterException {
	    this(midiNote, lowerBound, upperBound);
	    setNeutral(neutral);
    }

    /**
     * Sets the neutral velocity value of this {@code HardwarePart}. If {@code newNeutral} is outside of the bounds,
     * then {@code neutral} is set to the bound value that is closest to {@code newNeutral}.
     *
     * @param newNeutral velocity value of this {@code HardwarePart}
     */
    public final void setNeutral(final int newNeutral) { neutral = newNeutral < lowerBound ? lowerBound : Math.min(newNeutral, upperBound); }
	
	/**
     * Sets the state of this {@code HardwarePart} to the state that corresponds to the {@code velocity} value, but only
     * if that {@code velocity} value is valid for this {@code HardwarePart}.
	 * 
	 * @param velocity value that is to be converted to a {@code stateIndex}
	 */
	final void toState(final int velocity) {
		if (validVelocity(velocity)) { states.get(velocityToStateIndex(velocity)).forEach(msg -> UsbMidiConnection.getInstance().send(msg)); }
	}
	
	/**
	 * Adds the passed in state (which is represented by a {@code Set} of {@code ShortMessage}s) to {@code track} at a
     * specified time ({@code tick}). However, this is only done if the passed in state is valid for this
     * {@code HardwarePart}.
	 * 
	 * @param track that is to have {@code ShortMessage}s added to it
	 * @param messages that are to be added to the {@code Track}
	 * @param tick of that the {@code ShortMessage}s should be played at from the start of the {@code Track}
     * @throws NullPointerException if {@code track} is {@code null}
	 */
	public final void addStateToTrack(final Track track, final Set<ShortMessage> messages, final int tick) throws NullPointerException {
	    Objects.requireNonNull(track, "Cannot add state to a null Track");
		if (validShortMessages(messages)) { messages.forEach(msg -> track.add(new MidiEvent(msg, tick))); }
	}

    /**
     * Transitions the {@code HardwarePart} to one of its bounding states.
     */
    public final void toUpperBound() { toState(upperBound); }

    /**
     * Transitions the {@code HardwarePart} to its other bounding state.
     */
    public final void toLowerBound() { toState(lowerBound); }

    /**
     * Transitions the {@code HardwarePart} to its neutral state.
     */
    public void toNeutral() { toState(neutral); }

    /**
     * Returns the state that represents one bounding state of this {@code HardwarePart}.
     *
     * @return The state that represents one bounding state of this {@code HardwarePart}
     */
    public final Set<ShortMessage> getUpperBoundState() { return getState(upperBound); }

    /**
     * Returns the state the represents the other bounding state of this {@code HardwarePart}.
     *
     * @return The state the represents the other bounding state of this {@code HardwarePart}
     */
    public final Set<ShortMessage> getLowerBoundState() { return getState(lowerBound); }

    /**
     * Returns the state the represents the neutral state of this {@code HardwarePart}.
     *
     * @return The state that represents the neutral state of this {@code HardwarePart}
     */
    public final Set<ShortMessage> getNeutralState() { return getState(neutral); }

    /**
     * Returns the {@code upperBound} {@code HardwarePartState} of this {@code HardwarePart}.
     *
     * @return The {@code upperBound} {@code HardwarePartState} of this {@code HardwarePart}
     */
    final HardwarePartState getUpperBoundHardwarePartState() { return getHardwarePartState(upperBound); }

    /**
     * Returns the {@code lowerBound} {@code HardwarePartState} of this {@code HardwarePart}.
     *
     * @return The {@code lowerBound} {@code HardwarePartState} of this {@code HardwarePart}
     */
    final HardwarePartState getLowerBoundHardwarePartState() { return getHardwarePartState(lowerBound); }

    /**
     * Returns the {@code neutral} {@code HardwarePartState} of this {@code HardwarePart}.
     *
     * @return The {@code neutral} {@code HardwarePartState} of this {@code HardwarePart}
     */
    final HardwarePartState getNeutralHardwarePartState() { return new HardwarePartState(this, getState(neutral)); }

    /**
     * Returns the corresponding {@code PartState} associated with the {@code velocity} if {@code velocity} is a valid
     * value.
     *
     * @param velocity value that maps to the returned {@code HardwarePartState}
     * @return The {@code HardwarePartState} that corresponds to {@code velocity}
     * @throws InvalidParameterException if {@code velocity} is a invalid
     */
    private HardwarePartState getHardwarePartState(final int velocity) throws InvalidParameterException {
        if (validVelocity(velocity) && velocity >= lowerBound && velocity <= upperBound) { return new HardwarePartState(this, getState(velocity)); }
        else { throw new InvalidParameterException("Cannot retrieve the PartState associated with the velocity value: " + velocity); }
    }
	
	/**
	 * Converts {@code velocity} into an index in {@code states}.
	 *  
	 * @param velocity value that maps to an index in {@code states}
	 * @return An {@code int} representing an index in {@code states}
	 */
	private int velocityToStateIndex(final int velocity) { return velocity - lowerBound; }

    /**
     * Returns a {@code boolean} representing whether {@code velocity} is valid for this {@code HardwarePart}.
     *
     * @param velocity value that is to be checked
     * @return A {@code boolean} representing whether {@code velocity} is valid
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
     * {@code HardwarePart}. If any of them are not, then {@code messages} are not valid. Otherwise, {@code messages}
     * are valid.
     *
     * @param messages The {@code Set} of {@code ShortMessage}s that are to be checked for validity
     * @return a {@code boolean} representing whether the given {@code Set} of {@code ShortMessage}s is a valid group of
     *         {@code ShortMessage}s for this {@code HardwarePart}
     */
    private boolean validShortMessages(final Set<ShortMessage> messages) {
        return messages != null && states.get(velocityToStateIndex(getVelocityVal(messages.iterator().next()))).containsAll(messages);
    }

    /**
     * Returns this {@code HardwarePart}'s state based on the velocity value iff that velocity value is valid for this
     * {@code HardwarePart}.
     *
     * @param velocity value that is to be checked for validity
     * @return A {@code Set} of {@code ShortMessage}s that represents the state associated with the given velocity
     *         value
     * @throws InvalidParameterException if the value of {@code velocity} is invalid
     */
    private Set<ShortMessage> getState(final int velocity) throws InvalidParameterException {
        if (validVelocity(velocity)) { return states.get(velocityToStateIndex(velocity)); }
        else { throw new InvalidParameterException(" for velocityToStateIndex conversion. No State can be returned."); }
    }

    /**
     * Returns the velocity value associated with {@code msg}.
     *
     * @param msg whose velocity value will be returned
     * @return The velocity value of {@code msg} as an {@code int}
     * @throws NullPointerException if {@code msg} is {@code null}
     */
    private static int getVelocityVal(final ShortMessage msg) throws NullPointerException { return msg.getData2(); }

} // end of HardwarePart