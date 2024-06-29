package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedLongSupplier {

    long getAsLong() throws Throwable;

}
