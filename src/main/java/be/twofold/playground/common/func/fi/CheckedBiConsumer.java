package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedBiConsumer<T, U> {

    void accept(T t, U u) throws Throwable;

}
