package be.twofold.playground.common;

import java.util.function.*;

public final class Check {
    private Check() {
    }

    public static <T> T notNull(T obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return obj;
    }

    public static <T> T notNull(T obj, String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
        return obj;
    }

    public static <T> T notNull(T obj, Supplier<String> messageSupplier) {
        if (obj == null) {
            throw new NullPointerException(messageSupplier == null ? null : messageSupplier.get());
        }
        return obj;
    }

    public static void argument(boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    public static void argument(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void argument(boolean expression, Supplier<String> messageSupplier) {
        if (!expression) {
            throw new IllegalArgumentException(messageSupplier == null ? null : messageSupplier.get());
        }
    }

    public static int index(int index, int length) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException(String.format("Index %s out of bounds for length %s", index, length));
        }
        return index;
    }

    public static int fromToIndex(int fromIndex, int toIndex, int length) {
        if (fromIndex < 0 || fromIndex > toIndex || toIndex > length) {
            throw new IndexOutOfBoundsException(String.format("Range [%s, %s) out of bounds for length %s", fromIndex, toIndex, length));
        }
        return fromIndex;
    }
}
