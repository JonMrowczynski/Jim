package canisius.jim;

import javax.sound.midi.ShortMessage;

/**
	This is simply a data structure similar to that of a Pair, however with
	additional meaning since only two types of objects can be paired together 
	with this class: a Part and a given State that is associated with that part.
	This data structure is mainly implemented for its assistance with the Heart
	class and dealing with Emotions. 
	
	@author Jon Mrowczynski
	@version 1.0
*/

public class PartState {

	private Part part = null;
	private ShortMessage[] state = null;
	
	/**
	 * The constructor simply takes a {@code Part} and a corresponding state that 
	 * is associated with that given {@code Part}.
	 * 
	 * @param part A {@code Part} of the {@code Ruppet}
	 * @param state The state associated with the given {@code Part}
	 */

	public PartState(final Part part, final ShortMessage[] state) {

		this.part = part;
		this.state = state;

	}

	/**
	 * Gets the {@code Part} that is stored in the {@code Part} state pair.
	 * 
	 * @return The {@code Part} in the {@code Part} state pair
	 */

	public final Part getPart() { return part; }
	
	/**
	 * Gets the state that is stored in the {@code Part} state pair.
	 * 
	 * @return The state in the {@code Part} state pair.
	 */
	
	public final ShortMessage[] getState() { return state; }

} // end of PartState



