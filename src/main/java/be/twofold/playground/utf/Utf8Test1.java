package be.twofold.playground.utf;

import java.nio.charset.*;
import java.util.*;

public class Utf8Test1 {

    private static final BitSet s1 = new BitSet();
    private static final Map<Integer, BitSet> s2 = new TreeMap<>();
    private static final Map<Integer, Map<Integer, BitSet>> s3 = new TreeMap<>();
    private static final Map<Integer, Map<Integer, Map<Integer, BitSet>>> s4 = new TreeMap<>();

    private static List<Range> ss1;
    private static final Map<Integer, List<Range>> ss2 = new TreeMap<>();
    private static final Map<Integer, Map<Integer, List<Range>>> ss3 = new TreeMap<>();
    private static final Map<Integer, Map<Integer, Map<Integer, List<Range>>>> ss4 = new TreeMap<>();

    public static void main(String[] args) {
        for (int cp = 0; cp < Character.MAX_CODE_POINT; cp++) {
            byte[] bytes = Character.toString(cp).getBytes(StandardCharsets.UTF_8);

            if (bytes.length == 4) {
                int i0 = Byte.toUnsignedInt(bytes[0]);
                int i1 = Byte.toUnsignedInt(bytes[1]);
                int i2 = Byte.toUnsignedInt(bytes[2]);
                int i3 = Byte.toUnsignedInt(bytes[3]);

                s4
                    .computeIfAbsent(i3, __ -> new TreeMap<>())
                    .computeIfAbsent(i2, __ -> new TreeMap<>())
                    .computeIfAbsent(i1, __ -> new BitSet())
                    .set(i0);
            }

            if (bytes.length == 3) {
                int i0 = Byte.toUnsignedInt(bytes[0]);
                int i1 = Byte.toUnsignedInt(bytes[1]);
                int i2 = Byte.toUnsignedInt(bytes[2]);

                s3
                    .computeIfAbsent(i2, __ -> new TreeMap<>())
                    .computeIfAbsent(i1, __ -> new BitSet())
                    .set(i0);
            }

            if (bytes.length == 2) {
                int i0 = Byte.toUnsignedInt(bytes[0]);
                int i1 = Byte.toUnsignedInt(bytes[1]);

                s2
                    .computeIfAbsent(i1, __ -> new BitSet())
                    .set(i0);
            }

            if (bytes.length == 1) {
                s1.set(Byte.toUnsignedInt(bytes[0]));
            }
        }

        ss1 = toRanges(s1);
        for (Map.Entry<Integer, BitSet> e : s2.entrySet()) {
            ss2.put(e.getKey(), toRanges(e.getValue()));
        }
        for (Map.Entry<Integer, Map<Integer, BitSet>> e1 : s3.entrySet()) {
            for (Map.Entry<Integer, BitSet> e2 : e1.getValue().entrySet()) {
                ss3
                    .computeIfAbsent(e1.getKey(), __ -> new TreeMap<>())
                    .put(e2.getKey(), toRanges(e2.getValue()));
            }
        }
        for (Map.Entry<Integer, Map<Integer, Map<Integer, BitSet>>> e1 : s4.entrySet()) {
            for (Map.Entry<Integer, Map<Integer, BitSet>> e2 : e1.getValue().entrySet()) {
                for (Map.Entry<Integer, BitSet> e3 : e2.getValue().entrySet()) {
                    ss4
                        .computeIfAbsent(e1.getKey(), __ -> new TreeMap<>())
                        .computeIfAbsent(e2.getKey(), __ -> new TreeMap<>())
                        .put(e3.getKey(), toRanges(e3.getValue()));
                }
            }
        }

        Map<Integer, Map<Map<List<Range>, List<Range>>, List<Range>>> t1 = new HashMap<>();
        for (Map.Entry<Integer, Map<Integer, Map<Integer, List<Range>>>> e1 : ss4.entrySet()) {
            Map<Integer, Map<List<Range>, List<Range>>> t2 = new HashMap<>();
            for (Map.Entry<Integer, Map<Integer, List<Range>>> e2 : e1.getValue().entrySet()) {
                t2.put(e2.getKey(), mergeMapValues(e2.getValue()));
            }
            t1.put(e1.getKey(), mergeMapValues(t2));
        }
        System.out.println(mergeMapValues(t1));

        Map<List<Range>, List<Range>> something = mergeMapValues(ss2);
        System.out.println(something);
    }

    private static <T> Map<T, List<Range>> mergeMapValues(Map<Integer, T> map) {
        Map<T, List<Integer>> grouping = new HashMap<>();
        for (Map.Entry<Integer, T> entry : map.entrySet()) {
            grouping
                .computeIfAbsent(entry.getValue(), __ -> new ArrayList<>())
                .add(entry.getKey());
        }

        Map<T, List<Range>> result = new HashMap<>();
        for (Map.Entry<T, List<Integer>> entry : grouping.entrySet()) {
            BitSet bitSet = new BitSet();
            entry.getValue().forEach(bitSet::set);
            result.put(entry.getKey(), toRanges(bitSet));
        }

        return result;
    }

    private static List<Range> toRanges(BitSet bitSet) {
        if (bitSet == null) {
            return List.of();
        }

        int e = 0;
        List<Range> ranges = new ArrayList<>();
        while (true) {
            int s = bitSet.nextSetBit(e);
            if (s == -1) {
                break;
            }
            e = bitSet.nextClearBit(s + 1);
            ranges.add(new Range(s, e - 1));
        }
        return List.copyOf(ranges);
    }

}
