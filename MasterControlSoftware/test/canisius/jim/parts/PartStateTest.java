package canisius.jim.parts;

import org.junit.jupiter.api.Test;

import javax.sound.midi.ShortMessage;
import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jon Mrowczynski
 */
class PartStateTest {

    /**
     * A concrete implementation of {@code Part} that simply allows to help test {@code PartState}.
     *
     * @author Jon Mrowczynski
     */
    private static final class TestPart extends Part {
        TestPart(final int midiNote, final int lowerBound, final int upperBound) throws InvalidParameterException { super(midiNote, lowerBound, upperBound); }
    }

    @Test
    void getPart() {
        final var testPart = new TestPart(77, 3, 7);
        final var partState = new PartState(testPart, Set.of());
        assertSame(testPart, partState.getPart());
    }

    @Test
    void getState() {
        final var states = new HashSet<ShortMessage>();
        final var partState = new PartState(new TestPart(77, 3, 7), states);
        assertSame(states, partState.getState());
    }
}