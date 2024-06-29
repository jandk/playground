package be.twofold.playground;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class OneBRC {
    private static final String FILE = "D:\\measurements.txt";

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();

        int numProcessors = Runtime.getRuntime().availableProcessors() - 2;
        long[] offsets = new long[numProcessors + 1];

        var measurements = new HashMap<ByteArray, Statistics>();
        try (var input = new RandomAccessFile(FILE, "r")) {
            offsets[numProcessors] = input.length();
            long chunkSize = input.length() / numProcessors;

            byte[] buffer = new byte[256];
            for (int i = 1; i < numProcessors; i++) {
                long position = i * chunkSize;
                input.seek(position);
                input.read(buffer);
                int index = indexOf(buffer, 0, (byte) '\n');
                offsets[i] = position + index + 1;
            }

            try (var pool = Executors.newFixedThreadPool(numProcessors)) {
                var futures = IntStream.range(0, numProcessors)
                    .mapToObj(i -> pool.submit(() -> readFromFile(input, offsets[i], offsets[i + 1])))
                    .toList();

                for (var future : futures) {
                    try {
                        var result = future.get();
                        result.forEach((k, v) -> measurements.merge(k, v, Statistics::combine));
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        var result = new TreeMap<>(measurements);
        result.forEach((k, v) -> System.out.println(k + " " + v));

        System.out.println("Elapsed time: " + (System.currentTimeMillis() - startTime) + "ms");
    }

    private static Map<ByteArray, Statistics> readFromFile(RandomAccessFile input, long fromIndex, long toIndex) throws IOException {
        long filePos = fromIndex;
        int offset = 0;
        byte[] buffer = new byte[64 * 1024];
        var measurements = new HashMap<ByteArray, Statistics>();
        while (true) {
            if (filePos >= toIndex) {
                return measurements;
            }
            int toRead = (int) Math.min(buffer.length - offset, toIndex - filePos);
            synchronized (input) {
                input.seek(filePos);
                input.read(buffer, offset, toRead);
            }
            filePos += toRead;

            offset = 0;
            while (true) {
                int endOfLine = parseLine(buffer, offset, measurements);
                if (endOfLine == -1) {
                    int left = buffer.length - offset;
                    System.arraycopy(buffer, offset, buffer, 0, left);
                    offset = left;
                    break;
                } else {
                    offset = endOfLine;
                }
            }
        }
    }

    private static int parseLine(byte[] buffer, int offset, Map<ByteArray, Statistics> measurements) {
        int semiColonIndex = indexOf(buffer, offset + 1, (byte) ';');
        if (semiColonIndex == -1) {
            return -1;
        }

        int newLineIndex = indexOf(buffer, semiColonIndex + 1, (byte) '\n');
        if (newLineIndex == -1) {
            return -1;
        }

        var city = new ByteArray(Arrays.copyOfRange(buffer, offset, semiColonIndex));
        var value = parseLong(buffer, semiColonIndex + 1, newLineIndex - semiColonIndex - 1);
//        longs.add(value);
        measurements
            .computeIfAbsent(city, $ -> new Statistics())
            .accept(value);

        return newLineIndex + 1;
    }

    private static long parseLong(byte[] buffer, int fromIndex, int length) {
        if (buffer[fromIndex] == '-') {
            return -parseLong(buffer, fromIndex + 1, length - 1);
        }
        long result = 0;
        for (int i = fromIndex, limit = fromIndex + length; i < limit; i++) {
            if (buffer[i] == '.') {
                return result * 10 + buffer[i + 1] - '0';
            }
            result = result * 10 + buffer[i] - '0';
        }
        return result;
    }

    private static int indexOf(byte[] array, int start, byte value) {
        for (int i = start; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    static final class ByteArray implements Comparable<ByteArray> {
        private final byte[] array;

        ByteArray(byte[] array) {
            this.array = array;
        }

        @Override
        public int compareTo(ByteArray o) {
            return Arrays.compareUnsigned(array, o.array);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ByteArray ba
                && Arrays.equals(array, ba.array);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(array);
        }

        @Override
        public String toString() {
            return new String(array);
        }
    }

    static final class Statistics {
        private long count;
        private long sum;
        private long min;
        private long max;

        Statistics() {
            this(0, 0, Long.MAX_VALUE, Long.MIN_VALUE);
        }

        Statistics(long count, long sum, long min, long max) {
            this.count = count;
            this.sum = sum;
            this.min = min;
            this.max = max;
        }

        void accept(long value) {
            count++;
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        Statistics combine(Statistics other) {
            return new Statistics(
                count + other.count,
                sum + other.sum,
                Math.min(min, other.min),
                Math.max(max, other.max)
            );
        }

        long getMin() {
            return min;
        }

        long getMax() {
            return max;
        }

        double getMean() {
            return (double) sum / (double) count;
        }

        @Override
        public String toString() {
            return String.format("%.1f/%.1f/%.1f",
                getMin() / 10.0,
                getMean() / 10.0,
                getMax() / 10.0
            );
        }
    }
}
