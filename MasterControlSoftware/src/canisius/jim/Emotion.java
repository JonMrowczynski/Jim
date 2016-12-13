package canisius.jim;

/**
	This class represents the emotional states that a Ruppet can express.
	
	@author Jon Mrowczynski
	@version 1.0
*/

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class Emotion {
	
	/**
	 * Each emotion has a {@code Ruppet} associated with it as well as an {@code ArrayList}
	 * that represents the states that the {@code Ruppet} goes to in order to express that emotion.
	 */

	private final List<ShortMessage> attributes = new ArrayList<>();
	
	/**
	 * The only constructor for an {@code Emotion}. It takes a {@code Ruppet} that the emotion 
	 * is associated with as well as a variable amount of {@code PartState}s which the associated 
	 * {@code ShortMessage}s of the {@code PartState} gets added to the {@code Emotion} attributes 
	 * {@code ArrayList}
	 * <P>
	 * *NOTE*: That currently, only {@code Movable Part}s are meaningful in emotional states.
	 * 
	 * @param ruppet The {@code Ruppet} that the {@code Emotion} is associated with
	 * @param emotionPartStates The {@code PartStates} that are transitioned to for this {@code Emotion}
	 */

	public Emotion(final Ruppet ruppet, final PartState... emotionPartStates) {

		if (emotionPartStates.length > 0) {

			final List<Part> ruppetParts = new ArrayList<>();

			for (Part part : ruppet.getParts())
				if (part instanceof Movable) ruppetParts.add(part);

			/* For every PartState that was passed into the Emotion constructor add its state 
			   to the Emotion's attribute ArrayList */

			for (PartState emotionPartState : emotionPartStates) {

				ruppetParts.remove(emotionPartState.getPart());

				if (emotionPartState.getPart() instanceof Movable)
					RuppetControl.addStateToList(this.attributes, emotionPartState.getState());

			} 

			/* If there are still some Parts left over that are not a part of the current Emotion 
			   then add their neutral positions to the current Emotion's ArrayList so that a given 
			   state does not carry over from one emotion to another */

			for (Part ruppetPart : ruppetParts)
				if (ruppetPart instanceof Movable)
					RuppetControl.addStateToList(this.attributes, ( (Movable) ruppetPart).getNeutralState());

		} else
			throw new InvalidParameterException("emotionPartStates.length == 0. Please make sure that"
				+ " you passed in part states so that this emotion can be defined.");

	} // end of Emotion constructor 
	
	/**
	 * Creates a {@code Track} that can be added to a {@code Sequence} such that an {@code Emotion}
	 * can be synchronized with other emotional transitions, mouth movements, etc.
	 * <P>
	 * *NOTE*: That the lower jaw of the {@code Ruppet} is disabled to prevent interference between the 
	 * {@code Emotion} and the {@code Ruppet} talking.
	 * 
	 * @param track The {@code Track} that the {@code Emotion} is to be added to
	 * @param tick The point in time that the {@code Emotion} should be transitioned to
	 */

	public final void addEmotionToTrack(final Track track, final int tick) {

		for (ShortMessage msg : attributes)
			if (RuppetControl.getMidiNote(msg) != RuppetControl.LOWER_JAW)
				track.add(RuppetControl.makeEvent(msg, tick));

	}

	/**
	 * Returns an {@code ArrayList} that contains a collection of {@code ShortMessage}s that represent
	 * the states that need to be transitioned to in order for the {@code Ruppet} to express the corresponding
	 * {@code Emotion}.
	 * 
	 * @return The {@code ShortMessages} that are associated with the emotional state
	 */

	public final List<ShortMessage> getAttributes() { return attributes; }

} // end of Emotion class


