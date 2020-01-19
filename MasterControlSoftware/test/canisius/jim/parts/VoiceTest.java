package canisius.jim.parts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Jon Mrowczynski
 */
class VoiceTest extends SoftwarePartTest {

    Voice voice;

    @BeforeEach void setUp() { softwarePart = voice = new Voice(ruppet, actions); }

    @Test
    void givePresentation() {

    }
}