package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.cards.status.Wound;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;

/**
 * Mark of Pain: at the start of combat it puts two Wounds into the discard pile instead of the draw
 * pile, so they only reach the hand after the next shuffle. The vanilla energy and equip hooks are
 * inherited unchanged.
 *
 * <p>Replaces the vanilla prototype through {@code patches.RelicSwapPatch}.
 */
public class MarkOfPain extends com.megacrit.cardcrawl.relics.MarkOfPain {
    public MarkOfPain() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new MarkOfPain();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** Vanilla used {@code MakeTempCardInDrawPileAction}; the Wounds now start in the discard pile. */
    @Override
    public void atBattleStart() {
        flash();
        addToBot(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        addToBot(new MakeTempCardInDiscardAction(new Wound(), 2));
    }
}
