package anotherspirerework.cards.colorless;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.ScuffleDrawPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** Mayhem: every 10 cards you draw pays out one energy. */
public class Mayhem extends AbstractCard {
    public static final String ID = "Mayhem";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Mayhem() {
        super(ID, cardStrings.NAME, "colorless/power/mayhem", 1, cardStrings.DESCRIPTION,
                CardType.POWER, CardColor.COLORLESS, CardRarity.RARE, CardTarget.SELF);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new ScuffleDrawPower(p, 1), 1));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(0);
        }
    }

    public AbstractCard makeCopy() {
        return new Mayhem();
    }
}
