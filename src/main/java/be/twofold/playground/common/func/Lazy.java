package be.twofold.playground.common.func;

import be.twofold.playground.common.*;

import java.util.function.*;

public final class Lazy<T> implements Supplier<T> {
    private Supplier<? extends T> supplier;
    private volatile T value;

    private Lazy(Supplier<? extends T> supplier) {
        this.supplier = Check.notNull(supplier, "supplier");
    }

    private static <T> Lazy<T> lazy(Supplier<? extends T> supplier) {
        return new Lazy<>(supplier);
    }

    @Override
    public T get() {
        T value1 = value;
        if (value1 != null) {
            return value1;
        }

        synchronized (this) {
            T value2 = value;
            if (value2 != null) {
                return value2;
            }

            T result = supplier.get();
            value = result;
            supplier = null;
            return result;
        }
    }
}
