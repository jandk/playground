package be.twofold.playground.json;

import java.io.*;
import java.util.*;

final class PeekingReader {
    private static final int UNPEEKED = -2;

    private final Reader reader;
    private int peeked = UNPEEKED;
    private int line;
    private int column;

    public PeekingReader(Reader reader) {
        this.reader = Objects.requireNonNull(reader);
    }

    public int read() {
        if (peeked == UNPEEKED) {
            return readChar();
        }
        int result = peeked;
        if (result == '\n') {
            line++;
            column = 0;
        } else {
            column++;
        }
        peeked = UNPEEKED;
        return result;
    }

    public int peek() {
        if (peeked == UNPEEKED) {
            peeked = readChar();
        }
        return peeked;
    }

    public boolean eof() {
        return peek() == -1;
    }

    private int readChar() {
        try {
            int high = reader.read();
            if (high < 0 || !Character.isSurrogate((char) high)) {
                return high;
            }
            if (Character.isLowSurrogate((char) high)) {
                throw new IOException("Unpaired low surrogate");
            }

            int low = reader.read();
            if (low < 0 || Character.isHighSurrogate((char) low)) {
                throw new IOException("Unpaired high surrogate");
            }

            return Character.toCodePoint((char) high, (char) low);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
