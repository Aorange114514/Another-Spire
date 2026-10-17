package anotherspirerework.patches;

import anotherspirerework.powers.SadisticNaturePower;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.powers.WeakPower;

/**
 * Sadistic Nature: the Weak and Vulnerable sitting on enemies count twice.
 *
 * <p>Both powers work by scaling the damage in their own hook, so the doubling is applied to what
 * they returned. Weak subtracts a share of the damage ({@code result = damage * factor}), so the
 * doubled share is worked out from the damage itself; Vulnerable simply multiplies, so doubling the
 * result is the doubled effect. Paper Crane and Paper Frog are read by the base game before either
 * hook returns, which is why they stack with this instead of being overwritten.
 */
public class SadisticNaturePatch {

    @SpirePatch(clz = WeakPower.class, method = "atDamageGive")
    public static class Weak {
        @SpirePostfixPatch
        public static float after(float __result, WeakPower __instance, float damage, DamageInfo.DamageType type) {
            if (!doubles(__instance.owner, type) || __result <= 0.0F || __result >= damage) {
                return __result;
            }
            return __result * 2.0F - damage;
        }
    }

    @SpirePatch(clz = VulnerablePower.class, method = "atDamageReceive")
    public static class Vulnerable {
        @SpirePostfixPatch
        public static float after(float __result, VulnerablePower __instance, float damage, DamageInfo.DamageType type) {
            if (!doubles(__instance.owner, type)) {
                return __result;
            }
            return __result * 2.0F;
        }
    }

    /** Only enemy debuffs, and only the normal damage the base game scales. Package private so the
     *  two hooks above reach it directly rather than through a javac bridge. */
    static boolean doubles(com.megacrit.cardcrawl.core.AbstractCreature owner, DamageInfo.DamageType type) {
        return type == DamageInfo.DamageType.NORMAL && owner != null && !owner.isPlayer && SadisticNaturePower.active();
    }
}
