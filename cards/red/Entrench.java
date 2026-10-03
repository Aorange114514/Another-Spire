package anotherspirerework.cards.red;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.NextTurnBlockPower;

public class Entrench extends AbstractCard {
    public static final String ID = "Entrench";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Entrench() {
        super(ID, cardStrings.NAME, "red/skill/entrench", 1, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.RED, CardRarity.UNCOMMON, CardTarget.SELF);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        if (p.currentBlock > 0) {
            addToBot(new ApplyPowerAction(p, p, new NextTurnBlockPower(p, p.currentBlock), p.currentBlock));
        }
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(0);
        }
    }

    public AbstractCard makeCopy() {
        return new Entrench();
    }
}
