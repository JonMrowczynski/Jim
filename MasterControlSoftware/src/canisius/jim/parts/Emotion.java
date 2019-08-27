package canisius.jim.parts;

import canisius.jim.ruppet.Ruppet;

import javax.sound.midi.MidiEvent;
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
	 * @param partStates that are transitioned to for this {@code Emotion}.
	 * @throws InvalidParameterException if {@code partStates.length == 0}.
	 */

	Emotion(final Ruppet ruppet, final PartState... partStates) throws InvalidParameterException {
		if (partStates.length > 0) { addEmotionPartStates(ruppet, partStates); }
		else { throw new InvalidParameterException("partStates.length cannot be 0."); }
	}

	/**
	 * For every {@code PartState} in {@code partStates} add its {@code State} to this {@code Emotion}'s
	 * {@code attributes} iff the {@code PartState}'s corresponding {@code Part} is a {@code Movable}. If there are
	 * {@code Part}s that are not used to express this {@code Emotion}, then add their neutral positions to this
	 * {@code Emotion}'s attributes so that a given state does not carry over from one {@code Emotion} to another.
	 *
	 * @param partStates whose states are to be added to {@code attributes}.
	 */

	private void addEmotionPartStates(final Ruppet ruppet, final PartState[] partStates) {
		final List<Part> ruppetParts = new ArrayList<>();
		ruppet.getParts().stream().filter(part -> part instanceof Movable).forEach(ruppetParts::add);
		Arrays.stream(partStates).filter(partState -> partState.getPart() instanceof Movable).map(PartState::getState).forEach(attributes::addAll);
		ruppetParts.removeAll(Arrays.stream(partStates).map(PartState::getPart).collect(Collectors.toList()));
		ruppetParts.stream().filter(ruppetPart -> ruppetPart instanceof Movable).map(Part::getNeutralState).forEach(attributes::addAll);
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
		attributes.stream().filter(msg -> getMidiNote(msg) != Ruppet.LOWER_JAW_MIDI_NOTE).forEach(msg -> track.add(new MidiEvent(msg, tick)));
	}

	/**
	 * Returns an {@code Iterator} of the {@code ShortMessage}s that need to be transitioned to the electronics in order
	 * for the {@code Ruppet} to express the corresponding {@code Emotion}.
	 * 
	 * @return The {@code ShortMessage}s that are associated with this {@code Emotion}al state.
	 */

	final Iterator<ShortMessage> getAttributes() { return attributes.iterator(); }

	/**
	 * Gets the MIDI note that is associated with the given {@code ShortMessage}.
	 *
	 * @param msg The {@code ShortMessage} whose MIDI note will be returned
	 * @return The MIDI note of the {@code ShortMessage}
	 */

	private static byte getMidiNote(final ShortMessage msg) { return (byte) msg.getData1(); }

} // end of Emotion


