package be.twofold.playground;

public record CRCParameters(
    long polynomial,
    long initial,
    long finalXor,
    boolean reflectInput,
    boolean reflectOutput
) {
    public static final CRCParameters ModBus = new CRCParameters(0x8005, 0xFFFF, 0x0000, true, true);

    public int width() {
        if (polynomial > 0xFFFFFFFFL) {
            return 64;
        }
        if (polynomial > 0xFFFF) {
            return 32;
        }
        if (polynomial > 0xFF) {
            return 16;
        }
        return 8;
    }
}
