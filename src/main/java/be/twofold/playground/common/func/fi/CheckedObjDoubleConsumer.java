package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedObjDoubleConsumer<T> {
    void accept(T t, double value) throws Throwable;
}
