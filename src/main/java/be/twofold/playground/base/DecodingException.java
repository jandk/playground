package be.twofold.playground.base;

import java.io.*;

public final class DecodingException extends IOException {
    DecodingException(String message) {
        super(message);
    }
}
