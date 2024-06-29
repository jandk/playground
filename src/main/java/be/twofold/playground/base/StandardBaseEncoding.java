package be.twofold.playground.base;

import be.twofold.playground.common.*;

import java.io.*;
import java.util.*;

class StandardBaseEncoding extends BaseEncoding {
    final Alphabet alphabet;

    final Character paddingChar;
    volatile BaseEncoding upperCase;
    volatile BaseEncoding lowerCase;
    volatile BaseEncoding ignoreCase;

    StandardBaseEncoding(String alphabetChars, Character paddingChar) {
        this(new Alphabet(alphabetChars.toCharArray()), paddingChar);
    }

    StandardBaseEncoding(Alphabet alphabet, Character paddingChar) {
        this.alphabet = Check.notNull(alphabet);
        Check.argument(paddingChar == null || !alphabet.matches(paddingChar),
            () -> String.format("Padding character %s was already in alphabet", paddingChar));
        this.paddingChar = paddingChar;
    }

    @Override
    int maxEncodedSize(int bytes) {
        return alphabet.charsPerChunk * divideCeil(bytes, alphabet.bytesPerChunk);
    }

    @Override
    void encodeTo(Appendable target, byte[] bytes, int off, int len) throws IOException {
        Check.notNull(target);
        Check.fromToIndex(off, off + len, bytes.length);
        for (int i = 0; i < len; i += alphabet.bytesPerChunk) {
            encodeChunkTo(target, bytes, off + i, Math.min(alphabet.bytesPerChunk, len - i));
        }
    }

    void encodeChunkTo(Appendable target, byte[] bytes, int off, int len) throws IOException {
        Check.notNull(target);
        Check.fromToIndex(off, off + len, bytes.length);
        Check.argument(len <= alphabet.bytesPerChunk);
        long bitBuffer = 0;
        for (int i = 0; i < len; ++i) {
            bitBuffer |= bytes[off + i] & 0xFF;
            bitBuffer <<= 8; // Add additional zero byte in the end.
        }
        // Position of first character is length of bitBuffer minus bitsPerChar.
        int bitOffset = (len + 1) * 8 - alphabet.bitsPerChar;
        int bitsProcessed = 0;
        while (bitsProcessed < len * 8) {
            int charIndex = (int) (bitBuffer >>> (bitOffset - bitsProcessed)) & alphabet.mask;
            target.append(alphabet.encode(charIndex));
            bitsProcessed += alphabet.bitsPerChar;
        }
        if (paddingChar != null) {
            while (bitsProcessed < alphabet.bytesPerChunk * 8) {
                target.append(paddingChar.charValue());
                bitsProcessed += alphabet.bitsPerChar;
            }
        }
    }

    @Override
    int maxDecodedSize(int chars) {
        return (int) ((alphabet.bitsPerChar * (long) chars + 7L) / 8L);
    }

    @Override
    CharSequence trimTrailingPadding(CharSequence chars) {
        Check.notNull(chars);
        if (paddingChar == null) {
            return chars;
        }
        char padChar = paddingChar.charValue();
        int l;
        for (l = chars.length() - 1; l >= 0; l--) {
            if (chars.charAt(l) != padChar) {
                break;
            }
        }
        return chars.subSequence(0, l + 1);
    }

    @Override
    public boolean canDecode(CharSequence chars) {
        Check.notNull(chars);
        chars = trimTrailingPadding(chars);
        if (!alphabet.isValidPaddingStartPosition(chars.length())) {
            return false;
        }
        for (int i = 0; i < chars.length(); i++) {
            if (!alphabet.canDecode(chars.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    int decodeTo(byte[] target, CharSequence chars) throws DecodingException {
        Check.notNull(target);
        chars = trimTrailingPadding(chars);
        if (!alphabet.isValidPaddingStartPosition(chars.length())) {
            throw new DecodingException("Invalid input length " + chars.length());
        }
        int bytesWritten = 0;
        for (int charIdx = 0; charIdx < chars.length(); charIdx += alphabet.charsPerChunk) {
            long chunk = 0;
            int charsProcessed = 0;
            for (int i = 0; i < alphabet.charsPerChunk; i++) {
                chunk <<= alphabet.bitsPerChar;
                if (charIdx + i < chars.length()) {
                    chunk |= alphabet.decode(chars.charAt(charIdx + charsProcessed++));
                }
            }
            int minOffset = alphabet.bytesPerChunk * 8 - charsProcessed * alphabet.bitsPerChar;
            for (int offset = (alphabet.bytesPerChunk - 1) * 8; offset >= minOffset; offset -= 8) {
                target[bytesWritten++] = (byte) ((chunk >>> offset) & 0xFF);
            }
        }
        return bytesWritten;
    }

    @Override
    public BaseEncoding omitPadding() {
        return (paddingChar == null) ? this : newInstance(alphabet, null);
    }

    @Override
    public BaseEncoding withPadChar(char padChar) {
        if (8 % alphabet.bitsPerChar == 0
            || (paddingChar != null && paddingChar.charValue() == padChar)) {
            return this;
        } else {
            return newInstance(alphabet, padChar);
        }
    }

    @Override
    public BaseEncoding upperCase() {
        BaseEncoding result = upperCase;
        if (result == null) {
            Alphabet upper = alphabet.upperCase();
            result = upperCase = (upper == alphabet) ? this : newInstance(upper, paddingChar);
        }
        return result;
    }

    @Override
    public BaseEncoding lowerCase() {
        BaseEncoding result = lowerCase;
        if (result == null) {
            Alphabet lower = alphabet.lowerCase();
            result = lowerCase = (lower == alphabet) ? this : newInstance(lower, paddingChar);
        }
        return result;
    }

    @Override
    public BaseEncoding ignoreCase() {
        BaseEncoding result = ignoreCase;
        if (result == null) {
            Alphabet ignore = alphabet.ignoreCase();
            result = ignoreCase = (ignore == alphabet) ? this : newInstance(ignore, paddingChar);
        }
        return result;
    }

    BaseEncoding newInstance(Alphabet alphabet, Character paddingChar) {
        return new StandardBaseEncoding(alphabet, paddingChar);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("BaseEncoding.");
        builder.append(alphabet);
        if (8 % alphabet.bitsPerChar != 0) {
            if (paddingChar == null) {
                builder.append(".omitPadding()");
            } else {
                builder.append(".withPadChar('").append(paddingChar).append("')");
            }
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof StandardBaseEncoding) {
            StandardBaseEncoding that = (StandardBaseEncoding) other;
            return this.alphabet.equals(that.alphabet)
                && Objects.equals(this.paddingChar, that.paddingChar);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return alphabet.hashCode() ^ Objects.hashCode(paddingChar);
    }
}
