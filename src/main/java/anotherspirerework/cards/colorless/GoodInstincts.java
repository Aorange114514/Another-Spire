package anotherspirerework.cards.colorless;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.util.TempRandomCard;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** Good Instincts: a free bit of Block plus a temporary Skill card. */
public class GoodInstincts extends AbstractCard {
    public static final String ID = "Good Instincts";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public GoodInstincts() {
        super(ID, cardStrings.NAME, "colorless/skill/good_instincts", 0, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.COLORLESS, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseBlock = 6;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new GainBlockAction(p, p, this.block));
        TempRandomCard.addEtherealExhaust(CardType.SKILL);
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBlock(3);
        }
    }

    public AbstractCard makeCopy() {
        return new GoodInstincts();
    }
}
