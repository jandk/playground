package be.twofold.playground.common;

public final class Utf8 {
    private Utf8() {
    }

    public static boolean validate(byte[] array) {
        return validate(array, 0, array.length);
    }

    private static boolean validate(byte[] array, int fromIndex, int toIndex) {
        return false;
    }
}
