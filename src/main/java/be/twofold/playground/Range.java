package be.twofold.playground;

import java.util.*;

public record Range(int lower, int upper) implements Comparable<Range> {
    public Range {
        if (lower > upper) {
            throw new IllegalArgumentException("lower > upper");
        }
    }

    @Override
    public String toString() {
        return "[" + lower + " -> " + upper + "]";
    }

    public static void main(String[] args) {
        List<Range> ranges = List.of(
            new Range(1, 9),
            new Range(2, 5),
            new Range(19, 20),
            new Range(10, 11),
            new Range(12, 20),
            new Range(0, 3),
            new Range(0, 1),
            new Range(0, 2)
        );
        System.out.println(merge(ranges));
    }

    public static List<Range> merge(List<Range> ranges) {
        if (ranges == null || ranges.isEmpty()) {
            return List.of();
        }

        List<Range> sortedRanges = ranges.stream().sorted().toList();

        int min = ranges.get(0).lower;
        int max = ranges.get(0).upper;
        List<Range> result = new ArrayList<>();
        for (int i = 1; i < sortedRanges.size(); i++) {
            Range range = sortedRanges.get(i);
            if (range.lower > max + 1) {
                result.add(new Range(min, max));
                min = range.lower;
                max = range.upper;
            } else if (range.upper > max) {
                max = range.upper;
            }
        }
        result.add(new Range(min, max));
        return List.copyOf(result);
    }

    @Override
    public int compareTo(Range that) {
        int compare = Integer.compare(this.lower, that.lower);
        if (compare != 0) {
            return compare;
        }
        return Integer.compare(this.upper, that.upper);
    }
}
