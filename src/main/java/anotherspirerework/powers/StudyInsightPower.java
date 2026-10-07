package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.tempCards.Insight;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

/** Study: at the start of your turn add an Insight into your hand. */
public class StudyInsightPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpireRework.makeID("StudyInsight");
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("StudyInsight");

    public StudyInsightPower(AbstractCreature owner, int amount) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;
        updateDescription();
        loadRegion("draw");
    }

    @Override
    public void updateDescription() {
        if (this.amount > 1) {
            this.description = powerStrings.DESCRIPTIONS[0] + this.amount + powerStrings.DESCRIPTIONS[1];
        } else {
            this.description = powerStrings.DESCRIPTIONS[0] + this.amount + powerStrings.DESCRIPTIONS[2];
        }
    }

    @Override
    public void stackPower(int stackAmount) {
        this.fontScale = 8.0F;
        this.amount += stackAmount;
        updateDescription();
    }

    @Override
    public void atStartOfTurn() {
        flash();
        addToBot(new MakeTempCardInHandAction(new Insight(), this.amount, false));
    }
}
