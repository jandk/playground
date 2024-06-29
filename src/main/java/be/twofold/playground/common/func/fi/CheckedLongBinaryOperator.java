package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedLongBinaryOperator {

    long applyAsLong(long left, long right) throws Throwable;

}
