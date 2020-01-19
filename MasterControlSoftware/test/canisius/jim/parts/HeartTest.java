package canisius.jim.parts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Jon Mrowczynski
 */
class HeartTest extends SoftwarePartTest {

    Heart heart;

    @BeforeEach void setUp() { softwarePart = heart = new Heart(ruppet, actions); }

    @Test
    void getNeutral() {
        // Neutral is defined as all of the HardwareParts going to their neutral states.
        final var neutralSet = new HashSet<>(ruppet.getLowerJaw().getNeutralState());
        neutralSet.addAll(ruppet.getLipCorners().getNeutralState());
        neutralSet.addAll(ruppet.getEyebrows().getNeutralState());
        neutralSet.addAll(ruppet.getEyelids().getNeutralState());
        assertTrue(neutralSet.containsAll(heart.getNeutral().getAttributes()));
    }

    @Test
    void getHappy() {
        // Happy is defined as the lower jar going to the upper bound state and the lip corners going to the upper bound state.
        final var happySet = new HashSet<>(ruppet.getLowerJaw().getUpperBoundState());
        happySet.addAll(ruppet.getLipCorners().getUpperBoundState());
        happySet.addAll(ruppet.getEyebrows().getNeutralState());
        happySet.addAll(ruppet.getEyelids().getNeutralState());
        assertTrue(happySet.containsAll(heart.getHappy().getAttributes()));
    }

    @Test
    void getSad() {
        // Sad is defined as the lower jaw going to the upper bound state, the lip corners going to the lower bound state
        // and the eyebrows going to the upper bound state.
        final var sadSet = new HashSet<>(ruppet.getLowerJaw().getUpperBoundState());
        sadSet.addAll(ruppet.getLipCorners().getLowerBoundState());
        sadSet.addAll(ruppet.getEyebrows().getUpperBoundState());
        sadSet.addAll(ruppet.getEyelids().getNeutralState());
        assertTrue(sadSet.containsAll(heart.getSad().getAttributes()));
    }

    @Test
    void getAngry() {
        // Angry is defined as the lower jaw going to the upper bound state, the lip corners going to the lower bound state
        // and the eyebrows going to the lower bound state.
        final var angrySet = new HashSet<>(ruppet.getLowerJaw().getUpperBoundState());
        angrySet.addAll(ruppet.getLipCorners().getLowerBoundState());
        angrySet.addAll(ruppet.getEyebrows().getLowerBoundState());
        angrySet.addAll(ruppet.getEyelids().getNeutralState());
        assertTrue(angrySet.containsAll(heart.getAngry().getAttributes()));
    }

    @Test
    void getScared() {
        // Scared is define as the lower jaw going to its upper bound state, the lip corners going to its lower bound state
        // and the eyebrows going to the lower bound state.
        final var scaredSet = new HashSet<>(ruppet.getLowerJaw().getUpperBoundState());
        scaredSet.addAll(ruppet.getLipCorners().getLowerBoundState());
        scaredSet.addAll(ruppet.getEyebrows().getLowerBoundState());
        scaredSet.addAll(ruppet.getEyelids().getNeutralState());
        assertTrue(scaredSet.containsAll(heart.getScared().getAttributes()));
    }

    @Test
    void getSmile() {
        // A smile is defined as the lower jaw going to its upper bound state.
        final var smileSet = new HashSet<>(ruppet.getLowerJaw().getNeutralState());
        smileSet.addAll(ruppet.getLipCorners().getUpperBoundState());
        smileSet.addAll(ruppet.getEyebrows().getNeutralState());
        smileSet.addAll(ruppet.getEyelids().getNeutralState());
        assertTrue(smileSet.containsAll(heart.getSmile().getAttributes()));
    }
}