package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.actions.common.EmptyDeckShuffleAction;
import com.megacrit.cardcrawl.actions.utility.ScryAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

/**
 * Golden Eye: Scry 3 at the start of every turn, including the turn that opens a combat. The vanilla
 * "+2 cards to every scry" bonus is gone, so the amount is always exactly the 3 this relic grants.
 *
 * <p>Like every reworked relic it replaces the vanilla prototype through
 * {@code patches.RelicSwapPatch}, keeping the vanilla ID, art and acquisition path.
 */
public class GoldenEye extends com.megacrit.cardcrawl.relics.GoldenEye {
    /** Cards looked at by the scry. */
    public static final int SCRY = 3;

    /**
     * True once the opening hand of this combat has been scried for, so the turn start that follows
     * it does not scry a second time. Package private for the same accessor reason as the method.
     */
    static boolean scriedBeforeDraw;

    public GoldenEye() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new GoldenEye();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /**
     * The scry itself, shared by the opening hand and the start of every later turn.
     *
     * <p>Same timing as the base game's own scry power: an empty draw pile is shuffled first, so the
     * pick has something to work with. Package private on purpose: the nested patches below call it
     * directly, while a private method would compile to a synthetic accessor instead (the same
     * reason {@code ShuffleTriggerPatch.flush} is not private).
     */
    static void goldenEyeScry() {
        if (AbstractDungeon.player == null) {
            return;
        }
        AbstractRelic relic = AbstractDungeon.player.getRelic("GoldenEye");
        if (relic != null) {
            relic.flash();
        }
        if (AbstractDungeon.player.drawPile.size() <= 0) {
            AbstractDungeon.actionManager.addToTop(new EmptyDeckShuffleAction());
        }
        AbstractDungeon.actionManager.addToBottom(new ScryAction(SCRY));
    }

    /** Every later turn scries; the turn that opens a combat was already handled by OpeningScry. */
    @Override
    public void atTurnStart() {
        if (AbstractDungeon.player == null) {
            return;
        }
        if (scriedBeforeDraw) {
            // The opening hand has already been scried for this combat.
            scriedBeforeDraw = false;
            return;
        }
        goldenEyeScry();
    }

    /**
     * The first turn of a combat is special: the base game queues the opening draw before it runs the
     * start of turn relics, so scrying from {@code atTurnStart} would let the player draw first.
     * Scrying here instead puts the pick before the opening hand, which is the order every later
     * turn already has.
     */
    @SpirePatch(clz = AbstractPlayer.class, method = "applyStartOfCombatPreDrawLogic")
    public static class OpeningScry {
        @SpirePrefixPatch
        public static void before() {
            scriedBeforeDraw = AbstractDungeon.player != null
                    && AbstractDungeon.player.hasRelic("GoldenEye");
            if (scriedBeforeDraw) {
                goldenEyeScry();
            }
        }
    }

    /** Vanilla added two cards to every scry; the relic is a fixed scry of its own now. */
    @SpirePatch(clz = ScryAction.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {int.class})
    public static class NoBonus {
        @SpireInstrumentPatch
        public static ExprEditor edit() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("hasRelic")) {
                        m.replace("{ $_ = false; }");
                    }
                }
            };
        }
    }
}
