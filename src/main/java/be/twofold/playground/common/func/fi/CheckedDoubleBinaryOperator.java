package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedDoubleBinaryOperator {

    double applyAsDouble(double left, double right) throws Throwable;

}
