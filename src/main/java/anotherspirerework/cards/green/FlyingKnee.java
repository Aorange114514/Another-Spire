package anotherspirerework.cards.green;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.WeakPower;

/**
 * Flying Knee: doubles up against a Weakened enemy instead of granting next turn's energy.
 *
 * <p>The doubling happens in calculateCardDamage, which the base game runs both while the card is
 * being hovered and again right before it is played, so the number on the card and the number the
 * enemy takes can never disagree.
 */
public class FlyingKnee extends AbstractCard {
    public static final String ID = "Flying Knee";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public FlyingKnee() {
        super(ID, cardStrings.NAME, "green/attack/flying_knee", 1, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.GREEN, CardRarity.UNCOMMON, CardTarget.ENEMY);
        this.baseDamage = 8;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn), AbstractGameAction.AttackEffect.BLUNT_HEAVY));
    }

    @Override
    public void calculateCardDamage(AbstractMonster mo) {
        super.calculateCardDamage(mo);
        if (isWeakened(mo)) {
            this.damage *= 2;
            this.isDamageModified = true;
        }
    }

    private static boolean isWeakened(AbstractMonster mo) {
        return mo != null && mo.hasPower(WeakPower.POWER_ID);
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeDamage(3);
        }
    }

    public AbstractCard makeCopy() {
        return new FlyingKnee();
    }
}
