package anotherspirerework.util;

/** Integer arithmetic used by the uncapped-value patches.
 *
 * <p>The game stores block, energy and power amounts in {@code int} fields.  Keeping the
 * arithmetic here makes the overflow policy explicit: values outside that range remain at the
 * nearest representable value instead of wrapping around.</p>
 */
public final class SaturatingMath {
    private SaturatingMath() {
    }

    public static int fromLong(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) value;
    }

    public static int add(int left, int right) {
        return fromLong((long) left + right);
    }

    public static int subtract(int left, int right) {
        return fromLong((long) left - right);
    }

    public static int multiply(int left, int right) {
        return fromLong((long) left * right);
    }
}