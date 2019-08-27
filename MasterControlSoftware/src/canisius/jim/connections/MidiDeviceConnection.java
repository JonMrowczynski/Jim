package canisius.jim.connections;

import javax.sound.midi.MidiDevice;

/**
 * A {@code MidiDeviceConnection} allows one to connect to and disconnect from an instance of a {@code MidiDevice}. This
 * {@code MidiDevice} should not be instantiated directly, but rather acquired from the {@code MidiSystem}.
 *
 * @author Jon Mrowczynski
 */

public abstract class MidiDeviceConnection<T extends MidiDevice> {

    /**
     * The {@code MidiDevice} that a connection has been made to.
     */

    T midiDevice;

    /**
     * Instantiating a new {@code MidiDeviceConnection} automatically attempts to connect to the {@code MidiDevice}.
     */

    MidiDeviceConnection() { connect(); }

    /**
     * Connect to the {@code MidiDevice}.
     */

    protected abstract void connect();

    /**
     * Disconnect from the {@code MidiDevice} by closing it iff it is not {@code null} and it is open. Otherwise, this
     * method is a no-op.
     *
     * @see MidiDevice#isOpen()
     * @see MidiDevice#close()
     */

    public final void disconnect() { if (midiDevice != null && midiDevice.isOpen()) { midiDevice.close(); } }

    /**
     * Returns the {@code MidiDevice} if {@code connect} has been called. Otherwise, {@code null} is returned.
     *
     * @return the {@code MidiDevice} that has been connected to or {@code null}.
     */

    public final T getMidiDevice() { return midiDevice; }

} // end of MidiDeviceConnection
