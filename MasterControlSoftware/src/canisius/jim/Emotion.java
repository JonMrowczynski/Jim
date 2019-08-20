package canisius.jim;

import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An {@code Emotion} is a {@code Set} of {@code ShortMessage}s that when sent to the electronics sets the angular
 * positions of the servo motor such that a {@code Ruppet} expresses the corresponding emotion.
 *
 *  @author Jon Mrowczynski
 */

final class Emotion {

	/**
	 * The {@code Set} of {@code ShortMessage}s that are sent to the electronics to make the {@code Ruppet} express this
	 * {@code Emotion}.
	 */

	private final Set<ShortMessage> attributes = new HashSet<>();
	
	/**
	 * Takes a {@code Ruppet} that this {@code Emotion} is associated with as well as a variable amount of
	 * {@code PartState}s. The {@code PartState}s and their associated {@code ShortMessage}s get added to the
	 * {@code Emotion} attributes {@code Set}.
	 *
	 * Currently, only {@code Movable}s are meaningful in emotional states.
	 * 
	 * @param ruppet that the {@code Emotion} belongs to.
	 * @param emotionPartStates that are transitioned to for this {@code Emotion}.
	 * @throws InvalidParameterException if {@code emotionPartStates.length == 0}.
	 */

	Emotion(final Ruppet ruppet, final PartState... emotionPartStates) throws InvalidParameterException {
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
		} else { throw new InvalidParameterException("emotionPartStates.length cannot be 0."); }
	}
	
	/**
	 * Adds this {@code Emotion} to {@code track} at {@code tick} so that when the {@code Track} is added to a
	 * {@code Sequence} the {@code Emotion} can be synchronized and/or sequenced with other emotional transitions, mouth
	 * movements, etc.
	 *
	 * The lower jaw of the {@code Ruppet} is disabled to prevent interference between having the {@code Ruppet} express
	 * this {@code Emotion} and moving its lower jaw to talk.
	 * 
	 * @param track that the {@code Emotion} is to be added to.
	 * @param tick that the {@code Emotion} should be transitioned to.
	 */

	final void addEmotionToTrack(final Track track, final int tick) {
		attributes.stream().filter(msg -> RuppetUtils.getMidiNote(msg) != Ruppet.LOWER_JAW_MIDI_NOTE)
				.forEach(msg -> track.add(RuppetUtils.makeEvent(msg, tick)));
	}

	/**
	 * For every {@code PartState} in {@code emotionPartStates} add its {@code State} to this {@code Emotion}'s
	 * {@code attributes} iff the {@code PartState}'s corresponding {@code Part} is a {@code Movable}.
	 *
	 * @param emotionPartStates whose states are to be added to {@code attributes}.
	 */

	private void addEmotionPartStates(final PartState[] emotionPartStates) {
		Arrays.stream(emotionPartStates).filter(emotionPartState -> emotionPartState.getPart() instanceof Movable)
				.map(PartState::getState).forEach(attributes::addAll);
	}

	/**
	 * Adds the neutral states all of {@code Part}s in {@code ruppetParts} to this {@code Emotion}'s {@code attributes}.
	 *
	 * @param ruppetParts whose neutral state is to be added to this {@code Emotion}'s {@code attributes}.
	 */

	private void addNeutralPositions(final List<Part> ruppetParts) {
		ruppetParts.stream().filter(ruppetPart -> ruppetPart instanceof Movable).map(Part::getNeutralState).forEach(attributes::addAll);
	}

	/**
	 * Returns an {@code Iterator} of the {@code ShortMessage}s that need to be transitioned to the electronics in order
	 * for the {@code Ruppet} to express the corresponding {@code Emotion}.
	 * 
	 * @return The {@code ShortMessage}s that are associated with this {@code Emotion}al state.
	 */

	final Iterator<ShortMessage> getAttributes() { return attributes.iterator(); }

} // end of Emotion class


