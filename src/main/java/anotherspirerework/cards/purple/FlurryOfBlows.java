package anotherspirerework.cards.purple;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.utility.DiscardToHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.stances.AbstractStance;

/**
 * Flurry of Blows: comes back to the hand on every stance change, from wherever it is.
 *
 * <p>The base game only walks the discard pile when a stance changes, which is what the override
 * below answers; {@code FlurryOfBlowsPatch} adds the draw pile and the exhaust pile.
 */
public class FlurryOfBlows extends AbstractCard {
    public static final String ID = "FlurryOfBlows";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public FlurryOfBlows() {
        super(ID, cardStrings.NAME, "purple/attack/flurry_of_blows", 0, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.PURPLE, CardRarity.COMMON, CardTarget.ENEMY);
        this.baseDamage = 4;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn), AbstractGameAction.AttackEffect.BLUNT_HEAVY));
    }

    public void triggerExhaustedCardsOnStanceChange(AbstractStance newStance) {
        addToBot(new DiscardToHandAction(this));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeDamage(2);
        }
    }

    public AbstractCard makeCopy() {
        return new FlurryOfBlows();
    }
}
