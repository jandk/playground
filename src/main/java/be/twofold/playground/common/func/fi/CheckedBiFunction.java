package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedBiFunction<T, U, R> {

    R apply(T t, U u) throws Throwable;

}
