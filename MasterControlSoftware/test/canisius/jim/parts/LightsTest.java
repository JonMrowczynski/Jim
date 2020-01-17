package canisius.jim.parts;

import canisius.jim.ruppet.Ruppet;
import org.junit.jupiter.api.Test;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jon Mrowczynski
 */
class LightsTest {

    @Test
    void setNeutral() {
        // A newly instantiated Lights' neutral state should be Lights.OFF.
        final var lights = new Lights(Ruppet.LIGHTS_MIDI_NOTE);
        assertEquals(Lights.OFF, lights.getNeutralState().iterator().next().getData2());

        // Setting the neutral state below the lower bound should result in the neutral state being set to the lower bound.
        lights.setNeutral(Lights.OFF - 1);
        assertEquals(Lights.OFF, lights.getNeutralState().iterator().next().getData2());

        // Setting the neutral state above the upper bound should result in the neutral state being set to the upper bound.
        lights.setNeutral(Lights.ON + 1);
        assertEquals(Lights.ON, lights.getNeutralState().iterator().next().getData2());

        // Setting the neutral state to any valid value should result in a successful modification of the neutral state.
        final var average = (Lights.ON + Lights.OFF) / 2;
        lights.setNeutral(average);
        assertEquals(average, lights.getNeutralState().iterator().next().getData2());
    }

    @Test
    void addStateToTrack() {
        // Adding any state to a null Track should throw a NullPointerException
        final var lights = new Lights(Ruppet.LIGHTS_MIDI_NOTE);
        assertThrows(NullPointerException.class, () -> lights.addStateToTrack(null, lights.getLowerBoundState(), -1));

        try {
            // Adding a null Set of ShortMessages should result in a still empty Track.
            final var sequence = new Sequence(Sequence.PPQ, 160);
            final var track = sequence.createTrack();
            lights.addStateToTrack(track, null, -1);
            assertEquals(1, track.size());

            // Adding a singleton Set of a ShortMessage to a Track should contain the data of the ShortMessage with the maximum tick.
            lights.addStateToTrack(track, lights.getLowerBoundState(), 10);
            assertEquals(lights.getLowerBoundState().iterator().next().getData2(), track.get(0).getMessage().getMessage()[2]);
            assertEquals(2, track.size()); // Contains two events. The one we added and the eotEvent.
            assertEquals(10, track.ticks());

            // Adding 3 unique ShortMessages should result in a Track size of 4 with a tick that is the maximum added tick.
            lights.addStateToTrack(track, lights.getNeutralState(), 20);
            assertEquals(3, track.size());
            assertEquals(20, track.ticks());
            lights.addStateToTrack(track, lights.getUpperBoundState(), 30);
            assertEquals(4, track.size());
            assertEquals(30, track.ticks());
        } catch (InvalidMidiDataException e) { e.printStackTrace(); }
    }

    @Test
    void getUpperBoundState() {
        // The upper bound state of a Light should be Lights.ON.
        final var lights = new Lights(Ruppet.LIGHTS_MIDI_NOTE);
        assertEquals(Lights.ON, lights.getUpperBoundState().iterator().next().getData2());

        // The Light's upper bound state should only contain one element.
        assertEquals(1, lights.getUpperBoundState().size());
    }

    @Test
    void getLowerBoundState() {
        // The lower bound state of a Light should be Lights.OFF.
        final var lights = new Lights(Ruppet.LIGHTS_MIDI_NOTE);
        final var iterator = lights.getLowerBoundState().iterator();
        assertEquals(Lights.OFF, iterator.next().getData2());

        // The Lights's lower bound state should only contain one element.
        assertEquals(1, lights.getLowerBoundState().size());
    }

    @Test
    void getNeutralState() {
        // A newly instantiated Lights' neutral state should be Lights.OFF
        final var lights = new Lights(Ruppet.LIGHTS_MIDI_NOTE);
        assertEquals(Lights.OFF, lights.getNeutralState().iterator().next().getData2());

        // Setting the neutral state to any valid value should result in a successful modification of the neutral state.
        final var average = (Lights.OFF + Lights.ON) / 2;
        lights.setNeutral(average);
        assertEquals(average, lights.getNeutralState().iterator().next().getData2());
    }

    @Test
    void getUpperBoundPartState() {
        // A newly instantiated Lights' upper bound PartState should contain itself as the Part.
        final var lights = new Lights(Ruppet.LIGHTS_MIDI_NOTE);
        assertSame(lights, lights.getUpperBoundPartState().getPart());

        // A newly instantiated Lights' upper bound PartState should contain Lights.ON for its state.
        assertEquals(Lights.ON, lights.getUpperBoundPartState().getState().iterator().next().getData2());

        // A newly instantiated Lights' upper bound PartState should contain only one element.
        assertEquals(1, lights.getUpperBoundPartState().getState().size());
    }

    @Test
    void getLowerBoundPartState() {
        // A newly instantiated Lights' lower bound PartState should contain itself as the Part.
        final var lights = new Lights(Ruppet.LIGHTS_MIDI_NOTE);
        assertSame(lights, lights.getLowerBoundPartState().getPart());

        // A newly instantiated Lights' lower bound PartState should contain Lights.OFF for its state.
        assertEquals(Lights.OFF, lights.getLowerBoundPartState().getState().iterator().next().getData2());

        // A newly instantiated Lights' lower bound PartState should contain only one element.
        assertEquals(1, lights.getLowerBoundPartState().getState().size());
    }

    @Test
    void getNeutralPartState() {
        // A newly instantiated Lights' neutral PartState should contain itself as the Part.
        final var lights = new Lights(Ruppet.LIGHTS_MIDI_NOTE);
        assertSame(lights, lights.getNeutralPartState().getPart());

        // A newly instantiated Lights' neutral PartState should contain Lights.OFF for its state.
        assertEquals(Lights.OFF, lights.getNeutralPartState().getState().iterator().next().getData2());

        // A newly instantiated Lights' neutral PartState should contain only one element.
        assertEquals(1, lights.getNeutralPartState().getState().size());
    }
}