package be.twofold.playground.common;

public final class Ascii {
    private Ascii() {
    }

    public static boolean isAscii(char c) {
        return c <= 0x7f;
    }

    public static boolean isLower(char c) {
        return c >= 'a' && c <= 'z';
    }

    public static boolean isPrint(char ch) {
        return ch >= 0x20 && ch < 0x7f;
    }

    public static boolean isUpper(char c) {
        return c >= 'A' && c <= 'Z';
    }

    public static char toLower(char c) {
        return isUpper(c) ? (char) (c + 0x20) : c;
    }

    public static char toUpper(char c) {
        return isLower(c) ? (char) (c - 0x20) : c;
    }
}
