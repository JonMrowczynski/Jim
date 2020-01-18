package canisius.jim.parts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Jon Mrowczynski
 */
class HeartTest extends SoftwarePartTest {

    Heart heart;

    @BeforeEach void setUp() { heart = new Heart(ruppet, actions); }

    @Test
    void getTrack() {
    }

    @Test
    void readTimingInfoFromFile() {
        // First the existence of a File with the file name Heart.FILE_NAME is checked.
        final var file = new File(Heart.FILE_NAME);
        assertTrue(file.exists());

        // The contents of the File should not be empty.
        assertTrue(file.length() > 0);

        // Before data is read from the file, the size of the Heart Track should be 1 (for the eotEvent) and there
        // should be no Emotion transitions.
        assertEquals(1, heart.getTrack().size());
        assertEquals(0, heart.getTrack().ticks());
        assertEquals(0, heart.getNumberOfEmotionTransitions());

        // After data is read from the file, there should be at least 1 Emotion transition.
        heart.readTimingInfoFromFile();
        assertTrue(heart.getNumberOfEmotionTransitions() > 0);
    }

    @Test
    void setupTimings() {
    }

    @Test
    void getNumberOfEmotionTransitions() {
    }

    @Test
    void getNeutral() {
        // Neutral is defined as all of the HardwareParts going to their neutral states.
        final var neutralSet = Set.of(ruppet.getLowerJaw().getNeutralState(), ruppet.getLipCorners().getNeutralState(), ruppet.getEyebrows().getNeutralState(), ruppet.getEyelids().getNeutralState());
        neutralSet.containsAll(heart.getNeutral().getAttributes());
    }

    @Test
    void getHappy() {
        // Happy is defined as the lower jar going to the upper bound state and the lip corners going to the upper bound state.
        final var happySet = Set.of(ruppet.getLowerJaw().getUpperBoundState(), ruppet.getLipCorners().getUpperBoundState());
        happySet.containsAll(heart.getHappy().getAttributes());
    }

    @Test
    void getSad() {
        // Sad is defined as the lower jaw going to the upper bound state, the lip corners going to the lower bound state
        // and the eyebrows going to the upper bound state.
        final var sadSet = Set.of(ruppet.getLowerJaw().getUpperBoundState(), ruppet.getLipCorners().getLowerBoundState(), ruppet.getEyebrows().getUpperBoundState());
        assertTrue(sadSet.containsAll(heart.getSad().getAttributes()));
    }

    @Test
    void getAngry() {
        // Angry is defined as the lower jaw going to the upper bound state, the lip corners going to the lower bound state
        // and the eyebrows going to the lower bound state.
        final var angrySet = Set.of(ruppet.getLowerJaw().getUpperBoundState(), ruppet.getLipCorners().getLowerBoundState(), ruppet.getEyebrows().getLowerBoundState());
        assertTrue(angrySet.containsAll(heart.getAngry().getAttributes()));
    }

    @Test
    void getScared() {
        // Scared is define as the lower jaw going to its upper bound state, the lip corners going to its lower bound state
        // and the eyebrows going to the lower bound state.
        final var scaredSet = Set.of(ruppet.getLowerJaw().getUpperBoundState(), ruppet.getLipCorners().getLowerBoundState(), ruppet.getEyebrows().getLowerBoundState());
        assertTrue(scaredSet.containsAll(heart.getScared().getAttributes()));
    }

    @Test
    void getSmile() {
        // A smile is defined as the lower jaw going to its upper bound state.
        final var smileSet = Set.of(ruppet.getLowerJaw().getUpperBoundState());
        assertTrue(smileSet.containsAll(heart.getSmile().getAttributes()));
    }
}