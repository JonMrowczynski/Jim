package canisius.jim.parts;

import java.util.List;

/**
 * {@code Lights} represents the digital eye lights of the {@code Ruppet}, which can be turned either on or off.
 *
 *  @author Jon Mrowczynski
 */

public final class Lights extends Part {

	/**
	 * The on value of the {@code Lights}.
	 */

	private static final byte OFF = 0;

	/**
	 * The off value of the {@code Lights}.
	 */

	private static final byte ON = 10;

	/**
	 * Takes a {@code Ruppet}'s {@code List} of {@code Part}s as well as a MIDI note which should be associated with
	 * these {@code Lights} in order to operate them.
	 * 
	 * @param ruppetParts of a {@code Ruppet}.
	 * @param midiNote that should be associated with these {@code Lights}.
	 */

	public Lights(final List<Part> ruppetParts, final byte midiNote) {
		super(ruppetParts, midiNote, OFF, ON);
		setNeutral(OFF);
	}

	/**
	 * Turns the {@code Ruppet}'s eye lights on.
	 */

	public final void on() { toState(ON); }
	
	/**
	 * Turns the {@code Ruppet}'s eye lights off.
	 */
	
	final void off() { toState(OFF); }
	
} // end of Light

