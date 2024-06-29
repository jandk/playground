package be.twofold.playground;

public class ThreePhase {
    public static void main(String[] args) {
        test();
        if (true) return;
        double voltage = 230;
        double peakVoltage = Math.sqrt(2) * voltage;

        System.out.println("ms\tvAB\tvBC\tvCA\tiA\tiB\tiC");
        for (int i = 0; i <= 100; i++) {
            double x = 2 * Math.PI * i / 100;
            double frequency = 50.0;
            double ms = (i / 100.0) * (1000 / frequency);
            double vAB = peakVoltage * Math.sin(x);
            double vBC = peakVoltage * Math.sin(x + (2 * Math.PI / 3));
            double vCA = peakVoltage * Math.sin(x - (2 * Math.PI / 3));

            double rAB = 10;
            double rBC = 20;
            double rCA = 30;

            double iAB = vAB / rAB;
            double iBC = vBC / rBC;
            double iCA = vCA / rCA;

            double iA = iAB + iCA;
            double iB = iAB + iBC;
            double iC = iBC + iCA;

            System.out.println(ms + "\t" + vAB + "\t" + vBC + "\t" + vCA + "\t" + iA + "\t" + iB + "\t" + iC);
        }
    }

    private static void test() {
        Complex vAB = Complex.ofPolar(100, 0);
        Complex vBC = Complex.ofPolar(100, -(2 * Math.PI / 3));
        Complex vCA = Complex.ofPolar(100, +(2 * Math.PI / 3));
        Complex zAB = Complex.ofCartesian(10, 0);
        Complex zBC = Complex.ofCartesian(10, 0);
        Complex zCA = Complex.ofCartesian(10, 0);
//        Complex zCA = Complex.ofCartesian(Double.POSITIVE_INFINITY, 0);
//        Complex zAB = Complex.ofCartesian(10, -5);
//        Complex zBC = Complex.ofCartesian(16, 0);
//        Complex zCA = Complex.ofCartesian(8, 6);

        Complex iAB = vAB.div(zAB);
        Complex iBC = vBC.div(zBC);
        Complex iCA = vCA.div(zCA);

        Complex iA = iAB.sub(iCA);
        Complex iB = iBC.sub(iAB);
        Complex iC = iCA.sub(iBC);

        System.out.println(asPolar(iA));
        System.out.println(asPolar(iB));
        System.out.println(asPolar(iC));
    }

    private static String asPolar(Complex c) {
        return c.abs() + "∠" + Math.toDegrees(c.arg());
    }

    private record Complex(double re, double im) {
        public static Complex ofPolar(double mag, double ang) {
            double angRad = Math.toRadians(ang);
            return new Complex(mag * Math.cos(angRad), mag * Math.sin(angRad));
        }

        public static Complex ofCartesian(double re, double im) {
            return new Complex(re, im);
        }

        public double abs() {
            return Math.sqrt(re * re + im * im);
        }

        public double arg() {
            return Math.atan2(im, re);
        }

        public Polar toPolar() {
            double mag = Math.sqrt(re * re + im * im);
            double ang = Math.toDegrees(Math.atan2(im, re));
            return new Polar(mag, ang);
        }

        public Complex add(Complex other) {
            return new Complex(re + other.re, im + other.im);
        }

        public Complex sub(Complex other) {
            return new Complex(re - other.re, im - other.im);
        }

        public Complex mul(Complex other) {
            return new Complex(re * other.re - im * other.im, re * other.im + im * other.re);
        }

        public Complex div(Complex other) {
            double denominator = other.re * other.re + other.im * other.im;
            return new Complex((re * other.re + im * other.im) / denominator, (im * other.re - re * other.im) / denominator);
        }
    }

    public record Polar(double mag, double ang) {
        public Complex toComplex() {
            double ang = Math.toRadians(this.ang);
            return new Complex(mag * Math.cos(ang), mag * Math.sin(ang));
        }
    }
}
