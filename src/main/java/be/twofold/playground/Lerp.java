package be.twofold.playground;

public class Lerp {

    static double lerp1(double a, double b, double t) {
        return a + t * (b - a);
    }

    static double lerp2(double a, double b, double t) {
        return (1 - t) * a + t * b;
    }

    static double cubic(double p0, double p1, double p2, double t) {
        double a = lerp1(p0, p1, t);
        double b = lerp1(p1, p2, t);
        return lerp1(a, b, t);
    }

    static double cubic_inline(double p0, double p1, double p2, double t1) {
        double t2 = t1 * t1;

        double p0i = t2 - 2 * t1 + 1;
        double p1i = -2 * t2 + 2 * t1;
        double p2i = t2;
        return p0i * p0 + p1i * p1 + p2i * p2;
    }

    static double bezier(double p0, double p1, double p2, double p3, double t) {
        double a = lerp1(p0, p1, t);
        double b = lerp1(p1, p2, t);
        double c = lerp1(p2, p3, t);
        double d = lerp1(a, b, t);
        double e = lerp1(b, c, t);
        return lerp1(d, e, t);
    }

    static double bezier_inline(double p0, double p1, double p2, double p3, double t) {
        double u = 1 - t;

        double t2 = t * t;
        double u2 = u * u;

        double t3 = t * t * t;
        double u3 = u * u * u;

        double p1i = -3 * u3 + 3 * u2;
        double p2i = -3 * t3 + 3 * t2;

        return p0 * u3 + p1 * p1i + p2 * p2i + p3 * t3;
    }

    public static void main(String[] args) {

    }

}
