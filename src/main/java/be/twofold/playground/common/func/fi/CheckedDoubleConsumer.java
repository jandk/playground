package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedDoubleConsumer {

    void accept(double value) throws Throwable;

}
