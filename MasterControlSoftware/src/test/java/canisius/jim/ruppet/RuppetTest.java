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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
	
	Ruppet ruppet;
	
	// Since we are only testing static methods and getters, then we only need to construct a Ruppet object once to
	// test.
	@BeforeEach void setup() { ruppet = new Ruppet(); }
	
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
		final var sequencer = SequencerConnection.instance().getMidiDevice();
		final var tracks = ruppet.getTracks();
		sequencer.ifPresentOrElse(s -> assertTrue(IntStream.range(0, tracks.size()).anyMatch(s::getTrackMute)),
		                          Assertions::fail);
		
		// Muting all Tracks except for the Ruppet's Heart Track should cause all the Ruppet's Tracks to be muted
		// except for its Heart Track.
		ruppet.muteAllTracks(ruppet.getHeart().getTrack());
		assertFalse(sequencer.get().getTrackMute(tracks.indexOf(ruppet.getHeart().getTrack())));
		assertTrue(sequencer.get().getTrackMute(tracks.indexOf(ruppet.getVoice().getTrack())));
		assertTrue(sequencer.get().getTrackMute(tracks.indexOf(ruppet.getBlinkingTrack())));
		
		// Muting all Tracks except for all of them should result in none of the Ruppet's Tracks from being muted.
		ruppet.muteAllTracks(tracks.toArray(Track[]::new));
		assertFalse(IntStream.range(0, tracks.size()).anyMatch(i -> sequencer.get().getTrackMute(i)));
	}
}