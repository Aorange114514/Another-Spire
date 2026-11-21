package anotherspirerework.cards.red;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.CombustPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** Combust: lose HP every turn, and every HP loss burns every enemy. */
public class Combust extends AbstractCard {
    public static final String ID = "Combust";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Combust() {
        super(ID, cardStrings.NAME, "red/power/combust", 1, cardStrings.DESCRIPTION,
                CardType.POWER, CardColor.RED, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseMagicNumber = 5;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new CombustPower(p, this.magicNumber), this.magicNumber));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(2);
        }
    }

    public AbstractCard makeCopy() {
        return new Combust();
    }
}
