package anotherspirerework.cards.purple;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.cards.colorless.Beta;
import anotherspirerework.powers.AlphaPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** Alpha: a Power now. Shuffles a Beta in, and recovers a retained card from the discard each turn. */
public class Alpha extends AbstractCard {
    public static final String ID = "Alpha";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Alpha() {
        super(ID, cardStrings.NAME, "purple/skill/alpha", 1, cardStrings.DESCRIPTION,
                CardType.POWER, CardColor.PURPLE, CardRarity.RARE, CardTarget.SELF);
        this.cardsToPreview = new Beta();
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new AlphaPower(p, 1), 1));
        addToBot(new MakeTempCardInDrawPileAction(new Beta(), 1, true, true));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(0);
        }
    }

    public AbstractCard makeCopy() {
        return new Alpha();
    }
}
