package be.twofold.playground.common.func;

public abstract class Option<T> {
    Option() {
    }

    public static <T> Option<T> some(T value) {
        return new Some<>(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> Option<T> none() {
        return (Option<T>) None.Instance;
    }
}
