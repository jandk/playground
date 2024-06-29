package be.twofold.playground.pattern;

import be.twofold.playground.pattern.Pattern.*;

import java.util.*;

/**
 * A utility class to print out the pattern node tree.
 */

class PrintPattern {

    private static final HashMap<Pattern.Node, Integer> ids = new HashMap<>();

    private static void print(Pattern.Node node, String text, int depth) {
        if (!ids.containsKey(node))
            ids.put(node, ids.size());
        System.out.printf("%6d:%" + (depth == 0 ? "" : depth << 1) + "s<%s>",
            ids.get(node), "", text);
        if (ids.containsKey(node.next))
            System.out.printf(" (=>%d)", ids.get(node.next));
        System.out.printf("%n");
    }

    private static void print(String s, int depth) {
        System.out.printf("       %" + (depth == 0 ? "" : depth << 1) + "s<%s>%n",
            "", s);
    }

    private static String toStringCPS(int[] cps) {
        StringBuilder sb = new StringBuilder(cps.length);
        for (int cp : cps)
            sb.append(toStringCP(cp));
        return sb.toString();
    }

    private static String toStringCP(int cp) {
        return (ASCII.isPrint(cp) ? "" + (char) cp
            : "\\u" + Integer.toString(cp, 16));
    }

    private static String toStringRange(int min, int max) {
        if (max == Integer.MAX_VALUE) {
            if (min == 0)
                return " * ";
            else if (min == 1)
                return " + ";
            return "{" + min + ", max}";
        }
        return "{" + min + ", " + max + "}";
    }

    private static String toString(Pattern.Node node) {
        String name = node.getClass().getName();
        return name.substring(name.lastIndexOf('$') + 1);
    }

    static final HashMap<CharPredicate, String> pmap;

    static {
        pmap = new HashMap<>();
        pmap.put(Pattern.ALL(), "All");
        pmap.put(Pattern.VertWS(), "VertWS");
        pmap.put(Pattern.HorizWS(), "HorizWS");

        pmap.put(CharPredicates.ASCII_DIGIT(), "ASCII.DIGIT");
        pmap.put(CharPredicates.ASCII_WORD(), "ASCII.WORD");
        pmap.put(CharPredicates.ASCII_SPACE(), "ASCII.SPACE");
    }

    static void walk(Pattern.Node node, int depth) {
        depth++;
        while (node != null) {
            String name = toString(node);
            String str;
            if (node instanceof Pattern.Prolog) {
                print(node, name, depth);
                // print the loop here
                Pattern.Loop loop = ((Pattern.Prolog) node).loop;
                name = toString(loop);
                str = name + " " + toStringRange(loop.cmin, loop.cmax);
                print(loop, str, depth);
                walk(loop.body, depth);
                print("/" + name, depth);
                node = loop;
            } else if (node instanceof Pattern.Loop) {
                return;  // stop here, body.next -> loop
            } else if (node instanceof Pattern.Curly) {
                Pattern.Curly c = (Pattern.Curly) node;
                str = "Curly " + c.type + " " + toStringRange(c.cmin, c.cmax);
                print(node, str, depth);
                walk(c.atom, depth);
                print("/Curly", depth);
            } else if (node instanceof GroupHead) {
                GroupHead head = (GroupHead) node;
                GroupTail tail = head.tail;
                print(head, "Group.head", depth);
                walk(head.next, depth);
                print(tail, "/Group.tail", depth);
                node = tail;
            } else if (node instanceof GroupTail) {
                return;  // stopper
            } else if (node instanceof Ques) {
                print(node, "Ques " + ((Ques) node).type, depth);
                walk(((Ques) node).atom, depth);
                print("/Ques", depth);
            } else if (node instanceof Branch) {
                Branch b = (Branch) node;
                print(b, name, depth);
                int i = 0;
                while (true) {
                    if (b.atoms[i] != null) {
                        walk(b.atoms[i], depth);
                    } else {
                        print("  (accepted)", depth);
                    }
                    if (++i == b.size)
                        break;
                    print("-branch.separator-", depth);
                }
                node = b.conn;
                print(node, "/Branch", depth);
            } else if (node instanceof BranchConn) {
                return;
            } else if (node instanceof CharProperty) {
                str = pmap.get(((CharProperty) node).predicate);
                if (str == null)
                    str = toString(node);
                else
                    str = "Single \"" + str + "\"";
                print(node, str, depth);
            } else if (node instanceof SliceNode) {
                str = name + "  \"" +
                    toStringCPS(((SliceNode) node).buffer) + "\"";
                print(node, str, depth);
            } else if (node instanceof CharPropertyGreedy) {
                CharPropertyGreedy gcp = (CharPropertyGreedy) node;
                String pstr = pmap.get(gcp.predicate);
                if (pstr == null)
                    pstr = gcp.predicate.toString();
                else
                    pstr = "Single \"" + pstr + "\"";
                str = name + " " + pstr;
                if (gcp.cMin == 0)
                    str += "*";
                else if (gcp.cMin == 1)
                    str += "+";
                else
                    str += "{" + gcp.cMin + ",}";
                print(node, str, depth);
            } else if (node instanceof LastNode) {
                print(node, "END", depth);
            } else if (node == Pattern.accept) {
                return;
            } else {
                print(node, name, depth);
            }
            node = node.next;
        }
    }

    public static void main(String[] args) {
        Pattern p = Pattern.compile("-?(0|[1-9]\\d*)(\\.\\d+)?([eE][+-]?\\d+)?");
        System.out.println("   Pattern: " + p);
        walk(p.root, 0);
    }
}
