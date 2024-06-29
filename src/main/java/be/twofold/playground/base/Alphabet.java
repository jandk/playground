package be.twofold.playground.base;

import be.twofold.playground.common.*;

import java.util.*;

final class Alphabet {
    final int mask;
    final int bitsPerChar;
    final int charsPerChunk;
    final int bytesPerChunk;

    final char[] chars;
    private final byte[] decodabet;
    private final boolean[] validPadding;
    private final boolean ignoreCase;

    Alphabet(char[] chars) {
        this(chars, decodabetFor(chars), /* ignoreCase= */ false);
    }

    Alphabet(char[] chars, byte[] decodabet, boolean ignoreCase) {
        this.chars = Check.notNull(chars);
        try {
            this.bitsPerChar = BaseEncoding.log2(chars.length);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Illegal alphabet length " + chars.length, e);
        }

        // Compute how input bytes are chunked. For example, with base64 we chunk every 3 bytes into
        // 4 characters. We have bitsPerChar == 6, charsPerChunk == 4, and bytesPerChunk == 3.
        // We're looking for the smallest charsPerChunk such that bitsPerChar * charsPerChunk is a
        // multiple of 8. A multiple of 8 has 3 low zero bits, so we just need to figure out how many
        // extra zero bits we need to add to the end of bitsPerChar to get 3 in total.
        // The logic here would be wrong for bitsPerChar > 8, but since we require distinct ASCII
        // characters that can't happen.
        int zeroesInBitsPerChar = Integer.numberOfTrailingZeros(bitsPerChar);
        this.charsPerChunk = 1 << (3 - zeroesInBitsPerChar);
        this.bytesPerChunk = bitsPerChar >> zeroesInBitsPerChar;

        this.mask = chars.length - 1;

        this.decodabet = decodabet;

        boolean[] validPadding = new boolean[charsPerChunk];
        for (int i = 0; i < bytesPerChunk; i++) {
            validPadding[BaseEncoding.divideCeil(i * 8, bitsPerChar)] = true;
        }
        this.validPadding = validPadding;
        this.ignoreCase = ignoreCase;
    }

    static byte[] decodabetFor(char[] chars) {
        byte[] decodabet = new byte[128];
        Arrays.fill(decodabet, (byte) -1);
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            Check.argument(c < decodabet.length, () -> String.format("Non-ASCII character: %s", c));
            Check.argument(decodabet[c] == -1, () -> String.format("Duplicate character: %s", c));
            decodabet[c] = (byte) i;
        }
        return decodabet;
    }

    /**
     * Returns an equivalent {@code Alphabet} except it ignores case.
     */
    Alphabet ignoreCase() {
        if (ignoreCase) {
            return this;
        }

        // We can't use .clone() because of GWT.
        byte[] newDecodabet = Arrays.copyOf(decodabet, decodabet.length);
        for (int upper = 'A'; upper <= 'Z'; upper++) {
            int lower = upper | 0x20;
            byte decodeUpper = decodabet[upper];
            byte decodeLower = decodabet[lower];
            if (decodeUpper == -1) {
                newDecodabet[upper] = decodeLower;
            } else {
                int upperCopy = upper;
                Check.state(decodeLower == -1,
                    () -> String.format("Can't ignoreCase() since '%s' and '%s' encode different values", (char) upperCopy, (char) lower));
                newDecodabet[lower] = decodeUpper;
            }
        }
        return new Alphabet(chars, newDecodabet, /* ignoreCase= */ true);
    }

    char encode(int bits) {
        return chars[bits];
    }

    boolean isValidPaddingStartPosition(int index) {
        return validPadding[index % charsPerChunk];
    }

    boolean canDecode(char ch) {
        return Ascii.isAscii(ch) && decodabet[ch] != -1;
    }

    int decode(char ch) throws DecodingException {
        if (!(Ascii.isAscii(ch))) {
            throw new DecodingException("Unrecognized character: 0x" + Integer.toHexString(ch));
        }
        int result = decodabet[ch];
        if (result == -1) {
            if (Ascii.isPrint(ch)) {
                throw new DecodingException("Unrecognized character: 0x" + Integer.toHexString(ch));
            } else {
                throw new DecodingException("Unrecognized character: " + ch);
            }
        }
        return result;
    }

    boolean hasLowerCase() {
        for (char c : chars) {
            if (Ascii.isLower(c)) {
                return true;
            }
        }
        return false;
    }

    boolean hasUpperCase() {
        for (char c : chars) {
            if (Ascii.isUpper(c)) {
                return true;
            }
        }
        return false;
    }

    Alphabet upperCase() {
        if (!hasLowerCase()) {
            return this;
        }
        Check.state(!hasUpperCase(), "Cannot call upperCase() on a mixed-case alphabet");
        char[] upperCased = new char[chars.length];
        for (int i = 0; i < chars.length; i++) {
            upperCased[i] = Ascii.toUpper(chars[i]);
        }
        Alphabet upperCase = new Alphabet(upperCased);
        return ignoreCase ? upperCase.ignoreCase() : upperCase;
    }

    Alphabet lowerCase() {
        if (!hasUpperCase()) {
            return this;
        }
        Check.state(!hasLowerCase(), "Cannot call lowerCase() on a mixed-case alphabet");
        char[] lowerCased = new char[chars.length];
        for (int i = 0; i < chars.length; i++) {
            lowerCased[i] = Ascii.toLower(chars[i]);
        }
        Alphabet lowerCase = new Alphabet(lowerCased);
        return ignoreCase ? lowerCase.ignoreCase() : lowerCase;
    }

    public boolean matches(char c) {
        return c < decodabet.length && decodabet[c] != -1;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Alphabet) {
            Alphabet that = (Alphabet) other;
            return this.ignoreCase == that.ignoreCase && Arrays.equals(this.chars, that.chars);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(chars) + (ignoreCase ? 1231 : 1237);
    }
}
