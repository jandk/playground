package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedObjLongConsumer<T> {

    void accept(T t, long value) throws Throwable;

}
