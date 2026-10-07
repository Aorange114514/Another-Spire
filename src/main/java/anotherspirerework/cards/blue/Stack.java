package anotherspirerework.cards.blue;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Stack extends AbstractCard {
    public static final String ID = "Stack";
    private static final CardStrings strings = AnotherSpireRework.getCardStrings(ID);

    public Stack() {
        super(ID, strings.NAME, "blue/skill/stack", 1, strings.DESCRIPTION,
                CardType.SKILL, CardColor.BLUE, CardRarity.COMMON, CardTarget.SELF);
        baseBlock = 0;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        applyPowers();
        addToBot(new GainBlockAction(p, p, block));
        rawDescription = upgraded ? strings.UPGRADE_DESCRIPTION : strings.DESCRIPTION;
        initializeDescription();
    }

    public void applyPowers() {
        baseBlock = (AbstractDungeon.player == null ? 0 : AbstractDungeon.player.drawPile.size()) + (upgraded ? 3 : 0);
        super.applyPowers();
        rawDescription = (upgraded ? strings.UPGRADE_DESCRIPTION : strings.DESCRIPTION)
                + (AnotherSpireRework.inCombat() ? AnotherSpireRework.extendedDescription(strings, 0) : "");
        initializeDescription();
    }

    public void upgrade() {
        if (!upgraded) {
            upgradeName(); upgradeBlock(3);
            rawDescription = strings.UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }
    public AbstractCard makeCopy() { return new Stack(); }
}