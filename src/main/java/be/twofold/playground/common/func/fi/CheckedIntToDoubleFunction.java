package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedIntToDoubleFunction {

    double applyAsDouble(int value) throws Throwable;

}
