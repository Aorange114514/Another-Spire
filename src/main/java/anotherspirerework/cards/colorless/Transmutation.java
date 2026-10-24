package anotherspirerework.cards.colorless;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.actions.ExhaustAllHandAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * Transmutation: exhausts the whole hand and replaces every card with a random one, using the same
 * random-card source as Dead Branch.
 */
public class Transmutation extends AbstractCard {
    public static final String ID = "Transmutation";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Transmutation() {
        super(ID, cardStrings.NAME, "colorless/skill/transmutation", 1, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.COLORLESS, CardRarity.RARE, CardTarget.SELF);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ExhaustAllHandAction(count -> {
            for (int i = 0; i < count; i++) {
                addToBot(new MakeTempCardInHandAction(AbstractDungeon.returnTrulyRandomCardInCombat().makeCopy(), false));
            }
        }));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(0);
        }
    }

    public AbstractCard makeCopy() {
        return new Transmutation();
    }
}
