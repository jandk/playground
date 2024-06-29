package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedIntFunction<R> {

    R apply(int value) throws Throwable;

}
