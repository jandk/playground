package be.twofold.playground.injector.cyclic;

public class B {
    private final A a;

    public B(A a) {
        this.a = a;
    }
}
