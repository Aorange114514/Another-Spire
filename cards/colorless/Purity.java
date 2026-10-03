package anotherspirerework.cards.colorless;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.ExhaustAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * Vanilla effect (exhaust up to 3 (5) cards in your hand) plus Retain, so the card can be kept
 * until the hand is actually worth purging.
 */
public class Purity extends AbstractCard {
    public static final String ID = "Purity";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Purity() {
        super(ID, cardStrings.NAME, "colorless/skill/purity", 0, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.COLORLESS, CardRarity.UNCOMMON, CardTarget.NONE);
        this.baseMagicNumber = 3;
        this.magicNumber = this.baseMagicNumber;
        this.exhaust = true;
        this.selfRetain = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ExhaustAction(this.magicNumber, false, true, true));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(2);
        }
    }

    public AbstractCard makeCopy() {
        return new Purity();
    }
}
