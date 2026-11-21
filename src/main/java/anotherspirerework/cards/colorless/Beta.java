package anotherspirerework.cards.colorless;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.BetaPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** Beta: a Power now. Shuffles an Omega in, and draws a card for every Colorless card played. */
public class Beta extends AbstractCard {
    public static final String ID = "Beta";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Beta() {
        super(ID, cardStrings.NAME, "colorless/skill/beta", 2, cardStrings.DESCRIPTION,
                CardType.POWER, CardColor.COLORLESS, CardRarity.SPECIAL, CardTarget.SELF);
        this.cardsToPreview = new Omega();
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new BetaPower(p, 1), 1));
        addToBot(new MakeTempCardInDrawPileAction(new Omega(), 1, true, true));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(1);
        }
    }

    public AbstractCard makeCopy() {
        return new Beta();
    }
}
