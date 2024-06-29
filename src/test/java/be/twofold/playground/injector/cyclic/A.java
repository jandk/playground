package be.twofold.playground.injector.cyclic;

public class A {
    private final B b;

    public A(B b) {
        this.b = b;
    }
}
