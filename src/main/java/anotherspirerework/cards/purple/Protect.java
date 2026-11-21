package anotherspirerework.cards.purple;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** Protect: Block that comes back to the hand after being played, now Uncommon. */
public class Protect extends AbstractCard {
    public static final String ID = "Protect";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Protect() {
        super(ID, cardStrings.NAME, "purple/skill/protect", 2, cardStrings.DESCRIPTION,
                CardType.SKILL, CardColor.PURPLE, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseBlock = 12;
        this.returnToHand = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new GainBlockAction(p, p, this.block));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBlock(4);
        }
    }

    public AbstractCard makeCopy() {
        return new Protect();
    }
}
