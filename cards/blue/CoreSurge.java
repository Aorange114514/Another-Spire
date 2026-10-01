package anotherspire.cards.blue;

import anotherspire.AnotherSpire;
import anotherspire.actions.IncreaseCostAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.defect.IncreaseMaxOrbAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** Core Surge: an Attack turned into an Orb slot engine that gets more expensive each use. */
public class CoreSurge extends AbstractCard {
    public static final String ID = "Core Surge";

    private static final CardStrings cardStrings = AnotherSpire.getCardStrings(ID);

    public CoreSurge() {
        super(ID, cardStrings.NAME, "blue/attack/core_surge", 0, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.BLUE, CardRarity.RARE, CardTarget.NONE);
        this.baseMagicNumber = 1;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new IncreaseMaxOrbAction(1));
        addToBot(new DrawCardAction(p, this.magicNumber));
        addToBot(new IncreaseCostAction(this));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(1);
        }
    }

    public AbstractCard makeCopy() {
        return new CoreSurge();
    }
}
