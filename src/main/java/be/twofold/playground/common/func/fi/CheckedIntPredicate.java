package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedIntPredicate {

    boolean test(int value) throws Throwable;

}
