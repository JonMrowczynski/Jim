package canisius.jim.parts;

import org.junit.jupiter.api.Test;

import javax.sound.midi.ShortMessage;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Jon Mrowczynski
 */
class HardwarePartStateTest {

    /**
     * A concrete implementation of {@code HardwarePart} that simply helps test {@code HardwarePartState}.
     *
     * @author Jon Mrowczynski
     */
    private static final class TestHardwarePart extends HardwarePart {
        TestHardwarePart(final int midiNote, final int lowerBound, final int upperBound) { super(midiNote, lowerBound, upperBound); }
    }

    @Test
    void getHardwarePart() {
        final var testHardwarePart = new TestHardwarePart(77, 3, 7);
        final var hardwarePartState = new HardwarePartState(testHardwarePart, Set.of());
        assertSame(testHardwarePart, hardwarePartState.getHardwarePart());
    }

    @Test
    void getState() {
        final var states = new HashSet<ShortMessage>();
        final var hardwarePartState = new HardwarePartState(new TestHardwarePart(77, 3, 7), states);
        assertSame(states, hardwarePartState.getState());
    }
}