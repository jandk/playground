package be.twofold.playground;

import java.util.regex.*;

public class Strings {

    public static void main(String[] args) {
        Pattern compile = Pattern.compile("\\$");
    }

    public static void iterate(CharSequence s) {
        for (int i = 0, len = s.length(); i < len; ) {
            int cp = validCodePointAt(s, i);

            // do something with the codepoint

            i += Character.charCount(cp);
        }
    }

    private static int validCodePointAt(CharSequence s, int i) {
        char c1 = s.charAt(i);
        if (!Character.isHighSurrogate(c1)) {
            return c1;
        }
        if (++i < s.length()) {
            char c2 = s.charAt(i);
            if (Character.isLowSurrogate(c2)) {
                return Character.toCodePoint(c1, c2);
            }
        }
        throw new IllegalArgumentException("Unpaired surrogate");
    }

}
