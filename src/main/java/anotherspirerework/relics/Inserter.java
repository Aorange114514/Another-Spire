package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.actions.defect.IncreaseMaxOrbAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.relics.AbstractRelic;

/**
 * Inserter: every 2 turns, gain 1 Orb slot and Channel a random Orb. The vanilla relic only granted
 * the slot; the random Orb is new.
 *
 * <p>Like every reworked relic it replaces the vanilla prototype through
 * {@code patches.RelicSwapPatch}, keeping the vanilla ID, art and acquisition path. Every hook this
 * class does not override is inherited from the vanilla relic unchanged (including
 * {@code onEquip()}, which resets the counter).
 */
public class Inserter extends com.megacrit.cardcrawl.relics.Inserter {
    /** Turns between two slots. */
    public static final int TURNS = 2;

    public Inserter() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new Inserter();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    @Override
    public void atTurnStart() {
        if (counter < 0) {
            counter = 0;
        }
        counter++;
        if (counter < TURNS) {
            return;
        }
        counter = 0;
        flash();
        addToBot(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        addToBot(new IncreaseMaxOrbAction(1));
        addToBot(new ChannelAction(AbstractOrb.getRandomOrb(true)));
    }
}
