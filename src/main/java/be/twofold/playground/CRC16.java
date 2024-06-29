package be.twofold.playground;

import java.util.*;

public final class CRC16 {
    private final CRCParameters parameters;
    private final short[] table;

    public CRC16(CRCParameters parameters) {
        this.parameters = Objects.requireNonNull(parameters, "parameters");
        if (parameters.width() != 16) {
            throw new IllegalArgumentException("CRC width must be 32");
        }
        this.table = generate();
        for (int i = 0; i < 10; i++) {
            System.out.printf("%04x ", table[i]);
        }
    }

    private short[] generate() {
        int polynomial = (short) parameters.polynomial();

        short[] table = new short[256];
        int crc = 0x8000;
        for (int i = 1; i < 256; i <<= 1) {
            if ((crc & 0x8000) != 0) {
                crc = (crc << 1) ^ polynomial;
            } else {
                crc <<= 1;
            }
            for (int j = 0; j < i; j++) {
                table[i + j] = (short) (crc ^ table[j]);
            }
        }
        return table;
    }

    private static short reflect16(short s) {
        int i = Short.toUnsignedInt(s);
        i = (i & 0x5555) << 1 | (i >>> 1) & 0x5555;
        i = (i & 0x3333) << 2 | (i >>> 2) & 0x3333;
        i = (i & 0x0f0f) << 4 | (i >>> 4) & 0x0f0f;
        return Short.reverseBytes((short) i);
    }

    public static void main(String[] args) {
        CRC16 crc16 = new CRC16(CRCParameters.ModBus);
    }
}
