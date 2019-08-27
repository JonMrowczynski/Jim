package canisius.jim.parts;

import canisius.jim.connections.UsbMidiDevice;
import canisius.jim.ruppet.Ruppet;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.security.InvalidParameterException;
import java.util.*;

/**
 * This class provides the framework for the components of the {@code Ruppet} that are operated by the microcontroller.
 *
 * The two-dimensional array {@code states} contain {@code ShortMessage}s represent a certain state for the component.
 *
 * For the parts that move due to changes in the angular position of one or more servo motors, {@code states} represent
 * the angular positions of the servo motors.
 *
 * For the lights, {@code states} represents the brightness levels. Currently the lights are digitally operated
 * so the only meaningful states would be the first and last elements in the array.
 *
 *	@author Jon Mrowcsynski
 */

public abstract class Part {
	
	/**
	 * The highest velocity value that this {@code Part} can go to.
	 */

    private final int upperBound;
	
	/**
	 * The lowest velocity value that this {@code Part} can go to.
	 */

    private final int lowerBound;

    /**
     * The neutral velocity value of this {@code Part}.
     */

    private int neutral;

	/**
	 * The available states that a {@code Part} can go to. If there is a {@code Part} that requires more than one servo
     * motor to operate properly, then however many {@code ShortMessage}s can be added to the {@code Set} as required.
	 */

	final Map<Integer, Set<ShortMessage>> states = new LinkedHashMap<>();
	
	/**
	 * This initializes all of the states that the {@code Part} can go to. It also 
	 * adds the part to the {@code Ruppet}'s {@code ArrayList}.
	 * 
	 * @param ruppetParts The {@code ArrayList} that represents all of the {@code Part}s of a {@code Ruppet}.
	 * @param midiNote The corresponding MIDI note that is used to operate a given component to a {@code Part}.
	 */

	Part(final List<Part> ruppetParts, final int midiNote, final int lowerBound, final int upperBound) {
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
			try { states.put(i, new HashSet<>(Set.of(new ShortMessage(ShortMessage.NOTE_ON, 0, midiNote, i + lowerBound)))); }
			catch (InvalidMidiDataException e) { e.printStackTrace(); }
		}
        ruppetParts.add(this);
    }

    /**
     * Sets the neutral position of this {@code Movable}.
     *
     * @param newNeutral position of this {@code Movable}.
     * @throws InvalidParameterException if {@code newNeutral} value is an invalid value for {@code neutral}.
     */

    public final void setNeutral(final int newNeutral) throws InvalidParameterException {
        if (newNeutral >= lowerBound && newNeutral <= upperBound) { neutral = newNeutral; }
        else { throw new InvalidParameterException("Cannot set the neutral state value to: " + newNeutral
                + "\nThe provided value is not within the defined acceptable range of values: "
                + lowerBound + "-" + upperBound); }
    }
	
	/**
	 * Performs a similar task as the other {@code toState} method, however, it accomplishes the task with the
     * {@code velocity} value instead, but only if that {@code velocity} value is a valid {@code velocity} value for the
     * corresponding {@code Part}. The method figures out what state is specified by the passed in {@code velocity} by
     * converting the {@code velocity} value to a {@code Part} state by calling the {@code velocityToStateIndex} method.
	 * 
	 * @param velocity The {@code velocity} value that is to be converted to a {@code stateIndex}
	 */

	final void toState(final int velocity) {
		if (validVelocity(velocity)) { states.get(velocityToStateIndex(velocity)).forEach(msg -> UsbMidiDevice.getInstance().send(msg)); }
	}
	
	/**
	 * Simply adds the passed in state (which is represented by an array of {@code ShortMessage}s)
	 * to a given {@code Track} at a specified time (tick). However, this is only done if the passed in
	 * state is a valid state for the given {@code Part}.
	 * 
	 * @param track that is to have {@code ShortMessage}s added to it
	 * @param messages that are to be added to the {@code Track}
	 * @param tick of the {@code ShortMessage}s.
	 */

	public final void addStateToTrack(final Track track, final Set<ShortMessage> messages, final int tick) {
		if (validShortMessages(messages)) { messages.forEach(msg -> track.add(new MidiEvent(msg, tick))); }
	}

    /**
     * Moves the {@code Movable} to one of its most extreme angular positions.
     */

    public final void toUpperBound() { toState(upperBound); }

    /**
     * Moves the {@code Movable} to its other most extreme angular position that is in the opposite direction.
     */

    public final void toLowerBound() { toState(lowerBound); }

    /**
     * Moves the {@code Movable} to its neutral angular position.
     */

    public void toNeutral() { toState(neutral); }

    /**
     * Gets the state that represents the maximum position that the {@code Movable} can move to in one direction.
     *
     * @return The state that represents the upper bound of the {@code Movable}.
     */

    public final Set<ShortMessage> getUpperBoundState() { return getState(upperBound); }

    /**
     * Gets the state the represents the maximum position that the {@code Movable} can move to in the other direction.
     *
     * @return The state the represents the lower bound of the {@code Movable}.
     */

    public final Set<ShortMessage> getLowerBoundState() { return getState(lowerBound); }

    /**
     * Gets the state the represents the neutral position of the {@code Movable}.
     *
     * @return The state that represents the neutral position of the {@code Movable}.
     */

    final Set<ShortMessage> getNeutralState() { return getState(neutral); }

    /**
     * Gets the upper bound value for the {@code Movable}.
     *
     * @return The {@code int} that represents the upper bound of the {@code Movable}.
     */

    final PartState getUpperBoundPartState() { return getPartState(upperBound); }

    /**
     * Gets the lower bound value for the {@code Movable}.
     *
     * @return The {@code int} that represents the lower bound of the {@code Movable}.
     */

    final PartState getLowerBoundPartState() { return getPartState(lowerBound); }

    /**
     * Gets the state that represents the {@code Movable}'s neutral position.
     *
     * @return The state that represents the {@code Movable}'s neutral position.
     */

    final PartState getNeutralPartState() { return new PartState(this, getState(neutral)); }

    /**
     * Gets the corresponding part state associated with the given velocity value if the velocity value is a valid
     * velocity value. Otherwise, an {@code InvalidParameterException} is thrown.
     *
     * @param velocity The representation of an angular position of a servo arm
     * @return The {@code PartState} that represents the state of the corresponding {@code Ruppet Part}
     * @throws InvalidParameterException if {@code velocity} is an invalid velocity value.
     */

    private PartState getPartState(final int velocity) throws InvalidParameterException {
        if (validVelocity(velocity) && velocity >= lowerBound && velocity <= upperBound) { return new PartState(this, getState(velocity)); }
        else { throw new InvalidParameterException("Cannot retrieve the PartState associated with the velocity value: " + velocity); }
    }
	
	/**
	 * Converts {@code velocity} into an index that corresponds to the stat
	 *  
	 * @param velocity value that is to be converted to a {@code stateIndex}.
	 * @return The {@code stateIndex}.
	 */

	private int velocityToStateIndex(final int velocity) { return velocity - lowerBound; }

    /**
     * Returns a {@code boolean} representing whether {@code velocity} is valid for the current {@code Part}.
     *
     * @param velocity value that is to be checked.
     * @return A {@code boolean} representing whether {@code velocity} valid.
     */

    private boolean validVelocity(final int velocity) {
        if (velocity >= Ruppet.MIN_VELOCITY && velocity <= Ruppet.MAX_VELOCITY) { return true; }
        else {
            System.out.println("Invalid velocity value: " + velocity);
            return false;
        }
    }

    /**
     * Determine whether the messages that were passed in are defined in the states of the 2D array for the
     * given {@code Part}. If any of them are not, then the {@code ShortMessage}s are not valid, else,
     * the {@code ShortMessage}s are valid.
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
     * Gets the velocity value associated with {@code msg}.
     *
     * @param msg whose velocity value will be returned.
     * @return The velocity value of the {@code ShortMessage} as an {@code int}.
     */

    private static int getVelocityVal(final ShortMessage msg) { return msg.getData2(); }

} // end of Part


