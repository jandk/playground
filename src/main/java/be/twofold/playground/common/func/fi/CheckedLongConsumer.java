package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedLongConsumer {

    void accept(long value) throws Throwable;

}
