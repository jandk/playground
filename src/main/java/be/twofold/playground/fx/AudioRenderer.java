package be.twofold.playground.fx;

import javax.sound.sampled.*;
import java.nio.*;

final class AudioRenderer {

    private static final int SAMPLE_RATE = 48000;
    private final byte[] rawAudioBuffer = new byte[SAMPLE_RATE * 2];
    private final ShortBuffer audioBuffer = ByteBuffer.wrap(rawAudioBuffer).asShortBuffer();
    private final SourceDataLine line;
    private long lastTimeStamp = 0;

    public AudioRenderer() {
        final AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, true);
        try {
            line = AudioSystem.getSourceDataLine(format);
            line.open(format);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    void startAudio() {
        line.start();
    }

    void updateAudio(long timestamp) {
        // System.out.println(timestamp);
        if (lastTimeStamp == 0) {
            int bufferSize = (int) (SAMPLE_RATE * 0.05) * 2;
            line.write(new byte[bufferSize], 0, bufferSize);
            lastTimeStamp = timestamp;
            return;
        }
        double nanoPerSample = 1e9 / SAMPLE_RATE;
        double amplitude = 0.5;
        double frequency = 440;
        long elapsedTime = timestamp - lastTimeStamp;
        int samplesToRender = (int) Math.round((elapsedTime / 1e9) * SAMPLE_RATE);
        // System.out.println("line.available() = " + line.available());
        for (int i = 0; i < samplesToRender; i++) {
            double pos = ((lastTimeStamp + (i * nanoPerSample)) % 1e9) / 1e9;
            double value = Math.sin((2 * Math.PI) * pos * frequency);
            audioBuffer.put(i, (short) (value * amplitude * Short.MAX_VALUE));
        }
        line.write(rawAudioBuffer, 0, samplesToRender * 2);
        lastTimeStamp = timestamp;
    }


}
