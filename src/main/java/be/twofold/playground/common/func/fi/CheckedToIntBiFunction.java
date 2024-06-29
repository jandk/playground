package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedToIntBiFunction<T, U> {

    int applyAsInt(T t, U u) throws Throwable;

}
