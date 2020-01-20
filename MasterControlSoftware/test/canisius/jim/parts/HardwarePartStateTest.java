package canisius.jim.parts;

import org.junit.jupiter.api.Test;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Jon Mrowczynski
 */
class HardwarePartStateTest {

    @Test
    void getHardwarePart() throws InvalidMidiDataException {
        final var testHardwarePart = new TestHardwarePart(77, 3, 7);
        final var msg = new ShortMessage(ShortMessage.NOTE_ON, 0, 77, 100);
        final var hardwarePartState = new HardwarePartState(testHardwarePart, Set.of(msg));
        assertSame(testHardwarePart, hardwarePartState.getHardwarePart());
    }

    @Test
    void getState() throws InvalidMidiDataException {
        final var states = Set.of(new ShortMessage(ShortMessage.NOTE_ON, 0, 77, 100));
        final var hardwarePartState = new HardwarePartState(new TestHardwarePart(77, 3, 7), states);
        assertSame(states, hardwarePartState.getState());
    }
}