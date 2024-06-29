package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedDoublePredicate {

    boolean test(double value) throws Throwable;

}
