package anotherspire.cards.red;

import anotherspire.AnotherSpire;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Sentinel extends AbstractCard {
    public static final String ID = "Sentinel";

    private static final CardStrings cardStrings = AnotherSpire.getCardStrings(ID);

    public Sentinel() {
        super(ID, cardStrings.NAME, "red/skill/sentinel", 1, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.RED, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseBlock = 6;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new GainBlockAction(p, p, this.block));
        addToBot(new DrawCardAction(p, 1));
    }

    public void triggerOnExhaust() {
        addToTop(new GainEnergyAction(2));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBlock(3);
        }
    }

    public AbstractCard makeCopy() {
        return new Sentinel();
    }
}
