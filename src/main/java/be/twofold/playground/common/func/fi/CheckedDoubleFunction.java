package be.twofold.playground.common.func.fi;

@FunctionalInterface
public interface CheckedDoubleFunction<R> {

    R apply(double value) throws Throwable;

}
