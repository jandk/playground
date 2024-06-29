package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedIntUnaryOperator {

    int applyAsInt(int operand) throws Throwable;

}
