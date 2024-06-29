package be.twofold.playground;

class Solution {
    public int myAtoi(String s) {
        int i = 0;
        while (i < s.length() && s.charAt(i) == ' ') {
            i++;
        }
        if (i == s.length()) {
            return 0;
        }

        boolean negative = false;
        if (s.charAt(i) == '-') {
            negative = true;
            i++;
        } else if (s.charAt(i) == '+') {
            i++;
        }

        long result = 0;
        while (i < s.length() && Character.isDigit(s.charAt(i))) {
            result = result * 10 + (s.charAt(i) - '0');
            if (result > Integer.MAX_VALUE) {
                return negative ? Integer.MIN_VALUE : Integer.MAX_VALUE;
            }
            i++;
        }

        return (int) (negative ? -result : result);
    }
}
