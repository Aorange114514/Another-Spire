package anotherspirerework.cards.colorless;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.PutOnDeckAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * Thinking Ahead: draw first, then put one of the drawn cards back on top of the draw pile.
 * Unlike the base game version the Exhaust is not removed by the upgrade, the extra card is.
 */
public class ThinkingAhead extends AbstractCard {
    public static final String ID = "Thinking Ahead";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public ThinkingAhead() {
        super(ID, cardStrings.NAME, "colorless/skill/thinking_ahead", 0, cardStrings.DESCRIPTION,
                CardType.SKILL, CardColor.COLORLESS, CardRarity.RARE, CardTarget.NONE);
        this.baseMagicNumber = 2;
        this.magicNumber = this.baseMagicNumber;
        this.exhaust = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DrawCardAction(p, this.magicNumber));
        addToBot(new PutOnDeckAction(p, p, 1, false));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(1);
        }
    }

    public AbstractCard makeCopy() {
        return new ThinkingAhead();
    }
}
