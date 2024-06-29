package be.twofold.playground;

import be.twofold.playground.common.*;

import java.io.*;
import java.util.*;

class Base32Encoding {
    final Alphabet32 alphabet;
    final Character paddingChar = '=';

    Base32Encoding(String alphabetChars) {
        this(new Alphabet32(alphabetChars.toCharArray()));
    }

    private Base32Encoding(Alphabet32 alphabet) {
        this.alphabet = Check.notNull(alphabet);
        Check.argument(alphabet.chars.length == 32);
    }

    private static byte[] extract(byte[] result, int length) {
        if (length == result.length) {
            return result;
        }
        byte[] trunc = new byte[length];
        System.arraycopy(result, 0, trunc, 0, length);
        return trunc;
    }

    public static int divide(int p, int q) {
        int div = p / q;
        int rem = p - q * div;
        return rem == 0 ? div : div + 1;
    }

    int maxEncodedSize(int bytes) {
        return 8 * divide(bytes, 5);
    }

    void encodeTo(Appendable target, byte[] bytes, int off, int len) throws IOException {
        Check.notNull(target);
        Check.fromToIndex(off, off + len, bytes.length);
        for (int i = 0; i < len; i += 5) {
            encodeChunkTo(target, bytes, off + i, Math.min(5, len - i));
        }
    }

    void encodeChunkTo(Appendable target, byte[] bytes, int off, int len) throws IOException {
        Check.notNull(target);
        Check.fromToIndex(off, off + len, bytes.length);
        Check.argument(len <= 5);
        long bitBuffer = 0;
        for (int i = 0; i < len; ++i) {
            bitBuffer |= bytes[off + i] & 0xFF;
            bitBuffer <<= 8; // Add additional zero byte in the end.
        }
        // Position of first character is length of bitBuffer minus bitsPerChar.
        int bitOffset = (len + 1) * 8 - 5;
        int bitsProcessed = 0;
        while (bitsProcessed < len * 8) {
            int charIndex = (int) (bitBuffer >>> (bitOffset - bitsProcessed)) & alphabet.mask;
            target.append(alphabet.encode(charIndex));
            bitsProcessed += 5;
        }
        while (bitsProcessed < 5 * 8) {
            target.append(paddingChar);
            bitsProcessed += 5;
        }
    }

    int maxDecodedSize(int chars) {
        return (int) ((5 * (long) chars + 7L) / 8L);
    }

    CharSequence trimTrailingPadding(CharSequence chars) {
        Check.notNull(chars);
        int l;
        for (l = chars.length() - 1; l >= 0; l--) {
            if (chars.charAt(l) != paddingChar) {
                break;
            }
        }
        return chars.subSequence(0, l + 1);
    }

    int decodeTo(byte[] target, CharSequence chars) {
        Check.notNull(target);
        chars = trimTrailingPadding(chars);
        if (!alphabet.isValidPaddingStartPosition(chars.length())) {
            throw new IllegalArgumentException("Invalid input length " + chars.length());
        }
        int bytesWritten = 0;
        for (int charIdx = 0; charIdx < chars.length(); charIdx += 8) {
            long chunk = 0;
            int charsProcessed = 0;
            for (int i = 0; i < 8; i++) {
                chunk <<= 5;
                if (charIdx + i < chars.length()) {
                    chunk |= alphabet.decode(chars.charAt(charIdx + charsProcessed++));
                }
            }
            int minOffset = 5 * 8 - charsProcessed * 5;
            for (int offset = (5 - 1) * 8; offset >= minOffset; offset -= 8) {
                target[bytesWritten++] = (byte) ((chunk >>> offset) & 0xFF);
            }
        }
        return bytesWritten;
    }

    public String encode(byte[] bytes) {
        return encode(bytes, 0, bytes.length);
    }

    public final String encode(byte[] bytes, int off, int len) {
        Check.fromToIndex(off, off + len, bytes.length);
        StringBuilder result = new StringBuilder(maxEncodedSize(len));
        try {
            encodeTo(result, bytes, off, len);
        } catch (IOException impossible) {
            throw new AssertionError(impossible);
        }
        return result.toString();
    }

    public final byte[] decode(CharSequence chars) {
        chars = trimTrailingPadding(chars);
        byte[] tmp = new byte[maxDecodedSize(chars.length())];
        int len = decodeTo(tmp, chars);
        return extract(tmp, len);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof Base32Encoding other
            && alphabet.equals(other.alphabet)
            && Objects.equals(paddingChar, other.paddingChar);
    }

    @Override
    public int hashCode() {
        return alphabet.hashCode() ^ Objects.hashCode(paddingChar);
    }

    @Override
    public String toString() {
        return "BaseEncoding." + alphabet +
            ".withPadChar('" + paddingChar + "')";
    }
}
