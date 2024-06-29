/*
 * Copyright (c) 1999, 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package be.twofold.playground.pattern;

import java.util.*;

public final class Pattern {

    /**
     * The original regular-expression pattern string.
     *
     * @serial
     */
    private final String pattern;
    private final boolean caseInsensitive;

    /**
     * The starting point of state machine for the find operation.  This allows
     * a match to start anywhere in the input.
     */
    transient Node root;

    /**
     * The root of object tree for a match operation.  The pattern is matched
     * at the beginning.  This may include a find that uses BnM or a First
     * node.
     */
    transient Node matchRoot;

    /**
     * Temporary storage used by parsing pattern slice.
     */
    transient int[] buffer;

    /**
     * A temporary storage used for predicate for double return.
     */
    transient CharPredicate predicate;

    /**
     * Temporary null terminated code point array used by pattern compiling.
     */
    private transient int[] temp;

    /**
     * Index into the pattern string that keeps track of how much has been
     * parsed.
     */
    private transient int cursor;

    /**
     * Holds the length of the pattern string.
     */
    private transient int patternLength;

    /**
     * Compiles the given regular expression into a pattern.
     *
     * @param regex The expression to be compiled
     * @return the given regular expression compiled into a pattern
     * @throws PatternSyntaxException If the expression's syntax is invalid
     */
    public static Pattern compile(String regex) {
        return new Pattern(regex, false);
    }

    /**
     * Compiles the given regular expression into a pattern with the given
     * flags.
     *
     * @param regex The expression to be compiled
     * @return the given regular expression compiled into a pattern with the given flags
     * @throws IllegalArgumentException If bit values other than those corresponding to the defined
     *                                  match flags are set in {@code flags}
     * @throws PatternSyntaxException   If the expression's syntax is invalid
     */
    public static Pattern compile(String regex, boolean caseInsensitive) {
        return new Pattern(regex, caseInsensitive);
    }

    /**
     * Returns the regular expression from which this pattern was compiled.
     *
     * @return The source of this pattern
     */
    public String pattern() {
        return pattern;
    }

    /**
     * <p>Returns the string representation of this pattern. This
     * is the regular expression from which this pattern was
     * compiled.</p>
     *
     * @return The string representation of this pattern
     * @since 1.5
     */
    public String toString() {
        return pattern;
    }

    /**
     * Returns a literal pattern {@code String} for the specified
     * {@code String}.
     *
     * <p>This method produces a {@code String} that can be used to
     * create a {@code Pattern} that would match the string
     * {@code s} as if it were a literal pattern.</p> Metacharacters
     * or escape sequences in the input sequence will be given no special
     * meaning.
     *
     * @param s The string to be literalized
     * @return A literal string replacement
     * @since 1.5
     */
    public static String quote(String s) {
        int slashEIndex = s.indexOf("\\E");
        if (slashEIndex == -1)
            return "\\Q" + s + "\\E";

        int lenHint = s.length();
        lenHint = (lenHint < Integer.MAX_VALUE - 8 - lenHint) ?
            (lenHint << 1) : (Integer.MAX_VALUE - 8);

        StringBuilder sb = new StringBuilder(lenHint);
        sb.append("\\Q");
        int current = 0;
        do {
            sb.append(s, current, slashEIndex)
                .append("\\E\\\\E\\Q");
            current = slashEIndex + 2;
        } while ((slashEIndex = s.indexOf("\\E", current)) != -1);

        return sb.append(s, current, s.length())
            .append("\\E")
            .toString();
    }

    /**
     * This private constructor is used to create all Patterns. The pattern
     * string and match flags are all that is needed to completely describe
     * a Pattern. An empty pattern string results in an object tree with
     * only a Start node and a LastNode node.
     */
    private Pattern(String pattern, boolean caseInsensitive) {
        this.pattern = pattern;
        this.caseInsensitive = caseInsensitive;

        if (!this.pattern.isEmpty()) {
            try {
                compile();
            } catch (StackOverflowError soe) {
                throw error("Stack overflow during pattern compilation");
            }
        } else {
            root = new Start(lastAccept);
            matchRoot = lastAccept;
        }
    }

    /**
     * Copies regular expression to an int array and invokes the parsing
     * of the expression which will create the object tree.
     */
    private void compile() {
        // Handle canonical equivalences
        patternLength = pattern.length();

        // Copy pattern to int array for convenience
        // Use double zero to terminate pattern
        temp = new int[patternLength + 2];

        int c, count = 0;
        // Convert all chars into code points
        for (int x = 0; x < patternLength; x += Character.charCount(c)) {
            c = pattern.codePointAt(x);
            temp[count++] = c;
        }

        patternLength = count;   // patternLength now in code points

        // Allocate all temporary objects here.
        buffer = new int[32];

        // Start recursive descent parsing
        matchRoot = expr(lastAccept);
        // Check extra pattern characters
        if (patternLength != cursor) {
            if (peek() == ')') {
                throw error("Unmatched closing ')'");
            } else {
                throw error("Unexpected internal error");
            }
        }

        // Peephole optimization
        if (matchRoot instanceof Slice) {
            root = BnM.optimize(matchRoot);
            if (root == matchRoot) {
                root = new Start(matchRoot);
            }
        } else if (matchRoot instanceof Begin) {
            root = matchRoot;
        } else {
            root = new Start(matchRoot);
        }

        // Release temporary storage
        temp = null;
        buffer = null;
        patternLength = 0;
    }

    /*
     * The following private methods are mainly used to improve the
     * readability of the code. In order to let the Java compiler easily
     * inline them, we should not put many assertions or error checks in them.
     */

    /**
     * Mark the end of pattern with a specific character.
     */
    private void mark(int c) {
        temp[patternLength] = c;
    }

    /**
     * Peek the next character, and do not advance the cursor.
     */
    private int peek() {
        return temp[cursor];
    }

    /**
     * Read the next character, and advance the cursor by one.
     */
    private int read() {
        return temp[cursor++];
    }

    /**
     * Advance the cursor by one, and peek the next character.
     */
    private int next() {
        return temp[++cursor];
    }

    /**
     * Read the character after the next one, and advance the cursor by two.
     */
    private int skip() {
        int i = cursor;
        int ch = temp[i + 1];
        cursor = i + 2;
        return ch;
    }

    /**
     * Unread one next character, and retreat cursor by one.
     */
    private void unread() {
        cursor--;
    }

    /**
     * Internal method used for handling all syntax errors. The pattern is
     * displayed with a pointer to aid in locating the syntax error.
     */
    private PatternSyntaxException error(String s) {
        return new PatternSyntaxException(s, pattern, cursor - 1);
    }

    /**
     * The expression is parsed with branch nodes added for alternations.
     * This may be called recursively to parse sub expressions that may
     * contain alternations.
     */
    private Node expr(Node end) {
        Node prev = null;
        Node firstTail = null;
        Branch branch = null;
        Node branchConn = null;

        while (true) {
            Node node = sequence(end);
            Node nodeTail = root;      //double return
            if (prev == null) {
                prev = node;
                firstTail = nodeTail;
            } else {
                // Branch
                if (branchConn == null) {
                    branchConn = new BranchConn();
                    branchConn.next = end;
                }
                if (node == end) {
                    // if the node returned from sequence() is "end"
                    // we have an empty expr, set a null atom into
                    // the branch to indicate to go "next" directly.
                    node = null;
                } else {
                    // the "tail.next" of each atom goes to branchConn
                    nodeTail.next = branchConn;
                }
                if (prev == branch) {
                    branch.add(node);
                } else {
                    if (prev == end) {
                        prev = null;
                    } else {
                        // replace the "end" with "branchConn" at its tail.next
                        // when put the "prev" into the branch as the first atom.
                        firstTail.next = branchConn;
                    }
                    prev = branch = new Branch(prev, node, branchConn);
                }
            }
            if (peek() != '|') {
                return prev;
            }
            next();
        }
    }

    /**
     * Parsing of sequences between alternations.
     */
    private Node sequence(Node end) {
        Node head = null;
        Node tail = null;
        Node node;
        LOOP:
        while (true) {
            int ch = peek();
            switch (ch) {
                case '(':
                    // Because group handles its own closure,
                    // we need to treat it differently
                    node = group0();
                    // Check for comment or flag group
                    if (node == null)
                        continue;
                    if (head == null)
                        head = node;
                    else
                        tail.next = node;
                    // Double return: Tail was returned in root
                    tail = root;
                    continue;
                case '[':
                    node = newCharProperty(clazz(true));
                    break;
                case '\\':
                    ch = next();
                    if (ch == 'p' || ch == 'P') {
                        boolean oneLetter = true;
                        boolean comp = (ch == 'P');
                        ch = next(); // Consume { if present
                        if (ch != '{') {
                            unread();
                        } else {
                            oneLetter = false;
                        }
                        // node = newCharProperty(family(oneLetter, comp));
                        node = newCharProperty(family(oneLetter, comp));
                    } else {
                        unread();
                        node = atom();
                    }
                    break;
                case '^':
                    next();
                    node = new Begin();
                    break;
                case '$':
                    next();
                    node = new Dollar();
                    break;
                case '.':
                    next();
                    node = new CharProperty(ALL());
                    break;
                case '|':
                case ')':
                    break LOOP;
                case ']': // Now interpreting dangling ] and } as literals
                case '}':
                    node = atom();
                    break;
                case '?':
                case '*':
                case '+':
                    next();
                    throw error("Dangling meta character '" + ((char) ch) + "'");
                case 0:
                    if (cursor >= patternLength) {
                        break LOOP;
                    }
                    // Fall through
                default:
                    node = atom();
                    break;
            }

            node = closure(node);
            /* save the top dot-greedy nodes (.*, .+) as well
            if (node instanceof GreedyCharProperty &&
                ((GreedyCharProperty)node).cp instanceof Dot) {
                topClosureNodes.add(node);
            }
            */
            if (head == null) {
                head = tail = node;
            } else {
                tail.next = node;
                tail = node;
            }
        }
        if (head == null) {
            return end;
        }
        tail.next = end;
        root = tail;      //double return
        return head;
    }

    /**
     * Parse and add a new Single or Slice.
     */
    private Node atom() {
        int first = 0;
        int prev = -1;
        int ch = peek();
        while (true) {
            switch (ch) {
                case '*':
                case '+':
                case '?':
                case '{':
                    if (first > 1) {
                        cursor = prev;    // Unwind one character
                        first--;
                    }
                    break;
                case '$':
                case '.':
                case '^':
                case '(':
                case '[':
                case '|':
                case ')':
                    break;
                case '\\':
                    ch = next();
                    if (ch == 'p' || ch == 'P') { // Property
                        if (first > 0) { // Slice is waiting; handle it first
                            unread();
                            break;
                        } else { // No slice; just return the family node
                            boolean comp = (ch == 'P');
                            boolean oneLetter = true;
                            ch = next(); // Consume { if present
                            if (ch != '{')
                                unread();
                            else
                                oneLetter = false;
                            return newCharProperty(family(oneLetter, comp));
                        }
                    }
                    unread();
                    prev = cursor;
                    ch = escape(false, first == 0, false);
                    if (ch >= 0) {
                        append(ch, first);
                        first++;
                        ch = peek();
                        continue;
                    } else if (first == 0) {
                        return root;
                    }
                    // Unwind meta escape sequence
                    cursor = prev;
                    break;
                case 0:
                    if (cursor >= patternLength) {
                        break;
                    }
                    // Fall through
                default:
                    prev = cursor;
                    append(ch, first);
                    first++;
                    ch = next();
                    continue;
            }
            break;
        }
        if (first == 1) {
            return newCharProperty(single(buffer[0]));
        } else {
            return newSlice(buffer, first);
        }
    }

    private void append(int ch, int index) {
        int len = buffer.length;
        if (index - len >= 0) {
            buffer = Arrays.copyOf(buffer, len * 2);
        }
        buffer[index] = ch;
    }

    /**
     * Parses an escape sequence to determine the actual value that needs
     * to be matched.
     * If -1 is returned and create was true a new object was added to the tree
     * to handle the escape sequence.
     * If the returned value is greater than zero, it is the value that
     * matches the escape sequence.
     */
    private int escape(boolean inclass, boolean create, boolean isrange) {
        int ch = skip();
        switch (ch) {
            case '0':
                return o();
            case 'A':
                if (inclass) break;
                if (create) root = new Begin();
                return -1;
            case 'B':
                if (inclass) break;
                if (create) root = new Bound(Bound.NONE);
                return -1;
            case 'D':
                if (create) {
                    predicate = CharPredicates.ASCII_DIGIT();
                    predicate = predicate.negate();
                    if (!inclass)
                        root = newCharProperty(predicate);
                }
                return -1;
            case 'G':
                if (inclass) break;
                if (create) root = new LastMatch();
                return -1;
            case 'H':
                if (create) {
                    predicate = HorizWS().negate();
                    if (!inclass)
                        root = newCharProperty(predicate);
                }
                return -1;
            case 'N':
                return N();
            case 'R':
                if (inclass) break;
                if (create) root = new LineEnding();
                return -1;
            case 'S':
                if (create) {
                    predicate = CharPredicates.ASCII_SPACE();
                    predicate = predicate.negate();
                    if (!inclass)
                        root = newCharProperty(predicate);
                }
                return -1;
            case 'V':
                if (create) {
                    predicate = VertWS().negate();
                    if (!inclass)
                        root = newCharProperty(predicate);
                }
                return -1;
            case 'W':
                if (create) {
                    predicate = CharPredicates.ASCII_WORD();
                    predicate = predicate.negate();
                    if (!inclass)
                        root = newCharProperty(predicate);
                }
                return -1;
            case 'Z':
                if (inclass) break;
                if (create) {
                    root = new Dollar();
                }
                return -1;
            case 'a':
                return '\007';
            case 'b':
                if (inclass) break;
                if (create) {
                    root = new Bound(Bound.BOTH);
                }
                return -1;
            case 'c':
                return c();
            case 'd':
                if (create) {
                    predicate = CharPredicates.ASCII_DIGIT();
                    if (!inclass)
                        root = newCharProperty(predicate);
                }
                return -1;
            case 'e':
                return '\033';
            case 'f':
                return '\f';
            case 'h':
                if (create) {
                    predicate = HorizWS();
                    if (!inclass)
                        root = newCharProperty(predicate);
                }
                return -1;
            case 'n':
                return '\n';
            case 'r':
                return '\r';
            case 's':
                if (create) {
                    predicate = CharPredicates.ASCII_SPACE();
                    if (!inclass)
                        root = newCharProperty(predicate);
                }
                return -1;
            case 't':
                return '\t';
            case 'u':
                return u();
            case 'v':
                // '\v' was implemented as VT/0x0B in releases < 1.8 (though
                // undocumented). In JDK8 '\v' is specified as a predefined
                // character class for all vertical whitespace characters.
                // So [-1, root=VertWS node] pair is returned (instead of a
                // single 0x0B). This breaks the range if '\v' is used as
                // the start or end value, such as [\v-...] or [...-\v], in
                // which a single definite value (0x0B) is expected. For
                // compatibility concern '\013'/0x0B is returned if isrange.
                if (isrange)
                    return '\013';
                if (create) {
                    predicate = VertWS();
                    if (!inclass)
                        root = newCharProperty(predicate);
                }
                return -1;
            case 'w':
                if (create) {
                    predicate = CharPredicates.ASCII_WORD();
                    if (!inclass)
                        root = newCharProperty(predicate);
                }
                return -1;
            case 'x':
                return x();
            case 'z':
                if (inclass) break;
                if (create) root = new End();
                return -1;
            case '.':
                return '.';
            default:
                break;
        }
        throw error("Illegal/unsupported escape sequence");
    }

    /**
     * Parse a character class, and return the node that matches it.
     * <p>
     * Consumes a ] on the way out if consume is true. Usually consume
     * is true except for the case of [abc&&def] where def is a separate
     * right hand node with "understood" brackets.
     */
    private CharPredicate clazz(boolean consume) {
        CharPredicate prev = null;
        CharPredicate curr = null;

        boolean isNeg = false;
        int ch = next();

        // Negates if first char in a class, otherwise literal
        if (ch == '^' && temp[cursor - 1] == '[') {
            ch = next();
            isNeg = true;
        }
        while (true) {
            switch (ch) {
                case '[':
                    curr = clazz(true);
                    if (prev == null)
                        prev = curr;
                    else
                        prev = prev.union(curr);
                    ch = peek();
                    continue;
                case '&':
                    ch = next();
                    if (ch == '&') {
                        ch = next();
                        CharPredicate right = null;
                        while (ch != ']' && ch != '&') {
                            if (ch == '[') {
                                if (right == null)
                                    right = clazz(true);
                                else
                                    right = right.union(clazz(true));
                            } else { // abc&&def
                                unread();
                                if (right == null) {
                                    right = clazz(false);
                                } else {
                                    right = right.union(clazz(false));
                                }
                            }
                            ch = peek();
                        }
                        if (right != null)
                            curr = right;
                        if (prev == null) {
                            if (right == null)
                                throw error("Bad class syntax");
                            else
                                prev = right;
                        } else {
                            prev = prev.and(curr);
                        }
                    } else {
                        // treat as a literal &
                        unread();
                        break;
                    }
                    continue;
                case 0:
                    if (cursor >= patternLength)
                        throw error("Unclosed character class");
                    break;
                case ']':
                    if (prev != null) {
                        if (consume)
                            next();
                        if (isNeg)
                            return prev.negate();
                        return prev;
                    }
                    break;
                default:
                    break;
            }
            curr = range();
            if (curr != null) {
                if (prev == null)
                    prev = curr;
                else if (prev != curr)
                    prev = prev.union(curr);
            }

            ch = peek();
        }
    }

    /**
     * Returns a suitably optimized, single character predicate
     */
    private CharPredicate single(final int ch) {
        if (caseInsensitive) {
            int lower, upper;
            upper = Character.toUpperCase(ch);
            lower = Character.toLowerCase(upper);
            if (upper != lower)
                return SingleU(lower);
        }
        return Single(ch);  // Match a given BMP character
    }

    /**
     * Parse a single character or a character range in a character class
     * and return its representative node.
     */
    private CharPredicate range() {
        int ch = peek();
        if (ch == '\\') {
            ch = next();
            if (ch == 'p' || ch == 'P') { // A property
                boolean comp = (ch == 'P');
                boolean oneLetter = true;
                // Consume { if present
                ch = next();
                if (ch != '{')
                    unread();
                else
                    oneLetter = false;
                return family(oneLetter, comp);
            } else { // ordinary escape
                boolean isrange = temp[cursor + 1] == '-';
                unread();
                ch = escape(true, true, isrange);
                if (ch == -1)
                    return predicate;
            }
        } else {
            next();
        }
        if (ch >= 0) {
            if (peek() == '-') {
                int endRange = temp[cursor + 1];
                if (endRange == '[') {
                    return single(ch);
                }
                if (endRange != ']') {
                    next();
                    int m = peek();
                    if (m == '\\') {
                        m = escape(true, false, true);
                    } else {
                        next();
                    }
                    if (m < ch) {
                        throw error("Illegal character range");
                    }
                    if (caseInsensitive) {
                        return CIRange(ch, m);
                    } else {
                        return Range(ch, m);
                    }
                }
            }
            return single(ch);
        }
        throw error("Unexpected character '" + ((char) ch) + "'");
    }

    /**
     * Parses a Unicode character family and returns its representative node.
     */
    private CharPredicate family(boolean singleLetter, boolean isComplement) {
        next();
        String name;
        CharPredicate p = null;

        if (singleLetter) {
            int c = peek();
            if (!Character.isSupplementaryCodePoint(c)) {
                name = String.valueOf((char) c);
            } else {
                name = new String(temp, cursor, 1);
            }
            read();
        } else {
            int i = cursor;
            mark('}');
            while (read() != '}') {
            }
            mark('\000');
            int j = cursor;
            if (j > patternLength)
                throw error("Unclosed character family");
            if (i + 1 >= j)
                throw error("Empty character family");
            name = new String(temp, i, j - i - 1);
        }

        int i = name.indexOf('=');
        if (i != -1) {
            // property construct \p{name=value}
            String value = name.substring(i + 1);
            name = name.substring(0, i).toLowerCase(Locale.ENGLISH);
            switch (name) {
                case "sc":
                case "script":
                    p = CharPredicates.forUnicodeScript(value);
                    break;
                case "blk":
                case "block":
                    p = CharPredicates.forUnicodeBlock(value);
                    break;
                case "gc":
                case "general_category":
                    p = CharPredicates.forProperty(value, caseInsensitive);
                    break;
                default:
                    break;
            }
            if (p == null)
                throw error("Unknown Unicode property {name=<" + name + ">, "
                    + "value=<" + value + ">}");

        } else {
            if (name.startsWith("In")) {
                // \p{InBlockName}
                p = CharPredicates.forUnicodeBlock(name.substring(2));
            } else if (name.startsWith("Is")) {
                // \p{IsGeneralCategory} and \p{IsScriptName}
                String shortName = name.substring(2);
                p = CharPredicates.forUnicodeProperty(shortName, caseInsensitive);
                if (p == null)
                    p = CharPredicates.forProperty(shortName, caseInsensitive);
                if (p == null)
                    p = CharPredicates.forUnicodeScript(shortName);
            } else {
                p = CharPredicates.forProperty(name, caseInsensitive);
            }
            if (p == null)
                throw error("Unknown character property name {" + name + "}");
        }
        if (isComplement) {
            // it might be too expensive to detect if a complement of
            // CharProperty can match "certain" supplementary. So just
            // go with StartS.
            p = p.negate();
        }
        return p;
    }

    private CharProperty newCharProperty(CharPredicate p) {
        if (p == null)
            return null;
        if (p instanceof BmpCharPredicate)
            return new BmpCharProperty((BmpCharPredicate) p);
        else {
            return new CharProperty(p);
        }
    }

    /**
     * Parses a group and returns the head node of a set of nodes that process
     * the group. Sometimes a double return system is used where the tail is
     * returned in root.
     */
    private Node group0() {
        root = null;
        next();
        Node head = createGroup();
        Node tail = root;
        head.next = expr(tail);

        int testChar = read();
        if ((int) ')' != testChar) {
            throw error("Unclosed group");
        }

        // Check for quantifiers
        Node node = closure(head);
        if (node == head) { // No closure
            root = tail;
            return node;    // Dual return
        }
        if (head == tail) { // Zero length assertion
            root = node;
            return node;    // Dual return
        }

        if (node instanceof Ques) {
            Ques ques = (Ques) node;
            if (ques.type == Qtype.POSSESSIVE) {
                root = node;
                return node;
            }
            tail.next = new BranchConn();
            tail = tail.next;
            if (ques.type == Qtype.GREEDY) {
                head = new Branch(head, null, tail);
            } else { // Reluctant quantifier
                head = new Branch(null, head, tail);
            }
            root = tail;
            return head;
        } else if (node instanceof Curly) {
            Curly curly = (Curly) node;
            if (curly.type == Qtype.POSSESSIVE) {
                root = node;
                return node;
            }
            Loop loop;
            if (curly.type == Qtype.GREEDY) {
                loop = new Loop();
            } else {  // Reluctant Curly
                loop = new LazyLoop();
            }
            Prolog prolog = new Prolog(loop);
            loop.cmin = curly.cmin;
            loop.cmax = curly.cmax;
            loop.body = head;
            tail.next = loop;
            root = loop;
            return prolog; // Dual return
        }
        throw error("Internal logic error");
    }

    /**
     * Create group head and tail nodes using double return. If the group is
     * created with anonymous true then it is a pure group and should not
     * affect group counting.
     */
    private Node createGroup() {
        GroupHead head = new GroupHead();
        root = new GroupTail();

        // for debug/print only, head.match does NOT need the "tail" info
        head.tail = (GroupTail) root;

        return head;
    }

    enum Qtype {
        GREEDY, LAZY, POSSESSIVE, INDEPENDENT
    }

    private Qtype qtype() {
        int ch = next();
        if (ch == '?') {
            next();
            return Qtype.LAZY;
        } else if (ch == '+') {
            next();
            return Qtype.POSSESSIVE;
        }
        return Qtype.GREEDY;
    }

    private Node curly(Node prev, int cmin) {
        Qtype qtype = qtype();
        if (qtype == Qtype.GREEDY) {
            if (prev instanceof BmpCharProperty) {
                return new BmpCharPropertyGreedy((BmpCharProperty) prev, cmin);
            } else if (prev instanceof CharProperty) {
                return new CharPropertyGreedy((CharProperty) prev, cmin);
            }
        }
        return new Curly(prev, cmin, Integer.MAX_VALUE, qtype);
    }

    /**
     * Processes repetition. If the next character peeked is a quantifier
     * then new nodes must be appended to handle the repetition.
     * Prev could be a single or a group, so it could be a chain of nodes.
     */
    private Node closure(Node prev) {
        int ch = peek();
        switch (ch) {
            case '?':
                return new Ques(prev, qtype());
            case '*':
                return curly(prev, 0);
            case '+':
                return curly(prev, 1);
            case '{':
                ch = skip();
                if (ASCII.isDigit(ch)) {
                    int cmin = 0, cmax;
                    try {
                        do {
                            cmin = Math.addExact(Math.multiplyExact(cmin, 10),
                                ch - '0');
                        } while (ASCII.isDigit(ch = read()));
                        if (ch == ',') {
                            ch = read();
                            if (ch == '}') {
                                unread();
                                return curly(prev, cmin);
                            } else {
                                cmax = 0;
                                while (ASCII.isDigit(ch)) {
                                    cmax = Math.addExact(Math.multiplyExact(cmax, 10),
                                        ch - '0');
                                    ch = read();
                                }
                            }
                        } else {
                            cmax = cmin;
                        }
                    } catch (ArithmeticException ae) {
                        throw error("Illegal repetition range");
                    }
                    if (ch != '}')
                        throw error("Unclosed counted closure");
                    if (cmax < cmin)
                        throw error("Illegal repetition range");
                    unread();
                    return (cmin == 0 && cmax == 1)
                        ? new Ques(prev, qtype())
                        : new Curly(prev, cmin, cmax, qtype());
                } else {
                    throw error("Illegal repetition");
                }
            default:
                return prev;
        }
    }

    /**
     * Utility method for parsing control escape sequences.
     */
    private int c() {
        if (cursor < patternLength) {
            return read() ^ 64;
        }
        throw error("Illegal control escape sequence");
    }

    /**
     * Utility method for parsing octal escape sequences.
     */
    private int o() {
        int n = read();
        if (((n - '0') | ('7' - n)) >= 0) {
            int m = read();
            if (((m - '0') | ('7' - m)) >= 0) {
                int o = read();
                if ((((o - '0') | ('7' - o)) >= 0) && (((n - '0') | ('3' - n)) >= 0)) {
                    return (n - '0') * 64 + (m - '0') * 8 + (o - '0');
                }
                unread();
                return (n - '0') * 8 + (m - '0');
            }
            unread();
            return (n - '0');
        }
        throw error("Illegal octal escape sequence");
    }

    /**
     * Utility method for parsing hexadecimal escape sequences.
     */
    private int x() {
        int n = read();
        if (ASCII.isHexDigit(n)) {
            int m = read();
            if (ASCII.isHexDigit(m)) {
                return ASCII.toDigit(n) * 16 + ASCII.toDigit(m);
            }
        } else if (n == '{' && ASCII.isHexDigit(peek())) {
            int ch = 0;
            while (ASCII.isHexDigit(n = read())) {
                ch = (ch << 4) + ASCII.toDigit(n);
                if (ch > Character.MAX_CODE_POINT)
                    throw error("Hexadecimal codepoint is too big");
            }
            if (n != '}')
                throw error("Unclosed hexadecimal escape sequence");
            return ch;
        }
        throw error("Illegal hexadecimal escape sequence");
    }

    /**
     * Utility method for parsing unicode escape sequences.
     */
    private int cursor() {
        return cursor;
    }

    private void setcursor(int pos) {
        cursor = pos;
    }

    private int uxxxx() {
        int n = 0;
        for (int i = 0; i < 4; i++) {
            int ch = read();
            if (!ASCII.isHexDigit(ch)) {
                throw error("Illegal Unicode escape sequence");
            }
            n = n * 16 + ASCII.toDigit(ch);
        }
        return n;
    }

    private int u() {
        int n = uxxxx();
        if (Character.isHighSurrogate((char) n)) {
            int cur = cursor();
            if (read() == '\\' && read() == 'u') {
                int n2 = uxxxx();
                if (Character.isLowSurrogate((char) n2))
                    return Character.toCodePoint((char) n, (char) n2);
            }
            setcursor(cur);
        }
        return n;
    }

    private int N() {
        if (read() == '{') {
            int i = cursor;
            while (read() != '}') {
                if (cursor >= patternLength)
                    throw error("Unclosed character name escape sequence");
            }
            String name = new String(temp, i, cursor - i - 1);
            try {
                return Character.codePointOf(name);
            } catch (IllegalArgumentException x) {
                throw error("Unknown character name [" + name + "]");
            }
        }
        throw error("Illegal character name escape sequence");
    }

    /**
     * Utility method for creating a string slice matcher.
     */
    private Node newSlice(int[] buf, int count) {
        int[] tmp = new int[count];
        if (caseInsensitive) {
            for (int i = 0; i < count; i++) {
                tmp[i] = Character.toLowerCase(Character.toUpperCase(buf[i]));
            }
            return new SliceU(tmp);
        }
        System.arraycopy(buf, 0, tmp, 0, count);
        return new Slice(tmp);
    }

    /**
     * Base class for all node classes. Subclasses should override the match()
     * method as appropriate. This class is an accepting node, so its match()
     * always returns true.
     */
    static class Node {
        Node next;

        Node() {
            next = Pattern.accept;
        }
    }

    static class LastNode extends Node {
    }

    /**
     * Used for REs that can start anywhere within the input string.
     * This basically tries to match repeatedly at each spot in the
     * input string, moving forward after each try. An anchored search
     * or a BnM will bypass this node completely.
     */
    static class Start extends Node {
        Start(Node node) {
            this.next = node;
        }
    }

    /**
     * Node to anchor at the beginning of input. This object implements the
     * match for a \A sequence, and the caret anchor will use this if not in
     * multiline mode.
     */
    static final class Begin extends Node {
    }

    /**
     * Node to anchor at the end of input. This is the absolute end, so this
     * should not match at the last newline before the end as $ will.
     */
    static final class End extends Node {
    }

    /**
     * Node to match the location where the last match ended.
     * This is used for the \G construct.
     */
    static final class LastMatch extends Node {
    }

    /**
     * Node to anchor at the end of a line or the end of input based on the
     * multiline mode.
     * <p>
     * When not in multiline mode, the $ can only match at the very end
     * of the input, unless the input ends in a line terminator in which
     * it matches right before the last line terminator.
     * <p>
     * Note that \r\n is considered an atomic line terminator.
     * <p>
     * Like ^ the $ operator matches at a position, it does not match the
     * line terminators themselves.
     */
    static final class Dollar extends Node {
    }

    /**
     * Node class that matches a Unicode line ending '\R'
     */
    static final class LineEnding extends Node {
    }

    /**
     * Abstract node class to match one character satisfying some
     * boolean property.
     */
    static class CharProperty extends Node {
        final CharPredicate predicate;

        CharProperty(CharPredicate predicate) {
            this.predicate = predicate;
        }
    }

    /**
     * Optimized version of CharProperty that works only for
     * properties never satisfied by Supplementary characters.
     */
    private static class BmpCharProperty extends CharProperty {
        BmpCharProperty(BmpCharPredicate predicate) {
            super(predicate);
        }
    }

    /**
     * Base class for all Slice nodes
     */
    static class SliceNode extends Node {
        final int[] buffer;

        SliceNode(int[] buf) {
            buffer = buf;
        }
    }

    /**
     * Node class for a case sensitive/BMP-only sequence of literal
     * characters.
     */
    static class Slice extends SliceNode {
        Slice(int[] buf) {
            super(buf);
        }
    }

    /**
     * Node class for a unicode_case_insensitive/BMP-only sequence of
     * literal characters. Uses unicode case folding.
     */
    static final class SliceU extends SliceNode {
        SliceU(int[] buf) {
            super(buf);
        }
    }

    /**
     * The 0 or 1 quantifier. This one class implements all three types.
     */
    static final class Ques extends Node {
        final Node atom;
        final Qtype type;

        Ques(Node node, Qtype type) {
            this.atom = node;
            this.type = type;
        }
    }

    /**
     * Handles the greedy style repetition with the specified minimum
     * and the maximum equal to MAX_REPS, for *, + and {N,} quantifiers.
     */
    static class CharPropertyGreedy extends Node {
        final CharPredicate predicate;
        final int cMin;

        CharPropertyGreedy(CharProperty cp, int cMin) {
            this.predicate = cp.predicate;
            this.cMin = cMin;
        }

    }

    static final class BmpCharPropertyGreedy extends CharPropertyGreedy {
        BmpCharPropertyGreedy(BmpCharProperty bcp, int cmin) {
            super(bcp, cmin);
        }
    }

    /**
     * Handles the curly-brace style repetition with a specified minimum and
     * maximum occurrences. The * quantifier is handled as a special case.
     * This class handles the three types.
     */
    static final class Curly extends Node {
        final Node atom;
        final Qtype type;
        final int cmin;
        final int cmax;

        Curly(Node node, int cmin, int cmax, Qtype type) {
            this.atom = node;
            this.type = type;
            this.cmin = cmin;
            this.cmax = cmax;
        }
    }

    /**
     * A Guard node at the end of each atom node in a Branch. It
     * serves the purpose of chaining the "match" operation to
     * "next" but not the "study", so we can collect the TreeInfo
     * of each atom node without including the TreeInfo of the
     * "next".
     */
    static final class BranchConn extends Node {
    }

    /**
     * Handles the branching of alternations. Note this is also used for
     * the ? quantifier to branch between the case where it matches once
     * and where it does not occur.
     */
    static final class Branch extends Node {
        Node[] atoms = new Node[2];
        int size = 2;
        final Node conn;

        Branch(Node first, Node second, Node branchConn) {
            conn = branchConn;
            atoms[0] = first;
            atoms[1] = second;
        }

        void add(Node node) {
            if (size >= atoms.length) {
                Node[] tmp = new Node[atoms.length * 2];
                System.arraycopy(atoms, 0, tmp, 0, atoms.length);
                atoms = tmp;
            }
            atoms[size++] = node;
        }
    }

    /**
     * The GroupHead saves the location where the group begins in the locals
     * and restores them when the match is done.
     * <p>
     * The matchRef is used when a reference to this group is accessed later
     * in the expression. The locals will have a negative value in them to
     * indicate that we do not want to unset the group if the reference
     * doesn't match.
     */
    static final class GroupHead extends Node {
        GroupTail tail;    // for debug/print only, match does not need to know
    }

    /**
     * The GroupTail handles the setting of group beginning and ending
     * locations when groups are successfully matched. It must also be able to
     * unset groups that have to be backed off of.
     * <p>
     * The GroupTail node is also used when a previous group is referenced,
     * and in that case no group information needs to be set.
     */
    static final class GroupTail extends Node {
    }

    /**
     * This sets up a loop to handle a recursive quantifier structure.
     */
    static final class Prolog extends Node {
        final Loop loop;

        Prolog(Loop loop) {
            this.loop = loop;
        }
    }

    /**
     * Handles the repetition count for a greedy Curly. The matchInit
     * is called from the Prolog to save the index of where the group
     * beginning is stored. A zero length group check occurs in the
     * normal match but is skipped in the matchInit.
     */
    static class Loop extends Node {
        Node body;
        int cmin, cmax;
    }

    /**
     * Handles the repetition count for a reluctant Curly. The matchInit
     * is called from the Prolog to save the index of where the group
     * beginning is stored. A zero length group check occurs in the
     * normal match but is skipped in the matchInit.
     */
    static final class LazyLoop extends Loop {
    }

    /**
     * Handles word boundaries. Includes a field to allow this one class to
     * deal with the different types of word boundaries we can match. The word
     * characters include underscores, letters, and digits. Non spacing marks
     * can are also part of a word if they have a base character, otherwise
     * they are ignored for purposes of finding word boundaries.
     */
    static final class Bound extends Node {
        static final int BOTH = 0x3;
        static final int NONE = 0x4;
        final int type;

        Bound(int n) {
            type = n;
        }
    }

    /**
     * Attempts to match a slice in the input using the Boyer-Moore string
     * matching algorithm. The algorithm is based on the idea that the
     * pattern can be shifted farther ahead in the search text if it is
     * matched right to left.
     * <p>
     * The pattern is compared to the input one character at a time, from
     * the rightmost character in the pattern to the left. If the characters
     * all match the pattern has been found. If a character does not match,
     * the pattern is shifted right a distance that is the maximum of two
     * functions, the bad character shift and the good suffix shift. This
     * shift moves the attempted match position through the input more
     * quickly than a naive one position at a time check.
     * <p>
     * The bad character shift is based on the character from the text that
     * did not match. If the character does not appear in the pattern, the
     * pattern can be shifted completely beyond the bad character. If the
     * character does occur in the pattern, the pattern can be shifted to
     * line the pattern up with the next occurrence of that character.
     * <p>
     * The good suffix shift is based on the idea that some subset on the right
     * side of the pattern has matched. When a bad character is found, the
     * pattern can be shifted right by the pattern length if the subset does
     * not occur again in pattern, or by the amount of distance to the
     * next occurrence of the subset in the pattern.
     * <p>
     * Boyer-Moore search methods adapted from code by Amy Yu.
     */
    static class BnM extends Node {
        final int[] buffer;
        final int[] lastOcc;
        final int[] optoSft;

        /**
         * Pre calculates arrays needed to generate the bad character
         * shift and the good suffix shift. Only the last seven bits
         * are used to see if chars match; This keeps the tables small
         * and covers the heavily used ASCII range, but occasionally
         * results in an aliased match for the bad character shift.
         */
        static Node optimize(Node node) {
            if (!(node instanceof Slice)) {
                return node;
            }

            int[] src = ((Slice) node).buffer;
            int patternLength = src.length;
            // The BM algorithm requires a bit of overhead;
            // If the pattern is short don't use it, since
            // a shift larger than the pattern length cannot
            // be used anyway.
            if (patternLength < 4) {
                return node;
            }
            int i, j;
            int[] lastOcc = new int[128];
            int[] optoSft = new int[patternLength];
            // Precalculate part of the bad character shift
            // It is a table for where in the pattern each
            // lower 7-bit value occurs
            for (i = 0; i < patternLength; i++) {
                lastOcc[src[i] & 0x7F] = i + 1;
            }
            // Precalculate the good suffix shift
            // i is the shift amount being considered
            NEXT:
            for (i = patternLength; i > 0; i--) {
                // j is the beginning index of suffix being considered
                for (j = patternLength - 1; j >= i; j--) {
                    // Testing for good suffix
                    if (src[j] == src[j - i]) {
                        // src[j..len] is a good suffix
                        optoSft[j - 1] = i;
                    } else {
                        // No match. The array has already been
                        // filled up with correct values before.
                        continue NEXT;
                    }
                }
                // This fills up the remaining of optoSft
                // any suffix can not have larger shift amount
                // then its sub-suffix. Why???
                while (j > 0) {
                    optoSft[--j] = i;
                }
            }
            // Set the guard value because of unicode compression
            optoSft[patternLength - 1] = 1;
            return new BnM(src, lastOcc, optoSft, node.next);
        }

        BnM(int[] src, int[] lastOcc, int[] optoSft, Node next) {
            this.buffer = src;
            this.lastOcc = lastOcc;
            this.optoSft = optoSft;
            this.next = next;
        }
    }

    @FunctionalInterface
    interface CharPredicate {
        boolean is(int ch);

        default CharPredicate and(CharPredicate p) {
            return ch -> is(ch) && p.is(ch);
        }

        default CharPredicate union(CharPredicate p) {
            return ch -> is(ch) || p.is(ch);
        }

        default CharPredicate union(CharPredicate p1,
                                    CharPredicate p2) {
            return ch -> is(ch) || p1.is(ch) || p2.is(ch);
        }

        default CharPredicate negate() {
            return ch -> !is(ch);
        }
    }

    interface BmpCharPredicate extends CharPredicate {

        default CharPredicate and(CharPredicate p) {
            if (p instanceof BmpCharPredicate)
                return (BmpCharPredicate) (ch -> is(ch) && p.is(ch));
            return ch -> is(ch) && p.is(ch);
        }

        default CharPredicate union(CharPredicate p) {
            if (p instanceof BmpCharPredicate)
                return (BmpCharPredicate) (ch -> is(ch) || p.is(ch));
            return ch -> is(ch) || p.is(ch);
        }

    }

    /**
     * matches a Perl vertical whitespace
     */
    static BmpCharPredicate VertWS() {
        return cp -> (cp >= 0x0A && cp <= 0x0D) ||
            cp == 0x85 || cp == 0x2028 || cp == 0x2029;
    }

    /**
     * matches a Perl horizontal whitespace
     */
    static BmpCharPredicate HorizWS() {
        return cp ->
            cp == 0x09 || cp == 0x20 || cp == 0xa0 || cp == 0x1680 ||
                cp == 0x180e || cp >= 0x2000 && cp <= 0x200a || cp == 0x202f ||
                cp == 0x205f || cp == 0x3000;
    }

    /**
     * for the Unicode category ALL and the dot metacharacter when
     * in dotall mode.
     */
    static CharPredicate ALL() {
        return ch -> true;
    }

    /**
     * A bmp/optimized predicate of single
     */
    static BmpCharPredicate Single(int c) {
        return ch -> ch == c;
    }

    /**
     * Unicode case insensitive matches a given Unicode character
     */
    static CharPredicate SingleU(int lower) {
        return ch -> lower == ch ||
            lower == Character.toLowerCase(Character.toUpperCase(ch));
    }

    private static boolean inRange(int lower, int ch, int upper) {
        return lower <= ch && ch <= upper;
    }

    /**
     * Characters within a explicit value range
     */
    static CharPredicate Range(int lower, int upper) {
        if (upper < Character.MIN_HIGH_SURROGATE ||
            lower > Character.MAX_LOW_SURROGATE &&
                upper < Character.MIN_SUPPLEMENTARY_CODE_POINT)
            return (BmpCharPredicate) (ch -> inRange(lower, ch, upper));
        return ch -> inRange(lower, ch, upper);
    }

    static CharPredicate CIRange(int lower, int upper) {
        return ch -> {
            if (inRange(lower, ch, upper))
                return true;
            int up = Character.toUpperCase(ch);
            return inRange(lower, up, upper) ||
                inRange(lower, Character.toLowerCase(up), upper);
        };
    }

    /**
     * This must be the very first initializer.
     */
    static final Node accept = new Node();

    static final Node lastAccept = new LastNode();

}
