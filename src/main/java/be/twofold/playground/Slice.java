package be.twofold.playground;

import java.util.*;

public final class Slice {
    private final byte[] array;
    private final int offset;
    private final int length;

    public Slice(byte[] array, int offset, int length) {
        Objects.checkFromIndexSize(offset, length, array.length);
        this.array = array;
        this.offset = offset;
        this.length = length;
    }

    public Slice subSlice(int offset) {
        return subSlice(offset, length - offset);
    }

    public Slice subSlice(int offset, int length) {
        Objects.checkFromIndexSize(offset, length, this.length);
        return new Slice(array, this.offset + offset, length);
    }
}
