package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedSupplier<T> {

    T get() throws Throwable;

}
