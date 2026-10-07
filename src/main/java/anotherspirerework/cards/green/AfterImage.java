package anotherspirerework.cards.green;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AfterImagePower;

/** After Image: the upgrade raises the Block it gives instead of lowering the cost. */
public class AfterImage extends AbstractCard {
    public static final String ID = "After Image";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public AfterImage() {
        super(ID, cardStrings.NAME, "green/power/after_image", 1, cardStrings.DESCRIPTION, CardType.POWER, CardColor.GREEN, CardRarity.RARE, CardTarget.SELF);
        this.baseMagicNumber = 1;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new AfterImagePower(p, this.magicNumber), this.magicNumber));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(1);
        }
    }

    public AbstractCard makeCopy() {
        return new AfterImage();
    }
}
