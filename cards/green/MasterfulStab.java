package anotherspire.cards.green;

import anotherspire.AnotherSpire;
import anotherspire.actions.DiscardForShivsAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** Masterful Stab: trades cards for Shivs (the upgrade hands out Shiv+). */
public class MasterfulStab extends AbstractCard {
    public static final String ID = "Masterful Stab";

    private static final CardStrings cardStrings = AnotherSpire.getCardStrings(ID);

    private static final int MAX_DISCARD = 3;

    public MasterfulStab() {
        super(ID, cardStrings.NAME, "green/attack/masterful_stab", 0, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.GREEN, CardRarity.UNCOMMON, CardTarget.NONE);
        this.cardsToPreview = new Shiv();
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DiscardForShivsAction(MAX_DISCARD, this.upgraded));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            if (this.cardsToPreview != null) {
                this.cardsToPreview.upgrade();
            }
            this.rawDescription = cardStrings.UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }

    public AbstractCard makeCopy() {
        return new MasterfulStab();
    }
}
