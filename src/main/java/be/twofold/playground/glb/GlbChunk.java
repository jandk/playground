package be.twofold.playground.glb;

import java.nio.*;

public record GlbChunk(int length, GlbChunkType type, ByteBuffer data) {
}
