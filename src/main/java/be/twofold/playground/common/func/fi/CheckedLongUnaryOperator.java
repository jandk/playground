package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedLongUnaryOperator {

    long applyAsLong(long operand) throws Throwable;

}
