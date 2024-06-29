package be.twofold.playground.zero;

public enum EntryType {
    DebugString((byte) 0xfd);

    private static final EntryType[] Lookup = buildLookup();

    private final byte code;

    EntryType(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static EntryType fromCode(byte code) {
        return Lookup[Byte.toUnsignedInt(code)];
    }

    private static EntryType[] buildLookup() {
        EntryType[] lookup = new EntryType[256];
        for (EntryType type : values()) {
            lookup[Byte.toUnsignedInt(type.code)] = type;
        }
        return lookup;
    }
}
