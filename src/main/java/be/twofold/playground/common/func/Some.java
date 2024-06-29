package be.twofold.playground.common.func;

final class Some<T> extends Option<T> {
    private final T value;

    Some(T value) {
        this.value = value;
    }
}
