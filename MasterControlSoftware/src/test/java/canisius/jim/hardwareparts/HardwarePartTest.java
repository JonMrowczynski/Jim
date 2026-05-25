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

package canisius.jim.hardwareparts;

import org.junit.jupiter.api.Test;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jon Mrowczynski
 */
abstract class HardwarePartTest {
	
	static int default_neutral;
	
	protected HardwarePart hardwarePart;
	
	@Test void setNeutral() {
		final var iterator = hardwarePart.getNeutralState().iterator();
		// A newly instantiated Part's neutral state should be default_neutral.
		assertEquals(default_neutral, iterator.next().getMessage()[2]);
		
		// Setting the neutral state below the lower bound should result in the neutral state being set to the lower
		// bound.
		hardwarePart.setNeutral(HardwarePart.MIN_BOUND - 1);
		assertEquals(HardwarePart.MIN_BOUND, iterator.next().getMessage()[2]);
		
		// Setting the neutral state above the upper bound should result in the neutral state being set to the upper
		// bound.
		hardwarePart.setNeutral(HardwarePart.MAX_BOUND + 1);
		assertEquals(HardwarePart.MAX_BOUND, iterator.next().getMessage()[2]);
		
		// Setting the neutral state to any valid value should result in a successful modification of the neutral
		// state.
		final var average = (HardwarePart.MAX_BOUND + HardwarePart.MIN_BOUND) / 2;
		hardwarePart.setNeutral(average);
		assertEquals(average, iterator.next().getMessage()[2]);
	}
	
	@Test void addStateToTrack() throws InvalidMidiDataException {
		final var messages = hardwarePart.getLowerBoundState();
		// Adding any state to a null Track should throw a NullPointerException.
		assertThrows(NullPointerException.class, () -> hardwarePart.addStateToTrack(null, messages, -1));
		
		// Adding a null Set of ShortMessages should result in a still empty Track.
		final var sequence = new Sequence(Sequence.PPQ, 160);
		final var track = sequence.createTrack();
		hardwarePart.addStateToTrack(track, null, -1);
		assertEquals(1, track.size());
		
		// Adding a singleton Set of a ShortMessage to a Track should contain the data of the ShortMessage with the
		// maximum tick.
		hardwarePart.addStateToTrack(track, messages, 10);
		assertEquals(messages.iterator().next().getMessage()[2], track.get(0).getMessage().getMessage()[2]);
		assertEquals(2, track.size()); // Contains two events. The one we added and the eotEvent.
		assertEquals(10, track.ticks());
		
		// Adding 3 unique ShortMessages should result in a Track size of 4 with a tick that is the maximum added tick.
		hardwarePart.addStateToTrack(track, hardwarePart.getNeutralState(), 20);
		assertEquals(3, track.size());
		assertEquals(20, track.ticks());
		hardwarePart.addStateToTrack(track, hardwarePart.getUpperBoundState(), 30);
		assertEquals(4, track.size());
		assertEquals(30, track.ticks());
	}
	
	@Test void getUpperBoundState() {
		final var messages = hardwarePart.getUpperBoundState();
		// The upper bound state of a Part should be Part.MAX_BOUND.
		assertEquals(HardwarePart.MAX_BOUND, messages.iterator().next().getMessage()[2]);
		
		// The Part's upper bound state should only contain one element.
		assertEquals(1, messages.size());
	}
	
	@Test void getLowerBoundState() {
		// The lower bound state of a Part should be Part.MIN_BOUND.
		final var messages = hardwarePart.getLowerBoundState();
		assertEquals(HardwarePart.MIN_BOUND, messages.iterator().next().getMessage()[2]);
		
		// The Part's lower bound state should only contain one element.
		assertEquals(1, hardwarePart.getLowerBoundState().size());
	}
	
	@Test void getNeutralState() {
		final var iterator = hardwarePart.getNeutralState().iterator();
		// A newly instantiated Part's neutral state should be default_neutral
		assertEquals(default_neutral, iterator.next().getMessage()[2]);
		
		// Setting the neutral state to any valid value should result in a successful modification of the neutral
		// state.
		final var average = (HardwarePart.MIN_BOUND + HardwarePart.MAX_BOUND) / 2;
		hardwarePart.setNeutral(average);
		assertEquals(average, iterator.next().getMessage()[2]);
	}
	
	@Test void getUpperBoundHardwarePartState() {
		final var hardwarePartState = hardwarePart.getUpperBoundHardwarePartState();
		// A newly instantiated Part's upper bound PartState should contain itself as the Part.
		assertSame(hardwarePart, hardwarePartState.hardwarePart());
		
		// A newly instantiated Part's upper bound PartState should contain Part.MAX_BOUND for its state.
		assertEquals(HardwarePart.MAX_BOUND, hardwarePartState.state().iterator().next().getMessage()[2]);
		
		// A newly instantiated Part's upper bound PartState should contain only one element.
		assertEquals(1, hardwarePartState.state().size());
	}
	
	@Test void getLowerBoundHardwarePartState() {
		final var hardwarePartState = hardwarePart.getLowerBoundHardwarePartState();
		// A newly instantiated Part's lower bound PartState should contain itself as the Part.
		assertSame(hardwarePart, hardwarePartState.hardwarePart());
		
		// A newly instantiated Part's lower bound PartState should contain Part.MIN_BOUND for its state.
		assertEquals(HardwarePart.MIN_BOUND, hardwarePartState.state().iterator().next().getMessage()[2]);
		
		// A newly instantiated Part's lower bound PartState should contain only one element.
		assertEquals(1, hardwarePartState.state().size());
	}
	
	@Test void getNeutralHardwarePartState() {
		final var hardwarePartState = hardwarePart.getNeutralHardwarePartState();
		// A newly instantiated Part's neutral PartState should contain itself as the Part.
		assertSame(hardwarePart, hardwarePartState.hardwarePart());
		
		final var state = hardwarePartState.state();
		// A newly instantiated Part's neutral PartState should contain default_neutral for its state.
		assertEquals(default_neutral, state.iterator().next().getMessage()[2]);
		
		// A newly instantiated Part's neutral PartState should contain only one element.
		assertEquals(1, state.size());
	}
}