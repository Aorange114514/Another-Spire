package anotherspirerework.cards.blue;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.HelloWorldAnyCardPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class HelloWorld extends AbstractCard {
    public static final String ID = "Hello World";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public HelloWorld() {
        super(ID, cardStrings.NAME, "blue/power/hello_world", 1, cardStrings.DESCRIPTION, CardType.POWER, CardColor.BLUE, CardRarity.UNCOMMON, CardTarget.SELF);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new HelloWorldAnyCardPower(p, 1), 1));
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
        return new HelloWorld();
    }
}
