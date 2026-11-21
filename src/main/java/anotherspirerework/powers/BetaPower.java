package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

/**
 * Beta: draw a card for every Colorless card the player plays. The trigger uses onAfterUseCard so
 * the draw resolves after the played card's own actions, the same timing Dark Embrace and Rushdown
 * have for their respective triggers.
 */
public class BetaPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpireRework.makeID("Beta");
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("Beta");

    public BetaPower(AbstractCreature owner, int amount) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;
        updateDescription();
        loadRegion("carddraw");
    }

    @Override
    public void stackPower(int stackAmount) {
        this.fontScale = 8.0F;
        this.amount += stackAmount;
        updateDescription();
    }

    @Override
    public void onAfterUseCard(AbstractCard card, UseCardAction action) {
        if (card.color == AbstractCard.CardColor.COLORLESS) {
            flash();
            addToBot(new DrawCardAction(this.owner, this.amount));
        }
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
