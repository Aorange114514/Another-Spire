package anotherspire.cards.blue;

import anotherspire.AnotherSpire;
import com.megacrit.cardcrawl.actions.defect.AnimateOrbAction;
import com.megacrit.cardcrawl.actions.defect.EvokeOrbAction;
import com.megacrit.cardcrawl.actions.defect.EvokeWithoutRemovingOrbAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Dualcast extends AbstractCard {
    public static final String ID = "Dualcast";

    private static final CardStrings cardStrings = AnotherSpire.getCardStrings(ID);

    public Dualcast() {
        super(ID, cardStrings.NAME, "blue/skill/dualcast", 1, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.BLUE, CardRarity.BASIC, CardTarget.NONE);
        this.showEvokeValue = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new AnimateOrbAction(1));
        addToBot(new EvokeWithoutRemovingOrbAction(1));
        addToBot(new AnimateOrbAction(1));
        addToBot(new EvokeWithoutRemovingOrbAction(1));
        addToBot(new AnimateOrbAction(1));
        addToBot(new EvokeWithoutRemovingOrbAction(1));
        addToBot(new AnimateOrbAction(1));
        addToBot(new EvokeOrbAction(1));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(0);
        }
    }

    public AbstractCard makeCopy() {
        return new Dualcast();
    }
}
