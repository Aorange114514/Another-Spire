package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.actions.unique.DiscoveryAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;

/**
 * Enchiridion: at the start of each combat, choose 1 of 3 random Power cards. The vanilla relic
 * handed out one random Power card instead.
 *
 * <p>Like every reworked relic it replaces the vanilla prototype through
 * {@code patches.RelicSwapPatch}, keeping the vanilla ID, art and acquisition path. Every hook this
 * class does not override is inherited from the vanilla relic unchanged.
 */
public class Enchiridion extends com.megacrit.cardcrawl.relics.Enchiridion {
    public Enchiridion() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new Enchiridion();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** Replaces the vanilla single random Power card with the three-card choice. */
    @Override
    public void atPreBattle() {
        flash();
        addToBot(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        addToBot(new DiscoveryAction(AbstractCard.CardType.POWER, 1));
    }
}
