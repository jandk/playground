package be.twofold.playground.stl;

import be.twofold.playground.tinyobj.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public final class StlReader implements AutoCloseable {
    private final SeekableByteChannel channel;

    public StlReader(SeekableByteChannel channel) {
        this.channel = channel;
    }

    public static Stl read(Path path) {
        try {
            return new StlReader(Files.newByteChannel(path)).read();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Stl read() throws IOException {
        byte[] header = readHeader();
        int faceCount = readBuffer(4).getInt();
        List<StlFace> faces = readFaces(faceCount);
        return new Stl(header, faces);
    }

    private byte[] readHeader() {
        return readBuffer(80).array();
    }

    private List<StlFace> readFaces(int faceCount) {
        return IntStream.range(0, faceCount)
            .mapToObj(i -> readFace())
            .toList();
    }

    private StlFace readFace() {
        ByteBuffer buffer = readBuffer(50);
        Vector3 normal = readVector3(buffer);
        Vector3 v1 = readVector3(buffer);
        Vector3 v2 = readVector3(buffer);
        Vector3 v3 = readVector3(buffer);
        buffer.getShort(); // attribute byte count
        return new StlFace(normal, v1, v2, v3);
    }

    private static Vector3 readVector3(ByteBuffer buffer) {
        float x = buffer.getFloat();
        float y = buffer.getFloat();
        float z = buffer.getFloat();
        return new Vector3(
            x == -0.0f ? 0.0f : x,
            y == -0.0f ? 0.0f : y,
            z == -0.0f ? 0.0f : z
        );
    }

    private ByteBuffer readBuffer(int size) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
            channel.read(buffer);
            buffer.flip();
            return buffer;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
