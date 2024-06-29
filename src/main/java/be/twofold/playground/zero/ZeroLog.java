package be.twofold.playground.zero;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

public class ZeroLog {

    public static void main(String[] args) {
        ZoneId zone = ZoneId.of("Europe/Brussels");
        Path path = Paths.get("D:\\Jan\\Downloads\\20220212_16.25_538ZFAZ75LCK13306_MBB.bin");
        List<LogEntry> entries = readEntriesFromFile(path);

        for (LogEntry entry : entries) {
            if (entry.header().type() == (byte) 0xfd) {
                String s = new String(entry.content(), 0, entry.content().length - 1, StandardCharsets.ISO_8859_1).strip();
                System.out.println(entry.header().timestamp().atZone(zone).toLocalDateTime() + "\t" + s);
            }
        }

        System.out.println(entries.size());
    }

    private static String toHexString(byte[] array) {
        char[] hex = "0123456789abcdef".toCharArray();
        char[] rawString = new char[array.length * 3 - 1];
        Arrays.fill(rawString, ' ');
        for (int i = 0, o = 0; i < array.length; i++, o += 3) {
            rawString[o] = hex[(array[i] & 0xf0) >>> 4];
            rawString[o + 1] = hex[array[i] & 0x0f];
        }
        return new String(rawString);
    }

    private static List<LogEntry> readEntriesFromFile(Path path) {
        List<LogEntry> entries = new ArrayList<>();
        try (InputStream input = Files.newInputStream(path)) {
            while (findHeader(input)) {
                int length = input.read();
                byte[] rawEntry = input.readNBytes(length - 2);
                LogEntry entry = LogEntry.from(escape(rawEntry));
                entries.add(entry);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return entries;
    }

    private static boolean findHeader(InputStream input) throws IOException {
        while (true) {
            int read = input.read();
            if (read == 0xb2) {
                return true;
            }
            if (read == -1) {
                return false;
            }
        }
    }

    private static byte[] escape(byte[] array) {
//        return array;
        int o = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == (byte) 0xfe) {
                array[o++] = (byte) (array[i] ^ (array[++i] - 1));
            } else {
                array[o++] = array[i];
            }
        }
        return Arrays.copyOf(array, o);
    }

    private static int[] parseLine(String s) {
        return Arrays.stream(s.substring(1, s.length() - 1).split(", "))
            .mapToInt(Integer::parseInt)
            .toArray();
    }
}
