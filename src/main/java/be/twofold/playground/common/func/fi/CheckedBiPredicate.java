package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedBiPredicate<T, U> {

    boolean test(T t, U u) throws Throwable;

}
