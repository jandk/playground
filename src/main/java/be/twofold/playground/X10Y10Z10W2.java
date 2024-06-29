package be.twofold.playground;

import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;

public final class X10Y10Z10W2 {
    public static void main(String[] args) throws IOException {
        Path path = Path.of("D:\\Jan\\Desktop\\msh_fc_lx_665c2b13_001.core_69300h_175F8h.stream");
        byte[] bytes = Files.readAllBytes(path);

        int[] ints = new int[bytes.length / 4];
        ByteBuffer.wrap(bytes)
            .order(ByteOrder.LITTLE_ENDIAN)
            .asIntBuffer()
            .get(ints);

        System.out.println(fromSNorm10(512));
        System.out.println("Total: " + ints.length);

        // X10Y10Z10W2
        double sad = 0;
        int[] counts = new int[4];

        DoubleSummaryStatistics xStats = new DoubleSummaryStatistics();
        DoubleSummaryStatistics yStats = new DoubleSummaryStatistics();
        DoubleSummaryStatistics zStats = new DoubleSummaryStatistics();
        for (int i = 0; i < ints.length; i += 2) {
            int value = ints[i];
            int x = (value >> 0) & 0x3ff;
            int y = (value >> 10) & 0x3ff;
            int z = (value >> 20) & 0x3ff;
            int w = (value >> 30) & 0x3;

            counts[w]++;

            float fx = fromSNorm10(x);
            float fy = fromSNorm10(y);
            float fz = fromSNorm10(z);

            xStats.accept(fx);
            yStats.accept(fy);
            zStats.accept(fz);

            float length = (float) Math.sqrt(fx * fx + fy * fy + fz * fz);

            sad += Math.abs(length - 1);
        }
        System.out.printf("X10Y10Z10W2: %.2f%n", sad);
        System.out.println("Counts: " + counts[0] + ", " + counts[1] + ", " + counts[2] + ", " + counts[3]);
        System.out.println("X: " + xStats);
        System.out.println("Y: " + yStats);
        System.out.println("Z: " + zStats);
    }

    private static float fromSNorm10(int i) {
        // Convert i to signed
        int s = (i ^ 0x200) - 0x200;
        return s / 510f;
    }

//    private static float fromSNorm10(int i) {
//        // Convert i to signed
//        int s = i << 22 >> 22;
//        return s / 511f;
//    }

    private static float fromUNorm10(int i) {
        float f = i / 1023f;
        return Math.fma(f, 2f, -1f);
    }
}
