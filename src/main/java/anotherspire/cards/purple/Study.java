package anotherspire.cards.purple;

import anotherspire.AnotherSpire;
import anotherspire.powers.StudyInsightPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Study extends AbstractCard {
    public static final String ID = "Study";

    private static final CardStrings cardStrings = AnotherSpire.getCardStrings(ID);

    public Study() {
        super(ID, cardStrings.NAME, "purple/power/study", 2, cardStrings.DESCRIPTION, CardType.POWER, CardColor.PURPLE, CardRarity.UNCOMMON, CardTarget.SELF);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new StudyInsightPower(p, 1), 1));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(1);
        }
    }

    public AbstractCard makeCopy() {
        return new Study();
    }
}
