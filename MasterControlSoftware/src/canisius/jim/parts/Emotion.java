/*
 * Copyright (c) 2018-2019 Jon Mrowczynski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
 * positions of the servo motors such that a {@code Ruppet} expresses the corresponding {@code Emotion}.
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
	 * {@code PartState}s. The {@code PartState}s and their associated {@code ShortMessage}s are added to the
	 * {@code Emotion attributes Set}.
	 *
	 * @param ruppet that this {@code Emotion} belongs to.
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
	 * {@code Emotion}'s {@code attributes} so that a given state does not carry over from one {@code Emotion} to
	 * another.
	 *
	 * @param ruppet that this {@code Emotion} belongs to.
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
	 * {@code Sequence} the {@code Emotion} can be synchronized and/or sequenced with other {@code Emotion} transitions,
	 * mouth movements, etc.
	 *
	 * The {@code Ruppet}'s {@code lowerJaw} is disabled to prevent interference between having the {@code Ruppet}
	 * express this {@code Emotion} and moving its {@code lowerJaw} to talk.
	 * 
	 * @param track that will contain this {@code Emotion}.
	 * @param tick that indicates the transition time of this {@code Emotion}.
	 */

	final void addEmotionToTrack(final Track track, final int tick) {
		attributes.stream().filter(msg -> getMidiNote(msg) != Ruppet.LOWER_JAW_MIDI_NOTE).forEach(msg -> track.add(new MidiEvent(msg, tick)));
	}

	/**
	 * Returns an {@code Iterator} of {@code ShortMessage}s that need to be transmitted to the electronics in order for
	 * the {@code Ruppet} to express this {@code Emotion}.
	 * 
	 * @return The {@code ShortMessage}s that are associated with this {@code Emotion}al state.
	 */

	final Iterator<ShortMessage> getAttributes() { return attributes.iterator(); }

	/**
	 * Returns the MIDI note that is associated with {@code shortMessage}.
	 *
	 * @param shortMessage whose MIDI note will be returned.
	 * @return A {@code byte} representing the MIDI note of {@code shortMessage}.
	 */

	private static byte getMidiNote(final ShortMessage shortMessage) { return (byte) shortMessage.getData1(); }

} // end of Emotion


