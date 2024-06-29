package be.twofold.playground.common.func;

import be.twofold.playground.common.func.fi.*;

import java.io.*;
import java.util.function.*;

public final class Unchecked {
    private Unchecked() {
        throw new UnsupportedOperationException();
    }

    public static <T, U> BiConsumer<T, U> biConsumer(CheckedBiConsumer<T, U> consumer) {
        return (t, u) -> {
            try {
                consumer.accept(t, u);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static <T, U, R> BiFunction<T, U, R> biFunction(CheckedBiFunction<T, U, R> function) {
        return (t, u) -> {
            try {
                return function.apply(t, u);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static <T> BinaryOperator<T> binaryOperator(CheckedBinaryOperator<T> operator) {
        return (t, u) -> {
            try {
                return operator.apply(t, u);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static <T, U> BiPredicate<T, U> biPredicate(CheckedBiPredicate<T, U> predicate) {
        return (t, u) -> {
            try {
                return predicate.test(t, u);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static BooleanSupplier booleanSupplier(CheckedBooleanSupplier supplier) {
        return () -> {
            try {
                return supplier.getAsBoolean();
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static <T> Consumer<T> consumer(CheckedConsumer<T> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static DoubleBinaryOperator doubleBinaryOperator(CheckedDoubleBinaryOperator operator) {
        return (left, right) -> {
            try {
                return operator.applyAsDouble(left, right);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static DoubleConsumer doubleConsumer(CheckedDoubleConsumer consumer) {
        return value -> {
            try {
                consumer.accept(value);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static <R> DoubleFunction<R> doubleFunction(CheckedDoubleFunction<R> function) {
        return value -> {
            try {
                return function.apply(value);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static DoublePredicate doublePredicate(CheckedDoublePredicate predicate) {
        return value -> {
            try {
                return predicate.test(value);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static DoubleSupplier doubleSupplier(CheckedDoubleSupplier supplier) {
        return () -> {
            try {
                return supplier.getAsDouble();
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static DoubleToIntFunction doubleToIntFunction(CheckedDoubleToIntFunction function) {
        return value -> {
            try {
                return function.applyAsInt(value);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static DoubleToLongFunction doubleToLongFunction(CheckedDoubleToLongFunction function) {
        return value -> {
            try {
                return function.applyAsLong(value);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static DoubleUnaryOperator doubleUnaryOperator(CheckedDoubleUnaryOperator operator) {
        return operand -> {
            try {
                return operator.applyAsDouble(operand);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static <T, R> Function<T, R> function(CheckedFunction<T, R> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static IntBinaryOperator intBinaryOperator(CheckedIntBinaryOperator operator) {
        return (left, right) -> {
            try {
                return operator.applyAsInt(left, right);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static IntConsumer intConsumer(CheckedIntConsumer consumer) {
        return value -> {
            try {
                consumer.accept(value);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static <R> IntFunction<R> intFunction(CheckedIntFunction<R> function) {
        return value -> {
            try {
                return function.apply(value);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static IntPredicate intPredicate(CheckedIntPredicate predicate) {
        return value -> {
            try {
                return predicate.test(value);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static IntSupplier intSupplier(CheckedIntSupplier supplier) {
        return () -> {
            try {
                return supplier.getAsInt();
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static IntToDoubleFunction intToDoubleFunction(CheckedIntToDoubleFunction function) {
        return value -> {
            try {
                return function.applyAsDouble(value);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static IntToLongFunction intToLongFunction(CheckedIntToLongFunction function) {
        return value -> {
            try {
                return function.applyAsLong(value);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static IntUnaryOperator intUnaryOperator(CheckedIntUnaryOperator operator) {
        return operand -> {
            try {
                return operator.applyAsInt(operand);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static LongBinaryOperator longBinaryOperator(CheckedLongBinaryOperator operator) {
        return (left, right) -> {
            try {
                return operator.applyAsLong(left, right);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static LongConsumer longConsumer(CheckedLongConsumer consumer) {
        return value -> {
            try {
                consumer.accept(value);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static <R> LongFunction<R> longFunction(CheckedLongFunction<R> function) {
        return value -> {
            try {
                return function.apply(value);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static LongPredicate longPredicate(CheckedLongPredicate predicate) {
        return value -> {
            try {
                return predicate.test(value);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static LongSupplier longSupplier(CheckedLongSupplier supplier) {
        return () -> {
            try {
                return supplier.getAsLong();
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static LongToDoubleFunction longToDoubleFunction(CheckedLongToDoubleFunction function) {
        return value -> {
            try {
                return function.applyAsDouble(value);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static LongToIntFunction longToIntFunction(CheckedLongToIntFunction function) {
        return value -> {
            try {
                return function.applyAsInt(value);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static LongUnaryOperator longUnaryOperator(CheckedLongUnaryOperator operator) {
        return operand -> {
            try {
                return operator.applyAsLong(operand);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static <T> ObjDoubleConsumer<T> objDoubleConsumer(CheckedObjDoubleConsumer<T> consumer) {
        return (t, value) -> {
            try {
                consumer.accept(t, value);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static <T> ObjIntConsumer<T> objIntConsumer(CheckedObjIntConsumer<T> consumer) {
        return (t, value) -> {
            try {
                consumer.accept(t, value);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static <T> ObjLongConsumer<T> objLongConsumer(CheckedObjLongConsumer<T> consumer) {
        return (t, value) -> {
            try {
                consumer.accept(t, value);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static <T> Predicate<T> predicate(CheckedPredicate<T> predicate) {
        return t -> {
            try {
                return predicate.test(t);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static <T> Supplier<T> supplier(CheckedSupplier<T> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static <T, U> ToDoubleBiFunction<T, U> toDoubleBiFunction(CheckedToDoubleBiFunction<T, U> function) {
        return (t, u) -> {
            try {
                return function.applyAsDouble(t, u);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static <T> ToDoubleFunction<T> toDoubleFunction(CheckedToDoubleFunction<T> function) {
        return value -> {
            try {
                return function.applyAsDouble(value);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static <T, U> ToIntBiFunction<T, U> toIntBiFunction(CheckedToIntBiFunction<T, U> function) {
        return (t, u) -> {
            try {
                return function.applyAsInt(t, u);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static <T> ToIntFunction<T> toIntFunction(CheckedToIntFunction<T> function) {
        return value -> {
            try {
                return function.applyAsInt(value);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static <T, U> ToLongBiFunction<T, U> toLongBiFunction(CheckedToLongBiFunction<T, U> function) {
        return (t, u) -> {
            try {
                return function.applyAsLong(t, u);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static <T> ToLongFunction<T> toLongFunction(CheckedToLongFunction<T> function) {
        return value -> {
            try {
                return function.applyAsLong(value);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    public static <T> UnaryOperator<T> unaryOperator(CheckedUnaryOperator<T> operator) {
        return t -> {
            try {
                return operator.apply(t);
            } catch (Throwable throwable) {
                throw handleThrowable(throwable);
            }
        };
    }

    private static RuntimeException handleThrowable(Throwable throwable) {
        if (throwable instanceof Error) {
            throw (Error) throwable;
        }

        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        }

        if (throwable instanceof IOException) {
            throw new UncheckedIOException((IOException) throwable);
        }

        if (throwable instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }

        throw new RuntimeException(throwable);
    }
}
