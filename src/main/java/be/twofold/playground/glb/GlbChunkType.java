package be.twofold.playground.glb;

import java.util.*;

enum GlbChunkType {
    JSON(0x4E4F534A),
    BIN(0x004E4942);

    private final int code;

    GlbChunkType(int code) {
        this.code = code;
    }

    public static GlbChunkType fromCode(int code) {
        return Arrays.stream(values())
            .filter(value -> value.code == code)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid code: " + code));
    }
}
