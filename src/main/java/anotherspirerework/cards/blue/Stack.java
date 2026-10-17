package anotherspirerework.cards.blue;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;

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
        rawDescription = strings.DESCRIPTION;
        initializeDescription();
    }

    public void applyPowers() {
        baseBlock = AbstractDungeon.player == null ? 0 : AbstractDungeon.player.drawPile.size();
        super.applyPowers();
        rawDescription = strings.DESCRIPTION
                + (AnotherSpireRework.inCombat() ? AnotherSpireRework.extendedDescription(strings, 0) : "");
        initializeDescription();
    }

    /** Called by ShuffleTriggerPatch once a shuffle has landed: every Stack just shuffled in goes on top. */
    public static void moveToTopOfDrawPile() {
        CardGroup drawPile = AbstractDungeon.player.drawPile;
        for (AbstractCard c : new ArrayList<>(drawPile.group)) {
            if (c instanceof Stack) {
                drawPile.group.remove(c);
                drawPile.addToTop(c);
            }
        }
    }

    public void upgrade() {
        if (!upgraded) {
            upgradeName();
            upgradeBaseCost(0);
        }
    }

    public AbstractCard makeCopy() { return new Stack(); }
}
