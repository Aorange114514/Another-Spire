package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.defect.SeekAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

/**
 * Metamorphosis: search the draw pile for cards every time it is shuffled. The search action is
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
        flash();
        addToTop(new SeekAction(this.amount));
    }

    @Override
    public void updateDescription() {
        this.description = powerStrings.DESCRIPTIONS[0];
    }
}
