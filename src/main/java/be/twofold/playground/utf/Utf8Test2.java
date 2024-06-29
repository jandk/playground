package be.twofold.playground.utf;

import java.nio.charset.*;
import java.util.*;

public class Utf8Test2 {

    public static void main(String[] args) {
//        for (int i = 0x00; i <= 0x7f; i++) {
//        }
        for (int cp = 0x0080; cp <= 0x07FF; cp++) {
            byte[] array = Character.toString(cp).getBytes(StandardCharsets.UTF_8);
            printArray(array);
        }
        for (int cp = 0x0800; cp <= 0xFFFF; cp++) {
            if (Character.isSurrogate((char) cp)) {
                continue;
            }
        }
        for (int cp = 0x10000; cp <= 0x10FFFF; cp++) {
        }
    }

    private static void printArray(byte[] array) {
        String hex = "0123456789abcdef";
        int length = array.length * 3 - 1;
        char[] chars = new char[length];
        Arrays.fill(chars, ' ');
        for (int i = 0; i < array.length; i++) {
            chars[i * 3 + 0] = hex.charAt((array[i] & 0xf0) >>> 4);
            chars[i * 3 + 1] = hex.charAt((array[i] & 0x0f));
        }
        System.out.println(chars);
    }

}
