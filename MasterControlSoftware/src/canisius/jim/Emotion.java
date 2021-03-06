/*
 * MIT License
 *
 * Copyright (c) 2013-2019 Jon Mrowczynski
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

package canisius.jim;

import canisius.jim.parts.HardwarePart;
import canisius.jim.parts.HardwarePartState;
import canisius.jim.parts.Movable;
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
public final class Emotion {

	/**
	 * The {@code Set} of {@code ShortMessage}s that are sent to the electronics to make the {@code Ruppet} express this
	 * {@code Emotion}.
	 */
	private final Set<ShortMessage> states = new HashSet<>();
	
	/**
	 * Takes a {@code Ruppet} that this {@code Emotion} is associated with as well as a variable amount of
	 * {@code PartState}s. The {@code PartState}s and their associated {@code ShortMessage}s are added to
	 * {@code attributes}.
	 *
	 * @param ruppet that this {@code Emotion} belongs to
	 * @param hardwarePartStates that are transitioned to for this {@code Emotion}
	 * @throws InvalidParameterException if {@code hardwarePartStates.length == 0}
	 * @throws NullPointerException if {@code ruppet} or {@code partStates} is {@code null}
	 */
	public Emotion(final Ruppet ruppet, final HardwarePartState... hardwarePartStates) throws InvalidParameterException, NullPointerException {
		Objects.requireNonNull(ruppet, "Cannot initialize " + Emotion.class.getSimpleName() + " with null ruppet");
		Objects.requireNonNull(hardwarePartStates, "Cannot initialize " + Emotion.class.getSimpleName() + " with null partStates");
		if (hardwarePartStates.length > 0) { addEmotionPartStates(ruppet, hardwarePartStates); }
		else { throw new InvalidParameterException("partStates.length cannot be 0."); }
	}

	/**
	 * For every {@code PartState} in {@code partStates} add its {@code State} to this {@code Emotion}'s
	 * {@code attributes} iff the {@code PartState}'s corresponding {@code Part} is a {@code Movable}. If there are
	 * {@code Part}s that are not used to express this {@code Emotion}, then add their neutral positions to this
	 * {@code Emotion}'s {@code attributes} so that a given state does not carry over from one {@code Emotion} to
	 * another.
	 *
	 * @param ruppet that this {@code Emotion} belongs to
	 * @param hardwarePartStates whose states are to be added to {@code attributes}
	 * @throws NullPointerException if {@code ruppet} or {@code partStates} is {@code null}
	 */
	private void addEmotionPartStates(final Ruppet ruppet, final HardwarePartState[] hardwarePartStates) throws NullPointerException {
		Objects.requireNonNull(ruppet, "Cannot add emotion part states with a null ruppet");
		Objects.requireNonNull(hardwarePartStates, "Cannot add emotion part states with null partStates");
		final var ruppetParts = new ArrayList<HardwarePart>();
		ruppet.getHardwareParts().stream().filter(part -> part instanceof Movable).forEach(ruppetParts::add);
		Arrays.stream(hardwarePartStates).filter(hardwarePartState -> hardwarePartState.getHardwarePart() instanceof Movable).map(HardwarePartState::getState).forEach(states::addAll);
		ruppetParts.removeAll(Arrays.stream(hardwarePartStates).map(HardwarePartState::getHardwarePart).collect(Collectors.toList()));
		ruppetParts.stream().filter(ruppetPart -> ruppetPart instanceof Movable).map(HardwarePart::getNeutralState).forEach(states::addAll);
	}
	
	/**
	 * Adds this {@code Emotion} to {@code track} at {@code tick} so that when the {@code Track} is added to a
	 * {@code Sequence} the {@code Emotion} can be synchronized and/or sequenced with other {@code Emotion} transitions,
	 * mouth movements, etc.
	 *
	 * The {@code Ruppet}'s {@code lowerJaw} is disabled to prevent interference between having the {@code Ruppet}
	 * express this {@code Emotion} and moving its {@code lowerJaw} to talk.
	 * 
	 * @param track that will contain this {@code Emotion}
	 * @param tick that indicates the transition time of this {@code Emotion}
	 * @throws NullPointerException if {@code track} is {@code null}
	 */
	public final void addEmotionToTrack(final Track track, final int tick) throws NullPointerException {
		Objects.requireNonNull(track, "Cannot add emotion to a null Track");
		states.stream().filter(msg -> msg.getData1() != Ruppet.LOWER_JAW_MIDI_NOTE).forEach(msg -> track.add(new MidiEvent(msg, tick)));
	}

	/**
	 * Returns an {@code Set} of {@code ShortMessage}s that need to be transmitted to the electronics in order for the
	 * {@code Ruppet} to express this {@code Emotion}.
	 * 
	 * @return the {@code ShortMessage}s that are associated with this {@code Emotion}
	 */
	public final Set<ShortMessage> getStates() { return states; }

} // end of Emotion