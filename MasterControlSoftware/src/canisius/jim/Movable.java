package canisius.jim;

import java.security.InvalidParameterException;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

/**
	This class represents all of the parts of the {@code Ruppet} that are controlled by one or 
	more servo motors. If the {@code Movable Part} requires two servo motors to be operated, then
	the states for the second servo motor depends on whether their motion will be in parallel 
	or antiparallel with one another. If their motion is in parallel, then the velocity values 
	will be the same as the first servo motors'. If the motion is antiparallel, then the velocity 
	values will be inversely related.
	
	@author Jon Mrowczynski
	@version 1.2
*/

public final class Movable extends Part {
	
	private byte neutral = -1;
	
	/**
	 * If the {@code Movable Part} only requires one servo motor to be operated, then the {@code Movable}
	 * constructor simply calls its superclass's constructor.
	 * 
	 * @param ruppetParts The {@code ArrayList} of all of the {@code Part}s of the {@code Ruppet}
	 * @param servo The MIDI note associated with this specific servo that is to operate this {@code Movable Part}
	 * @param lowerBound The lowest angular position that the servo arm should move to
	 * @param upperBound The highest angular position that the servo arm should move to 
	 */

	public Movable(final List<Part> ruppetParts, final byte servo, final byte lowerBound, final byte upperBound) { 
		super(ruppetParts, (byte) 1, servo, lowerBound, upperBound); 
		neutral = (byte) ((upperBound + lowerBound) / 2);
	}
	
	/**
	 * If the {@code Movable Part} requires two servo motors to be operated, then it first calls 
	 * the superclass's constructor to create the state for the first servo motor and then it creates 
	 * the state for the second servo motor.
	 * 
	 * @param ruppetParts The {@code ArrayList} of all of the {@code Part}s of the {@code Ruppet}
	 * @param servo1 The MIDI note that is associated with the first servo motor
	 * @param lowerBound The lowest angular position that the servo arm should move to
	 * @param upperBound The highest angular position that the servo arm should move to
	 * @param servo2 The MIDI note that is associated with the second servo motor
	 * @param parallelism Represents whether the servo motors are to be operated in parallel or anti-parallel from one another
	 */

	public Movable(final List<Part> ruppetParts, final byte servo1, final byte lowerBound, final byte upperBound, final byte servo2, final String parallelism) {
		super(ruppetParts, (byte) 2, servo1, lowerBound, upperBound);
		neutral = (byte) ((upperBound + lowerBound) / 2);
		for(int i = 0; i < numOfStates; ++i) {
			states[i][1] = new ShortMessage();
			try {
				if (parallelism.toLowerCase().equals("parallel"))
					states[i][1].setMessage(ShortMessage.NOTE_ON, RuppetControl.CHAN_1, servo2, i + lowerBound);
				else if (parallelism.toLowerCase().equals("antiparallel"))
					states[i][1].setMessage(ShortMessage.NOTE_ON, RuppetControl.CHAN_1, servo2, ((upperBound + lowerBound) - (i + lowerBound)));
				else 
					throw new InvalidParameterException("The String: " + parallelism + " is not defined for this constructor.");
			} catch(InvalidMidiDataException ex) { ex.printStackTrace(); }
		}
	}
	
	/**
	 * Allows the neutral position of the {@code Movable Part} to be dynamically determined.
	 * 
	 * @param newNeutral The new neutral position of the {@code Movable Part}
	 */

	public final void setNeutral(final byte newNeutral) {
		if (newNeutral >= lowerBound && newNeutral <= upperBound) 
			neutral = newNeutral;
		else
			throw new InvalidParameterException("Cannot set the neutral state value to: " + newNeutral
				+ "\nThe provided value is not within the defined acceptable range of values: "
				+ lowerBound + "-" + upperBound);
	}
	
	/**
	 * Moves the {@code Movable} to one of its most extreme angular positions.
	 */
	
	public final void toUpperBound() { toState(upperBound); }
	
	/**
	 * Moves the {@code Movable} to its other most extreme angular position that is 
	 * in the opposite direction.
	 */
	
	public final void toLowerBound() { toState(lowerBound); }
	
	/**
	 * Moves the {@code Movable} to its neutral position.
	 */
	
	public final void toNeutral() { toState(neutral); 	}
	
	/**
	 * Gets the state that represents the maximum position that the {@code Movable Part}
	 * can move to in one direction.
	 * 
	 * @return The state that represents the upper bound of the {@code Movable Part}.
	 */
	
	public final ShortMessage[] getUpperBoundState() { return getState(upperBound); } 
	
	/**
	 * Gets the state the represents the maximum position that the {@code Movable}
	 * can move to in the other direction.
	 * 
	 * @return The state the represents the lower bound of the {@code Movable Part}.
	 */
	
	public final ShortMessage[] getLowerBoundState() { return getState(lowerBound); }  
	
	/**
	 * Gets the state the represents the neutral position of the {@code Movable Part}.
	 * 
	 * @return The state that represents the neutral position of the {@code Movable Part}.
	 */
	
	public final ShortMessage[] getNeutralState() { return getState(neutral); } 
	
	/**
	 * Gets the upper bound value for the {@code Movable Part}.
	 * 
	 * @return The integer value that represents the upper bound of the {@code Movable Part}.
	 */

	public final byte getUpperBound() { return upperBound; }
	
	/**
	 * Gets the lower bound value for the {@code Movable Part}.
	 * 
	 * @return The integer value that represents the lower bound of the {@code Movable Part}.
	 */
	
	public final byte getLowerBound() { return lowerBound; }
	
	/**
	 * Gets the neutral value for the {@code Movable Part}.
	 * 
	 * @return The integer value that represents the neutral position of the {@code Movable Part}.
	 */
	
	public final byte getNeutral() { return neutral; }
	
	/**
	 * Gets the state that represents the {@Movable Part}'s neutral position.
	 * 
	 * @return The state that represents the {@Movable Part}'s neutral position.
	 */
	
	public final PartState getNeutralPartState() { return new PartState(this, getState(neutral)); }
	
	/**
	 * Gets the corresponding part state associated with the given velocity value if the velocity 
	 * value is a valid velocity value. Otherwise, an {@code InvalidParameterException} is throw.
	 * 
	 * @param velocity The representation of an angular position of a servo arm
	 * @return The {@code PartState} that represents the state of the corresponding {@code Ruppet Part}
	 */

	public final PartState getPartState(final byte velocity) {
		if (validVelocity(velocity) && velocity >= lowerBound && velocity <= upperBound)
			return new PartState(this, getState(velocity));
		else
			throw new InvalidParameterException("Cannot retrieve the PartState associated with the velocity value: " + velocity);
	} 
	
	@Override
	public final String toString() {
		String lines = "For Movable object: \n\n";
		lines += "upperBound: " + upperBound + "\n";
		lines += "lowerBound: " + lowerBound + "\n";
		lines += "neutral: " + neutral + "\n";
		lines += "numOfStates: " + numOfStates + "\n";
		final int numOfMovableParts = states[0].length;
		for (int i = 1; i <= numOfMovableParts; ++i) {
			if (numOfMovableParts > 1)
				lines += "For Movable Part " + i + "\n";
			for (int j = 0; j < numOfStates; ++j)
				lines += "Velocity value for state " + j + ": " + 
						 RuppetControl.getVelocityVal(states[j][0]) + "\n";
		}
		return lines;
	}
		
} // end of Movable class