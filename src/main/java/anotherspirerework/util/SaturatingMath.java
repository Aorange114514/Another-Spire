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

    /**
     * The total damage a Poison stack deals when its tick is repeated {@code extraTicks} more times.
     *
     * <p>Every tick removes one layer, so a stack of 7 with two extra ticks deals 7 + 6 + 5 and ends
     * at 4. Layers that have already run out contribute nothing.
     */
    public static int poisonPreview(int amount, int extraTicks) {
        int total = amount;
        for (int i = 1; i <= extraTicks; i++) {
            total += Math.max(0, amount - i);
        }
        return total;
    }
}