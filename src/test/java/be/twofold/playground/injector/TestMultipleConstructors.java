package be.twofold.playground.injector;

import jakarta.inject.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

public class TestMultipleConstructors {

    private final Injector injector = new Injector();

    @Test
    void testNoConstructor() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> injector.getInstance(NoConstructor.class))
            .withMessage("No public constructors found");
    }

    @Test
    void testSingleConstructor() {
        SingleConstructor instance = injector.getInstance(SingleConstructor.class);
        assertThat(instance).isNotNull();
    }

    @Test
    void testMultipleConstructors() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> injector.getInstance(MultipleConstructors.class))
            .withMessage("Found multiple public constructors, but none annotated with @Inject");
    }

    @Test
    void testMultipleConstructorsSingleAnnotated() {
        MultipleConstructorsSingleAnnotated instance = injector.getInstance(MultipleConstructorsSingleAnnotated.class);
        assertThat(instance).isNotNull();
    }

    @Test
    void testMultipleConstructorsMultipleAnnotated() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> injector.getInstance(MultipleConstructorsMultipleAnnotated.class))
            .withMessage("Found multiple public constructors annotated with @Inject");
    }

}

class NoConstructor {
}

class SingleConstructor {
    public SingleConstructor() {
    }
}

class MultipleConstructors {
    public MultipleConstructors() {
    }

    public MultipleConstructors(String s) {
    }
}

class MultipleConstructorsSingleAnnotated {
    @Inject
    public MultipleConstructorsSingleAnnotated() {
    }

    public MultipleConstructorsSingleAnnotated(String s) {
    }
}

class MultipleConstructorsMultipleAnnotated {
    @Inject
    public MultipleConstructorsMultipleAnnotated() {
    }

    @Inject
    public MultipleConstructorsMultipleAnnotated(String s) {
    }
}

