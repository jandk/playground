package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedLongToDoubleFunction {

    double applyAsDouble(long value) throws Throwable;

}
