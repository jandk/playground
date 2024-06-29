package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedIntToLongFunction {

    long applyAsLong(int value) throws Throwable;

}
