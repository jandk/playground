package be.twofold.playground.base;

import be.twofold.playground.common.*;

import java.io.*;

final class Base16Encoding extends StandardBaseEncoding {
    final char[] encoding = new char[512];

    Base16Encoding(String alphabetChars) {
        this(new Alphabet(alphabetChars.toCharArray()));
    }

    Base16Encoding(Alphabet alphabet) {
        super(alphabet, null);
        Check.argument(alphabet.chars.length == 16);
        for (int i = 0; i < 256; ++i) {
            encoding[i] = alphabet.encode(i >>> 4);
            encoding[i | 0x100] = alphabet.encode(i & 0xF);
        }
    }

    @Override
    void encodeTo(Appendable target, byte[] bytes, int off, int len) throws IOException {
        Check.notNull(target);
        Check.fromToIndex(off, off + len, bytes.length);
        for (int i = 0; i < len; ++i) {
            int b = bytes[off + i] & 0xFF;
            target.append(encoding[b]);
            target.append(encoding[b | 0x100]);
        }
    }

    @Override
    int decodeTo(byte[] target, CharSequence chars) throws DecodingException {
        Check.notNull(target);
        if (chars.length() % 2 == 1) {
            throw new DecodingException("Invalid input length " + chars.length());
        }
        int bytesWritten = 0;
        for (int i = 0; i < chars.length(); i += 2) {
            int decoded = alphabet.decode(chars.charAt(i)) << 4 | alphabet.decode(chars.charAt(i + 1));
            target[bytesWritten++] = (byte) decoded;
        }
        return bytesWritten;
    }

    @Override
    BaseEncoding newInstance(Alphabet alphabet, Character paddingChar) {
        return new Base16Encoding(alphabet);
    }
}
