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

import org.junit.jupiter.api.Test;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Jon Mrowczynski
 */
class HardwarePartStateTest {
	
	@Test void getHardwarePart() throws InvalidMidiDataException {
		final var testHardwarePart = new TestHardwarePart(77, 3, 7);
		final var msg = new ShortMessage(ShortMessage.NOTE_ON, 0, 77, 100);
		final var hardwarePartState = new HardwarePartState(testHardwarePart, Set.of(msg));
		assertSame(testHardwarePart, hardwarePartState.hardwarePart());
	}
	
	@Test void getState() throws InvalidMidiDataException {
		final var states = Set.of(new ShortMessage(ShortMessage.NOTE_ON, 0, 77, 100));
		final var hardwarePartState = new HardwarePartState(new TestHardwarePart(77, 3, 7), states);
		assertSame(states, hardwarePartState.state());
	}
}