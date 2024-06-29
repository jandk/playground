package be.twofold.playground;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;
import java.util.function.*;

public record Complex(double re, double im) {
    public static Complex fromPolar(double r, double theta) {
        return new Complex(r * Math.cos(theta), r * Math.sin(theta));
    }

    public static void main(String[] args) {
        var a1 = Math.toRadians(0);
        var a2 = Math.toRadians(120);
        var a3 = Math.toRadians(240);
        var v = 230 / Math.sqrt(3);

        var v1 = calculateAndPrint("V1", () -> fromPolar(v, a1));
        var v2 = calculateAndPrint("V2", () -> fromPolar(v, a2));
        var v3 = calculateAndPrint("V3", () -> fromPolar(v, a3));

        var v12 = calculateAndPrint("V12", () -> v1.sub(v2));
        var v23 = calculateAndPrint("V23", () -> v2.sub(v3));
        var v31 = calculateAndPrint("V31", () -> v3.sub(v1));

        var z12 = calculateAndPrint("Z1", () -> new Complex(100, 0));
        var z23 = calculateAndPrint("Z2", () -> new Complex(0, 0));
        var z31 = calculateAndPrint("Z3", () -> new Complex(0, 0));

        var i12 = calculateAndPrint("I12", () -> v12.div(z12));
        var i23 = calculateAndPrint("I23", () -> v23.div(z23));
        var i31 = calculateAndPrint("I31", () -> v31.div(z31));

        var i1 = calculateAndPrint("I1", () -> i12.sub(i31));
        var i2 = calculateAndPrint("I2", () -> i23.sub(i12));
        var i3 = calculateAndPrint("I3", () -> i31.sub(i23));

        var p1 = calculateAndPrint("P1", () -> v12.mul(i1));
        var p2 = calculateAndPrint("P2", () -> v23.mul(i2));

        var p = calculateAndPrint("P", () -> p1.add(p2));

        drawComplex(List.of(
            new ComplexGroup("Voltages", Color.BLUE, 1, List.of(
                new ComplexValue("V1", v1),
                new ComplexValue("V2", v2),
                new ComplexValue("V3", v3)
            )),
            new ComplexGroup("Currents", Color.RED, 50, List.of(
                new ComplexValue("I1", i1),
                new ComplexValue("I2", i2),
                new ComplexValue("I3", i3)
            )),
            new ComplexGroup("Impedances", Color.GREEN, 5, List.of(
                new ComplexValue("Z1", z12),
                new ComplexValue("Z2", z23),
                new ComplexValue("Z3", z31)
            )),
            new ComplexGroup("Powers", Color.ORANGE, 1, List.of(
                new ComplexValue("P1", p1),
                new ComplexValue("P2", p2),
                new ComplexValue("P", p)
            ))
        ));
    }

    private static Complex calculateAndPrint(String name, Supplier<Complex> supplier) {
        var c = supplier.get();
        System.out.println(name + ": " + c);
        return c;
    }

    private static void drawComplex(List<ComplexGroup> complexGroups) {
        // Draw complex numbers on a plot in swing
        var frame = new JFrame("Complex Numbers Graph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.getContentPane().add(new ComplexGraph(complexGroups));
        frame.setVisible(true);
    }

    public Complex add(Complex c) {
        return new Complex(
            re + c.re,
            im + c.im
        );
    }

    public Complex sub(Complex c) {
        return new Complex(
            re - c.re,
            im - c.im
        );
    }

    public Complex mul(Complex c) {
        return new Complex(
            re * c.re - im * c.im,
            re * c.im + im * c.re
        );
    }

    public Complex div(Complex c) {
        var d = c.re * c.re + c.im * c.im;
        if (d < 1e-3) {
            return new Complex(0, 0);
        }
        return new Complex(
            (re * c.re + im * c.im) / d,
            (im * c.re - re * c.im) / d
        );
    }

    public Complex neg() {
        return new Complex(-re, -im);
    }

    public double abs() {
        return Math.sqrt(re * re + im * im);
    }

    public double arg() {
        return Math.atan2(im, re);
    }

    public Complex conj() {
        return new Complex(re, -im);
    }

    @Override
    public String toString() {
        return String.format("%.2f ∠ %.2f° -- %.2f + %.2fi", abs(), Math.toDegrees(arg()), re, im);
    }

    private static final class ComplexGraph extends JPanel {
        private List<ComplexGroup> complexGroups;

        public ComplexGraph(List<ComplexGroup> complexGroups) {
            this.complexGroups = complexGroups;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            var centerX = getWidth() / 2;
            var centerY = getHeight() / 2;

            g.setColor(Color.BLACK);
            g.drawLine(centerX, 0, centerX, getHeight());
            g.drawLine(0, centerY, getWidth(), centerY);

            for (var group : complexGroups) {
                for (var value : group.values()) {
                    Complex complex = value.value();

                    var x = (int) (centerX + complex.re() * group.scale());
                    var y = (int) (centerY + complex.im() * group.scale());

                    g.setColor(group.color());
                    // g.fillOval(x - 3, y - 3, 6, 6);

                    // Draw arrows
                    drawArrow(g, centerX, centerY, complex, group.scale());

                    // Draw labels
                    g.setColor(Color.BLACK);
                    g.drawString(value.name(), x + 5, y - 5);
                }
            }

            // Draw group labels
            for (int i = 0; i < complexGroups.size(); i++) {
                ComplexGroup group = complexGroups.get(i);
                g.setColor(group.color());
                g.drawString(group.name(), 10, 20 + i * 15);
            }
        }

        private void drawArrow(Graphics g, double x1, double y1, Complex complex, double scale) {
            Graphics2D g2d = (Graphics2D) g;

            double angle = complex.arg();
            int len = (int) (complex.abs() * scale);

            AffineTransform oldTransform = g2d.getTransform();
            AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
            at.concatenate(AffineTransform.getRotateInstance(angle));
            g2d.transform(at);

            g2d.drawLine(0, 0, len, 0);
            g2d.fillPolygon(new int[]{len, len - 6, len - 6, len}, new int[]{0, -6, 6, 0}, 4);
            g2d.setTransform(oldTransform);
        }
    }

    private record ComplexGroup(
        String name,
        Color color,
        double scale,
        List<ComplexValue> values
    ) {
    }

    private record ComplexValue(
        String name,
        Complex value
    ) {
    }
}
