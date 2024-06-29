package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedDoubleSupplier {

    double getAsDouble() throws Throwable;

}
