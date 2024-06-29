package be.twofold.playground.zero;

import java.util.*;

record LogEntry(LogEntryHeader header, byte[] content) {

    public static LogEntry from(byte[] entry) {
        LogEntryHeader header = LogEntryHeader.from(entry);
        byte[] content = Arrays.copyOfRange(entry, 11, entry.length);
        return new LogEntry(header, content);
    }

}
