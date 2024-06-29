package be.twofold.playground.fx;

import javafx.beans.property.*;

class FrameStats {
    private static final long nsPerFrame = 1_000_000_000 / 60;
    private long frameTime;
    private long expectedFrameTime;
    private long frameCount;
    private final int frameDiff = 0;
    private double meanFrameInterval; // millis
    private final ReadOnlyStringWrapper text = new ReadOnlyStringWrapper(this, "text", "Frame count: 0 Average frame interval: N/A");

    public long getFrameCount() {
        return frameCount;
    }

    public double getMeanFrameInterval() {
        return meanFrameInterval;
    }

    public void addFrame(long frameDurationNanos) {
        meanFrameInterval = (meanFrameInterval * frameCount + frameDurationNanos / 1_000_000.0) / (frameCount + 1);
        frameCount++;
        frameTime += frameDurationNanos;
        expectedFrameTime += nsPerFrame;

        // System.out.println((expectedFrameTime - frameTime)/1_000_000);

        text.set(toString());
    }

    public String getText() {
        return text.get();
    }

    public ReadOnlyStringProperty textProperty() {
        return text.getReadOnlyProperty();
    }

    @Override
    public String toString() {
        return String.format("Frame count: %,d Average frame interval: %.3f milliseconds", getFrameCount(), getMeanFrameInterval());
    }
}
