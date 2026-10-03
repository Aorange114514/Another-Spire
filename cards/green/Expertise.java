package anotherspirerework.cards.green;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
import com.megacrit.cardcrawl.actions.unique.ExpertiseAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** Expertise: fills the hand up to 7 (8) cards, then makes you throw one away. */
public class Expertise extends AbstractCard {
    public static final String ID = "Expertise";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Expertise() {
        super(ID, cardStrings.NAME, "green/skill/expertise", 1, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.GREEN, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseMagicNumber = 7;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ExpertiseAction(p, this.magicNumber));
        addToBot(new DiscardAction(p, p, 1, false));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(1);
        }
    }

    public AbstractCard makeCopy() {
        return new Expertise();
    }
}
