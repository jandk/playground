package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedToLongBiFunction<T, U> {

    long applyAsLong(T t, U u) throws Throwable;

}
