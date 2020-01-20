package canisius.jim.parts;

import org.junit.jupiter.api.Test;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jon Mrowczynski
 */
abstract class HardwarePartTest {

    static int default_neutral;
    protected HardwarePart hardwarePart;

    @Test
    void setNeutral() {
        // A newly instantiated Part's neutral state should be default_neutral.
        assertEquals(default_neutral, hardwarePart.getNeutralState().iterator().next().getData2());

        // Setting the neutral state below the lower bound should result in the neutral state being set to the lower bound.
        hardwarePart.setNeutral(HardwarePart.MIN_BOUND - 1);
        assertEquals(HardwarePart.MIN_BOUND, hardwarePart.getNeutralState().iterator().next().getData2());

        // Setting the neutral state above the upper bound should result in the neutral state being set to the upper bound.
        hardwarePart.setNeutral(HardwarePart.MAX_BOUND + 1);
        assertEquals(HardwarePart.MAX_BOUND, hardwarePart.getNeutralState().iterator().next().getData2());

        // Setting the neutral state to any valid value should result in a successful modification of the neutral state.
        final var average = (HardwarePart.MAX_BOUND + HardwarePart.MIN_BOUND) / 2;
        hardwarePart.setNeutral(average);
        assertEquals(average, hardwarePart.getNeutralState().iterator().next().getData2());
    }

    @Test
    void addStateToTrack() throws InvalidMidiDataException {
        // Adding any state to a null Track should throw a NullPointerException.
        assertThrows(NullPointerException.class, () -> hardwarePart.addStateToTrack(null, hardwarePart.getLowerBoundState(), -1));

        // Adding a null Set of ShortMessages should result in a still empty Track.
        final var sequence = new Sequence(Sequence.PPQ, 160);
        final var track = sequence.createTrack();
        hardwarePart.addStateToTrack(track, null, -1);
        assertEquals(1, track.size());

        // Adding a singleton Set of a ShortMessage to a Track should contain the data of the ShortMessage with the maximum tick.
        hardwarePart.addStateToTrack(track, hardwarePart.getLowerBoundState(), 10);
        assertEquals(hardwarePart.getLowerBoundState().iterator().next().getData2(), track.get(0).getMessage().getMessage()[2]);
        assertEquals(2, track.size()); // Contains two events. The one we added and the eotEvent.
        assertEquals(10, track.ticks());

        // Adding 3 unique ShortMessages should result in a Track size of 4 with a tick that is the maximum added tick.
        hardwarePart.addStateToTrack(track, hardwarePart.getNeutralState(), 20);
        assertEquals(3, track.size());
        assertEquals(20, track.ticks());
        hardwarePart.addStateToTrack(track, hardwarePart.getUpperBoundState(), 30);
        assertEquals(4, track.size());
        assertEquals(30, track.ticks());
    }

    @Test
    void getUpperBoundState() {
        // The upper bound state of a Part should be Part.MAX_BOUND.
        assertEquals(HardwarePart.MAX_BOUND, hardwarePart.getUpperBoundState().iterator().next().getData2());

        // The Part's upper bound state should only contain one element.
        assertEquals(1, hardwarePart.getUpperBoundState().size());
    }

    @Test
    void getLowerBoundState() {
        // The lower bound state of a Part should be Part.MIN_BOUND.
        final var iterator = hardwarePart.getLowerBoundState().iterator();
        assertEquals(HardwarePart.MIN_BOUND, iterator.next().getData2());

        // The Part's lower bound state should only contain one element.
        assertEquals(1, hardwarePart.getLowerBoundState().size());
    }

    @Test
    void getNeutralState() {
        // A newly instantiated Part's neutral state should be default_neutral
        assertEquals(default_neutral, hardwarePart.getNeutralState().iterator().next().getData2());

        // Setting the neutral state to any valid value should result in a successful modification of the neutral state.
        final var average = (HardwarePart.MIN_BOUND + HardwarePart.MAX_BOUND) / 2;
        hardwarePart.setNeutral(average);
        assertEquals(average, hardwarePart.getNeutralState().iterator().next().getData2());
    }

    @Test
    void getUpperBoundHardwarePartState() {
        // A newly instantiated Part's upper bound PartState should contain itself as the Part.
        assertSame(hardwarePart, hardwarePart.getUpperBoundHardwarePartState().getHardwarePart());

        // A newly instantiated Part's upper bound PartState should contain Part.MAX_BOUND for its state.
        assertEquals(HardwarePart.MAX_BOUND, hardwarePart.getUpperBoundHardwarePartState().getState().iterator().next().getData2());

        // A newly instantiated Part's upper bound PartState should contain only one element.
        assertEquals(1, hardwarePart.getUpperBoundHardwarePartState().getState().size());
    }

    @Test
    void getLowerBoundHardwarePartState() {
        // A newly instantiated Part's lower bound PartState should contain itself as the Part.
        assertSame(hardwarePart, hardwarePart.getLowerBoundHardwarePartState().getHardwarePart());

        // A newly instantiated Part's lower bound PartState should contain Part.MIN_BOUND for its state.
        assertEquals(HardwarePart.MIN_BOUND, hardwarePart.getLowerBoundHardwarePartState().getState().iterator().next().getData2());

        // A newly instantiated Part's lower bound PartState should contain only one element.
        assertEquals(1, hardwarePart.getLowerBoundHardwarePartState().getState().size());
    }

    @Test
    void getNeutralHardwarePartState() {
        // A newly instantiated Part's neutral PartState should contain itself as the Part.
        assertSame(hardwarePart, hardwarePart.getNeutralHardwarePartState().getHardwarePart());

        // A newly instantiated Part's neutral PartState should contain default_neutral for its state.
        assertEquals(default_neutral, hardwarePart.getNeutralHardwarePartState().getState().iterator().next().getData2());

        // A newly instantiated Part's neutral PartState should contain only one element.
        assertEquals(1, hardwarePart.getNeutralHardwarePartState().getState().size());
    }
}