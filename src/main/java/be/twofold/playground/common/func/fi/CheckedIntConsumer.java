package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedIntConsumer {

    void accept(int value) throws Throwable;

}
