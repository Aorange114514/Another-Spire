package anotherspirerework.cards.red;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.DemonFormExhaustPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * Demon Form: a permanent damage bonus that grows with the exhaust pile. Ethereal, and upgrading
 * is what removes the Ethereal keyword instead of changing any number.
 */
public class DemonForm extends AbstractCard {
    public static final String ID = "Demon Form";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public DemonForm() {
        super(ID, cardStrings.NAME, "red/power/demon_form", 3, cardStrings.DESCRIPTION, CardType.POWER, CardColor.RED, CardRarity.RARE, CardTarget.NONE);
        this.isEthereal = true;
        this.baseMagicNumber = 1;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new DemonFormExhaustPower(p, this.magicNumber), this.magicNumber));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            this.isEthereal = false;
            this.rawDescription = cardStrings.UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }

    public AbstractCard makeCopy() {
        return new DemonForm();
    }
}
