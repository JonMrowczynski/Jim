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

package canisius.jim.parts;

import canisius.jim.connections.SequencerConnection;
import canisius.jim.connections.UsbMidiConnection;
import canisius.jim.ruppet.Ruppet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Make sure to run any SoftwarePartTest with the USB MIDI device plugged in.
 *
 * @author Jon Mrowczynski
 */
abstract class SoftwarePartTest {
	
	static Ruppet ruppet;
	
	static Sequence actions;
	
	protected SoftwarePart softwarePart;
	
	@BeforeAll static void init() throws InvalidMidiDataException {
		// A Ruppet cannot be instantiated without a USB MIDI connection
		assertTrue(UsbMidiConnection.doesUSBMidiDeviceExist());
		ruppet = new Ruppet();
		actions = new Sequence(Sequence.PPQ, SequencerConnection.RESOLUTION);
	}
	
	@Test void readTimingInfoFromFile() {
		// First the existence of a File with the file name Heart.FILE_NAME is checked.
		final var file = new File(softwarePart.fileName);
		assertTrue(file.exists());
		
		// The contents of the File should not be empty.
		assertTrue(file.length() > 0);
		
		// After a SoftwarePart has been initialized, there should be at least 1 transition.
		assertTrue(softwarePart.getNumberOfTransitions() > 0);
	}
	
	@Test void setupTimings() throws IOException {
		// After a SoftwarePart has been initialized, the Track should have more than 1 event.
		assertTrue(softwarePart.getTrack().size() > 1);
		assertTrue(softwarePart.getTrack().ticks() > 0);
		
		// In addition, the number of events in the Track should be greater than the number of lines in the File
		final var file = new File(softwarePart.fileName);
		assertTrue(softwarePart.getTrack().size() > Files.readAllLines(file.toPath()).size());
	}
	
	@Test void getNumberOfTransitions() throws IOException {
		// The number of transitions should match the number of lines in the File.
		final var file = new File(softwarePart.fileName);
		assertEquals(Files.readAllLines(file.toPath()).size(), softwarePart.getNumberOfTransitions());
	}
	
	@Test void getTrack() {
		// The Track should not be null.
		assertNotNull(softwarePart.getTrack());
		
		// After a SoftwarePart has been initialized, there should be more than just the default event in the Track.
		assertTrue(softwarePart.getTrack().size() > 1);
		assertTrue(softwarePart.getTrack().ticks() > 0);
	}
}