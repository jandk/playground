package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedIntSupplier {

    int getAsInt() throws Throwable;

}
