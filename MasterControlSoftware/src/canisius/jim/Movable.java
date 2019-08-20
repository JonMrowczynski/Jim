package canisius.jim;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import java.security.InvalidParameterException;
import java.util.List;

/**
 * 	A {@code Movable} is a component of the {@code Ruppet} that can be controlled by one or more servo motors. If the
 * 	{@code Movable} requires two servo motors to be operated, then the states for the second servo motor depends on
 * 	whether the motion of the servo motors will be in parallel or antiparallel with one another. If their motion is in
 * 	parallel, then the velocity values will be the same as the first servo motors'. If the motion is antiparallel, then
 * 	the velocity values will be inversely related.
 *
 *  @author Jon Mrowczynski
 */

final class Movable extends Part {

	/**
	 * The {@code neutral} angular position of the motor(s).
	 */
	
	private int neutral;
	
	/**
	 * If the {@code Movable} only requires one servo motor to be operated, then the {@code Movable} constructor calls
	 * its superclass's constructor and sets {@code neutral} to the average of the {@code lowerBound} and
	 * {@code upperBound}.
	 * 
	 * @param ruppetParts of the {@code Ruppet}.
	 * @param midiNote associated with the servo(s) that is/are to operate this {@code Movable}.
	 * @param lowerBound that the servo arm can move to.
	 * @param upperBound that the servo arm can move to.
	 * @see Part#Part(List, int, int, int, int)    
	 */

	Movable(final List<Part> ruppetParts, final int midiNote, final int lowerBound, final int upperBound) {
		super(ruppetParts, 1, midiNote, lowerBound, upperBound);
		neutral = (upperBound + lowerBound) / 2;
	}
	
	/**
	 * If the {@code Movable} requires two servo motors to be operated, then it first calls the superclass's constructor
	 * to create the state for the first servo motor and then it creates the state for the second servo motor.
	 * 
	 * @param ruppetParts of the {@code Ruppet}.
	 * @param midiNote1 that is associated with the first servo motor.
	 * @param lowerBound that the servo arm can move to.
	 * @param upperBound that the servo arm can move to.
	 * @param midiNote2 that is associated with the second servo motor.
	 * @param parallelism Represents whether the servo motors are to be operated in parallel or anti-parallel relative to one another.
	 * @throws InvalidParameterException if {@code parallelism} is not "parallel" or "antiparallel".
	 * @see Part#Part(List, int, int, int, int)
	 */

	Movable(final List<Part> ruppetParts, final int midiNote1, final int lowerBound, final int upperBound, final int midiNote2, final String parallelism)
			throws InvalidParameterException {
		super(ruppetParts, 2, midiNote1, lowerBound, upperBound);
		neutral = (upperBound + lowerBound) / 2;
		for(int i = 0; i < numOfStates; ++i) {
			states[i][1] = new ShortMessage();
			try {
				switch(parallelism.toLowerCase()) {
                    case "parallel":
                        states[i][1].setMessage(ShortMessage.NOTE_ON, RuppetUtils.CHAN_1, midiNote2, i + lowerBound);
                        break;
                    case "antiparallel":
                        states[i][1].setMessage(ShortMessage.NOTE_ON, RuppetUtils.CHAN_1, midiNote2, ((upperBound + lowerBound) - (i + lowerBound)));
                        break;
                    default:
                        throw new InvalidParameterException("The String: " + parallelism + " is not defined for this constructor.");
                }
			} catch(InvalidMidiDataException ex) { ex.printStackTrace(); }
		}
	}
	
	/**
	 * Sets the neutral position of this {@code Movable}.
	 *
	 * @param newNeutral position of this {@code Movable}.
	 * @throws InvalidParameterException if {@code newNeutral} value is an invalid value for {@code neutral}.
	 */

	final void setNeutral(final int newNeutral) throws InvalidParameterException {
		if (newNeutral >= lowerBound && newNeutral <= upperBound) { neutral = newNeutral; }
		else { throw new InvalidParameterException("Cannot set the neutral state value to: " + newNeutral
				+ "\nThe provided value is not within the defined acceptable range of values: "
				+ lowerBound + "-" + upperBound); }
	}
	
	/**
	 * Moves the {@code Movable} to one of its most extreme angular positions.
	 */
	
	final void toUpperBound() { toState(upperBound); }
	
	/**
	 * Moves the {@code Movable} to its other most extreme angular position that is in the opposite direction.
	 */
	
	final void toLowerBound() { toState(lowerBound); }
	
	/**
	 * Moves the {@code Movable} to its neutral angular position.
	 */
	
	final void toNeutral() { toState(neutral); 	}
	
	/**
	 * Gets the state that represents the maximum position that the {@code Movable} can move to in one direction.
	 * 
	 * @return The state that represents the upper bound of the {@code Movable}.
	 */
	
	final ShortMessage[] getUpperBoundState() { return getState(upperBound); }
	
	/**
	 * Gets the state the represents the maximum position that the {@code Movable} can move to in the other direction.
	 * 
	 * @return The state the represents the lower bound of the {@code Movable}.
	 */
	
	final ShortMessage[] getLowerBoundState() { return getState(lowerBound); }
	
	/**
	 * Gets the state the represents the neutral position of the {@code Movable}.
	 * 
	 * @return The state that represents the neutral position of the {@code Movable}.
	 */
	
	final ShortMessage[] getNeutralState() { return getState(neutral); }
	
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
		
} // end of Movable class