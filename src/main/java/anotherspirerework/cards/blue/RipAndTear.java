package anotherspirerework.cards.blue;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.actions.ExhaustAllStatusAction;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.AttackDamageRandomEnemyAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * Rip and Tear: exhausts every Status card the player holds or has in their piles and hits a random
 * enemy once per card actually exhausted. The count only exists once the piles have been searched,
 * so the hits are queued from the action's callback and run right after it.
 */
public class RipAndTear extends AbstractCard {
    public static final String ID = "Rip and Tear";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public RipAndTear() {
        super(ID, cardStrings.NAME, "blue/attack/rip_and_tear", 2, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.BLUE, CardRarity.UNCOMMON, CardTarget.ALL_ENEMY);
        this.baseDamage = 7;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ExhaustAllStatusAction(count -> {
            for (int i = 0; i < count; i++) {
                addToTop(new AttackDamageRandomEnemyAction(this, AbstractGameAction.AttackEffect.SLASH_DIAGONAL));
            }
        }));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(1);
        }
    }

    public AbstractCard makeCopy() {
        return new RipAndTear();
    }
}
