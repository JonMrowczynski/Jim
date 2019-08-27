package canisius.jim.parts;

import javax.sound.midi.ShortMessage;
import java.util.Set;

/**
 * A {@code PartState} pairs a {@code Part} with a state that the part can be in. A state state is represented by a
 * {@code Set} of {@code ShortMessage}s that are to be sent to the electronics.
 * 
 * @author Jon Mrowczynski
 */

final class PartState {
	
	/**
	 * The {@code Part} of the {@code PartState} pair.
	 */

	private final Part part;
	
	/**
	 * The state of the {@code PartState} pair.
	 */
	
	private final Set<ShortMessage> state;
	
	/**
	 * Pairs together a {@code Part} and a state that the {@code Part} can be in where a state is a {@code Set} of
	 * {@code ShortMessage}s.
	 * 
	 * @param part of the {@code Ruppet}.
	 * @param state that the {@code Part} can be in.
	 * @throws NullPointerException if {@code part} or {@code state} is {@code null}.
	 */

	PartState(final Part part, final Set<ShortMessage> state) throws NullPointerException {
		if (part == null) { throw new NullPointerException("part cannot be null"); }
		if (state == null) { throw new NullPointerException("state cannot be null"); }
		this.part = part;
		this.state = state;
	}

	/**
	 * Gets the {@code Part} of the {@code PartState} pair.
	 * 
	 * @return The {@code Part} of the {@code PartState} pair.
	 */

	final Part getPart() { return part; }
	
	/**
	 * Gets the {@code Set} of {@code ShortMessage}s that represents the state of the {@code PartState} pair.
	 * 
	 * @return The {@code Set} of {@code ShortMessage}s that represents the state of the {@code PartState} pair.
	 */
	
	final Set<ShortMessage> getState() { return state; }

} // end of PartState



