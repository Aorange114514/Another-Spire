package anotherspire.powers;

import anotherspire.AnotherSpire;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.FocusPower;

/**
 * "Next Turn Focus", the Focus counterpart of the vanilla "Next Turn Block" power:
 * a positive amount is gained at the end of the turn, a negative amount is lost.
 * Leap / Blizzard use a negative amount (temporary Focus), Hyperbeam a positive one (it gives the Focus back).
 */
public class NextTurnFocusPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpire.makeID("NextTurnFocus");
    private static final PowerStrings powerStrings = AnotherSpire.getPowerStrings("NextTurnFocus");

    public NextTurnFocusPower(AbstractCreature owner, int amount) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = amount < 0 ? PowerType.DEBUFF : PowerType.BUFF;
        this.canGoNegative = true;
        updateDescription();
        loadRegion("focus");
    }

    @Override
    public void stackPower(int stackAmount) {
        this.fontScale = 8.0F;
        this.amount += stackAmount;
        if (this.amount == 0) {
            addToTop(new RemoveSpecificPowerAction(this.owner, this.owner, POWER_ID));
        } else {
            this.type = this.amount < 0 ? PowerType.DEBUFF : PowerType.BUFF;
            updateDescription();
        }
    }

    @Override
    public void updateDescription() {
        if (this.amount < 0) {
            this.description = powerStrings.DESCRIPTIONS[0] + (-this.amount) + powerStrings.DESCRIPTIONS[1];
        } else {
            this.description = powerStrings.DESCRIPTIONS[2] + this.amount + powerStrings.DESCRIPTIONS[1];
        }
    }

    @Override
    public void atEndOfTurn(boolean isPlayer) {
        if (!isPlayer) {
            return;
        }
        flash();
        addToBot(new ApplyPowerAction(this.owner, this.owner, new FocusPower(this.owner, this.amount), this.amount));
        addToBot(new RemoveSpecificPowerAction(this.owner, this.owner, POWER_ID));
    }
}
