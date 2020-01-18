package canisius.jim.parts;

import canisius.jim.ruppet.Ruppet;

import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.io.File;
import java.util.Objects;

/**
 * A {@code SoftwarePart} allows for a {@code Ruppet} to time specific actions to carry out during the running of a
 * script.
 *
 * @author Jon Mrowczynski
 */
public abstract class SoftwarePart {

    /**
     * The {@code File} that contains the timing information for the mouth movements.
     */
    protected final File transitionTimesFile;

    /**
     * The {@code Track} that stores the timing information for the {@code Ruppet}'s mouth movements based on the timing
     * information gathered from the {@code List}s {@code mouthDownTimes} and {@code mouthUpTimes}.
     */
    protected final Track track;

    /**
     * Constructs a {@code SoftwarePart} that can be used to time specific types of actions for the execution of a
     * script.
     *
     * @param ruppet that this {@code SoftwarePart} belongs to
     * @param actions that is used to create a {@code Track} that stores all of the timing information
     * @param fileName of the {@code File} that contains all of the timing information for this {@code SoftwarePart}
     * @throws NullPointerException if {@code ruppet}, {@code actions} or {@code fileName} is null;
     */
    SoftwarePart(final Ruppet ruppet, final Sequence actions, final String fileName) throws NullPointerException {
        Objects.requireNonNull(ruppet, "Cannot initialize a " + SoftwarePart.class.getSimpleName() + " with a null ruppet");
        track = Objects.requireNonNull(actions, "Cannot initialize a " + SoftwarePart.class.getSimpleName() + " with null actions").createTrack();
        transitionTimesFile = new File(fileName);
    }

    /**
     * Reads timing information from a {@code File} that will be used to setup the timed actions that will be carried
     * out upon the execution of a script.
     */
    protected abstract void readTimingInfoFromFile();

    /**
     * Sets up the {@code SoftwarePart} such that it will carry out specific timed actions during the running of a
     * script.
     */
    protected abstract void setupTimings();

    /**
     * Returns the {@code Track} that contains all of the {@code MidiEvent} timings.
     *
     * @return The {@code Track} that contains all of the {@code MidiEvent} timings
     */
    public final Track getTrack() { return track; }

} // end of SoftwarePart