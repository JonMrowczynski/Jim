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

final class Lights extends Part {

	/**
	 * The maximum brightness that the eye lights of the {@code Ruppet} should have.
	 */
	
	private final byte maxBrightness;
	
	/**
	 * The minimum brightness that the eye lights of the {@code Ruppet} should have.
	 */
	
	private final byte minBrightness;

	/**
	 * Represents the current brightness of the lights.
	 */

	private byte brightness;

	/**
	 * This constructor takes the {@code ArrayList} of {@code Ruppet Part}s that 
	 * the {@code Ruppet} physically possesses. 
	 * <P>
	 * It also takes a MIDI note which is the note that is associated with the 
	 * {@code Lights} object which is used to operate the eye lights.  
	 * <P>
	 * Lastly it takes two integer parameters that represent the lower and 
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

	Lights(final List<Part> ruppetParts, final byte midiNote, final byte lowerBound, final byte upperBound) {
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

	final void on() { toState(maxBrightness); }
	
	/**
	 * Turn the {@code Ruppet}'s eye lights off.
	 */
	
	final void off() { toState(minBrightness); }

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
	 * Makes sure that the passed in brightness value is within a given range of values.
	 * This range is ultimately determined by the PIC microcontroller firmware and 
	 * the firmware should be referred to if any modifications to those bounding 
	 * values need to be changed.
	 * 
	 * @param brightness value that is being checked.
	 * @return A {@code boolean} representing whether the velocity value valid.
	 */

	private boolean validBrightness(final byte brightness) { return validVelocity(brightness); }
	
} // end of Light class
