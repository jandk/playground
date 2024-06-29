package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedToIntFunction<T> {

    int applyAsInt(T value) throws Throwable;

}
