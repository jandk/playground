package be.twofold.playground.injector.singleton;

import be.twofold.playground.injector.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

public class TestInjectorSingleton {

    @Test
    public void testWithSingleton() {
        Injector injector = new Injector();

        WithSingleton singleton1 = injector.getInstance(WithSingleton.class);
        WithSingleton singleton2 = injector.getInstance(WithSingleton.class);
        assertThat(singleton1).isSameAs(singleton2);
    }

    @Test
    public void testWithoutSingleton() {
        Injector injector = new Injector();

        WithoutSingleton singleton1 = injector.getInstance(WithoutSingleton.class);
        WithoutSingleton singleton2 = injector.getInstance(WithoutSingleton.class);
        assertThat(singleton1).isNotSameAs(singleton2);
    }

}
