package be.twofold.playground.fx;

import javax.sound.sampled.*;
import java.nio.*;

public class AudioTest {

    private static final int SAMPLE_RATE = 48000;

    public static void main(String[] args) throws LineUnavailableException {
        final AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, true);
        final byte[] buffer = new byte[SAMPLE_RATE * 2];
        ShortBuffer shortBuffer = ByteBuffer.wrap(buffer).asShortBuffer();
        final SourceDataLine line = AudioSystem.getSourceDataLine(format);
        line.open(format);
        line.start();
        System.out.println(line.available());

        double amplitude = 0.1;
        double frequency = 440;
        double period = frequency / SAMPLE_RATE;
        new Thread(() -> {
            while (true) {
                for (int i = 0; i < SAMPLE_RATE; i++) {
                    double value = Math.sin((2 * Math.PI) * i * period);
                    shortBuffer.put(i, (short) (value * amplitude * Short.MAX_VALUE));
                }
                System.out.println(line.available());
                long t0 = System.nanoTime();
                line.write(buffer, 0, SAMPLE_RATE * 2);
                long t1 = System.nanoTime();
                System.out.println((t1 - t0) / 1e9);
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
