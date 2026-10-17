package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.actions.ShuffleSeekAction;
import anotherspirerework.patches.ShuffleTriggerPatch;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

/**
 * Metamorphosis: pick up to {@code amount} cards from the draw pile every time it is shuffled. The search action is
 * pushed right after the shuffle finished and before the draws that caused it finish resolving,
 * which is what the requirement asks for.
 */
public class ShuffleSeekPower extends AbstractPower implements DrawPileShuffleListener {
    public static final String POWER_ID = AnotherSpireRework.makeID("Metamorphosis");
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("Metamorphosis");

    public ShuffleSeekPower(AbstractCreature owner, int amount) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;
        updateDescription();
        loadRegion("carddraw");
    }

    @Override
    public void onDrawPileShuffled() {
        // The draw that follows the shuffle takes the whole pile anyway, so there is nothing to choose.
        if (AbstractDungeon.player.drawPile.size() <= ShuffleTriggerPatch.drawsAfterShuffle()) {
            return;
        }
        flash();
        addToTop(new ShuffleSeekAction(this.amount));
    }

    @Override
    public void updateDescription() {
        this.description = powerStrings.DESCRIPTIONS[0] + this.amount
                + (this.amount == 1 ? powerStrings.DESCRIPTIONS[1] : powerStrings.DESCRIPTIONS[2]);
    }
}
