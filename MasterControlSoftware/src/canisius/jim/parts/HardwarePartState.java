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
import java.util.Objects;
import java.util.Set;

/**
 * A {@code HardwarePartState} pairs a {@code HardwarePart} with a state that the part can be in. A state is represented
 * by a {@code Set} of {@code ShortMessage}s that can be sent to the electronics.
 * 
 * @author Jon Mrowczynski
 */
public final class HardwarePartState {
	
	/**
	 * The {@code HardwarePart} of the {@code HardwarePartState} pair.
	 */
	private final HardwarePart hardwarePart;
	
	/**
	 * The state of the {@code HardwarePartState} pair.
	 */
	private final Set<ShortMessage> state;
	
	/**
	 * Pairs together a {@code HardwarePart} and a state that the {@code HardwarePart} can be in where a state is a
	 * {@code Set} of {@code ShortMessage}s.
	 * 
	 * @param hardwarePart of the {@code Ruppet}
	 * @param state that the {@code HardwarePart} can be in
	 * @throws NullPointerException if {@code hardwarePart} or {@code state} is {@code null}
	 */
	HardwarePartState(final HardwarePart hardwarePart, final Set<ShortMessage> state) throws NullPointerException {
		this.hardwarePart = Objects.requireNonNull(hardwarePart, "hardwarePart cannot be null");
		this.state = Objects.requireNonNull(state, "state cannot be null");
	}

	/**
	 * Returns the {@code HardwarePart} of the {@code HardwarePartState} pair.
	 * 
	 * @return The {@code HardwarePart} of the {@code HardwarePartState} pair
	 */
	public final HardwarePart getHardwarePart() { return hardwarePart; }
	
	/**
	 * Returns the {@code Set} of {@code ShortMessage}s that represents the state of the {@code HardwarePartState} pair.
	 * 
	 * @return The {@code Set} of {@code ShortMessage}s that represents the state of the {@code HardwarePartState} pair
	 */
	public final Set<ShortMessage> getState() { return state; }

} // end of HardwarePartState