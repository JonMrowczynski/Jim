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

package canisius.jim.connections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import java.util.Optional;

/**
 * A {@code MidiDeviceConnection} allows one to connect to and disconnect from an instance of a {@link MidiDevice} that
 * is acquired from the {@link MidiSystem}.
 *
 * @param <T> the type of {@link MidiDevice}.
 * @author Jon Mrowczynski
 */
abstract class MidiDeviceConnection<T extends MidiDevice> {
	
	/**
	 * The {@link MidiDevice} that has been connected.
	 */
	protected @Nullable T midiDevice;
	
	/**
	 * Instantiating a new {@code MidiDeviceConnection} automatically attempts to connect to the {@link MidiDevice}.
	 */
	protected MidiDeviceConnection() { connect(); }
	
	/**
	 * Connect to the {@link MidiDevice}.
	 */
	protected abstract void connect();
	
	/**
	 * Disconnect from the {@link MidiDevice} by closing it iff it is not {@code null} and it is open. Otherwise, this
	 * method is a no-op.
	 */
	public void disconnect() { if (midiDevice != null && midiDevice.isOpen()) { midiDevice.close(); } }
	
	/**
	 * Returns the {@link MidiDevice} if {@code connect} has been called. Otherwise, {@code null} is returned.
	 *
	 * @return the connected {@link MidiDevice} or {@code null}
	 */
	public final @NotNull Optional<T> getMidiDevice() { return Optional.ofNullable(midiDevice); }
}