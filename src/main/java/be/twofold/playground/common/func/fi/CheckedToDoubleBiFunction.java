package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedToDoubleBiFunction<T, U> {

    double applyAsDouble(T t, U u) throws Throwable;

}
