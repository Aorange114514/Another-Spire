package anotherspirerework.cards.blue;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.CreativeAIUpgradedPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.CreativeAIPower;

/** Creative AI: same cost, but the upgraded version hands out already upgraded Power cards. */
public class CreativeAI extends AbstractCard {
    public static final String ID = "Creative AI";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public CreativeAI() {
        super(ID, cardStrings.NAME, "blue/power/creative_ai", 3, cardStrings.DESCRIPTION, CardType.POWER, CardColor.BLUE, CardRarity.RARE, CardTarget.SELF);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        if (this.upgraded) {
            addToBot(new ApplyPowerAction(p, p, new CreativeAIUpgradedPower(p, 1, true), 1));
        } else {
            addToBot(new ApplyPowerAction(p, p, new CreativeAIPower(p, 1), 1));
        }
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            this.rawDescription = cardStrings.UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }

    public AbstractCard makeCopy() {
        return new CreativeAI();
    }
}
