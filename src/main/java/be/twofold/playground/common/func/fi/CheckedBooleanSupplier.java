package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedBooleanSupplier {

    boolean getAsBoolean() throws Throwable;

}