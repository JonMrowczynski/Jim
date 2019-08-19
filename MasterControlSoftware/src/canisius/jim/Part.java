package canisius.jim;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

/**
 *	This class provides the framework for the components of the Ruppet that are operated by the microcontroller.
 *
 *	The two-dimensional array states[][] contain ShortMessages which are MIDI messages that represent a certain 
 *	state for the component. For example, 
 *
 *	For the parts that move due to changes in the angular position of one or more servo motors, the two dimensional
 *	array states[][] represent the angular positions of the servo motors. A two dimensional array was implemented 
 *	so that the ShortMessages associated with the certain states 
 *
 *	For the lights, they would represent the brightness levels. Currently the lights are digitally operated though
 *	so the only meaningful states would be the first and last elements in the array.
 *
 *	@author Jon Mrowcsynski
 */

abstract class Part {
	
	/**
	 * The value that represents state that has the highest velocity value that {@code Part} should go to
	 */
	
	int upperBound = -1;
	
	/**
	 * The value that represents the state that has the lowest velocity value that the {@code Part} should go to
	 */
	
	int lowerBound = -1;
	
	/**
	 * The total number of states that the {@code Part} is able ot transition to
	 */

	final int numOfStates;

	/**
	 * The available states that a {@code Part} can go to. 
	 * <P>
	 * The reason for the 2D array is to support {@code Part}s that require multiple servo
	 * motors to be operated where each column represents the angular positions that an individual 
	 * servo motor can go to.
	 */

	final ShortMessage[][] states;
	
	/**
	 * This initializes all of the states that the {@code Part} can go to. It also 
	 * adds the part to the {@code Ruppet}'s {@code ArrayList}.
	 * 
	 * @param ruppetParts The {@code ArrayList} that represents all of the {@code Part}s of a {@code Ruppet}.
	 * @param numOfOutputs The number of MIDI outputs required to operate a given {@code Part}.
	 * @param midiNote The corresponding MIDI note that is used to operate a given component to a {@code Part}.
	 */

	Part(final List<Part> ruppetParts, final int numOfOutputs, final int midiNote, final int lowerBound, final int upperBound) {
		ruppetParts.add(this);
		checkAndSetBoundaryValues(lowerBound, upperBound);
		numOfStates = (this.upperBound - this.lowerBound) + 1;
		states = new ShortMessage[numOfStates][numOfOutputs];
		for(int i = 0; i < numOfStates; ++i) {
			states[i][0] = new ShortMessage();
			try { states[i][0].setMessage(ShortMessage.NOTE_ON, RuppetControl.CHAN_1, midiNote, i + lowerBound); } 
			catch (InvalidMidiDataException e) { e.printStackTrace(); }
		}
	}
	
	/**
	 * This method takes in an array of {@code ShortMessage}s that represent a state of the {@code Part}.
	 * It then moves the {@code Part} to the passed in state only if that state is a valid state for that {@code Part}.
	 * 
	 * @param messages The array of {@code ShortMessage}s that represent a state for the {@code Part}
	 */

	protected void toState(final ShortMessage[] messages) {
		if (validShortMessages(messages)) { Arrays.stream(messages).forEach(msg -> MidiConnection.getUsbReceiver().send(msg, -1)); }
	} 
	
	/**
	 * Performs a similar task as the other {@code toState} method, however, it accomplishes
	 * the task with the {@code velocity} value instead, but only if that {@code velocity}} value
	 * is a valid {@code velocity} value for the corresponding {@code Part}. The method figures out 
	 * what state is specified by the passed in {@code velocity} by converting the {@code velocity} value
	 * to a {@code Part} state by calling the {@code velocityToStateIndex} method.
	 * 
	 * @param velocity The {@code velocity} value that is to be converted to a {@code stateIndex}
	 */

	void toState(final int velocity) {
		if (validVelocity(velocity)) { Arrays.stream(states[velocityToStateIndex(velocity)]).forEach(msg -> MidiConnection.getUsbReceiver().send(msg, -1)); }
	}
	
	/**
	 * Simply adds the passed in state (which is represented by an array of {@code ShortMessage}s)
	 * to a given {@code Track} at a specified time (tick). However, this is only done if the passed in
	 * state is a valid state for the given {@code Part}.
	 * 
	 * @param track that is to have {@code ShortMessage}s added to it
	 * @param messages that are to be added to the {@code Track}
	 * @param tick of the {@code ShortMessage}s
	 */

	final void addStateToTrack(final Track track, final ShortMessage[] messages, final int tick) {
		if (validShortMessages(messages)) { Arrays.stream(messages).forEach(msg -> track.add(RuppetControl.makeEvent(msg, tick))); }
	} 
	
	/**
	 * Simply checks to see if the passed in velocity value is a valid velocity value for the current {@code Part}.
	 * 
	 * @param velocity The velocity value that is to be checked
	 * @return A boolean value representing of the velocity value is a valid velocity value
	 */

	final boolean validVelocity(final int velocity) {
		if (velocity >= RuppetControl.MIN_VELOCITY && velocity <= RuppetControl.MAX_VELOCITY) { return true; }
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
	 * @param messages The array of {@code ShortMessage}s that are to be checked for validity
	 * @return a {@code boolean} representing whether the given array of {@code ShortMessage} are a valid group of {@code ShortMessages}
	 */

	private boolean validShortMessages(final ShortMessage[] messages) {
		final ShortMessage[] state = states[velocityToStateIndex(RuppetControl.getVelocityVal(messages[0]))];
		for (int i = 0; i < state.length; ++i) {
			if (messages[i].getChannel() != state[i].getChannel()) { return false; }
			if (messages[i].getCommand() != state[i].getCommand()) { return false; }
			if (messages[i].getData1()   != state[i].getData1())   { return false; }
			if (messages[i].getData2()   != state[i].getData2())   { return false; }
		}
		return true;
	}

	/**
	 * Returns a {@code Part}'s state based on the velocity value, only if that velocity 
	 * value is valid for the current {@code Part}.
	 * 
	 * @param velocity The velocity value that is to be checked for validity
	 * @return The state associated with the given velocity value
	 */

	final ShortMessage[] getState(final int velocity) {
		if (validVelocity(velocity)) { return states[velocityToStateIndex(velocity)]; }
		else { throw new InvalidParameterException(" for velocityToStateIndex conversion. No State can be returned."); }
	}
	
	
	/**
	 * Checks to see if the passed in boundary values are valid. If they are, then assign 
	 * the {@code lowerBound} value to the {@code lowerBound} value of the {@code Part} 
	 * and do a similar thing for the {@code upperBound} value. Else throw an {@code InvalidParameterException}
 	 *
 	 * @param lowerBound The lowest velocity value that the {@code Part} can know
 	 * @param upperBound The highest velocity value that the {@code Part} can know
	 */

	private void checkAndSetBoundaryValues(final int lowerBound, final int upperBound) {
		if (lowerBound <= upperBound) {
			if (validVelocity(lowerBound)) { this.lowerBound = lowerBound; }
			else { throw new InvalidParameterException("for lowerBound. Boundary values could not be set."); }
			if (validVelocity(upperBound)) { this.upperBound = upperBound; }
			else { throw new InvalidParameterException("for upperBound. Boundary values could not be set."); }
		} else { throw new InvalidParameterException("Invalid boundary values: " +
				"\nlowerBound: " + lowerBound +
				"\nupperBound: " + upperBound +
				"\nCould not instantiate Movable Part."); }
	}
	
	/**
	 * Ultimately the conversion from {@code velocity} to {@code stateIndex} for any number of states is:
	 *  
	 * @param velocity The velocity value that is to be converted to a {@code stateIndex}
	 * @return The {@code stateIndex}.
	 */

	private int velocityToStateIndex(final int velocity) { return velocity - lowerBound; }

} // end of Part class


