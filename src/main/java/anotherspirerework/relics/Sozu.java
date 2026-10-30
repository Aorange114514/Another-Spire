package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.util.RelicChecks;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.common.ObtainPotionAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.potions.PotionSlot;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.ui.panels.PotionPopUp;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

/**
 * Sozu: gain [E] every turn (as vanilla), but potions may still be obtained. During a combat they
 * are locked until the player drops one; drinking, throwing or a Fairy in a Bottle never unlock it.
 *
 * <p>Replaces the vanilla prototype through {@code patches.RelicSwapPatch}. All the potion
 * restrictions the base game implements as {@code hasRelic("Sozu")} branches are switched off here,
 * and the potion-use lock is added on top.
 */
public class Sozu extends com.megacrit.cardcrawl.relics.Sozu {
    /** Set for the rest of a combat once the player discards a potion. */
    private static boolean unlocked;

    public Sozu() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new Sozu();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** Vanilla replaces the text with its own class description, so it has to be replaced here. */
    @Override
    public void updateDescription(AbstractPlayer.PlayerClass c) {
        this.description = getUpdatedDescription();
        this.tips.clear();
        this.tips.add(new PowerTip(this.name, this.description));
        initializeTips();
    }

    @Override
    public void atBattleStart() {
        resetBattle();
    }

    /** The three inputs of the lock: a fight is on, Sozu is held, nothing was discarded yet. */
    public static boolean potionLocked(boolean combat, boolean sozu, boolean discarded) {
        return RelicChecks.sozuLocked(combat, sozu, discarded);
    }

    /** True while potions cannot be used in the current combat. */
    public static boolean locked() {
        return potionLocked(AnotherSpireRework.inCombat(),
                AbstractDungeon.player != null && AbstractDungeon.player.hasRelic(ID), unlocked);
    }

    /** A new fight starts with the lock engaged; also used when a run starts. */
    public static void resetBattle() {
        unlocked = false;
    }

    /**
     * Called when the potion pop-up throws a potion away. Only that branch unlocks the relic; the
     * call site is instrumented below, because the pop-up also removes potions when drinking them.
     */
    public static void discard(int slot) {
        if (locked() && slot >= 0 && slot < AbstractDungeon.player.potions.size()
                && !(AbstractDungeon.player.potions.get(slot) instanceof PotionSlot)) {
            unlocked = true;
            AbstractDungeon.player.getRelic(ID).flash();
        }
    }

    /*----------The vanilla acquisition blocks, switched off.----------*/

    @SpirePatch(clz = AbstractPlayer.class, method = "obtainPotion", paramtypez = {AbstractPotion.class})
    public static class ObtainPotion {
        @SpireInstrumentPatch
        public static ExprEditor edit() {
            return RelicChecks.ignoreHasRelic(ID);
        }
    }

    @SpirePatch(clz = ObtainPotionAction.class, method = "update")
    public static class ObtainPotionActionUpdate {
        @SpireInstrumentPatch
        public static ExprEditor edit() {
            return RelicChecks.ignoreHasRelic(ID);
        }
    }

    @SpirePatch(clz = RewardItem.class, method = "claimReward")
    public static class PotionReward {
        @SpireInstrumentPatch
        public static ExprEditor edit() {
            return RelicChecks.ignoreHasRelic(ID);
        }
    }

    @SpirePatch(clz = StorePotion.class, method = "purchasePotion")
    public static class PotionShop {
        @SpireInstrumentPatch
        public static ExprEditor edit() {
            return RelicChecks.ignoreHasRelic(ID);
        }
    }

    @SpirePatch(clz = com.megacrit.cardcrawl.potions.EntropicBrew.class, method = "use")
    @SpirePatch(clz = com.megacrit.cardcrawl.vfx.ObtainPotionEffect.class, method = "update")
    public static class Brew {
        @SpireInstrumentPatch
        public static ExprEditor edit() {
            return RelicChecks.ignoreHasRelic(ID);
        }
    }

    /*----------The potion-use lock.----------*/

    /** AbstractPotion.canUse covers every potion; the four overriders are patched on top. */
    @SpirePatch(clz = AbstractPotion.class, method = "canUse")
    public static class PotionUse {
        @SpirePostfixPatch
        public static boolean after(boolean __result) {
            return __result && !locked();
        }
    }

    @SpirePatch(clz = com.megacrit.cardcrawl.potions.BloodPotion.class, method = "canUse")
    @SpirePatch(clz = com.megacrit.cardcrawl.potions.EntropicBrew.class, method = "canUse")
    @SpirePatch(clz = com.megacrit.cardcrawl.potions.FruitJuice.class, method = "canUse")
    @SpirePatch(clz = com.megacrit.cardcrawl.potions.SmokeBomb.class, method = "canUse")
    public static class OverridingPotionUse {
        @SpirePostfixPatch
        public static boolean after(boolean __result) {
            return __result && !locked();
        }
    }

    /** A Fairy in a Bottle must not revive the player while the lock is on. */
    @SpirePatch(clz = AbstractPlayer.class, method = "damage")
    public static class Fairy {
        @SpireInstrumentPatch
        public static ExprEditor edit() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall call) throws CannotCompileException {
                    if (call.getMethodName().equals("hasPotion")) {
                        call.replace("{ $_ = $1.equals(\"FairyPotion\")"
                                + " && anotherspirerework.relics.Sozu.locked() ? false : $proceed($$); }");
                    }
                }
            };
        }
    }

    /**
     * Only the second destroyPotion call in the pop-up is the discard branch; drinking and throwing
     * must never unlock Sozu.
     */
    @SpirePatch(clz = PotionPopUp.class, method = "updateInput")
    public static class PotionDiscard {
        @SpireInstrumentPatch
        public static ExprEditor edit() {
            return new ExprEditor() {
                private int removals;

                @Override
                public void edit(MethodCall call) throws CannotCompileException {
                    if (call.getClassName().equals("com.megacrit.cardcrawl.ui.panels.TopPanel")
                            && call.getMethodName().equals("destroyPotion") && ++removals == 2) {
                        call.replace("{ anotherspirerework.relics.Sozu.discard($1); $proceed($$); }");
                    }
                }
            };
        }
    }
}
