package be.twofold.playground.common;

import java.nio.charset.*;
import java.util.*;

public final class Utf8String {
    private final byte[] value;
    private int hashCode = 0;

    private Utf8String(byte[] value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof Utf8String
            && Arrays.equals(value, ((Utf8String) obj).value);
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Arrays.hashCode(value);
        }
        return hashCode;
    }

    @Override
    public String toString() {
        return new String(value, StandardCharsets.UTF_8);
    }
}
