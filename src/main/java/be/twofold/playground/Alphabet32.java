package be.twofold.playground;

import be.twofold.playground.common.*;

import java.util.*;

final class Alphabet32 {
    final char[] chars;
    final int mask = 0x1f;
    private final boolean[] validPadding;

    private static byte[] Decode = new byte[]{
        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
        (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
        (byte) 0x08, (byte) 0x09, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
        (byte) 0xff, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f, (byte) 0x10,
        (byte) 0x11, (byte) 0x01, (byte) 0x12, (byte) 0x13, (byte) 0x01, (byte) 0x14, (byte) 0x15, (byte) 0x00,
        (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, (byte) 0x1a, (byte) 0xff, (byte) 0x1b, (byte) 0x1c,
        (byte) 0x1d, (byte) 0x1e, (byte) 0x1f, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
        (byte) 0xff, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f, (byte) 0x10,
        (byte) 0x11, (byte) 0x01, (byte) 0x12, (byte) 0x13, (byte) 0x01, (byte) 0x14, (byte) 0x15, (byte) 0x00,
        (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, (byte) 0x1a, (byte) 0xff, (byte) 0x1b, (byte) 0x1c,
        (byte) 0x1d, (byte) 0x1e, (byte) 0x1f, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
    };

    Alphabet32(char[] chars) {
        this.chars = Check.notNull(chars);

        boolean[] validPadding = new boolean[8];
        for (int i = 0; i < 5; i++) {
            validPadding[Base32Encoding.divide(i * 8, 5)] = true;
        }
        this.validPadding = validPadding;
    }

    char encode(int bits) {
        return chars[bits];
    }

    boolean isValidPaddingStartPosition(int index) {
        return validPadding[index % 8];
    }

    int decode(char ch) {
        int result = ch > 127 ? -1 : Decode[ch];
        if (result == -1) {
            throw new IllegalArgumentException("Unrecognized character: 0x" + Integer.toHexString(ch));
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof Alphabet32 other
            && Arrays.equals(chars, other.chars);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(chars);
    }
}
