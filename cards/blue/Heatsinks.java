package anotherspirerework.cards.blue;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.HeatsinkPower;

/** Heatsinks: always draws 2 per Power card, and the upgrade makes it Innate. */
public class Heatsinks extends AbstractCard {
    public static final String ID = "Heatsinks";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    private static final int DRAW_AMOUNT = 2;

    public Heatsinks() {
        super(ID, cardStrings.NAME, "blue/power/heatsinks", 1, cardStrings.DESCRIPTION, CardType.POWER, CardColor.BLUE, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseMagicNumber = DRAW_AMOUNT;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new HeatsinkPower(p, DRAW_AMOUNT), DRAW_AMOUNT));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            this.isInnate = true;
            this.rawDescription = cardStrings.UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }

    public AbstractCard makeCopy() {
        return new Heatsinks();
    }
}
