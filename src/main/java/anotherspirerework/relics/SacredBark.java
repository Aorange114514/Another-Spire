package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.util.RelicChecks;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.potions.PotionSlot;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import javassist.expr.ExprEditor;

/**
 * Sacred Bark: gain [E] every turn, and lose [E] at the start of a turn spent with fewer than two
 * potions. The vanilla "+100% potion potency" is gone.
 *
 * <p>Replaces the vanilla prototype through {@code patches.RelicSwapPatch}.
 */
public class SacredBark extends com.megacrit.cardcrawl.relics.SacredBark {
    /** Fewer potions than this triggers the energy penalty. */
    public static final int MIN_POTIONS = 2;

    public SacredBark() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new SacredBark();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** The vanilla pickup effect re-initialises every potion for the potency bonus; ours does not. */
    @Override
    public void onEquip() {
        AbstractDungeon.player.energy.energyMaster++;
    }

    @Override
    public void onUnequip() {
        AbstractDungeon.player.energy.energyMaster--;
    }

    @Override
    public void atTurnStart() {
        int potions = 0;
        for (AbstractPotion potion : AbstractDungeon.player.potions) {
            if (!(potion instanceof PotionSlot)) {
                potions++;
            }
        }
        if (potions < MIN_POTIONS) {
            RelicChecks.loseEnergy();
        }
    }

    /** The potion itself must not be doubled any more; the base game tests the relic by ID. */
    @SpirePatch(clz = AbstractPotion.class, method = "getPotency", paramtypez = {})
    public static class Potency {
        @SpireInstrumentPatch
        public static ExprEditor edit() {
            return RelicChecks.ignoreHasRelic(ID);
        }
    }

    /** The potion tooltips were built from the doubled value; they have to advertise the real one. */
    @SpirePatch(clz = com.megacrit.cardcrawl.potions.AttackPotion.class, method = "initializeData")
    @SpirePatch(clz = com.megacrit.cardcrawl.potions.SkillPotion.class, method = "initializeData")
    @SpirePatch(clz = com.megacrit.cardcrawl.potions.PowerPotion.class, method = "initializeData")
    @SpirePatch(clz = com.megacrit.cardcrawl.potions.ColorlessPotion.class, method = "initializeData")
    @SpirePatch(clz = com.megacrit.cardcrawl.potions.DuplicationPotion.class, method = "initializeData")
    public static class PotionText {
        @SpireInstrumentPatch
        public static ExprEditor edit() {
            return RelicChecks.ignoreHasRelic(ID);
        }
    }
}
