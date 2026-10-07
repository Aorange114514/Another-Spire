package anotherspirerework.cards.blue;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.actions.ExhaustAllStatusAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.ShuffleAction;
import com.megacrit.cardcrawl.actions.defect.ShuffleAllAction;
import com.megacrit.cardcrawl.actions.unique.RemoveDebuffsAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** Reboot: full clean slate - debuffs gone, Status cards burnt, everything reshuffled. */
public class Reboot extends AbstractCard {
    public static final String ID = "Reboot";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Reboot() {
        super(ID, cardStrings.NAME, "blue/skill/reboot", 0, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.BLUE, CardRarity.RARE, CardTarget.SELF);
        this.baseMagicNumber = 4;
        this.magicNumber = this.baseMagicNumber;
        this.exhaust = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new RemoveDebuffsAction(p));
        addToBot(new ExhaustAllStatusAction());
        addToBot(new ShuffleAllAction());
        addToBot(new ShuffleAction(AbstractDungeon.player.drawPile, false));
        addToBot(new DrawCardAction(p, this.magicNumber));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(1);
        }
    }

    public AbstractCard makeCopy() {
        return new Reboot();
    }
}
