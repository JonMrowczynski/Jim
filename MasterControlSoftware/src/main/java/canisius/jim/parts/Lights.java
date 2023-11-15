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

/**
 * {@code Lights} represents the digital eye lights of the {@code Ruppet}, which can be turned either on or off.
 *
 * @author Jon Mrowczynski
 */
public final class Lights extends HardwarePart {
	
	/**
	 * Takes a {@code Ruppet}'s {@code List} of {@code Part}s as well as a MIDI note which should be associated with
	 * these {@code Lights} in order to operate them.
	 *
	 * @param midiNote that should be associated with these {@code Lights}
	 */
	public Lights(final byte midiNote) {
		super(midiNote, HardwarePart.MIN_BOUND, HardwarePart.MAX_BOUND, HardwarePart.MIN_BOUND);
	}
	
	/**
	 * Turns the {@code Ruppet}'s eye lights on.
	 */
	public void on() { toState(HardwarePart.MAX_BOUND); }
	
	/**
	 * Turns the {@code Ruppet}'s eye lights off.
	 */
	void off() { toState(HardwarePart.MIN_BOUND); }
}