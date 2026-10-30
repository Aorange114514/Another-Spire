package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;

/**
 * Ninja Scroll: at the start of every turn, add 1 Shiv to your hand (the vanilla relic added three
 * Shivs once, before the first draw of the combat).
 *
 * <p>Like every reworked relic it replaces the vanilla prototype through
 * {@code patches.RelicSwapPatch}, keeping the vanilla ID, art and acquisition path. Every hook this
 * class does not override is inherited from the vanilla relic unchanged.
 */
public class NinjaScroll extends com.megacrit.cardcrawl.relics.NinjaScroll {
    public NinjaScroll() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new NinjaScroll();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** The vanilla three-Shiv opening is replaced by the per-turn Shiv, so this hook does nothing. */
    @Override
    public void atBattleStartPreDraw() {
    }

    @Override
    public void atTurnStart() {
        flash();
        addToBot(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        addToBot(new MakeTempCardInHandAction(new Shiv()));
    }
}
