package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedToLongFunction<T> {

    long applyAsLong(T value) throws Throwable;

}
