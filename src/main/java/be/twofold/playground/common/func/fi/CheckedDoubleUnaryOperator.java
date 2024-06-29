package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedDoubleUnaryOperator {

    double applyAsDouble(double operand) throws Throwable;

}
