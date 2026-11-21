package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.actions.AlphaDiscardToHandAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

/** Alpha: at the end of every turn, choose cards from the discard pile, put them into the hand and Retain them. */
public class AlphaPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpireRework.makeID("Alpha");
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("Alpha");

    public AlphaPower(AbstractCreature owner, int amount) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;
        updateDescription();
        loadRegion("retain");
    }

    @Override
    public void stackPower(int stackAmount) {
        this.fontScale = 8.0F;
        this.amount += stackAmount;
        updateDescription();
    }

    @Override
    public void atEndOfTurn(boolean isPlayer) {
        if (!isPlayer) {
            return;
        }
        flash();
        addToBot(new AlphaDiscardToHandAction(this.amount));
    }

    @Override
    public void updateDescription() {
        if (this.amount > 1) {
            this.description = powerStrings.DESCRIPTIONS[0] + this.amount + powerStrings.DESCRIPTIONS[2];
        } else {
            this.description = powerStrings.DESCRIPTIONS[0] + this.amount + powerStrings.DESCRIPTIONS[1];
        }
    }
}
