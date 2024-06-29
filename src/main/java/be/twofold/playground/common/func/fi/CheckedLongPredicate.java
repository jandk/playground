package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedLongPredicate {

    boolean test(long value) throws Throwable;

}
