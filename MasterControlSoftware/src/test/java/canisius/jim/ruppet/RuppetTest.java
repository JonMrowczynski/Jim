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

package canisius.jim.ruppet;

import canisius.jim.connections.SequencerConnection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sound.midi.Track;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jon Mrowczynski
 */
class RuppetTest {
	
	static Ruppet ruppet;
	
	// Since we are only testing static methods and getters, then we only need to construct a Ruppet object once to
	// test.
	@BeforeAll static void init() { ruppet = new Ruppet(); }
	
	@Test void pause_ms() {
		// The pause should be in milliseconds and accurate to the nearest millisecond.
		new Thread(() -> {
			final var initialTime = System.currentTimeMillis();
			Ruppet.pause_ms(50);
			final var finalTime = System.currentTimeMillis();
			final var timeDelta = finalTime - initialTime;
			assertEquals(50, timeDelta);
		}).start();
	}
	
	@Test void muteAllTracks() {
		// Muting all Tracks should cause all the Ruppet's Tracks to be muted.
		ruppet.muteAllTracks();
		final var sequencer = SequencerConnection.getInstance().getMidiDevice();
		assertTrue(IntStream.range(0, ruppet.getTracks().size()).anyMatch(sequencer::getTrackMute));
		
		// Muting all Tracks except for the Ruppet's Heart Track should cause all the Ruppet's Tracks to be muted
		// except for its Heart Track.
		ruppet.muteAllTracks(ruppet.getHeart().getTrack());
		assertFalse(sequencer.getTrackMute(ruppet.getTracks().indexOf(ruppet.getHeart().getTrack())));
		assertTrue(sequencer.getTrackMute(ruppet.getTracks().indexOf(ruppet.getVoice().getTrack())));
		assertTrue(sequencer.getTrackMute(ruppet.getTracks().indexOf(ruppet.getBlinkingTrack())));
		
		// Muting all Tracks except for all of them should result in none of the Ruppet's Tracks from being muted.
		ruppet.muteAllTracks(ruppet.getTracks().toArray(Track[]::new));
		assertFalse(IntStream.range(0, ruppet.getTracks().size()).anyMatch(sequencer::getTrackMute));
	}
	
	@Test void getSoftwareParts() {
		// A Ruppet's SoftwareParts should contain its Heart and Voice.
		assertNotNull(ruppet.getSoftwareParts());
		final var softwareParts = Set.of(ruppet.getHeart(), ruppet.getVoice());
		assertEquals(softwareParts, ruppet.getSoftwareParts());
	}
	
	@Test void getHeart() {
		// A Ruppet's SoftwareParts should contain its Heart.
		assertNotNull(ruppet.getHeart());
		assertTrue(ruppet.getSoftwareParts().contains(ruppet.getHeart()));
	}
	
	@Test void getVoice() {
		// A Ruppet's SoftwareParts should contain its Voice.
		assertNotNull(ruppet.getVoice());
		assertTrue(ruppet.getSoftwareParts().contains(ruppet.getVoice()));
	}
	
	@Test void getTracks() {
		// A Ruppet's Tracks should contain its Heart, Voice, and blinking Track.
		assertNotNull(ruppet.getTracks());
		final var tracks =
				Set.of(ruppet.getHeart().getTrack(), ruppet.getVoice().getTrack(), ruppet.getBlinkingTrack());
		assertEquals(tracks, new HashSet<>(ruppet.getTracks()));
	}
	
	@Test void getBlinkingTrack() {
		// A Ruppet's blinking Track should contain more than 1 MidiEvent.
		assertNotNull(ruppet.getBlinkingTrack());
		assertTrue(ruppet.getBlinkingTrack().size() > 1);
		assertTrue(ruppet.getBlinkingTrack().ticks() > 0);
	}
	
	@Test void getHardwareParts() {
		// A Ruppet's HardwareParts should contain its lower jaw, lip corners, eyebrows, and eyelids.
		assertNotNull(ruppet.getHardwareParts());
		final var hardwareParts =
				Set.of(ruppet.getLowerJaw(), ruppet.getLipCorners(), ruppet.getEyebrows(), ruppet.getEyelids(),
				       ruppet.getLights());
		assertEquals(hardwareParts, ruppet.getHardwareParts());
	}
	
	@Test void getLowerJaw() {
		// A Ruppet's HardwareParts should contain its lower jaw.
		assertNotNull(ruppet.getLowerJaw());
		assertTrue(ruppet.getHardwareParts().contains(ruppet.getLowerJaw()));
	}
	
	@Test void getLipCorners() {
		// A Ruppet's HardwareParts should contain its lip corners.
		assertNotNull(ruppet.getLipCorners());
		assertTrue(ruppet.getHardwareParts().contains(ruppet.getLipCorners()));
	}
	
	@Test void getEyebrows() {
		// A Ruppet's HardWareParts should contain its eyebrows.
		assertNotNull(ruppet.getEyebrows());
		assertTrue(ruppet.getHardwareParts().contains(ruppet.getEyebrows()));
	}
	
	@Test void getEyelids() {
		// A Ruppet's HardwareParts should contain its eyelids.
		assertNotNull(ruppet.getEyelids());
		assertTrue(ruppet.getHardwareParts().contains(ruppet.getEyelids()));
	}
	
	@Test void getLights() {
		// A Ruppet's HardwareParts should contain its lights.
		assertNotNull(ruppet.getLights());
		assertTrue(ruppet.getHardwareParts().contains(ruppet.getLights()));
	}
}