package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedIntBinaryOperator {

    int applyAsInt(int left, int right) throws Throwable;

}
