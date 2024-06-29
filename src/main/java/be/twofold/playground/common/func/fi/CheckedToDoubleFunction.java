package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedToDoubleFunction<T> {

    double applyAsDouble(T value) throws Throwable;

}
