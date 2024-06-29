package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedLongFunction<R> {

    R apply(long value) throws Throwable;

}
