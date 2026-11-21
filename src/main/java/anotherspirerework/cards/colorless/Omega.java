package anotherspirerework.cards.colorless;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.OmegaRemovalPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** Omega: each copy played turns into one card removal reward when the combat ends. */
public class Omega extends AbstractCard {
    public static final String ID = "Omega";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Omega() {
        super(ID, cardStrings.NAME, "colorless/power/omega", 3, cardStrings.DESCRIPTION,
                CardType.POWER, CardColor.COLORLESS, CardRarity.SPECIAL, CardTarget.SELF);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new OmegaRemovalPower(p, 1), 1));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(2);
        }
    }

    public AbstractCard makeCopy() {
        return new Omega();
    }
}
