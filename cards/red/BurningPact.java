package anotherspire.cards.red;

import anotherspire.AnotherSpire;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.ExhaustAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** Burning Pact: vanilla effect, promoted to a Common card. */
public class BurningPact extends AbstractCard {
    public static final String ID = "Burning Pact";

    private static final CardStrings cardStrings = AnotherSpire.getCardStrings(ID);

    public BurningPact() {
        super(ID, cardStrings.NAME, "red/skill/burning_pact", 1, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.RED, CardRarity.COMMON, CardTarget.NONE);
        this.baseMagicNumber = 2;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ExhaustAction(1, false));
        addToBot(new DrawCardAction(p, this.magicNumber));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(1);
        }
    }

    public AbstractCard makeCopy() {
        return new BurningPact();
    }
}
