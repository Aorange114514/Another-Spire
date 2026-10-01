package anotherspire.cards.purple;

import anotherspire.AnotherSpire;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.tempCards.Safety;
import com.megacrit.cardcrawl.cards.tempCards.Smite;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * Carve Reality: no longer hits, it simply hands you a Smite and a Safety.
 * The Smite is shown next to the card the vanilla way (cardsToPreview).
 */
public class CarveReality extends AbstractCard {
    public static final String ID = "CarveReality";

    private static final CardStrings cardStrings = AnotherSpire.getCardStrings(ID);

    public CarveReality() {
        super(ID, cardStrings.NAME, "purple/attack/carve_reality", 1, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.PURPLE, CardRarity.UNCOMMON, CardTarget.NONE);
        this.cardsToPreview = new Smite();
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new MakeTempCardInHandAction(this.cardsToPreview.makeStatEquivalentCopy(), 1));
        addToBot(new MakeTempCardInHandAction(new Safety(), 1));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(0);
        }
    }

    public AbstractCard makeCopy() {
        return new CarveReality();
    }
}
