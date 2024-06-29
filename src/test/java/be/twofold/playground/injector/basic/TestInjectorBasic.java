package be.twofold.playground.injector.basic;

import be.twofold.playground.injector.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

class TestInjectorBasic {

    @Test
    void testBasic() {
        Injector injector = new Injector();
        A a = injector.getInstance(A.class);
        assertThat(a).isNotNull();
    }

}
