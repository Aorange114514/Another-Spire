package anotherspirerework.patches;

import anotherspirerework.util.SaturatingMath;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.CollectPower;
import com.megacrit.cardcrawl.powers.DexterityPower;
import com.megacrit.cardcrawl.powers.EnergizedBluePower;
import com.megacrit.cardcrawl.powers.EnergizedPower;
import com.megacrit.cardcrawl.powers.FocusPower;
import com.megacrit.cardcrawl.powers.GainStrengthPower;
import com.megacrit.cardcrawl.powers.PlatedArmorPower;
import com.megacrit.cardcrawl.powers.PoisonPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.powers.watcher.LikeWaterPower;

/**
 * The base game clamps a long list of power amounts to +-999 (Poison to 9999). These patches lift
 * every one of those clamps to the widest value an int can hold. The game stores amounts in int
 * fields (AbstractPower.amount), so int is the widest that is actually reachable; going to long
 * would mean rewriting the field, every formula that reads it and every save file.
 *
 * The trick is the same everywhere: a Prefix works out what the amount should have been, and a
 * Postfix puts that value back when (and only when) the base game's clamp actually changed it. No
 * part of the original code is copied or replaced, so other patches on these methods still run.
 */
public class NoPowerCapPatch {
    /** The cap the base game uses for almost every power. */
    private static final int CAP = 999;
    /** Poison is the odd one out and uses 9999. */
    private static final int POISON_CAP = 9999;

    /** The amount the base game should end up with. The game runs one of these at a time. */
    private static int wanted;

    /** Pre-value and delta of a plain "amount += ...", used to spot int overflow after the fact. */
    private static int stackedBefore;
    private static int stackedDelta;

    /** Saturates into the int range instead of wrapping around. */
    public static int saturate(long value) {
        return SaturatingMath.fromLong(value);
    }

    private static void wantStack(AbstractPower power, int stackAmount) {
        wanted = SaturatingMath.add(power.amount, stackAmount);
    }

    private static void wantReduce(AbstractPower power, int reduceAmount) {
        wanted = SaturatingMath.subtract(power.amount, reduceAmount);
    }

    private static void wantCreate(int amount) {
        wanted = saturate(amount);
    }

    /** Undoes the clamp when the base game actually hit it, and refreshes the power tooltip. */
    private static void restore(AbstractPower power, int cap) {
        if (power.amount != wanted && (power.amount == cap || power.amount == -cap)) {
            power.amount = wanted;
            power.updateDescription();
        }
    }

    /**
     * Plain "amount += stackAmount" arithmetic happens in every power, including the ones the base
     * game never clamped. A huge amount plus another buff can wrap around into a negative one, so
     * this checks the addition (the sign check is an exact overflow test for int addition) and
     * saturates instead of letting the amount flip.
     */
    @SpirePatch(clz = AbstractPower.class, method = "stackPower", paramtypez = {int.class})
    public static class EveryPowerStacked {
        @SpirePrefixPatch
        public static void Prefix(AbstractPower __instance, int stackAmount) {
            stackedBefore = __instance.amount;
            stackedDelta = stackAmount;
        }

        @SpirePostfixPatch
        public static void Postfix(AbstractPower __instance) {
            boolean overflowed = stackedDelta > 0
                    ? __instance.amount < stackedBefore
                    : stackedDelta < 0 && __instance.amount > stackedBefore;
            if (overflowed) {
                __instance.amount = saturate((long) stackedBefore + stackedDelta);
                __instance.updateDescription();
            }
        }
    }

    /** Strength: clamped in the constructor, stackPower and reducePower. */
    @SpirePatch(clz = StrengthPower.class, method = SpirePatch.CONSTRUCTOR)
    public static class StrengthCreated {
        @SpirePrefixPatch
        public static void Prefix(StrengthPower __instance, AbstractCreature owner, int amount) {
            wantCreate(amount);
        }

        @SpirePostfixPatch
        public static void Postfix(StrengthPower __instance) {
            restore(__instance, CAP);
        }
    }

    @SpirePatch(clz = StrengthPower.class, method = "stackPower", paramtypez = {int.class})
    public static class StrengthStacked {
        @SpirePrefixPatch
        public static void Prefix(AbstractPower __instance, int stackAmount) {
            wantStack(__instance, stackAmount);
        }

        @SpirePostfixPatch
        public static void Postfix(AbstractPower __instance) {
            restore(__instance, CAP);
        }
    }

    @SpirePatch(clz = StrengthPower.class, method = "reducePower", paramtypez = {int.class})
    public static class StrengthReduced {
        @SpirePrefixPatch
        public static void Prefix(AbstractPower __instance, int reduceAmount) {
            wantReduce(__instance, reduceAmount);
        }

        @SpirePostfixPatch
        public static void Postfix(AbstractPower __instance) {
            restore(__instance, CAP);
        }
    }

    /** Dexterity: clamped in the constructor, stackPower and reducePower. */
    @SpirePatch(clz = DexterityPower.class, method = SpirePatch.CONSTRUCTOR)
    public static class DexterityCreated {
        @SpirePrefixPatch
        public static void Prefix(DexterityPower __instance, AbstractCreature owner, int amount) {
            wantCreate(amount);
        }

        @SpirePostfixPatch
        public static void Postfix(DexterityPower __instance) {
            restore(__instance, CAP);
        }
    }

    @SpirePatch(clz = DexterityPower.class, method = "stackPower", paramtypez = {int.class})
    public static class DexterityStacked {
        @SpirePrefixPatch
        public static void Prefix(AbstractPower __instance, int stackAmount) {
            wantStack(__instance, stackAmount);
        }

        @SpirePostfixPatch
        public static void Postfix(AbstractPower __instance) {
            restore(__instance, CAP);
        }
    }

    @SpirePatch(clz = DexterityPower.class, method = "reducePower", paramtypez = {int.class})
    public static class DexterityReduced {
        @SpirePrefixPatch
        public static void Prefix(AbstractPower __instance, int reduceAmount) {
            wantReduce(__instance, reduceAmount);
        }

        @SpirePostfixPatch
        public static void Postfix(AbstractPower __instance) {
            restore(__instance, CAP);
        }
    }

    /** Focus: clamped in the constructor, stackPower and reducePower. */
    @SpirePatch(clz = FocusPower.class, method = SpirePatch.CONSTRUCTOR)
    public static class FocusCreated {
        @SpirePrefixPatch
        public static void Prefix(FocusPower __instance, AbstractCreature owner, int amount) {
            wantCreate(amount);
        }

        @SpirePostfixPatch
        public static void Postfix(FocusPower __instance) {
            restore(__instance, CAP);
        }
    }

    @SpirePatch(clz = FocusPower.class, method = "stackPower", paramtypez = {int.class})
    public static class FocusStacked {
        @SpirePrefixPatch
        public static void Prefix(AbstractPower __instance, int stackAmount) {
            wantStack(__instance, stackAmount);
        }

        @SpirePostfixPatch
        public static void Postfix(AbstractPower __instance) {
            restore(__instance, CAP);
        }
    }

    @SpirePatch(clz = FocusPower.class, method = "reducePower", paramtypez = {int.class})
    public static class FocusReduced {
        @SpirePrefixPatch
        public static void Prefix(AbstractPower __instance, int reduceAmount) {
            wantReduce(__instance, reduceAmount);
        }

        @SpirePostfixPatch
        public static void Postfix(AbstractPower __instance) {
            restore(__instance, CAP);
        }
    }

    /** Shackled (the Strength held back until the end of the turn): same three clamps. */
    @SpirePatch(clz = GainStrengthPower.class, method = SpirePatch.CONSTRUCTOR)
    public static class ShackledCreated {
        @SpirePrefixPatch
        public static void Prefix(GainStrengthPower __instance, AbstractCreature owner, int newAmount) {
            wantCreate(newAmount);
        }

        @SpirePostfixPatch
        public static void Postfix(GainStrengthPower __instance) {
            restore(__instance, CAP);
        }
    }

    @SpirePatch(clz = GainStrengthPower.class, method = "stackPower", paramtypez = {int.class})
    public static class ShackledStacked {
        @SpirePrefixPatch
        public static void Prefix(AbstractPower __instance, int stackAmount) {
            wantStack(__instance, stackAmount);
        }

        @SpirePostfixPatch
        public static void Postfix(AbstractPower __instance) {
            restore(__instance, CAP);
        }
    }

    @SpirePatch(clz = GainStrengthPower.class, method = "reducePower", paramtypez = {int.class})
    public static class ShackledReduced {
        @SpirePrefixPatch
        public static void Prefix(AbstractPower __instance, int reduceAmount) {
            wantReduce(__instance, reduceAmount);
        }

        @SpirePostfixPatch
        public static void Postfix(AbstractPower __instance) {
            restore(__instance, CAP);
        }
    }

    /** Plated Armor: only stackPower clamps, the constructor does not. */
    @SpirePatch(clz = PlatedArmorPower.class, method = "stackPower", paramtypez = {int.class})
    public static class PlatedArmorStacked {
        @SpirePrefixPatch
        public static void Prefix(AbstractPower __instance, int stackAmount) {
            wantStack(__instance, stackAmount);
        }

        @SpirePostfixPatch
        public static void Postfix(AbstractPower __instance) {
            restore(__instance, CAP);
        }
    }

    /** Poison: a wider cap than the others (9999), and only the constructor clamps. */
    @SpirePatch(clz = PoisonPower.class, method = SpirePatch.CONSTRUCTOR)
    public static class PoisonCreated {
        @SpirePrefixPatch
        public static void Prefix(PoisonPower __instance, AbstractCreature owner, AbstractCreature source, int poisonAmt) {
            wantCreate(poisonAmt);
        }

        @SpirePostfixPatch
        public static void Postfix(PoisonPower __instance) {
            restore(__instance, POISON_CAP);
        }
    }

    /** Collect (the Watcher's Miracle power): clamped in the constructor and stackPower. */
    @SpirePatch(clz = CollectPower.class, method = SpirePatch.CONSTRUCTOR)
    public static class CollectCreated {
        @SpirePrefixPatch
        public static void Prefix(CollectPower __instance, AbstractCreature owner, int numTurns) {
            wantCreate(numTurns);
        }

        @SpirePostfixPatch
        public static void Postfix(CollectPower __instance) {
            restore(__instance, CAP);
        }
    }

    @SpirePatch(clz = CollectPower.class, method = "stackPower", paramtypez = {int.class})
    public static class CollectStacked {
        @SpirePrefixPatch
        public static void Prefix(AbstractPower __instance, int stackAmount) {
            wantStack(__instance, stackAmount);
        }

        @SpirePostfixPatch
        public static void Postfix(AbstractPower __instance) {
            restore(__instance, CAP);
        }
    }

    /** Energized (the Silent's energy power): clamped in the constructor and stackPower. */
    @SpirePatch(clz = EnergizedPower.class, method = SpirePatch.CONSTRUCTOR)
    public static class EnergizedCreated {
        @SpirePrefixPatch
        public static void Prefix(EnergizedPower __instance, AbstractCreature owner, int energyAmt) {
            wantCreate(energyAmt);
        }

        @SpirePostfixPatch
        public static void Postfix(EnergizedPower __instance) {
            restore(__instance, CAP);
        }
    }

    @SpirePatch(clz = EnergizedPower.class, method = "stackPower", paramtypez = {int.class})
    public static class EnergizedStacked {
        @SpirePrefixPatch
        public static void Prefix(AbstractPower __instance, int stackAmount) {
            wantStack(__instance, stackAmount);
        }

        @SpirePostfixPatch
        public static void Postfix(AbstractPower __instance) {
            restore(__instance, CAP);
        }
    }

    /** Energized Blue (the Defect's energy power): clamped in the constructor and stackPower. */
    @SpirePatch(clz = EnergizedBluePower.class, method = SpirePatch.CONSTRUCTOR)
    public static class EnergizedBlueCreated {
        @SpirePrefixPatch
        public static void Prefix(EnergizedBluePower __instance, AbstractCreature owner, int energyAmt) {
            wantCreate(energyAmt);
        }

        @SpirePostfixPatch
        public static void Postfix(EnergizedBluePower __instance) {
            restore(__instance, CAP);
        }
    }

    @SpirePatch(clz = EnergizedBluePower.class, method = "stackPower", paramtypez = {int.class})
    public static class EnergizedBlueStacked {
        @SpirePrefixPatch
        public static void Prefix(AbstractPower __instance, int stackAmount) {
            wantStack(__instance, stackAmount);
        }

        @SpirePostfixPatch
        public static void Postfix(AbstractPower __instance) {
            restore(__instance, CAP);
        }
    }

    /** Like Water: only stackPower clamps. */
    @SpirePatch(clz = LikeWaterPower.class, method = "stackPower", paramtypez = {int.class})
    public static class LikeWaterStacked {
        @SpirePrefixPatch
        public static void Prefix(AbstractPower __instance, int stackAmount) {
            wantStack(__instance, stackAmount);
        }

        @SpirePostfixPatch
        public static void Postfix(AbstractPower __instance) {
            restore(__instance, CAP);
        }
    }
}
