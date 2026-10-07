package anotherspirerework.cards.blue;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class ForceField extends AbstractCard {
    public static final String ID = "Force Field";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public ForceField() {
        super(ID, cardStrings.NAME, "blue/skill/forcefield", 2, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.BLUE, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseBlock = 12;
        this.baseMagicNumber = 4;
        this.magicNumber = this.baseMagicNumber;
    }

    public static int countPowers() {
        int count = 0;
        for (AbstractCard c : AbstractDungeon.player.hand.group) {
            if (c.type == CardType.POWER) {
                count++;
            }
        }
        for (AbstractCard c : AbstractDungeon.player.drawPile.group) {
            if (c.type == CardType.POWER) {
                count++;
            }
        }
        for (AbstractCard c : AbstractDungeon.player.discardPile.group) {
            if (c.type == CardType.POWER) {
                count++;
            }
        }
        return count;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        applyPowers();
        addToBot(new GainBlockAction(p, p, this.block));
    }

    public void applyPowers() {
        int realBaseBlock = this.baseBlock;
        this.baseBlock += this.magicNumber * countPowers();
        super.applyPowers();
        this.baseBlock = realBaseBlock;
        this.isBlockModified = this.block != this.baseBlock;
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(2);
        }
    }

    public AbstractCard makeCopy() {
        return new ForceField();
    }
}
