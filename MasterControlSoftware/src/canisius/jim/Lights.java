package canisius.jim;

import java.util.List;

import javax.sound.midi.ShortMessage;

/**
	The {@code Lights Part} currently represents the eye lights of the {@code Ruppet}.

	Since the PIC is not set up to handle analog inputs, the methods 
	automatically correct for that such that the lights are digitally
	controlled. Therefore they currently only turn on and off.
	
	@author Jon Mrowczynski
	@version 1.0
*/

public final class Lights extends Part {

	/**
	 * The maximum brightness that the eye lights of the {@code Ruppet} should have.
	 */
	
	private byte maxBrightness = -1;
	
	/**
	 * The minimum brightness that the eye lights of the {@code Ruppet} should have.
	 */
	
	private byte minBrightness = -1;

	/**
	 * Represents the current brightness of the lights.
	 */

	private byte brightness = -1;

	/**
	 * This constructor takes the {@code ArrayList} of {@code Ruppet Part}s that 
	 * the {@code Ruppet} physically possesses. 
	 * <P>
	 * It also takes a MIDI note which is the note that is associated with the 
	 * {@code Lights} object which is used to operate the eye lights.  
	 * <P>
	 * Lastly it takes as parameters two integer values that represent the lower and 
	 * upper bounds that the states for the lights can take. This is to set some 
	 * brightness ranges that the lights can have as well as to make sure that 
	 * the lights do not get overloaded if they are controlled by an analog output 
	 * from the PIC microcontroller.
	 * 
	 * @param ruppetParts The {@code ArrayList} of {@code Part}s of the {@code Ruppet}.
	 * @param midiNote The MIDI note associated with this {@code Lights object}.
	 * @param lowerBound The minimum value for a state for the {@code Lights} object.
	 * @param upperBound The maximum value for a state for the {@code Lights} object.
	 */

	public Lights(final List<Part> ruppetParts, final byte midiNote, final byte lowerBound, final byte upperBound) { 
		
		super(ruppetParts, (byte) 1, midiNote, lowerBound, upperBound); 

		minBrightness = lowerBound;
		maxBrightness = upperBound;
		brightness = minBrightness;
	}

	/**
	 * Sets the brightness value of the {@code Ruppet}'s eye lights.
	 * <P>
	 * First, the integer value is checked to make sure that it represents a valid brightness value.
	 * If the new brightness value is valid, it sets the lights to off if the value is less than
	 * the maximum brightness value or it turns the lights on if the brightness value is equal 
	 * to the maximum brightness value.
	 * 
	 * @param newBrightness The new brightness value for the eye lights of the {@code Ruppet}.
	 */
	
	public final void setBrightness(final byte newBrightness) {

		if (validBrightness(newBrightness))
			if (newBrightness >= minBrightness && newBrightness < maxBrightness) 
				brightness = minBrightness;
			else 
				brightness = maxBrightness;
	} 

	/**
	 * Turns the {@code Ruppet}'s eye lights on.
	 */

	public final void on() { toState(maxBrightness); } 
	
	/**
	 * Turn the {@code Ruppet}'s eye lights off.
	 */
	
	public final void off() { toState(minBrightness); }

	/**
	 * Gets the current brightness level of the {@code Ruppet}'s eye lights.
	 * 
	 * @return The current brightness of the {@code Ruppet}'s eye lights.
	 */

	public final byte getBrightness() { return brightness; }
	
	/**
	 * Gets the maximum brightness level of the {@code Ruppet}'s eye lights.
	 * 
	 * @return The maximum brightness that the lights should go.
	 */
	
	public final byte getMaxBrightness() { return maxBrightness; }
	
	/**
	 * Gets the minimum brights level of the {@code Ruppet}'s eye lights.
	 * 
	 * @return The minimum brightness that the lights should go.
	 */
	
	public final byte getMinBrightness() { return minBrightness; }
	
	/**
	 * Gets the state that represents the {@code Ruppet}'s eye lights being on.
	 * 
	 * @return The state that represents the {@code Ruppet}'s eye lights being on.
	 */

	public final ShortMessage[] getLightOn() { return getState(maxBrightness); }
	
	/**
	 * Gets the state that represents the {@code Ruppet}'s eye lights being off.
	 * 
	 * @return The state that represents the {@code Ruppet}'s eye lights being off.
	 */
	
	public final ShortMessage[] getLightsOff() { return getState(minBrightness); }
	
	/**
	 * Returns a {@code String} that contains information about the minimum, maximum and 
	 * current brightness levels that this {@code Light} object currently has set.
	 * 
	 * @return A {@code String} representation of all of the fields of the {@code Light}
	 */
	 
	public final String toString() {
		
		String lines = "For the Lights object: \n\n";
		lines += "maxBrightness: " + maxBrightness + "\n";
		lines += "minBrightness: " + minBrightness + "\n";
		lines += "brightness: " + brightness + "\n";
		lines += "numOfStates: " + numOfStates + "\n";
		
		final int numOfLines = lines.length();
		for (int i = numOfLines; i < numOfStates + numOfLines; ++i) 
			lines += "Velocity at state " + (i - numOfLines) + ": " +
					 RuppetControl.getVelocityVal(states[i - numOfLines][0]) + "\n";
			
		return lines;
		
	} // end of toString
	
	/**
	 * Makes sure that the passed in brightness value is within a given range of values.
	 * This range is ultimately determined by the PIC microcontroller firmware and 
	 * the firmware should be referred to if any modifications to those bounding 
	 * values need to be changed.
	 * 
	 * @param brightness The brightness value that is being checked for validity.
	 * @return A {@code boolean} representing whether the velocity value is a valid velocity value.
	 */

	private final boolean validBrightness(final byte brightness) { return validVelocity(brightness); }
	
} // end of Light class

