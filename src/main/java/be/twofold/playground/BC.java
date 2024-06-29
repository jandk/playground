package be.twofold.playground;

import java.util.function.*;

public class BC {
    private static final int BITS = 6;
    private static final int IMAX = (1 << BITS) - 1;
    private static final float FMAX = IMAX;
    private static final int SHIFT_LEFT = 8 - BITS;
    private static final int SHIFT_RIGHT = BITS - SHIFT_LEFT;

    public static void main(String[] args) {
        System.out.printf("expand1: %.3f%n", compareExpand(BC::expand1));
        System.out.printf("expand2: %.3f%n", compareExpand(BC::expand2));
        System.out.printf("expand3: %.3f%n", compareExpand(BC::expand3));
        System.out.printf("expand4: %.3f%n", compareExpand(BC::expand4));

        System.out.printf("interp1: %.3f%n", compareInterp3(BC::interp1));
        System.out.printf("interp2: %.3f%n", compareInterp3(BC::interp2));
        System.out.printf("interp3: %.3f%n", compareInterp3(BC::interp3));
        System.out.printf("interp4: %.3f%n", compareInterp3(BC::interp4));
        System.out.printf("interp5: %.3f%n", compareInterp3(BC::interp5));
    }

    static double compareExpand(IntUnaryOperator operator) {
        double sad = 0;
        for (int i = 0; i <= IMAX; i++) {
            int r = operator.applyAsInt(i);
            sad += Math.abs((i / FMAX) - (r / 255.0));
        }
        return sad;
    }

    static double compareInterp3(Interp interp) {
        double sad = 0;
        for (int a = 0; a <= IMAX; a++) {
            for (int b = 0; b <= IMAX; b++) {
                for (int i = 1; i < 3; i++) {
                    int result = interp.apply(a, b, i);
                    float reference = lerp(a / FMAX, b / FMAX, i / 3.0f);
//                    System.out.printf("%d\t%d\t%d\t%d\t%.2f%n", a, b, i, result, reference);
                    sad += Math.abs(reference - (result / 255.0));
                }
            }
        }
        return sad;
    }

    interface Interp {
        int apply(int a, int b, int step);
    }

    private static int interp1(int a, int b, int step) {
        return packUNorm8(lerp(a / FMAX, b / FMAX, step / 3.0f));
    }

    private static int interp2(int a, int b, int step) {
        a = expand4(a);
        b = expand4(b);
        return ((3 - step) * a + step * b + 1) / 3;
    }

    private static int interp3(int a, int b, int step) {
        a = expand4(a);
        b = expand4(b);
        step = Math.round((step * 256) / 3f);
        return (a * (255 - step) + b * step + 128) >> 8;
    }

    private static int interp4(int a, int b, int step) {
        a = expand4(a);
        b = expand4(b);
        step = Math.round((step * 65536) / 3f);
        return (a * (65536 - step) + b * step) >> 16;
    }

    private static int interp5(int a, int b, int step) {
        a = a * 0x41;
        b = b * 0x41;
        step = Math.round((step * 65536) / 3f);
        return (a * (65536 - step) + b * step) >> 20;
    }

    private static int expand1(int i) {
        return i * 255 / IMAX;
    }

    private static int expand2(int i) {
        return i << SHIFT_LEFT | i >> SHIFT_RIGHT;
    }

    private static int expand3(int i) {
        return packUNorm8(i / FMAX);
    }

    private static int expand4(int i) {
        return (i * 259 + 33) >> 6;
    }

    public static float lerp(float a, float b, float t) {
        return Math.fma(t, b - a, a);
    }

    public static int packUNorm8(float value) {
        return Math.round(clamp01(value) * 255.0f);
    }

    public static float clamp01(float value) {
        return Math.clamp(value, 0.0f, 1.0f);
    }
}
