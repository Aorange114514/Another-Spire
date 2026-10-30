package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;

/**
 * Face of Cleric: every won combat raises the player's Max HP by 2 instead of the vanilla 1. The
 * whole vanilla hook was this one grant, so there is nothing to inherit for it.
 *
 * <p>Like every reworked relic it replaces the vanilla prototype through
 * {@code patches.RelicSwapPatch}, keeping the vanilla ID, art and acquisition path.
 */
public class FaceOfCleric extends com.megacrit.cardcrawl.relics.FaceOfCleric {
    public FaceOfCleric() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new FaceOfCleric();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** The vanilla grant was 1 Max HP; the whole hook is replaced so no second grant is added. */
    @Override
    public void onVictory() {
        AbstractDungeon.player.increaseMaxHp(2, true);
    }
}
