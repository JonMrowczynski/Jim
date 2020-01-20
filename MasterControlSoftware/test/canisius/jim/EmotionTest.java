package canisius.jim;

import canisius.jim.connections.SequencerConnection;
import canisius.jim.connections.UsbMidiConnection;
import canisius.jim.ruppet.Ruppet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jon Mrowczynski
 */
class EmotionTest {

    static Ruppet ruppet;
    static Sequence sequence;

    @BeforeAll
    static void init() throws InvalidMidiDataException {
        // A Ruppet cannot be instantiated without a USB MIDI connection
        assertTrue(UsbMidiConnection.doesUSBMidiDeviceExist());
        ruppet = new Ruppet();
        sequence = new Sequence(Sequence.PPQ, SequencerConnection.RESOLUTION);
    }

    @Test
    void addEmotionToTrack() {
        // A newly created Track should only contain one MidiEvent, the eotEvent, at tick 0.
        final var track = sequence.createTrack();
        assertEquals(1, track.size());
        assertEquals(0, track.ticks());

        // Adding an Emotion to a Track should result in all of the states of the Emotion being added to the Track as
        // MidiEvents, except for lower jaw states, at the specified tick.
        final var emotion = new Emotion(ruppet, ruppet.getLowerJaw().getLowerBoundHardwarePartState(), ruppet.getLipCorners().getUpperBoundHardwarePartState());
        emotion.addEmotionToTrack(track, 7);
        assertEquals(emotion.getStates().size(), track.size());
        assertEquals(7, track.ticks());
    }

    @Test
    void getStates() {
        // The number of states should be equal to the sum of all of the ShortMessages in all of the HardwarePartStates
        // and should contain the neutral states of all of the other HardwareParts that are not included from the Ruppet
        // in the Emotion constructor.
        var emotion = new Emotion(ruppet, ruppet.getLowerJaw().getLowerBoundHardwarePartState());
        assertNotNull(emotion.getStates());
        var states = new HashSet<>(ruppet.getLowerJaw().getLowerBoundHardwarePartState().getState());
        states.addAll(ruppet.getEyebrows().getNeutralHardwarePartState().getState());
        states.addAll(ruppet.getEyelids().getNeutralHardwarePartState().getState());
        states.addAll(ruppet.getLipCorners().getNeutralHardwarePartState().getState());
        assertEquals(states.size(), emotion.getStates().size());
        assertEquals(states, emotion.getStates());

        states = new HashSet<>(ruppet.getLowerJaw().getNeutralHardwarePartState().getState());
        states.addAll(ruppet.getLipCorners().getUpperBoundHardwarePartState().getState());
        states.addAll(ruppet.getEyebrows().getUpperBoundHardwarePartState().getState());
        states.addAll(ruppet.getEyelids().getNeutralHardwarePartState().getState());
        emotion = new Emotion(ruppet, ruppet.getLipCorners().getUpperBoundHardwarePartState(), ruppet.getEyebrows().getUpperBoundHardwarePartState());
        assertEquals(states.size(), emotion.getStates().size());
        assertEquals(states, emotion.getStates());
    }
}