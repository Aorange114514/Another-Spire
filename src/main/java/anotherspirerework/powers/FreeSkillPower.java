package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

/**
 * Wheel Kick: the next Skill you play costs 0. This is the Skill counterpart of the vanilla
 * FreeAttackPower (Swivel); FreeSkillCardPatch makes freeToPlay() recognise it.
 */
public class FreeSkillPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpireRework.makeID("FreeSkill");
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("FreeSkill");

    public FreeSkillPower(AbstractCreature owner, int amount) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;
        updateDescription();
        loadRegion("swivel");
    }

    @Override
    public void updateDescription() {
        if (this.amount == 1) {
            this.description = powerStrings.DESCRIPTIONS[0];
        } else {
            this.description = powerStrings.DESCRIPTIONS[1] + this.amount + powerStrings.DESCRIPTIONS[2];
        }
    }

    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (card.type == AbstractCard.CardType.SKILL && !card.purgeOnUse && this.amount > 0) {
            flash();
            this.amount--;
            if (this.amount == 0) {
                addToTop(new RemoveSpecificPowerAction(this.owner, this.owner, POWER_ID));
            }
        }
    }
}
