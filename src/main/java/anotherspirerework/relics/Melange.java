package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;

/**
 * Melange: playing a colourless card grants 3 Block. The vanilla scry on shuffle is gone.
 *
 * <p>Like every reworked relic it replaces the vanilla prototype through
 * {@code patches.RelicSwapPatch}, keeping the vanilla ID, art and acquisition path.
 */
public class Melange extends com.megacrit.cardcrawl.relics.Melange {
    /** Block granted by each colourless card. */
    public static final int BLOCK = 3;

    public Melange() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new Melange();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** The vanilla scry on every shuffle applies no more; the relic is a Block source now. */
    @Override
    public void onShuffle() {
    }

    /** Insight and Miracle are colourless too; Status and Curses are not "colourless cards" here. */
    public static boolean isColourlessCard(AbstractCard card) {
        return card != null && card.color == AbstractCard.CardColor.COLORLESS
                && card.type != AbstractCard.CardType.STATUS && card.type != AbstractCard.CardType.CURSE;
    }

    /** Colourless cards flash the relic and grant the Block; every other card is ignored. */
    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (!isColourlessCard(card)) {
            return;
        }
        flash();
        addToBot(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        addToBot(new GainBlockAction(AbstractDungeon.player, AbstractDungeon.player, BLOCK));
    }
}
