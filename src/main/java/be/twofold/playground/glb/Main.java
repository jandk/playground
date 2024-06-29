package be.twofold.playground.glb;

import java.io.*;
import java.nio.file.*;

public class Main {
    public static void main(String[] args) {
        try (GlbReader reader = new GlbReader(Files.newByteChannel(Path.of("D:\\Jan\\Desktop\\imp_LOD0.glb")))) {
            reader.read();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
