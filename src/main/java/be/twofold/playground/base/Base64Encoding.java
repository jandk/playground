package be.twofold.playground.base;

import be.twofold.playground.common.*;

import java.io.*;

final class Base64Encoding extends StandardBaseEncoding {
    Base64Encoding(String alphabetChars, Character paddingChar) {
        this(new Alphabet(alphabetChars.toCharArray()), paddingChar);
    }

    Base64Encoding(Alphabet alphabet, Character paddingChar) {
        super(alphabet, paddingChar);
        Check.argument(alphabet.chars.length == 64);
    }

    @Override
    void encodeTo(Appendable target, byte[] bytes, int off, int len) throws IOException {
        Check.notNull(target);
        Check.fromToIndex(off, off + len, bytes.length);
        int i = off;
        for (int remaining = len; remaining >= 3; remaining -= 3) {
            int chunk = (bytes[i++] & 0xFF) << 16 | (bytes[i++] & 0xFF) << 8 | bytes[i++] & 0xFF;
            target.append(alphabet.encode(chunk >>> 18));
            target.append(alphabet.encode((chunk >>> 12) & 0x3F));
            target.append(alphabet.encode((chunk >>> 6) & 0x3F));
            target.append(alphabet.encode(chunk & 0x3F));
        }
        if (i < off + len) {
            encodeChunkTo(target, bytes, i, off + len - i);
        }
    }

    @Override
    int decodeTo(byte[] target, CharSequence chars) throws DecodingException {
        Check.notNull(target);
        chars = trimTrailingPadding(chars);
        if (!alphabet.isValidPaddingStartPosition(chars.length())) {
            throw new DecodingException("Invalid input length " + chars.length());
        }
        int bytesWritten = 0;
        for (int i = 0; i < chars.length(); ) {
            int chunk = alphabet.decode(chars.charAt(i++)) << 18;
            chunk |= alphabet.decode(chars.charAt(i++)) << 12;
            target[bytesWritten++] = (byte) (chunk >>> 16);
            if (i < chars.length()) {
                chunk |= alphabet.decode(chars.charAt(i++)) << 6;
                target[bytesWritten++] = (byte) ((chunk >>> 8) & 0xFF);
                if (i < chars.length()) {
                    chunk |= alphabet.decode(chars.charAt(i++));
                    target[bytesWritten++] = (byte) (chunk & 0xFF);
                }
            }
        }
        return bytesWritten;
    }

    @Override
    BaseEncoding newInstance(Alphabet alphabet, Character paddingChar) {
        return new Base64Encoding(alphabet, paddingChar);
    }
}
