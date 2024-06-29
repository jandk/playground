package be.twofold.playground;

import java.math.*;

public final class Fnv1 {
    private static final int INIT_32 = 0x811c9dc5;
    private static final int PRIME_32 = 0x01000193;
    private static final long INIT_64 = 0xcbf29ce484222325L;
    private static final long PRIME_64 = 0x00000100000001B3L;
    private static final BigInteger INIT_128 = new BigInteger("6c62272e07bb014262b821756295c58d", 16);
    private static final BigInteger PRIME_128 = new BigInteger("0000000001000000000000000000013B", 16);
    private static final BigInteger INIT_256 = new BigInteger("dd268dbcaac550362d98c384c4e576ccc8b1536847b6bbb31023b4c8caee0535", 16);
    private static final BigInteger PRIME_256 = new BigInteger("0000000000000000000001000000000000000000000000000000000000000163", 16);
    private static final BigInteger INIT_512 = new BigInteger("b86db0b1171f4416dca1e50f309990acac87d059c90000000000000000000d21e948f68a34c192f62ea79bc942dbe7ce182036415f56e34bac982aac4afe9fd9", 16);
    private static final BigInteger PRIME_512 = new BigInteger("00000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000000000157", 16);
    private static final BigInteger INIT_1024 = new BigInteger("0000000000000000005f7a76758ecc4d32e56d5a591028b74b29fc4223fdada16c3bf34eda3674da9a21d9000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000004c6d7eb6e73802734510a555f256cc005ae556bde8cc9c6a93b21aff4b16c71ee90b3", 16);
    private static final BigInteger PRIME_1024 = new BigInteger("000000000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000018D", 16);

    private Fnv1() {
        throw new UnsupportedOperationException();
    }

    public static int fnv1_32(byte[] bytes) {
        int hash = INIT_32;
        for (byte b : bytes) {
            hash *= PRIME_32;
            hash ^= Byte.toUnsignedInt(b);
        }
        return hash;
    }

    public static int fnv1a_32(byte[] bytes) {
        int hash = INIT_32;
        for (byte b : bytes) {
            hash ^= Byte.toUnsignedInt(b);
            hash *= PRIME_32;
        }
        return hash;
    }

    public static long fnv1_64(byte[] bytes) {
        long hash = INIT_64;
        for (byte b : bytes) {
            hash *= PRIME_64;
            hash ^= Byte.toUnsignedInt(b);
        }
        return hash;
    }

    public static long fnv1a_64(byte[] bytes) {
        long hash = INIT_64;
        for (byte b : bytes) {
            hash ^= Byte.toUnsignedInt(b);
            hash *= PRIME_64;
        }
        return hash;
    }

    public static BigInteger fnv1_128(byte[] bytes) {
        BigInteger mask = BigInteger.ONE.shiftLeft(128).subtract(BigInteger.ONE);
        BigInteger hash = INIT_128;
        for (byte b : bytes) {
            hash = hash.multiply(PRIME_128);
            hash = hash.xor(BigInteger.valueOf(Byte.toUnsignedInt(b)));
            hash = hash.and(mask);
        }
        return hash;
    }

    public static BigInteger fnv1a_128(byte[] bytes) {
        BigInteger mask = BigInteger.ONE.shiftLeft(128).subtract(BigInteger.ONE);
        BigInteger hash = INIT_128;
        for (byte b : bytes) {
            hash = hash.xor(BigInteger.valueOf(Byte.toUnsignedInt(b)));
            hash = hash.multiply(PRIME_128);
            hash = hash.and(mask);
        }
        return hash;
    }

    public static BigInteger fnv1_256(byte[] bytes) {
        BigInteger mask = BigInteger.ONE.shiftLeft(256).subtract(BigInteger.ONE);
        BigInteger hash = INIT_256;
        for (byte b : bytes) {
            hash = hash.multiply(PRIME_256);
            hash = hash.xor(BigInteger.valueOf(Byte.toUnsignedInt(b)));
            hash = hash.and(mask);
        }
        return hash;
    }

    public static BigInteger fnv1a_256(byte[] bytes) {
        BigInteger mask = BigInteger.ONE.shiftLeft(256).subtract(BigInteger.ONE);
        BigInteger hash = INIT_256;
        for (byte b : bytes) {
            hash = hash.xor(BigInteger.valueOf(Byte.toUnsignedInt(b)));
            hash = hash.multiply(PRIME_256);
            hash = hash.and(mask);
        }
        return hash;
    }

    public static BigInteger fnv1_512(byte[] bytes) {
        BigInteger mask = BigInteger.ONE.shiftLeft(512).subtract(BigInteger.ONE);
        BigInteger hash = INIT_512;
        for (byte b : bytes) {
            hash = hash.multiply(PRIME_512);
            hash = hash.xor(BigInteger.valueOf(Byte.toUnsignedInt(b)));
            hash = hash.and(mask);
        }
        return hash;
    }

    public static BigInteger fnv1a_512(byte[] bytes) {
        BigInteger mask = BigInteger.ONE.shiftLeft(512).subtract(BigInteger.ONE);
        BigInteger hash = INIT_512;
        for (byte b : bytes) {
            hash = hash.xor(BigInteger.valueOf(Byte.toUnsignedInt(b)));
            hash = hash.multiply(PRIME_512);
            hash = hash.and(mask);
        }
        return hash;
    }

    public static BigInteger fnv1_1024(byte[] bytes) {
        BigInteger mask = BigInteger.ONE.shiftLeft(1024).subtract(BigInteger.ONE);
        BigInteger hash = INIT_1024;
        for (byte b : bytes) {
            hash = hash.multiply(PRIME_1024);
            hash = hash.xor(BigInteger.valueOf(Byte.toUnsignedInt(b)));
            hash = hash.and(mask);
        }
        return hash;
    }

    public static BigInteger fnv1a_1024(byte[] bytes) {
        BigInteger mask = BigInteger.ONE.shiftLeft(1024).subtract(BigInteger.ONE);
        BigInteger hash = INIT_1024;
        for (byte b : bytes) {
            hash = hash.xor(BigInteger.valueOf(Byte.toUnsignedInt(b)));
            hash = hash.multiply(PRIME_1024);
            hash = hash.and(mask);
        }
        return hash;
    }
}
