package canisius.jim.parts;

/**
 * A concrete implementation of {@code HardwarePart} that simply helps test {@code HardwarePartState}.
 *
 * @author Jon Mrowczynski
 */
public final class TestHardwarePart extends HardwarePart {
    public TestHardwarePart(final int midiNote, final int lowerBound, final int upperBound) { super(midiNote, lowerBound, upperBound); }
}