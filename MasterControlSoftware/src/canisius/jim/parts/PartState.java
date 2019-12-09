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

package canisius.jim.parts;

import javax.sound.midi.ShortMessage;
import java.util.Set;

/**
 * A {@code PartState} pairs a {@code Part} with a state that the part can be in. A state is represented by a
 * {@code Set} of {@code ShortMessage}s that can be sent to the electronics.
 * 
 * @author Jon Mrowczynski
 */
final class PartState {
	
	/**
	 * The {@code Part} of the {@code PartState} pair.
	 */
	private final Part part;
	
	/**
	 * The state of the {@code PartState} pair.
	 */
	private final Set<ShortMessage> state;
	
	/**
	 * Pairs together a {@code Part} and a state that the {@code Part} can be in where a state is a {@code Set} of
	 * {@code ShortMessage}s.
	 * 
	 * @param part of the {@code Ruppet}
	 * @param state that the {@code Part} can be in
	 * @throws NullPointerException if {@code part} or {@code state} is {@code null}
	 */
	PartState(final Part part, final Set<ShortMessage> state) throws NullPointerException {
		if (part == null) { throw new NullPointerException("part cannot be null"); }
		if (state == null) { throw new NullPointerException("state cannot be null"); }
		this.part = part;
		this.state = state;
	}

	/**
	 * Returns the {@code Part} of the {@code PartState} pair.
	 * 
	 * @return The {@code Part} of the {@code PartState} pair
	 */
	final Part getPart() { return part; }
	
	/**
	 * Returns the {@code Set} of {@code ShortMessage}s that represents the state of the {@code PartState} pair.
	 * 
	 * @return The {@code Set} of {@code ShortMessage}s that represents the state of the {@code PartState} pair
	 */
	final Set<ShortMessage> getState() { return state; }

} // end of PartState