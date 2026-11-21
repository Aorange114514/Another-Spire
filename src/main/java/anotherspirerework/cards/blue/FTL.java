package anotherspirerework.cards.blue;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.actions.ExhaustHandStatusesAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** FTL: a Skill now; the Status cards in hand become energy and cards, exhausted one by one. */
public class FTL extends AbstractCard {
    public static final String ID = "FTL";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public FTL() {
        super(ID, cardStrings.NAME, "blue/attack/ftl", 1, cardStrings.DESCRIPTION,
                CardType.SKILL, CardColor.BLUE, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseMagicNumber = 1;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ExhaustHandStatusesAction(1, this.magicNumber));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(1);
        }
    }

    public AbstractCard makeCopy() {
        return new FTL();
    }
}
