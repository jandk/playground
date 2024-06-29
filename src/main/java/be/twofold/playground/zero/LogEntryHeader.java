package be.twofold.playground.zero;

import java.nio.*;
import java.time.*;

record LogEntryHeader(
    byte type,
    Instant timestamp,
    byte unknown1,
    byte unknown2,
    byte unknown3,
    byte unknown4,
    byte unknown5,
    byte unknown6
) {

    public static LogEntryHeader from(byte[] entry) {
        ByteBuffer buffer = ByteBuffer.wrap(entry, 0, 11)
            .order(ByteOrder.LITTLE_ENDIAN);

        byte type = buffer.get();
        Instant timestamp = Instant.ofEpochSecond(buffer.getInt());
        byte unknown1 = buffer.get();
        byte unknown2 = buffer.get();
        byte unknown3 = buffer.get();
        byte unknown4 = buffer.get();
        byte unknown5 = buffer.get();
        byte unknown6 = buffer.get();

        return new LogEntryHeader(type, timestamp, unknown1, unknown2, unknown3, unknown4, unknown5, unknown6);
    }

    @Override
    public String toString() {
        return "LogEntryHeader{" +
            "type=" + type + ", " +
            "timestamp=" + timestamp +
            "}";
    }

}
