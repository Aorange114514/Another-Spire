package anotherspirerework.cards.green;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.SlyToolsPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** Tools of the Trade: every Skill card played this fight also becomes Sly. */
public class ToolsOfTheTrade extends AbstractCard {
    public static final String ID = "Tools of the Trade";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public ToolsOfTheTrade() {
        super(ID, cardStrings.NAME, "green/power/tools_of_the_trade", 2, cardStrings.DESCRIPTION, CardType.POWER, CardColor.GREEN, CardRarity.RARE, CardTarget.SELF);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new SlyToolsPower(p)));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(1);
        }
    }

    public AbstractCard makeCopy() {
        return new ToolsOfTheTrade();
    }
}
