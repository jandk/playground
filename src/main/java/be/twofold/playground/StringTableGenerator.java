package be.twofold.playground;

import java.util.*;
import java.util.regex.*;

public final class StringTableGenerator {

    public static void main(String[] args) {
        Pattern pattern = Pattern.compile("^[\\u0009-\\u000D\\u0020\\u0085\\u2028\\u2029\\u3000\\u1680\\u2000-\\u2006\\u2008-\\u200A\\u205F\\u00A0\\u2007\\u202F]$");
//        Pattern pattern = Pattern.compile("^[ \\t\\r\\n]$");

        char[] characters = matchingCharacters(pattern);
        int tableSize = nextPowerOfTwo(characters.length * 5 / 4);
        int shift = Integer.numberOfLeadingZeros(tableSize) + 1;

        // Reasoning about multiplier here.
        // It needs to start somewhere around 1 << shift, because otherwise we won't get any
        // We start at 1 << shift, because if not
        int[] buckets = new int[tableSize];
        for (int multiplier = 1 << shift; multiplier < Integer.MAX_VALUE; multiplier++) {
            if (checkKey(characters, buckets, multiplier, shift)) {
                dump(buckets, multiplier, shift);
                return;
            }
        }

//        Matcher matcher = new Matcher();
//        for (char ch = Character.MIN_VALUE; ch < Character.MAX_VALUE; ch++) {
//            if (ArrayUtils.contains(characters, ch)) {
//                if (!matcher.matches(ch)) {
//                    System.out.println("Expected " + escape(ch) + " to match, but DIDN'T");
//                }
//            } else {
//                if (matcher.matches(ch)) {
//                    System.out.println("Expected " + escape(ch) + " to NOT match, but DID");
//                }
//            }
//        }
    }

    private static char[] matchingCharacters(Pattern pattern) {
        List<Character> list = new ArrayList<>();
        for (char ch = Character.MIN_VALUE; ch < Character.MAX_VALUE; ch++) {
            String s = Character.toString(ch);
            if (pattern.matcher(s).matches()) {
                list.add(ch);
            }
        }
        char[] result = new char[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    private static boolean checkKey(char[] characters, int[] buckets, int multiplier, int shift) {
        Arrays.fill(buckets, 0);
        for (char c : characters) {
            int bucket = (multiplier * c) >>> shift;
            if (buckets[bucket] != 0) {
                return false;
            }
            buckets[bucket] = c;
        }
        return true;
    }

    private static void dump(int[] buckets, int multiplier, int shift) {
        StringBuilder table = new StringBuilder()
            .append("        \"");

        // 16 = 12 spaces + 2 quotes + space and plus
        for (int i = 0, lineLength = 16; i < buckets.length; i++) {
            String s = StringTableGenerator.escape(buckets[i]);
            lineLength += s.length();
            if (lineLength > 80) {
                lineLength = 16;
                table.append("\" +\n        \"");
            }
            table.append(s);
        }
        table.append("\";");

        System.out.println("final class Matcher {");
        System.out.println("    private static final String Table = \"\" +");
        System.out.println(table);
        System.out.println();
        System.out.println("    public boolean matches(char c) {");
        System.out.println("        return Table.charAt((c * " + multiplier + ") >>> " + shift + ") == c;");
        System.out.println("    }");
        System.out.println("}");
    }

    private static int nextPowerOfTwo(int n) {
        int highestOneBit = Integer.highestOneBit(n);
        if (highestOneBit == n) {
            return highestOneBit;
        }
        return highestOneBit << 1;
    }

    private static String escape(int ch) {
        switch (ch) {
            case 0:
                return "\\0";
            case '\n':
                return "\\n";
            case '\r':
                return "\\r";
            default:
                return String.format("\\u%04x", ch);
        }
    }

    static final class Matcher {
        private static final String Table = "" +
            "\u200a\u2028\u2029\u0020\0\0\0\0\u1680\u202f\u0009\n\u000b\u000c" +
            "\r\u3000\0\0\0\u00a0\u2000\u0085\u2001\u2002\u2003\u2004\u2005" +
            "\u2006\u2007\u2008\u2009\u205f";

        public boolean matches(char c) {
            return Table.charAt((c * 150812695) >>> 27) == c;
        }
    }

}
