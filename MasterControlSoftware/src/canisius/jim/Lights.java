package canisius.jim;

import java.util.List;

/**
 * A {@code Lights} represents the digital eye lights of the {@code Ruppet}, which can be turned either on or off.
 *
 *  @author Jon Mrowczynski
 */

final class Lights extends Part {

	/**
	 * The on value of the {@code Lights}.
	 */

	private static final byte OFF = 0;

	/**
	 * The off value of the {@code Lights}.
	 */

	private static final byte ON = 10;

	/**
	 * This constructor takes the {@code List} of {@code Ruppet Part}s that the {@code Ruppet} physically possesses.
	 *
	 * It also takes a MIDI note which is the note that is associated with the {@code Lights} object which is used to
	 * operate the eye lights.
	 * 
	 * @param ruppetParts The {@code List} of {@code Part}s of the {@code Ruppet}.
	 * @param midiNote The MIDI note associated with this {@code Lights object}.
	 */

	Lights(final List<Part> ruppetParts, final byte midiNote) {
		super(ruppetParts, (byte) 1, midiNote, ON, OFF);
	}

	/**
	 * Turns the {@code Ruppet}'s eye lights on.
	 */

	final void on() { toState(ON); }
	
	/**
	 * Turns the {@code Ruppet}'s eye lights off.
	 */
	
	final void off() { toState(OFF); }
	
} // end of Light class

