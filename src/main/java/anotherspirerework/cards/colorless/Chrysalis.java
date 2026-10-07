package anotherspirerework.cards.colorless;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.ShuffleEnergyPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** Chrysalis: an Attack turned Power that pays one energy every time the draw pile is shuffled. */
public class Chrysalis extends AbstractCard {
    public static final String ID = "Chrysalis";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Chrysalis() {
        super(ID, cardStrings.NAME, "colorless/skill/chrysalis", 2, cardStrings.DESCRIPTION,
                CardType.POWER, CardColor.COLORLESS, CardRarity.RARE, CardTarget.SELF);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new ShuffleEnergyPower(p, 1), 1));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(1);
        }
    }

    public AbstractCard makeCopy() {
        return new Chrysalis();
    }
}
