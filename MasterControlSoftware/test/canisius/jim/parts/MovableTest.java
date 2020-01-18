package canisius.jim.parts;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Jon Mrowczynski
 */
class MovableTest extends HardwarePartTest {
    @BeforeAll static void init() { default_neutral = 7; }
    @BeforeEach void setUp() { hardwarePart = new Movable(77, HardwarePart.MIN_BOUND, HardwarePart.MAX_BOUND, default_neutral); }
}