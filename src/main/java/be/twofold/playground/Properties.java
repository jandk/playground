package be.twofold.playground;

import be.twofold.playground.common.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * The {@code Properties} class represents a persistent set of
 * properties. The {@code Properties} can be saved to a stream
 * or loaded from a stream. Each key and its corresponding value in
 * the property list is a string.
 * <p>
 * A property list can contain another property list as its
 * "defaults"; this second property list is searched if
 * the property key is not found in the original property list.
 * <p>
 * Because {@code Properties} inherits from {@code Hashtable}, the
 * {@code put} and {@code putAll} methods can be applied to a
 * {@code Properties} object.  Their use is strongly discouraged as they
 * allow the caller to insert entries whose keys or values are not
 * {@code Strings}.  The {@code setProperty} method should be used
 * instead.  If the {@code store} or {@code save} method is called
 * on a "compromised" {@code Properties} object that contains a
 * non-{@code String} key or value, the call will fail. Similarly,
 * the call to the {@code propertyNames} or {@code list} method
 * will fail if it is called on a "compromised" {@code Properties}
 * object that contains a non-{@code String} key.
 *
 * <p>
 * The iterators returned by the {@code iterator} method of this class's
 * "collection views" (that is, {@code entrySet()}, {@code keySet()}, and
 * {@code values()}) may not fail-fast (unlike the Hashtable implementation).
 * These iterators are guaranteed to traverse elements as they existed upon
 * construction exactly once, and may (but are not guaranteed to) reflect any
 * modifications subsequent to construction.
 * <p>
 * The {@link #load(Reader) load(Reader)} {@code /}
 * {@link #store(Writer, String) store(Writer, String)}
 * methods load and store properties from and to a character based stream
 * in a simple line-oriented format specified below.
 * <p>
 * The {@link #load(InputStream) load(InputStream)} {@code /}
 * {@link #store(OutputStream, String) store(OutputStream, String)}
 * methods work the same way as the load(Reader)/store(Writer, String) pair, except
 * the input/output stream is encoded in ISO 8859-1 character encoding.
 * Characters that cannot be directly represented in this encoding can be written using
 * Unicode escapes as defined in section {@jls 3.3} of
 * <cite>The Java Language Specification</cite>;
 * only a single 'u' character is allowed in an escape
 * sequence.
 *
 * <p>This class is thread-safe: multiple threads can share a single
 * {@code Properties} object without the need for external synchronization.
 *
 * @author Arthur van Hoff
 * @author Michael McCloskey
 * @author Xueming Shen
 * @apiNote The {@code Properties} class does not inherit the concept of a load factor
 * from its superclass, {@code Hashtable}.
 * @since 1.0
 */
public class Properties {

    /**
     * Properties does not store values in its inherited Hashtable, but instead
     * in an internal ConcurrentHashMap.  Synchronization is omitted from
     * simple read operations.  Writes and bulk operations remain synchronized,
     * as in Hashtable.
     */
    private transient volatile Map<String, String> map;

    /**
     * Creates an empty property list with no default values.
     *
     * @implNote The initial capacity of a {@code Properties} object created
     * with this constructor is unspecified.
     */
    public Properties() {
        this(8);
    }

    /**
     * Creates an empty property list with no default values, and with an
     * initial size accommodating the specified number of elements without the
     * need to dynamically resize.
     *
     * @param initialCapacity the {@code Properties} will be sized to
     *                        accommodate this many elements
     * @throws IllegalArgumentException if the initial capacity is less than
     *                                  zero.
     */
    public Properties(int initialCapacity) {
        map = new ConcurrentHashMap<>(initialCapacity);
    }

    /**
     * Calls the {@code Hashtable} method {@code put}. Provided for
     * parallelism with the {@code getProperty} method. Enforces use of
     * strings for property keys and values. The value returned is the
     * result of the {@code Hashtable} call to {@code put}.
     *
     * @param key   the key to be placed into this property list.
     * @param value the value corresponding to {@code key}.
     * @return the previous value of the specified key in this property
     * list, or {@code null} if it did not have one.
     * @see #getProperty
     * @since 1.2
     */
    public synchronized Object setProperty(String key, String value) {
        return map.put(key, value);
    }


    /**
     * Reads a property list (key and element pairs) from the input
     * character stream in a simple line-oriented format.
     * <p>
     * Properties are processed in terms of lines. There are two
     * kinds of line, <i>natural lines</i> and <i>logical lines</i>.
     * A natural line is defined as a line of
     * characters that is terminated either by a set of line terminator
     * characters ({@code \n} or {@code \r} or {@code \r\n})
     * or by the end of the stream. A natural line may be either a blank line,
     * a comment line, or hold all or some of a key-element pair. A logical
     * line holds all the data of a key-element pair, which may be spread
     * out across several adjacent natural lines by escaping
     * the line terminator sequence with a backslash character
     * {@code \}.  Note that a comment line cannot be extended
     * in this manner; every natural line that is a comment must have
     * its own comment indicator, as described below. Lines are read from
     * input until the end of the stream is reached.
     *
     * <p>
     * A natural line that contains only white space characters is
     * considered blank and is ignored.  A comment line has an ASCII
     * {@code '#'} or {@code '!'} as its first non-white
     * space character; comment lines are also ignored and do not
     * encode key-element information.  In addition to line
     * terminators, this format considers the characters space
     * ({@code ' '}, {@code '\u005Cu0020'}), tab
     * ({@code '\t'}, {@code '\u005Cu0009'}), and form feed
     * ({@code '\f'}, {@code '\u005Cu000C'}) to be white
     * space.
     *
     * <p>
     * If a logical line is spread across several natural lines, the
     * backslash escaping the line terminator sequence, the line
     * terminator sequence, and any white space at the start of the
     * following line have no affect on the key or element values.
     * The remainder of the discussion of key and element parsing
     * (when loading) will assume all the characters constituting
     * the key and element appear on a single natural line after
     * line continuation characters have been removed.  Note that
     * it is <i>not</i> sufficient to only examine the character
     * preceding a line terminator sequence to decide if the line
     * terminator is escaped; there must be an odd number of
     * contiguous backslashes for the line terminator to be escaped.
     * Since the input is processed from left to right, a
     * non-zero even number of 2<i>n</i> contiguous backslashes
     * before a line terminator (or elsewhere) encodes <i>n</i>
     * backslashes after escape processing.
     *
     * <p>
     * The key contains all of the characters in the line starting
     * with the first non-white space character and up to, but not
     * including, the first unescaped {@code '='},
     * {@code ':'}, or white space character other than a line
     * terminator. All of these key termination characters may be
     * included in the key by escaping them with a preceding backslash
     * character; for example,<p>
     * <p>
     * {@code \:\=}<p>
     * <p>
     * would be the two-character key {@code ":="}.  Line
     * terminator characters can be included using {@code \r} and
     * {@code \n} escape sequences.  Any white space after the
     * key is skipped; if the first non-white space character after
     * the key is {@code '='} or {@code ':'}, then it is
     * ignored and any white space characters after it are also
     * skipped.  All remaining characters on the line become part of
     * the associated element string; if there are no remaining
     * characters, the element is the empty string
     * {@code ""}.  Once the raw character sequences
     * constituting the key and element are identified, escape
     * processing is performed as described above.
     *
     * <p>
     * As an example, each of the following three lines specifies the key
     * {@code "Truth"} and the associated element value
     * {@code "Beauty"}:
     * <pre>
     * Truth = Beauty
     *  Truth:Beauty
     * Truth                    :Beauty
     * </pre>
     * As another example, the following three lines specify a single
     * property:
     * <pre>
     * fruits                           apple, banana, pear, \
     *                                  cantaloupe, watermelon, \
     *                                  kiwi, mango
     * </pre>
     * The key is {@code "fruits"} and the associated element is:
     * <pre>"apple, banana, pear, cantaloupe, watermelon, kiwi, mango"</pre>
     * Note that a space appears before each {@code \} so that a space
     * will appear after each comma in the final result; the {@code \},
     * line terminator, and leading white space on the continuation line are
     * merely discarded and are <i>not</i> replaced by one or more other
     * characters.
     * <p>
     * As a third example, the line:
     * <pre>cheeses
     * </pre>
     * specifies that the key is {@code "cheeses"} and the associated
     * element is the empty string {@code ""}.
     * <p>
     * <a id="unicodeescapes"></a>
     * Characters in keys and elements can be represented in escape
     * sequences similar to those used for character and string literals
     * (see sections {@jls 3.3} and {@jls 3.10.6} of
     * <cite>The Java Language Specification</cite>).
     * <p>
     * The differences from the character escape sequences and Unicode
     * escapes used for characters and strings are:
     *
     * <ul>
     * <li> Octal escapes are not recognized.
     *
     * <li> The character sequence {@code \b} does <i>not</i>
     * represent a backspace character.
     *
     * <li> The method does not treat a backslash character,
     * {@code \}, before a non-valid escape character as an
     * error; the backslash is silently dropped.  For example, in a
     * Java string the sequence {@code "\z"} would cause a
     * compile time error.  In contrast, this method silently drops
     * the backslash.  Therefore, this method treats the two character
     * sequence {@code "\b"} as equivalent to the single
     * character {@code 'b'}.
     *
     * <li> Escapes are not necessary for single and double quotes;
     * however, by the rule above, single and double quote characters
     * preceded by a backslash still yield single and double quote
     * characters, respectively.
     *
     * <li> Only a single 'u' character is allowed in a Unicode escape
     * sequence.
     *
     * </ul>
     * <p>
     * The specified stream remains open after this method returns.
     *
     * @param reader the input character stream.
     * @throws IOException              if an error occurred when reading from the
     *                                  input stream.
     * @throws IllegalArgumentException if a malformed Unicode escape
     *                                  appears in the input.
     * @throws NullPointerException     if {@code reader} is null.
     * @since 1.6
     */
    public synchronized void load(Reader reader) throws IOException {
        Check.notNull(reader, "reader parameter is null");
        load0(new LineReader(reader));
    }

    /**
     * Reads a property list (key and element pairs) from the input
     * byte stream. The input stream is in a simple line-oriented
     * format as specified in
     * {@link #load(Reader) load(Reader)} and is assumed to use
     * the ISO 8859-1 character encoding; that is each byte is one Latin1
     * character. Characters not in Latin1, and certain special characters,
     * are represented in keys and elements using Unicode escapes as defined in
     * section {@jls 3.3} of
     * <cite>The Java Language Specification</cite>.
     * <p>
     * The specified stream remains open after this method returns.
     *
     * @param inStream the input stream.
     * @throws IOException              if an error occurred when reading from the
     *                                  input stream.
     * @throws IllegalArgumentException if the input stream contains a
     *                                  malformed Unicode escape sequence.
     * @throws NullPointerException     if {@code inStream} is null.
     * @since 1.2
     */
    public synchronized void load(InputStream inStream) throws IOException {
        Check.notNull(inStream, "inStream parameter is null");
        load0(new LineReader(inStream));
    }

    private void load0(LineReader lr) throws IOException {
        StringBuilder outBuffer = new StringBuilder();
        int limit;
        int keyLen;
        int valueStart;
        boolean hasSep;
        boolean precedingBackslash;

        while ((limit = lr.readLine()) >= 0) {
            keyLen = 0;
            valueStart = limit;
            hasSep = false;

            //System.out.println("line=<" + new String(lineBuf, 0, limit) + ">");
            precedingBackslash = false;
            while (keyLen < limit) {
                char c = lr.lineBuf[keyLen];
                //need check if escaped.
                if ((c == '=' || c == ':') && !precedingBackslash) {
                    valueStart = keyLen + 1;
                    hasSep = true;
                    break;
                } else if ((c == ' ' || c == '\t' || c == '\f') && !precedingBackslash) {
                    valueStart = keyLen + 1;
                    break;
                }
                if (c == '\\') {
                    precedingBackslash = !precedingBackslash;
                } else {
                    precedingBackslash = false;
                }
                keyLen++;
            }
            while (valueStart < limit) {
                char c = lr.lineBuf[valueStart];
                if (c != ' ' && c != '\t' && c != '\f') {
                    if (!hasSep && (c == '=' || c == ':')) {
                        hasSep = true;
                    } else {
                        break;
                    }
                }
                valueStart++;
            }
            String key = loadConvert(lr.lineBuf, 0, keyLen, outBuffer);
            String value = loadConvert(lr.lineBuf, valueStart, limit - valueStart, outBuffer);
            map.put(key, value);
        }
    }

    /* Read in a "logical line" from an InputStream/Reader, skip all comment
     * and blank lines and filter out those leading whitespace characters
     * (\u0020, \u0009 and \u000c) from the beginning of a "natural line".
     * Method returns the char length of the "logical line" and stores
     * the line in "lineBuf".
     */
    private static class LineReader {
        LineReader(InputStream inStream) {
            this.inStream = inStream;
            inByteBuf = new byte[8192];
        }

        LineReader(Reader reader) {
            this.reader = reader;
            inCharBuf = new char[8192];
        }

        char[] lineBuf = new char[1024];
        private byte[] inByteBuf;
        private char[] inCharBuf;
        private int inLimit = 0;
        private int inOff = 0;
        private InputStream inStream;
        private Reader reader;

        int readLine() throws IOException {
            // use locals to optimize for interpreted performance
            int len = 0;
            int off = inOff;
            int limit = inLimit;

            boolean skipWhiteSpace = true;
            boolean appendedLineBegin = false;
            boolean precedingBackslash = false;
            boolean fromStream = inStream != null;
            char c;

            while (true) {
                if (off >= limit) {
                    inLimit = limit = fromStream ? inStream.read(inByteBuf)
                        : reader.read(inCharBuf);
                    if (limit <= 0) {
                        if (len == 0) {
                            return -1;
                        }
                        return precedingBackslash ? len - 1 : len;
                    }
                    off = 0;
                }

                // (char)(byte & 0xFF) is equivalent to calling a ISO8859-1 decoder.
                c = (fromStream) ? (char) (inByteBuf[off++] & 0xFF) : inCharBuf[off++];

                if (skipWhiteSpace) {
                    if (c == ' ' || c == '\t' || c == '\f') {
                        continue;
                    }
                    if (!appendedLineBegin && (c == '\r' || c == '\n')) {
                        continue;
                    }
                    skipWhiteSpace = false;
                    appendedLineBegin = false;

                }
                if (len == 0) { // Still on a new logical line
                    if (c == '#' || c == '!') {
                        // Comment, quickly consume the rest of the line

                        // When checking for new line characters a range check,
                        // starting with the higher bound ('\r') means one less
                        // branch in the common case.
                        commentLoop:
                        while (true) {
                            if (fromStream) {
                                byte b;
                                while (off < limit) {
                                    b = inByteBuf[off++];
                                    if ((b == '\r' || b == '\n'))
                                        break commentLoop;
                                }
                                if (off == limit) {
                                    inLimit = limit = inStream.read(inByteBuf);
                                    if (limit <= 0) { // EOF
                                        return -1;
                                    }
                                    off = 0;
                                }
                            } else {
                                while (off < limit) {
                                    c = inCharBuf[off++];
                                    if ((c == '\r' || c == '\n'))
                                        break commentLoop;
                                }
                                if (off == limit) {
                                    inLimit = limit = reader.read(inCharBuf);
                                    if (limit <= 0) { // EOF
                                        return -1;
                                    }
                                    off = 0;
                                }
                            }
                        }
                        skipWhiteSpace = true;
                        continue;
                    }
                }

                if (c != '\n' && c != '\r') {
                    lineBuf[len++] = c;
                    if (len == lineBuf.length) {
                        lineBuf = Arrays.copyOf(lineBuf, len * 3 / 2);
                    }
                    // flip the preceding backslash flag
                    precedingBackslash = c == '\\' && !precedingBackslash;
                } else {
                    // reached EOL
                    if (len == 0) {
                        skipWhiteSpace = true;
                        continue;
                    }
                    if (off >= limit) {
                        inLimit = limit = fromStream ? inStream.read(inByteBuf)
                            : reader.read(inCharBuf);
                        off = 0;
                        if (limit <= 0) { // EOF
                            return precedingBackslash ? len - 1 : len;
                        }
                    }
                    if (precedingBackslash) {
                        // backslash at EOL is not part of the line
                        len -= 1;
                        // skip leading whitespace characters in the following line
                        skipWhiteSpace = true;
                        appendedLineBegin = true;
                        precedingBackslash = false;
                        // take care not to include any subsequent \n
                        if (c == '\r') {
                            if (fromStream) {
                                if (inByteBuf[off] == '\n') {
                                    off++;
                                }
                            } else {
                                if (inCharBuf[off] == '\n') {
                                    off++;
                                }
                            }
                        }
                    } else {
                        inOff = off;
                        return len;
                    }
                }
            }
        }
    }

    /*
     * Converts encoded &#92;uxxxx to unicode chars
     * and changes special saved chars to their original forms
     */
    private String loadConvert(char[] in, int off, int len, StringBuilder out) {
        char aChar;
        int end = off + len;
        int start = off;
        while (off < end) {
            aChar = in[off++];
            if (aChar == '\\') {
                break;
            }
        }
        if (off == end) { // No backslash
            return new String(in, start, len);
        }

        // backslash found at off - 1, reset the shared buffer, rewind offset
        out.setLength(0);
        off--;
        out.append(in, start, off - start);

        while (off < end) {
            aChar = in[off++];
            if (aChar == '\\') {
                // No need to bounds check since LineReader::readLine excludes
                // unescaped \s at the end of the line
                aChar = in[off++];
                if (aChar == 'u') {
                    // Read the xxxx
                    if (off > end - 4)
                        throw new IllegalArgumentException(
                            "Malformed \\uxxxx encoding.");
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = in[off++];
                        value = switch (aChar) {
                            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> (value << 4) + aChar - '0';
                            case 'a', 'b', 'c', 'd', 'e', 'f' -> (value << 4) + 10 + aChar - 'a';
                            case 'A', 'B', 'C', 'D', 'E', 'F' -> (value << 4) + 10 + aChar - 'A';
                            default -> throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
                        };
                    }
                    out.append((char) value);
                } else {
                    if (aChar == 't') aChar = '\t';
                    else if (aChar == 'r') aChar = '\r';
                    else if (aChar == 'n') aChar = '\n';
                    else if (aChar == 'f') aChar = '\f';
                    out.append(aChar);
                }
            } else {
                out.append(aChar);
            }
        }
        return out.toString();
    }

    /*
     * Converts unicodes to encoded &#92;uxxxx and escapes
     * special characters with a preceding slash
     */
    private String saveConvert(String theString,
                               boolean escapeSpace,
                               boolean escapeUnicode) {
        int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuilder outBuffer = new StringBuilder(bufLen);
        HexFormat hex = HexFormat.of().withUpperCase();
        for (int x = 0; x < len; x++) {
            char aChar = theString.charAt(x);
            // Handle common case first, selecting largest block that
            // avoids the specials below
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    outBuffer.append('\\');
                    outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }
            switch (aChar) {
                case ' ':
                    if (x == 0 || escapeSpace)
                        outBuffer.append('\\');
                    outBuffer.append(' ');
                    break;
                case '\t':
                    outBuffer.append('\\');
                    outBuffer.append('t');
                    break;
                case '\n':
                    outBuffer.append('\\');
                    outBuffer.append('n');
                    break;
                case '\r':
                    outBuffer.append('\\');
                    outBuffer.append('r');
                    break;
                case '\f':
                    outBuffer.append('\\');
                    outBuffer.append('f');
                    break;
                case '=': // Fall through
                case ':': // Fall through
                case '#': // Fall through
                case '!':
                    outBuffer.append('\\');
                    outBuffer.append(aChar);
                    break;
                default:
                    if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode) {
                        outBuffer.append("\\u");
                        outBuffer.append(hex.toHexDigits(aChar));
                    } else {
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }

    private static void writeComments(BufferedWriter bw, String comments)
        throws IOException {
        HexFormat hex = HexFormat.of().withUpperCase();
        bw.write("#");
        int len = comments.length();
        int current = 0;
        int last = 0;
        while (current < len) {
            char c = comments.charAt(current);
            if (c > '\u00ff' || c == '\n' || c == '\r') {
                if (last != current)
                    bw.write(comments.substring(last, current));
                if (c > '\u00ff') {
                    bw.write("\\u");
                    bw.write(hex.toHexDigits(c));
                } else {
                    bw.newLine();
                    if (c == '\r' &&
                        current != len - 1 &&
                        comments.charAt(current + 1) == '\n') {
                        current++;
                    }
                    if (current == len - 1 ||
                        (comments.charAt(current + 1) != '#' &&
                            comments.charAt(current + 1) != '!'))
                        bw.write("#");
                }
                last = current + 1;
            }
            current++;
        }
        if (last != current)
            bw.write(comments.substring(last, current));
        bw.newLine();
    }

    /**
     * Calls the {@code store(OutputStream out, String comments)} method
     * and suppresses IOExceptions that were thrown.
     *
     * @param out      an output stream.
     * @param comments a description of the property list.
     * @throws ClassCastException if this {@code Properties} object
     *                            contains any keys or values that are not
     *                            {@code Strings}.
     * @deprecated This method does not throw an IOException if an I/O error
     * occurs while saving the property list.  The preferred way to save a
     * properties list is via the {@code store(OutputStream out,
     * String comments)} method or the
     * {@code storeToXML(OutputStream os, String comment)} method.
     */
    @Deprecated
    public void save(OutputStream out, String comments) {
        try {
            store(out, comments);
        } catch (IOException ignored) {
        }
    }

    /**
     * Writes this property list (key and element pairs) in this
     * {@code Properties} table to the output character stream in a
     * format suitable for using the {@link #load(Reader) load(Reader)}
     * method.
     * <p>
     * Properties from the defaults table of this {@code Properties}
     * table (if any) are <i>not</i> written out by this method.
     * <p>
     * If the comments argument is not null, then an ASCII {@code #}
     * character, the comments string, and a line separator are first written
     * to the output stream. Thus, the {@code comments} can serve as an
     * identifying comment. Any one of a line feed ('\n'), a carriage
     * return ('\r'), or a carriage return followed immediately by a line feed
     * in comments is replaced by a line separator generated by the {@code Writer}
     * and if the next character in comments is not character {@code #} or
     * character {@code !} then an ASCII {@code #} is written out
     * after that line separator.
     * <p>
     * Next, a comment line is always written, consisting of an ASCII
     * {@code #} character, the current date and time (as if produced
     * by the {@code toString} method of {@code Date} for the
     * current time), and a line separator as generated by the {@code Writer}.
     * <p>
     * Then every entry in this {@code Properties} table is
     * written out, one per line. For each entry the key string is
     * written, then an ASCII {@code =}, then the associated
     * element string. For the key, all space characters are
     * written with a preceding {@code \} character.  For the
     * element, leading space characters, but not embedded or trailing
     * space characters, are written with a preceding {@code \}
     * character. The key and element characters {@code #},
     * {@code !}, {@code =}, and {@code :} are written
     * with a preceding backslash to ensure that they are properly loaded.
     * <p>
     * After the entries have been written, the output stream is flushed.
     * The output stream remains open after this method returns.
     *
     * @param writer   an output character stream writer.
     * @param comments a description of the property list.
     * @throws IOException          if writing this property list to the specified
     *                              output stream throws an {@code IOException}.
     * @throws ClassCastException   if this {@code Properties} object
     *                              contains any keys or values that are not {@code Strings}.
     * @throws NullPointerException if {@code writer} is null.
     * @since 1.6
     */
    public void store(Writer writer, String comments)
        throws IOException {
        store0((writer instanceof BufferedWriter) ? (BufferedWriter) writer
                : new BufferedWriter(writer),
            comments,
            false);
    }

    /**
     * Writes this property list (key and element pairs) in this
     * {@code Properties} table to the output stream in a format suitable
     * for loading into a {@code Properties} table using the
     * {@link #load(InputStream) load(InputStream)} method.
     * <p>
     * Properties from the defaults table of this {@code Properties}
     * table (if any) are <i>not</i> written out by this method.
     * <p>
     * This method outputs the comments, properties keys and values in
     * the same format as specified in
     * {@link #store(Writer, String) store(Writer)},
     * with the following differences:
     * <ul>
     * <li>The stream is written using the ISO 8859-1 character encoding.
     *
     * <li>Characters not in Latin-1 in the comments are written as
     * {@code \u005Cu}<i>xxxx</i> for their appropriate unicode
     * hexadecimal value <i>xxxx</i>.
     *
     * <li>Characters less than {@code \u005Cu0020} and characters greater
     * than {@code \u005Cu007E} in property keys or values are written
     * as {@code \u005Cu}<i>xxxx</i> for the appropriate hexadecimal
     * value <i>xxxx</i>.
     * </ul>
     * <p>
     * After the entries have been written, the output stream is flushed.
     * The output stream remains open after this method returns.
     *
     * @param out      an output stream.
     * @param comments a description of the property list.
     * @throws IOException          if writing this property list to the specified
     *                              output stream throws an {@code IOException}.
     * @throws ClassCastException   if this {@code Properties} object
     *                              contains any keys or values that are not {@code Strings}.
     * @throws NullPointerException if {@code out} is null.
     * @since 1.2
     */
    public void store(OutputStream out, String comments)
        throws IOException {
        store0(new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.ISO_8859_1)),
            comments,
            true);
    }

    private void store0(BufferedWriter bw, String comments, boolean escUnicode)
        throws IOException {
        if (comments != null) {
            writeComments(bw, comments);
        }
        bw.write("#" + new Date());
        bw.newLine();
        synchronized (this) {
            for (Map.Entry<String, String> e : map.entrySet()) {
                String key = e.getKey();
                String val = e.getValue();
                key = saveConvert(key, true, escUnicode);
                /* No need to escape embedded and trailing spaces for value, hence
                 * pass false to flag.
                 */
                val = saveConvert(val, false, escUnicode);
                bw.write(key + "=" + val);
                bw.newLine();
            }
        }
        bw.flush();
    }

    /**
     * Searches for the property with the specified key in this property list.
     * If the key is not found in this property list, the default property list,
     * and its defaults, recursively, are then checked. The method returns
     * {@code null} if the property is not found.
     *
     * @param key the property key.
     * @return the value in this property list with the specified key value.
     * @see #setProperty
     */
    public String getProperty(String key) {
        return map.get(key);
    }

    /**
     * Searches for the property with the specified key in this property list.
     * If the key is not found in this property list, the default property list,
     * and its defaults, recursively, are then checked. The method returns the
     * default value argument if the property is not found.
     *
     * @param key          the hashtable key.
     * @param defaultValue a default value.
     * @return the value in this property list with the specified key value.
     * @see #setProperty
     */
    public String getProperty(String key, String defaultValue) {
        String val = getProperty(key);
        return (val == null) ? defaultValue : val;
    }

    /**
     * Returns an unmodifiable set of keys from this property list
     * where the key and its corresponding value are strings,
     * including distinct keys in the default property list if a key
     * of the same name has not already been found from the main
     * properties list.  Properties whose key or value is not
     * of type {@code String} are omitted.
     * <p>
     * The returned set is not backed by this {@code Properties} object.
     * Changes to this {@code Properties} object are not reflected in the
     * returned set.
     *
     * @return an unmodifiable set of keys in this property list where
     * the key and its corresponding value are strings,
     * including the keys in the default property list.
     * @since 1.6
     */
    public Set<String> stringPropertyNames() {
        Map<String, String> h = new HashMap<>(map);
        return Collections.unmodifiableSet(h.keySet());
    }

    /**
     * Prints this property list out to the specified output stream.
     * This method is useful for debugging.
     *
     * @param out an output stream.
     * @throws ClassCastException if any key in this property list
     *                            is not a string.
     */
    public void list(PrintStream out) {
        out.println("-- listing properties --");
        Map<String, String> h = new HashMap<>(map);
        for (Map.Entry<String, String> e : h.entrySet()) {
            String key = e.getKey();
            String val = e.getValue();
            if (val.length() > 40) {
                val = val.substring(0, 37) + "...";
            }
            out.println(key + "=" + val);
        }
    }

    /**
     * Prints this property list out to the specified output stream.
     * This method is useful for debugging.
     *
     * @param out an output stream.
     * @throws ClassCastException if any key in this property list
     *                            is not a string.
     * @since 1.1
     */
    /*
     * Rather than use an anonymous inner class to share common code, this
     * method is duplicated in order to ensure that a non-1.1 compiler can
     * compile this file.
     */
    public void list(PrintWriter out) {
        out.println("-- listing properties --");
        Map<String, String> h = new HashMap<>(map);
        for (Map.Entry<String, String> e : h.entrySet()) {
            String key = e.getKey();
            String val = e.getValue();
            if (val.length() > 40) {
                val = val.substring(0, 37) + "...";
            }
            out.println(key + "=" + val);
        }
    }

    //
    // Hashtable methods overridden and delegated to a ConcurrentHashMap instance

    @Override
    public synchronized String toString() {
        return map.toString();
    }

    @Override
    public synchronized boolean equals(Object o) {
        return map.equals(o);
    }

    @Override
    public synchronized int hashCode() {
        return map.hashCode();
    }

}
