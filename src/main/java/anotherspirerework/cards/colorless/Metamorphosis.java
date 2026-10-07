package anotherspirerework.cards.colorless;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.ShuffleSeekPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** Metamorphosis: an Attack turned Power that searches the draw pile every time it is shuffled. */
public class Metamorphosis extends AbstractCard {
    public static final String ID = "Metamorphosis";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Metamorphosis() {
        super(ID, cardStrings.NAME, "colorless/skill/metamorphosis", 2, cardStrings.DESCRIPTION,
                CardType.POWER, CardColor.COLORLESS, CardRarity.RARE, CardTarget.SELF);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new ShuffleSeekPower(p, 1), 1));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(1);
        }
    }

    public AbstractCard makeCopy() {
        return new Metamorphosis();
    }
}
