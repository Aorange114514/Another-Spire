package anotherspirerework.util;

import java.util.Random;

/** Dependency-free regression test, executed by Maven's test phase. */
public final class SaturatingMathTest {
    private static int checks;

    public static void main(String[] args) {
        int[] edges = {Integer.MIN_VALUE, Integer.MIN_VALUE + 1, -9999, -1, 0, 1,
                999, 9999, Integer.MAX_VALUE - 1, Integer.MAX_VALUE};
        for (int left : edges) {
            for (int right : edges) {
                checkPair(left, right);
            }
        }
        Random random = new Random(20261001L);
        for (int i = 0; i < 10000; i++) {
            checkPair(random.nextInt(), random.nextInt());
        }
        equal(Integer.MAX_VALUE, SaturatingMath.fromLong(Long.MAX_VALUE));
        equal(Integer.MIN_VALUE, SaturatingMath.fromLong(Long.MIN_VALUE));
        System.out.println("SaturatingMath: " + checks + " checks passed.");
    }

    private static void checkPair(int left, int right) {
        equal(expected((long) left + right), SaturatingMath.add(left, right));
        equal(expected((long) left - right), SaturatingMath.subtract(left, right));
        equal(expected((long) left * right), SaturatingMath.multiply(left, right));
    }

    private static int expected(long value) {
        return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, value));
    }

    private static void equal(int expected, int actual) {
        checks++;
        if (expected != actual) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
    }
}