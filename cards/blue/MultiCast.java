package anotherspirerework.cards.blue;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.actions.MultiCastXAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class MultiCast extends AbstractCard {
    public static final String ID = "Multi-Cast";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public MultiCast() {
        super(ID, cardStrings.NAME, "blue/skill/multicast", -1, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.BLUE, CardRarity.RARE, CardTarget.NONE);
        this.showEvokeValue = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new MultiCastXAction(p, this.energyOnUse, this.upgraded, this.freeToPlayOnce));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            this.rawDescription = cardStrings.UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }

    public AbstractCard makeCopy() {
        return new MultiCast();
    }
}
