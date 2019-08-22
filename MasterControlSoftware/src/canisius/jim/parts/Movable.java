package canisius.jim.parts;

import canisius.jim.ruppet.RuppetUtils;

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

public final class Movable extends Part {
	
	/**
	 * If the {@code Movable} only requires one servo motor to be operated, then the {@code Movable} constructor calls
	 * its superclass's constructor and sets {@code neutral} to the average of the {@code lowerBound} and
	 * {@code upperBound}.
	 * 
	 * @param ruppetParts of the {@code Ruppet}.
	 * @param midiNote associated with the servo(s) that is/are to operate this {@code Movable}.
	 * @param lowerBound that the servo arm can move to.
	 * @param upperBound that the servo arm can move to.
	 * @see Part#Part(List, int, int, int)
	 */

    public Movable(final List<Part> ruppetParts, final int midiNote, final int lowerBound, final int upperBound) {
		super(ruppetParts, midiNote, lowerBound, upperBound);
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
	 * @see Part#Part(List, int, int, int)
	 */

    public Movable(final List<Part> ruppetParts, final int midiNote1, final int lowerBound, final int upperBound, final int midiNote2, final String parallelism)
			throws InvalidParameterException {
	    this(ruppetParts, midiNote1, lowerBound, upperBound);
		for (int i = 0; i < states.size(); ++i) {
			try {
				switch(parallelism.toLowerCase()) {
                    case "parallel":
                        states.get(i).add(new ShortMessage(ShortMessage.NOTE_ON, RuppetUtils.CHAN_1, midiNote2, i + lowerBound));
                        break;
                    case "antiparallel":
                        states.get(i).add(new ShortMessage(ShortMessage.NOTE_ON, RuppetUtils.CHAN_1, midiNote2, ((upperBound + lowerBound) - (i + lowerBound))));
                        break;
                    default:
                        throw new InvalidParameterException("The String: " + parallelism + " is not defined for this constructor.");
                }
			} catch(InvalidMidiDataException ex) { ex.printStackTrace(); }
		}
	}
		
} // end of Movable class