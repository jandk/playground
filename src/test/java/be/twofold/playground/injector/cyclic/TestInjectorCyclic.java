package be.twofold.playground.injector.cyclic;

import be.twofold.playground.injector.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

class TestInjectorCyclic {

    @Test
    void testCyclic() {
        Injector injector = new Injector();

        assertThatIllegalStateException()
            .isThrownBy(() -> injector.getInstance(A.class))
            .withMessage("Circular dependency detected");
    }

}
