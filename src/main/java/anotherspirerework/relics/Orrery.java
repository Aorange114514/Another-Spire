package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.effects.ChooseDeckCardsEffect;
import com.megacrit.cardcrawl.relics.AbstractRelic;

/**
 * Orrery: on pickup, choose any number of cards out of 10 Commons, 10 Uncommons and 10 Rares to add
 * to your deck. The vanilla relic granted four card rewards instead.
 *
 * <p>Like every reworked relic it replaces the vanilla prototype through
 * {@code patches.RelicSwapPatch}, keeping the vanilla ID, art and acquisition path. Every hook this
 * class does not override is inherited from the vanilla relic unchanged.
 */
public class Orrery extends com.megacrit.cardcrawl.relics.Orrery {
    public Orrery() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new Orrery();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** Replaces the vanilla four card rewards with the 30-card pick; the vanilla body is not run. */
    @Override
    public void onEquip() {
        flash();
        ChooseDeckCardsEffect.start(ChooseDeckCardsEffect.Choice.ADD_TO_DECK,
                AnotherSpireRework.getRelicDescription(relicId, 1), 30);
    }
}
