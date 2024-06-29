package be.twofold.playground;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import java.nio.*;
import java.util.*;
import java.util.concurrent.*;

@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class ByteToIntArrayBench {

    private byte[] array;

    public static void main(String[] args) throws RunnerException {
        ByteToIntArrayBench bench = new ByteToIntArrayBench();
        bench.setup();
        bench.byteBuffer();

        Options opt = new OptionsBuilder()
            .include(ByteToIntArrayBench.class.getSimpleName())
            .build();

        new Runner(opt).run();
    }

    @Setup
    public void setup() {
        array = new byte[1024 * 1024];
        new Random(0).nextBytes(array);
    }

    @Benchmark
    public int[] byteBuffer() {
        int[] result = new int[array.length / 4];
        ByteBuffer.wrap(array).asIntBuffer().get(result);
        return result;
    }

    @Benchmark
    public int[] byteBufferLoop() {
        int[] result = new int[array.length / 4];
        ByteBuffer buffer = ByteBuffer.wrap(array);
        for (int i = 0; i < result.length; i++) {
            result[i] = buffer.getInt();
        }
        return result;
    }

    @Benchmark
    public int[] manually() {
        int[] result = new int[array.length / 4];
        for (int i = 0; i < result.length; i++) {
            result[i] = toInt(array[i * 4], array[i * 4 + 1], array[i * 4 + 2], array[i * 4 + 3]);
        }
        return result;
    }

    private static int toInt(byte b1, byte b2, byte b3, byte b4) {
        return (b1 & 0xFF) << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | (b4 & 0xFF);
    }

}
