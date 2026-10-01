package anotherspire.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.actions.unique.TriplePoisonAction;
import com.megacrit.cardcrawl.powers.AbstractPower;

/**
 * Catalyst+: the action computes "poison.amount * 2" as the amount to add, in plain int math and
 * before any power patch ever sees the number, so an enemy sitting on more than ~1.07 billion Poison
 * would make that multiply wrap into a negative (and the Poison would drop instead of triple).
 *
 * Saturating the current amount first keeps the tripling inside the int range, which is the same
 * "over the limit means at the limit" rule the other patches follow. The base Catalyst
 * (DoublePoisonAction) adds the current amount without multiplying, so it needs no help.
 */
@SpirePatch(clz = TriplePoisonAction.class, method = "update")
public class TriplePoisonSaturatePatch {
    /** Integer.MAX_VALUE / 3: the largest Poison whose tripling still fits in an int. */
    private static final int MAX_TRIPLE_POISON = Integer.MAX_VALUE / 3;

    @SpirePrefixPatch
    public static void Prefix(TriplePoisonAction __instance) {
        if (__instance.target == null) {
            return;
        }
        AbstractPower poison = __instance.target.getPower("Poison");
        if (poison != null && poison.amount > MAX_TRIPLE_POISON) {
            poison.amount = MAX_TRIPLE_POISON;
            poison.updateDescription();
        }
    }
}
