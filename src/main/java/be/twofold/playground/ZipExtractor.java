package be.twofold.playground;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class ZipExtractor {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: ZipExtractor <sourceFile> <targetFolder>");
            return;
        }

        Path sourceFile = Path.of(args[0]);
        Path targetFolder = Path.of(args[1]);

        try (ZipInputStream in = new ZipInputStream(Files.newInputStream(sourceFile, StandardOpenOption.READ))) {
            while (true) {
                ZipEntry entry = in.getNextEntry();
                if (entry == null) {
                    break;
                }

                Path filePath = targetFolder.resolve(entry.getName());
                if (!entry.isDirectory()) {
                    try (OutputStream out = Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                        in.transferTo(out);
                    }
                }

                in.closeEntry();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
