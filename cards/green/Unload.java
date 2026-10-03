package anotherspirerework.cards.green;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.ShivMasteryPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** Unload: an Attack turned into the Shiv engine (Retain + first Shiv of the turn hits harder). */
public class Unload extends AbstractCard {
    public static final String ID = "Unload";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Unload() {
        super(ID, cardStrings.NAME, "green/attack/unload", 1, cardStrings.DESCRIPTION, CardType.POWER, CardColor.GREEN, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseMagicNumber = 9;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new ShivMasteryPower(p, this.magicNumber), this.magicNumber));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(3);
        }
    }

    public AbstractCard makeCopy() {
        return new Unload();
    }
}
