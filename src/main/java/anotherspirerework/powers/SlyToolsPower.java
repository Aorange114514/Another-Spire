package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.util.SlyMark;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;

/** Tools of the Trade: every Skill card the player plays this fight also gains Sly. */
public class SlyToolsPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpireRework.makeID("SlyTools");

    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("SlyTools");

    public SlyToolsPower(AbstractCreature owner) {
        this.owner = owner;
        ID = POWER_ID;
        name = powerStrings.NAME;
        amount = -1;
        type = PowerType.BUFF;
        loadRegion("tools");
        updateDescription();
    }

    @Override
    public void updateDescription() {
        description = powerStrings.DESCRIPTIONS[0];
    }

    /** One copy is enough: a second one changes nothing. */
    @Override
    public void stackPower(int stackAmount) {
    }

    /**
     * The played card is already the instance that will cycle back through the draw and discard
     * piles later, so marking it here is enough; it is discarded from the hand on a later draw.
     */
    @Override
    public void onPlayCard(AbstractCard card, AbstractMonster m) {
        if (card != null && card.type == AbstractCard.CardType.SKILL) {
            SlyMark.apply(card);
        }
    }
}
