package canisius.jim.parts;

import canisius.jim.connections.SequencerConnection;
import canisius.jim.ruppet.Ruppet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jon Mrowczynski
 */
abstract class SoftwarePartTest {
    static Ruppet ruppet;
    static Sequence actions;
    @BeforeAll static void init() {
        ruppet = new Ruppet();
        try { actions = new Sequence(Sequence.PPQ, SequencerConnection.RESOLUTION); } catch (InvalidMidiDataException e) { e.printStackTrace(); }
    }
}