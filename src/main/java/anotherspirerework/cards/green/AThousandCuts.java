package anotherspirerework.cards.green;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.ThousandCutsPower;

public class AThousandCuts extends AbstractCard {
    public static final String ID = "A Thousand Cuts";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public AThousandCuts() {
        super(ID, cardStrings.NAME, "green/power/a_thousand_cuts", 1, cardStrings.DESCRIPTION, CardType.POWER, CardColor.GREEN, CardRarity.RARE, CardTarget.SELF);
        this.baseMagicNumber = 1;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new ThousandCutsPower(p, this.magicNumber), this.magicNumber));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(1);
        }
    }

    public AbstractCard makeCopy() {
        return new AThousandCuts();
    }
}
