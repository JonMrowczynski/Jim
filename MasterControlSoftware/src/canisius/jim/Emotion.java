package canisius.jim;

import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An {@code Emotion} is a set of {@code ShortMessage}s that, when sent to the control electronics, sets servo motor
 * states such that a {@code Ruppet} expresses the corresponding emotion.
 *
 *  @author Jon Mrowczynski
 */

final class Emotion {

	/**
	 * Contains the {@code ShortMessage}s that are to be sent to the control electronics to make the {@code Ruppet}
	 * express an emotion.
	 */

	private final Set<ShortMessage> attributes = new HashSet<>();
	
	/**
	 * Takes a {@code Ruppet} that the emotion is associated with as well as a variable amount of {@code PartState}s
	 * which the associated {@code ShortMessage}s of the {@code PartState} gets added to the {@code Emotion} attributes
	 * {@code ArrayList}.
	 *
	 * Currently, only {@code Movable Part}s are meaningful in emotional states.
	 * 
	 * @param ruppet that the {@code Emotion} belongs to.
	 * @param emotionPartStates that are transitioned to for this {@code Emotion}.
	 */

	Emotion(final Ruppet ruppet, final PartState... emotionPartStates) {
		if (emotionPartStates.length > 0) {
			final List<Part> ruppetParts = new ArrayList<>();
			ruppet.getParts().stream().filter(part -> part instanceof Movable).forEach(ruppetParts::add);
			addEmotionPartStates(emotionPartStates);
			/*
			 * If there are Parts that are not part of the current Emotion then add their neutral positions to this
			 * Emotion's attributes so that a given state does not carry over from one Emotion to another.
			 */
			ruppetParts.removeAll(Arrays.stream(emotionPartStates).map(PartState::getPart).collect(Collectors.toList()));
			addNeutralPositions(ruppetParts);
		} else { throw new InvalidParameterException("emotionPartStates.length cannot = 0."); }
	}
	
	/**
	 * Adds this {@code Emotion} to the given {@code track} at the given {@code tick} so that when it is added to a
	 * {@code Sequence} the {@code Emotion} can be synchronized and/or sequenced with other emotional transitions, mouth
	 * movements, etc.
	 *
	 * The lower jaw of the {@code Ruppet} is disabled to prevent interference between the {@code Emotion} and the
	 * {@code Ruppet} talking.
	 * 
	 * @param track that the {@code Emotion} is to be added to.
	 * @param tick that the {@code Emotion} should be transitioned to.
	 */

	final void addEmotionToTrack(final Track track, final int tick) {
		attributes.stream().filter(msg -> RuppetControl.getMidiNote(msg) != RuppetControl.LOWER_JAW)
				.forEach(msg -> track.add(RuppetControl.makeEvent(msg, tick)));
	}

	/**
	 * For every {@code PartState} in {@code emotionPartStates} add its {@code State} to this {@code Emotion}'s
	 * {@code attributes} iff the {@code PartState}'s corresponding {@code Part} is a {@code Movable}. Simultaneously,
	 * remove the corresponding {@code Part} from {@code ruppetParts} iff i
	 *
	 * @param emotionPartStates whose states are to be added to {@code attributes}.
	 */

	private void addEmotionPartStates(final PartState[] emotionPartStates) {
		Arrays.stream(emotionPartStates).filter(emotionPartState -> emotionPartState.getPart() instanceof Movable)
				.map(PartState::getState).forEach(state -> attributes.addAll(Arrays.asList(state)));
	}

	/**
	 * Adds the neutral states all of {@code Part}s in {@code ruppetParts} to this {@code Emotion}'s {@code attributes}.
	 *
	 * @param ruppetParts whose neutral state is to be added to this {@code Emotion}'s {@code attributes}.
	 */

	private void addNeutralPositions(final List<Part> ruppetParts) {
		ruppetParts.stream().filter(ruppetPart -> ruppetPart instanceof Movable)
				.forEach(ruppetPart -> RuppetControl.addStateToList(attributes, ((Movable) ruppetPart).getNeutralState()));
	}

	/**
	 * Returns an {@code Iterator} of the {@code ShortMessage}s that need to be transitioned to the control electronics
	 * in order for the {@code Ruppet} to express the corresponding {@code Emotion}.
	 * 
	 * @return The {@code ShortMessages} that are associated with the emotional state.
	 */

	final Iterator<ShortMessage> getAttributes() { return attributes.iterator(); }

} // end of Emotion class


