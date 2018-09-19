package canisius.jim;

import javax.sound.midi.ShortMessage;

/**
 * This is a simple data structure that pairs a {@code Part} with a state that the 
 * part can be in, where the state is represented by a {@code ShortMessage} array that
 * contains {@code MidiMessage}s that are to be sent to the {@code Ruppet}.
 * <p>
 * This data structure is mainly implemented to help with the {@code Heart}, {@code Movable},
 * and {@code Emotion} class.
 * 
 * @author Jon Mrowczynski
 */

final class PartState {
	
	/**
	 * The {@code Part} that is to be paired with a state in this {@code PartState} pair.
	 */

	private final Part part;
	
	/**
	 * The state of the part represented by an array of {@code ShortMessage}s.
	 */
	
	private final ShortMessage[] state;
	
	/**
	 * Pairs together a {@code Part} and an array of {@code ShortMessage}s which represents a state that
	 * the {@code Part} can be in.
	 * 
	 * @param part of the {@code Ruppet}
	 * @param state associated with the given {@code Part}
	 */

	PartState(final Part part, final ShortMessage[] state) {
		this.part = part;
		this.state = state;
	}

	/**
	 * Gets the {@code Part} of the {@code PartState} pair.
	 * 
	 * @return the {@code Part} of the {@code PartState} pair.
	 */

	final Part getPart() { return part; }
	
	/**
	 * Gets the {@code ShortMessage} array that represents a state that corresponds to the 
	 * {@code Part} in the {@code PartState} pair. 
	 * 
	 * @return the state of the {@code PartState} pair.
	 */
	
	final ShortMessage[] getState() { return state; }

} // end of PartState class



