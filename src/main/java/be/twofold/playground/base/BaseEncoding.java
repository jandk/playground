package be.twofold.playground.base;

import be.twofold.playground.common.*;

import java.io.*;

public abstract class BaseEncoding {

    static final BaseEncoding BASE64 = new Base64Encoding("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", '=');
    static final BaseEncoding BASE64_URL = new Base64Encoding("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_", '=');
    static final BaseEncoding BASE32 = new StandardBaseEncoding("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567", '=');
    static final BaseEncoding BASE32_HEX = new StandardBaseEncoding("0123456789ABCDEFGHIJKLMNOPQRSTUV", '=');
    static final BaseEncoding BASE16 = new Base16Encoding("0123456789ABCDEF");

    BaseEncoding() {
    }

    static byte[] extract(byte[] result, int length) {
        if (length == result.length) {
            return result;
        }
        byte[] trunc = new byte[length];
        System.arraycopy(result, 0, trunc, 0, length);
        return trunc;
    }

    public static BaseEncoding base64() {
        return BASE64;
    }

    public static BaseEncoding base64Url() {
        return BASE64_URL;
    }

    public static BaseEncoding base32() {
        return BASE32;
    }

    public static BaseEncoding base32Hex() {
        return BASE32_HEX;
    }

    public static BaseEncoding base16() {
        return BASE16;
    }

    public String encode(byte[] bytes) {
        return encode(bytes, 0, bytes.length);
    }

    public final String encode(byte[] bytes, int off, int len) {
        Check.fromToIndex(off, off + len, bytes.length);
        StringBuilder result = new StringBuilder(maxEncodedSize(len));
        try {
            encodeTo(result, bytes, off, len);
        } catch (IOException impossible) {
            throw new AssertionError(impossible);
        }
        return result.toString();
    }

    public abstract boolean canDecode(CharSequence chars);

    public final byte[] decode(CharSequence chars) {
        try {
            return decodeChecked(chars);
        } catch (DecodingException badInput) {
            throw new IllegalArgumentException(badInput);
        }
    }

    final byte[] decodeChecked(CharSequence chars)
        throws DecodingException {
        chars = trimTrailingPadding(chars);
        byte[] tmp = new byte[maxDecodedSize(chars.length())];
        int len = decodeTo(tmp, chars);
        return extract(tmp, len);
    }

    abstract int maxEncodedSize(int bytes);

    abstract void encodeTo(Appendable target, byte[] bytes, int off, int len) throws IOException;

    abstract int maxDecodedSize(int chars);

    abstract int decodeTo(byte[] target, CharSequence chars) throws DecodingException;

    CharSequence trimTrailingPadding(CharSequence chars) {
        return Check.notNull(chars);
    }

    public abstract BaseEncoding omitPadding();

    public abstract BaseEncoding withPadChar(char padChar);

    public abstract BaseEncoding upperCase();

    public abstract BaseEncoding lowerCase();

    public abstract BaseEncoding ignoreCase();


    static int log2(int length) {
        if ((length & (length - 1)) != 0) {
            throw new ArithmeticException("mode was UNNECESSARY, but rounding was necessary");
        }
        return (Integer.SIZE - 1) - Integer.numberOfLeadingZeros(length);
    }

    static int divideCeil(int p, int q) {
        return (p + q - 1) / q;
    }

    static int divideFloor(int p, int q) {
        return p / q;
    }
}
