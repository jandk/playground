package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedDoubleToIntFunction {

    int applyAsInt(double value) throws Throwable;

}
