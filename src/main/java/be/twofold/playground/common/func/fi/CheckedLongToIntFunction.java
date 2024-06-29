package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedLongToIntFunction {

    int applyAsInt(long value) throws Throwable;

}
