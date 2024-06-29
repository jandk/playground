package be.twofold.playground.base32;

import java.nio.charset.*;
import java.util.*;

public final class Base32 {

    private static final int MASK_8BITS = 0xff;
    private static final byte PAD = '='; // Allow static access to default
    private static final int EOF = -1;
    private static final int BYTES_PER_ENCODED_BLOCK = 8;

    /**
     * This array is a lookup table that translates Unicode characters drawn from the "Base32 Alphabet" (as specified
     * in Table 3 of RFC 4648) into their 5-bit positive integer equivalents. Characters that are not in the Base32
     * alphabet but fall within the bounds of the array are translated to -1.
     */
    private static final byte[] DECODE_TABLE = {
        //  0   1   2   3   4   5   6   7   8   9   A   B   C   D   E   F
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 00-0f
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 10-1f
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 20-2f
        -1, -1, 26, 27, 28, 29, 30, 31, -1, -1, -1, -1, -1, -1, -1, -1, // 30-3f 2-7
        -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, // 40-4f A-O
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,                     // 50-5a P-Z
        -1, -1, -1, -1, -1, // 5b-5f
        -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, // 60-6f a-o
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,                     // 70-7a p-z
    };

    /**
     * This array is a lookup table that translates 5-bit positive integer index values into their "Base32 Alphabet"
     * equivalents as specified in Table 3 of RFC 4648.
     */
    private static final byte[] ENCODE_TABLE = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        '2', '3', '4', '5', '6', '7',
    };

    private static final long MASK_4BITS = 0x0fL;
    private static final long MASK_3BITS = 0x07L;
    private static final long MASK_2BITS = 0x03L;
    private static final long MASK_1BITS = 0x01L;
    private static final int DEFAULT_BUFFER_RESIZE_FACTOR = 2;
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;

    // The static final fields above are used for the original static byte[] methods on Base32.
    // The private member fields below are used with the new streaming approach, which requires
    // some state be preserved between calls of encode() and decode().

    /**
     * Compares two {@code int} values numerically treating the values
     * as unsigned. Taken from JDK 1.8.
     *
     * <p>TODO: Replace with JDK 1.8 Integer::compareUnsigned(int, int).</p>
     *
     * @param x the first {@code int} to compare
     * @param y the second {@code int} to compare
     * @return the value {@code 0} if {@code x == y}; a value less
     * than {@code 0} if {@code x < y} as unsigned values; and
     * a value greater than {@code 0} if {@code x > y} as
     * unsigned values
     */
    private static int compareUnsigned(final int x, final int y) {
        return Integer.compare(x + Integer.MIN_VALUE, y + Integer.MIN_VALUE);
    }

    /**
     * Create a positive capacity at least as large the minimum required capacity.
     * If the minimum capacity is negative then this throws an OutOfMemoryError as no array
     * can be allocated.
     *
     * @param minCapacity the minimum capacity
     * @return the capacity
     * @throws OutOfMemoryError if the {@code minCapacity} is negative
     */
    private static int createPositiveCapacity(final int minCapacity) {
        if (minCapacity < 0) {
            // overflow
            throw new OutOfMemoryError("Unable to allocate array size: " + (minCapacity & 0xffffffffL));
        }
        // This is called when we require buffer expansion to a very big array.
        // Use the conservative maximum buffer size if possible, otherwise the biggest required.
        //
        // Note: In this situation JDK 1.8 java.util.ArrayList returns Integer.MAX_VALUE.
        // This excludes some VMs that can exceed MAX_BUFFER_SIZE but not allocate a full
        // Integer.MAX_VALUE length array.
        // The result is that we may have to allocate an array of this size more than once if
        // the capacity must be expanded again.
        return Math.max(minCapacity, MAX_BUFFER_SIZE);
    }

    /**
     * Increases our buffer by the {@link #DEFAULT_BUFFER_RESIZE_FACTOR}.
     *
     * @param context     the context to be used
     * @param minCapacity the minimum required capacity
     * @return the resized byte[] buffer
     * @throws OutOfMemoryError if the {@code minCapacity} is negative
     */
    private static byte[] resizeBuffer(final Context context, final int minCapacity) {
        // Overflow-conscious code treats the min and new capacity as unsigned.
        final int oldCapacity = context.buffer.length;
        int newCapacity = oldCapacity * DEFAULT_BUFFER_RESIZE_FACTOR;
        if (compareUnsigned(newCapacity, minCapacity) < 0) {
            newCapacity = minCapacity;
        }
        if (compareUnsigned(newCapacity, MAX_BUFFER_SIZE) > 0) {
            newCapacity = createPositiveCapacity(minCapacity);
        }

        final byte[] b = new byte[newCapacity];
        System.arraycopy(context.buffer, 0, b, 0, context.buffer.length);
        context.buffer = b;
        return b;
    }

    /**
     * <p>
     * Decodes all of the provided data, starting at inPos, for inAvail bytes. Should be called at least twice: once
     * with the data to decode, and once with inAvail set to "-1" to alert decoder that EOF has been reached. The "-1"
     * call is not necessary when decoding, but it doesn't hurt, either.
     * </p>
     * <p>
     * Ignores all non-Base32 characters. This is how chunked (e.g. 76 character) data is handled, since CR and LF are
     * silently ignored, but has implications for other bytes, too. This method subscribes to the garbage-in,
     * garbage-out philosophy: it will not check the provided data for validity.
     * </p>
     * <p>
     *
     * @param input   byte[] array of ascii data to Base32 decode.
     * @param inPos   Position to start reading data from.
     * @param inAvail Amount of bytes available from input for decoding.
     * @param context the context to be used
     */
    void decode(final byte[] input, int inPos, final int inAvail, final Context context) {
        // package protected for access from I/O streams

        if (context.eof) {
            return;
        }
        if (inAvail < 0) {
            context.eof = true;
        }
        int decodeSize = BYTES_PER_ENCODED_BLOCK - 1;
        for (int i = 0; i < inAvail; i++) {
            final byte b = input[inPos++];
            if (b == PAD) {
                // We're done.
                context.eof = true;
                break;
            }
            final byte[] buffer = ensureBufferSize(decodeSize, context);
            if (b >= 0 && b < DECODE_TABLE.length) {
                final int result = DECODE_TABLE[b];
                if (result >= 0) {
                    context.modulus = (context.modulus + 1) % BYTES_PER_ENCODED_BLOCK;
                    // collect decoded bytes
                    context.lbitWorkArea = (context.lbitWorkArea << 5) + result;
                    if (context.modulus == 0) { // we can output the 5 bytes
                        buffer[context.pos++] = (byte) ((context.lbitWorkArea >> 32) & MASK_8BITS);
                        buffer[context.pos++] = (byte) ((context.lbitWorkArea >> 24) & MASK_8BITS);
                        buffer[context.pos++] = (byte) ((context.lbitWorkArea >> 16) & MASK_8BITS);
                        buffer[context.pos++] = (byte) ((context.lbitWorkArea >> 8) & MASK_8BITS);
                        buffer[context.pos++] = (byte) (context.lbitWorkArea & MASK_8BITS);
                    }
                }
            }
        }

        // Two forms of EOF as far as Base32 decoder is concerned: actual
        // EOF (-1) and first time '=' character is encountered in stream.
        // This approach makes the '=' padding characters completely optional.
        if (context.eof && context.modulus > 0) { // if modulus == 0, nothing to do
            final byte[] buffer = ensureBufferSize(decodeSize, context);

            // We ignore partial bytes, i.e. only multiples of 8 count.
            // Any combination not part of a valid encoding is either partially decoded
            // or will raise an exception. Possible trailing characters are 2, 4, 5, 7.
            // It is not possible to encode with 1, 3, 6 trailing characters.
            // For backwards compatibility 3 & 6 chars are decoded anyway rather than discarded.
            // See the encode(byte[]) method EOF section.
            switch (context.modulus) {
//              case 0 : // impossible, as excluded above
                case 1: // 5 bits - either ignore entirely, or raise an exception
                    validateTrailingCharacters();
                case 2: // 10 bits, drop 2 and output one byte
                    validateCharacter(MASK_2BITS, context);
                    buffer[context.pos++] = (byte) ((context.lbitWorkArea >> 2) & MASK_8BITS);
                    break;
                case 3: // 15 bits, drop 7 and output 1 byte, or raise an exception
                    validateTrailingCharacters();
                    // Not possible from a valid encoding but decode anyway
                    buffer[context.pos++] = (byte) ((context.lbitWorkArea >> 7) & MASK_8BITS);
                    break;
                case 4: // 20 bits = 2*8 + 4
                    validateCharacter(MASK_4BITS, context);
                    context.lbitWorkArea = context.lbitWorkArea >> 4; // drop 4 bits
                    buffer[context.pos++] = (byte) ((context.lbitWorkArea >> 8) & MASK_8BITS);
                    buffer[context.pos++] = (byte) ((context.lbitWorkArea) & MASK_8BITS);
                    break;
                case 5: // 25 bits = 3*8 + 1
                    validateCharacter(MASK_1BITS, context);
                    context.lbitWorkArea = context.lbitWorkArea >> 1;
                    buffer[context.pos++] = (byte) ((context.lbitWorkArea >> 16) & MASK_8BITS);
                    buffer[context.pos++] = (byte) ((context.lbitWorkArea >> 8) & MASK_8BITS);
                    buffer[context.pos++] = (byte) ((context.lbitWorkArea) & MASK_8BITS);
                    break;
                case 6: // 30 bits = 3*8 + 6, or raise an exception
                    validateTrailingCharacters();
                    // Not possible from a valid encoding but decode anyway
                    context.lbitWorkArea = context.lbitWorkArea >> 6;
                    buffer[context.pos++] = (byte) ((context.lbitWorkArea >> 16) & MASK_8BITS);
                    buffer[context.pos++] = (byte) ((context.lbitWorkArea >> 8) & MASK_8BITS);
                    buffer[context.pos++] = (byte) ((context.lbitWorkArea) & MASK_8BITS);
                    break;
                case 7: // 35 bits = 4*8 +3
                    validateCharacter(MASK_3BITS, context);
                    context.lbitWorkArea = context.lbitWorkArea >> 3;
                    buffer[context.pos++] = (byte) ((context.lbitWorkArea >> 24) & MASK_8BITS);
                    buffer[context.pos++] = (byte) ((context.lbitWorkArea >> 16) & MASK_8BITS);
                    buffer[context.pos++] = (byte) ((context.lbitWorkArea >> 8) & MASK_8BITS);
                    buffer[context.pos++] = (byte) ((context.lbitWorkArea) & MASK_8BITS);
                    break;
                default:
                    // modulus can be 0-7, and we excluded 0,1 already
                    throw new IllegalStateException("Impossible modulus " + context.modulus);
            }
        }
    }

    /**
     * <p>
     * Encodes all of the provided data, starting at inPos, for inAvail bytes. Must be called at least twice: once with
     * the data to encode, and once with inAvail set to "-1" to alert encoder that EOF has been reached, so flush last
     * remaining bytes (if not multiple of 5).
     * </p>
     *
     * @param input   byte[] array of binary data to Base32 encode.
     * @param inPos   Position to start reading data from.
     * @param inAvail Amount of bytes available from input for encoding.
     * @param context the context to be used
     */
    void encode(final byte[] input, int inPos, final int inAvail, final Context context) {
        // package protected for access from I/O streams

        if (context.eof) {
            return;
        }
        // inAvail < 0 is how we're informed of EOF in the underlying data we're
        // encoding.
        /**
         * Convenience variable to help us determine when our buffer is going to run out of room and needs resizing.
         * {@code encodeSize = {@link #BYTES_PER_ENCODED_BLOCK} + lineSeparator.length;}
         */
        int encodeSize = BYTES_PER_ENCODED_BLOCK;
        if (inAvail < 0) {
            context.eof = true;
            if (0 == context.modulus) {
                return; // no leftovers to process and not using chunking
            }
            final byte[] buffer = ensureBufferSize(encodeSize, context);
            final int savedPos = context.pos;
            switch (context.modulus) { // % 5
                case 0:
                    break;
                case 1: // Only 1 octet; take top 5 bits then remainder
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea >> 3) & 0x1f]; // 8-1*5 = 3
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea << 2) & 0x1f]; // 5-3=2
                    buffer[context.pos++] = PAD;
                    buffer[context.pos++] = PAD;
                    buffer[context.pos++] = PAD;
                    buffer[context.pos++] = PAD;
                    buffer[context.pos++] = PAD;
                    buffer[context.pos++] = PAD;
                    break;
                case 2: // 2 octets = 16 bits to use
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea >> 11) & 0x1f]; // 16-1*5 = 11
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea >> 6) & 0x1f]; // 16-2*5 = 6
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea >> 1) & 0x1f]; // 16-3*5 = 1
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea << 4) & 0x1f]; // 5-1 = 4
                    buffer[context.pos++] = PAD;
                    buffer[context.pos++] = PAD;
                    buffer[context.pos++] = PAD;
                    buffer[context.pos++] = PAD;
                    break;
                case 3: // 3 octets = 24 bits to use
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea >> 19) & 0x1f]; // 24-1*5 = 19
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea >> 14) & 0x1f]; // 24-2*5 = 14
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea >> 9) & 0x1f]; // 24-3*5 = 9
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea >> 4) & 0x1f]; // 24-4*5 = 4
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea << 1) & 0x1f]; // 5-4 = 1
                    buffer[context.pos++] = PAD;
                    buffer[context.pos++] = PAD;
                    buffer[context.pos++] = PAD;
                    break;
                case 4: // 4 octets = 32 bits to use
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea >> 27) & 0x1f]; // 32-1*5 = 27
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea >> 22) & 0x1f]; // 32-2*5 = 22
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea >> 17) & 0x1f]; // 32-3*5 = 17
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea >> 12) & 0x1f]; // 32-4*5 = 12
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea >> 7) & 0x1f]; // 32-5*5 =  7
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea >> 2) & 0x1f]; // 32-6*5 =  2
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea << 3) & 0x1f]; // 5-2 = 3
                    buffer[context.pos++] = PAD;
                    break;
                default:
                    throw new IllegalStateException("Impossible modulus " + context.modulus);
            }
            context.currentLinePos += context.pos - savedPos; // keep track of current line position
            // if currentPos == 0 we are at the start of a line, so don't add CRLF
        } else {
            for (int i = 0; i < inAvail; i++) {
                final byte[] buffer = ensureBufferSize(encodeSize, context);
                context.modulus = (context.modulus + 1) % 5;
                int b = input[inPos++];
                if (b < 0) {
                    b += 256;
                }
                context.lbitWorkArea = (context.lbitWorkArea << 8) + b; // BITS_PER_BYTE
                if (0 == context.modulus) { // we have enough bytes to create our output
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea >> 35) & 0x1f];
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea >> 30) & 0x1f];
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea >> 25) & 0x1f];
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea >> 20) & 0x1f];
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea >> 15) & 0x1f];
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea >> 10) & 0x1f];
                    buffer[context.pos++] = ENCODE_TABLE[(int) (context.lbitWorkArea >> 5) & 0x1f];
                    buffer[context.pos++] = ENCODE_TABLE[(int) context.lbitWorkArea & 0x1f];
                    context.currentLinePos += BYTES_PER_ENCODED_BLOCK;
                }
            }
        }
    }

    /**
     * Validates whether decoding the final trailing character is possible in the context
     * of the set of possible base 32 values.
     *
     * <p>The character is valid if the lower bits within the provided mask are zero. This
     * is used to test the final trailing base-32 digit is zero in the bits that will be discarded.
     *
     * @param emptyBitsMask The mask of the lower bits that should be empty
     * @param context       the context to be used
     * @throws IllegalArgumentException if the bits being checked contain any non-zero value
     */
    private void validateCharacter(final long emptyBitsMask, final Context context) {
        // Use the long bit work area
        if (isStrictDecoding() && (context.lbitWorkArea & emptyBitsMask) != 0) {
            throw new IllegalArgumentException(
                "Strict decoding: Last encoded character (before the paddings if any) is a valid base 32 alphabet but not a possible encoding. " +
                    "Expected the discarded bits from the character to be zero.");
        }
    }

    /**
     * Validates whether decoding allows final trailing characters that cannot be
     * created during encoding.
     *
     * @throws IllegalArgumentException if strict decoding is enabled
     */
    private void validateTrailingCharacters() {
        if (isStrictDecoding()) {
            throw new IllegalArgumentException(
                "Strict decoding: Last encoded character(s) (before the paddings if any) are valid base 32 alphabet but not a possible encoding. " +
                    "Decoding requires either 2, 4, 5, or 7 trailing 5-bit characters to create bytes.");
        }
    }

    /**
     * Returns the amount of buffered data available for reading.
     *
     * @param context the context to be used
     * @return The amount of buffered data available for reading.
     */
    int available(final Context context) {  // package protected for access from I/O streams
        return context.buffer != null ? context.pos - context.readPos : 0;
    }

    /**
     * Decodes a byte[] containing characters in the Base-N alphabet.
     *
     * @param pArray A byte array containing Base-N character data
     * @return a byte array containing binary data
     */
    public byte[] decode(final byte[] pArray) {
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        final Context context = new Context();
        decode(pArray, 0, pArray.length, context);
        decode(pArray, 0, EOF, context); // Notify decoder of EOF.
        final byte[] result = new byte[context.pos];
        readResults(result, 0, result.length, context);
        return result;
    }

    /**
     * Decodes a String containing characters in the Base-N alphabet.
     *
     * @param pArray A String containing Base-N character data
     * @return a byte array containing binary data
     */
    public byte[] decode(final String pArray) {
        return decode(pArray.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encodes a byte[] containing binary data, into a byte[] containing characters in the alphabet.
     *
     * @param pArray a byte array containing binary data
     * @return A byte array containing only the base N alphabetic character data
     */
    public byte[] encode(final byte[] pArray) {
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        return encode(pArray, 0, pArray.length);
    }

    /**
     * Encodes a byte[] containing binary data, into a byte[] containing
     * characters in the alphabet.
     *
     * @param pArray a byte array containing binary data
     * @param offset initial offset of the subarray.
     * @param length length of the subarray.
     * @return A byte array containing only the base N alphabetic character data
     * @since 1.11
     */
    public byte[] encode(final byte[] pArray, final int offset, final int length) {
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        final Context context = new Context();
        encode(pArray, offset, length, context);
        encode(pArray, offset, EOF, context); // Notify encoder of EOF.
        final byte[] buf = new byte[context.pos - context.readPos];
        readResults(buf, 0, buf.length, context);
        return buf;
    }

    /**
     * Encodes a byte[] containing binary data, into a String containing characters in the Base-N alphabet.
     * Uses UTF8 encoding.
     *
     * @param pArray a byte array containing binary data
     * @return A String containing only Base-N character data
     */
    public String encodeToString(final byte[] pArray) {
        return new String(encode(pArray), StandardCharsets.UTF_8);
    }

    /**
     * Ensure that the buffer has room for {@code size} bytes
     *
     * @param size    minimum spare space required
     * @param context the context to be used
     * @return the buffer
     */
    private byte[] ensureBufferSize(final int size, final Context context) {
        if (context.buffer == null) {
            context.buffer = new byte[Math.max(size, DEFAULT_BUFFER_SIZE)];
            context.pos = 0;
            context.readPos = 0;

            // Overflow-conscious:
            // x + y > z  ==  x + y - z > 0
        } else if (context.pos + size - context.buffer.length > 0) {
            return resizeBuffer(context, context.pos + size);
        }
        return context.buffer;
    }

    /**
     * Calculates the amount of space needed to encode the supplied array.
     *
     * @param pArray byte[] array which will later be encoded
     * @return amount of space needed to encoded the supplied array.
     * Returns a long since a max-len array will require &gt; Integer.MAX_VALUE
     */
    public long getEncodedLength(final byte[] pArray) {
        // Calculate non-chunked size - rounded up to allow for padding
        // cast to long is needed to avoid possibility of overflow
        return ((pArray.length + 4) / 5) * (long) BYTES_PER_ENCODED_BLOCK;
    }

    /**
     * Returns true if decoding behavior is strict. Decoding will raise an {@link IllegalArgumentException} if trailing
     * bits are not part of a valid encoding.
     *
     * <p>
     * The default is false for lenient decoding. Decoding will compose trailing bits into 8-bit bytes and discard the
     * remainder.
     * </p>
     *
     * @return true if using strict decoding
     * @since 1.15
     */
    public boolean isStrictDecoding() {
        return true;
    }

    /**
     * Extracts buffered data into the provided byte[] array, starting at position bPos, up to a maximum of bAvail
     * bytes. Returns how many bytes were actually extracted.
     * <p>
     * Package protected for access from I/O streams.
     *
     * @param b       byte[] array to extract the buffered data into.
     * @param bPos    position in byte[] array to start extraction at.
     * @param bAvail  amount of bytes we're allowed to extract. We may extract fewer (if fewer are available).
     * @param context the context to be used
     * @return The number of bytes successfully extracted into the provided byte[] array.
     */
    int readResults(final byte[] b, final int bPos, final int bAvail, final Context context) {
        if (context.buffer != null) {
            final int len = Math.min(available(context), bAvail);
            System.arraycopy(context.buffer, context.readPos, b, bPos, len);
            context.readPos += len;
            if (context.readPos >= context.pos) {
                context.buffer = null; // so hasData() will return false, and this method can return -1
            }
            return len;
        }
        return context.eof ? EOF : 0;
    }

    /**
     * Holds thread context so classes can be thread-safe.
     * <p>
     * This class is not itself thread-safe; each thread must allocate its own copy.
     *
     * @since 1.7
     */
    static class Context {

        /**
         * Place holder for the bytes we're dealing with for our based logic.
         * Bitwise operations store and extract the encoding or decoding from this variable.
         */
        int ibitWorkArea;

        /**
         * Place holder for the bytes we're dealing with for our based logic.
         * Bitwise operations store and extract the encoding or decoding from this variable.
         */
        long lbitWorkArea;

        /**
         * Buffer for streaming.
         */
        byte[] buffer;

        /**
         * Position where next character should be written in the buffer.
         */
        int pos;

        /**
         * Position where next character should be read from the buffer.
         */
        int readPos;

        /**
         * Boolean flag to indicate the EOF has been reached. Once EOF has been reached, this object becomes useless,
         * and must be thrown away.
         */
        boolean eof;

        /**
         * Variable tracks how many characters have been written to the current line. Only used when encoding. We use
         * it to make sure each encoded line never goes beyond lineLength (if lineLength &gt; 0).
         */
        int currentLinePos;

        /**
         * Writes to the buffer only occur after every 3/5 reads when encoding, and every 4/8 reads when decoding. This
         * variable helps track that.
         */
        int modulus;

        Context() {
        }

        /**
         * Returns a String useful for debugging (especially within a debugger.)
         *
         * @return a String useful for debugging.
         */
        @SuppressWarnings("boxing") // OK to ignore boxing here
        @Override
        public String toString() {
            return String.format("%s[buffer=%s, currentLinePos=%s, eof=%s, ibitWorkArea=%s, lbitWorkArea=%s, " +
                    "modulus=%s, pos=%s, readPos=%s]", this.getClass().getSimpleName(), Arrays.toString(buffer),
                currentLinePos, eof, ibitWorkArea, lbitWorkArea, modulus, pos, readPos);
        }
    }
}
