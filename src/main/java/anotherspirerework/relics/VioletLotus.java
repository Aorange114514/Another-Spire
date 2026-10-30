package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.stances.AbstractStance;

/**
 * Violet Lotus: leaving Calm still grants the vanilla extra [E], and now draws a card as well. The
 * vanilla relic is inherited for everything else, so the energy comes from {@code super} and only
 * the extra draw is new.
 *
 * <p>Like every reworked relic it replaces the vanilla prototype through
 * {@code patches.RelicSwapPatch}, keeping the vanilla ID, art and acquisition path.
 */
public class VioletLotus extends com.megacrit.cardcrawl.relics.VioletLotus {
    public VioletLotus() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new VioletLotus();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** The vanilla energy is queued first, then the card this rework adds on the same stance exit. */
    @Override
    public void onChangeStance(AbstractStance prevStance, AbstractStance newStance) {
        super.onChangeStance(prevStance, newStance);
        if (prevStance != null && "Calm".equals(prevStance.ID)
                && newStance != null && !"Calm".equals(newStance.ID)) {
            addToBot(new DrawCardAction(1));
        }
    }
}
