package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.DexterityPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.ui.campfire.AbstractCampfireOption;
import com.megacrit.cardcrawl.ui.campfire.LiftOption;
import java.util.ArrayList;

/**
 * Girya: at the start of every combat it grants Strength <em>and</em> Dexterity equal to the number
 * of Lifts taken this run (the vanilla relic paid Strength only), and it can be lifted up to five
 * times instead of the vanilla three. The Lift count stays in the vanilla counter, so the effect
 * and the displayed number stay in step.
 *
 * <p>Replaces the vanilla prototype through {@code patches.RelicSwapPatch}. The Lift button's
 * tooltip is patched here too, so the campfire text matches what the relic actually does.
 */
public class Girya extends com.megacrit.cardcrawl.relics.Girya {
    /** The rework raises the vanilla three lifts to five. */
    public static final int MAX_LIFTS = 5;

    public Girya() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new Girya();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** Vanilla paid Strength only; the rework pays the same amount of Dexterity too. */
    @Override
    public void atBattleStart() {
        if (counter > 0) {
            flash();
            addToTop(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player,
                    new StrengthPower(AbstractDungeon.player, counter), counter));
            addToTop(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player,
                    new DexterityPower(AbstractDungeon.player, counter), counter));
            addToTop(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        }
    }

    @Override
    public void addCampfireOption(ArrayList<AbstractCampfireOption> options) {
        options.add(new LiftOption(counter < MAX_LIFTS));
    }

    /** The Lift button's tooltip: +1 Strength and +1 Dexterity, up to five times. */
    @SpirePatch(clz = LiftOption.class, method = SpirePatch.CONSTRUCTOR)
    public static class GiryaLiftText {
        @SpirePostfixPatch
        public static void after(LiftOption __instance, boolean active) {
            if (active) {
                basemod.ReflectionHacks.setPrivate(__instance, AbstractCampfireOption.class,
                        "description", CardCrawlGame.languagePack
                                .getUIString(AnotherSpireRework.makeID("Lift Option")).TEXT[0]);
            }
        }
    }
}
