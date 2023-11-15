/*
 * MIT License
 *
 * Copyright (c) 2013-2023 Jon Mrowczynski
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

import canisius.jim.connections.SequencerConnection;
import canisius.jim.connections.UsbMidiConnection;
import canisius.jim.ruppet.Ruppet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jon Mrowczynski
 */
class EmotionTest {
	
	static Ruppet ruppet;
	
	static Sequence sequence;
	
	@BeforeAll static void init() throws InvalidMidiDataException {
		// A Ruppet cannot be instantiated without a USB MIDI connection
		assertTrue(UsbMidiConnection.doesUSBMidiDeviceExist());
		ruppet = new Ruppet();
		sequence = new Sequence(Sequence.PPQ, SequencerConnection.RESOLUTION);
	}
	
	@Test void addEmotionToTrack() {
		// A newly created Track should only contain one MidiEvent, the eotEvent, at tick 0.
		final var track = sequence.createTrack();
		assertEquals(1, track.size());
		assertEquals(0, track.ticks());
		
		// Adding an Emotion to a Track should result in all the states of the Emotion being added to the Track as
		// MidiEvents, except for lower jaw states, at the specified tick.
		final var emotion = new Emotion(ruppet, ruppet.getLowerJaw().getLowerBoundHardwarePartState(),
		                                ruppet.getLipCorners().getUpperBoundHardwarePartState());
		emotion.addEmotionToTrack(track, 7);
		assertEquals(emotion.getStates().size(), track.size());
		assertEquals(7, track.ticks());
	}
	
	@Test void getStates() {
		// The number of states should be equal to the sum of all the ShortMessages in all the HardwarePartStates
		// and should contain the neutral states of all the other HardwareParts that are not included from the
		// Ruppet
		// in the Emotion constructor.
		var emotion = new Emotion(ruppet, ruppet.getLowerJaw().getLowerBoundHardwarePartState());
		assertNotNull(emotion.getStates());
		var states = new HashSet<>(ruppet.getLowerJaw().getLowerBoundHardwarePartState().state());
		states.addAll(ruppet.getEyebrows().getNeutralHardwarePartState().state());
		states.addAll(ruppet.getEyelids().getNeutralHardwarePartState().state());
		states.addAll(ruppet.getLipCorners().getNeutralHardwarePartState().state());
		assertEquals(states.size(), emotion.getStates().size());
		assertEquals(states, emotion.getStates());
		
		states = new HashSet<>(ruppet.getLowerJaw().getNeutralHardwarePartState().state());
		states.addAll(ruppet.getLipCorners().getUpperBoundHardwarePartState().state());
		states.addAll(ruppet.getEyebrows().getUpperBoundHardwarePartState().state());
		states.addAll(ruppet.getEyelids().getNeutralHardwarePartState().state());
		emotion = new Emotion(ruppet, ruppet.getLipCorners().getUpperBoundHardwarePartState(),
		                      ruppet.getEyebrows().getUpperBoundHardwarePartState());
		assertEquals(states.size(), emotion.getStates().size());
		assertEquals(states, emotion.getStates());
	}
}