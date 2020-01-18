package canisius.jim.parts;

import canisius.jim.ruppet.Ruppet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Jon Mrowczynski
 */
class LightsTest extends HardwarePartTest {
    @BeforeAll static void init() { default_neutral = HardwarePart.MIN_BOUND; }
    @BeforeEach void setUp() { hardwarePart = new Lights(Ruppet.LIGHTS_MIDI_NOTE); }
}