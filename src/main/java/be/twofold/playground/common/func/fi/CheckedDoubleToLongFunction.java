package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedDoubleToLongFunction {

    long applyAsLong(double value) throws Throwable;

}
