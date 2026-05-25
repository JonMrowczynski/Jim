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

import org.jetbrains.annotations.NotNull;

import javax.sound.midi.MidiMessage;
import java.util.Objects;
import java.util.Set;

/**
 * A {@code HardwarePartState} pairs a {@code HardwarePart} with a state that the part can be in. A state is represented
 * by a {@code Set} of {@code ShortMessage}s that can be sent to the electronics.
 *
 * @param hardwarePart The {@code HardwarePart} of the {@code HardwarePartState} pair.
 * @param state        The state of the {@code HardwarePartState} pair.
 * @author Jon Mrowczynski
 */
public record HardwarePartState(@NotNull HardwarePart hardwarePart, @NotNull Set<? extends MidiMessage> state) {
	
	/**
	 * Pairs together a {@code HardwarePart} and a state that the {@code HardwarePart} can be in where a state is a
	 * {@code Set} of {@code ShortMessage}s.
	 *
	 * @param hardwarePart of the {@code Ruppet}
	 * @param state        that the {@code HardwarePart} can be in
	 * @throws NullPointerException     if {@code hardwarePart} or {@code state} is {@code null}
	 * @throws IllegalArgumentException if {@code state.isEmpty()}
	 */
	public HardwarePartState(final @NotNull HardwarePart hardwarePart,
	                         final @NotNull Set<? extends MidiMessage> state) {
		this.hardwarePart = Objects.requireNonNull(hardwarePart, "hardwarePart cannot be null");
		this.state = Objects.requireNonNull(state, "state cannot be null");
		if (state.isEmpty()) { throw new IllegalArgumentException("state cannot be empty"); }
	}
}