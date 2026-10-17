package anotherspirerework.cards.green;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.actions.BanePoisonAction;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * Bane: against a poisoned enemy the hit's unblocked damage is added back as Poison.
 *
 * <p>Whether the enemy is poisoned is decided when the card is played, while the amount can only be
 * read after the hit has resolved, so the two are split over two actions.
 */
public class Bane extends AbstractCard {
    public static final String ID = "Bane";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Bane() {
        super(ID, cardStrings.NAME, "green/attack/bane", 1, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.GREEN, CardRarity.UNCOMMON, CardTarget.ENEMY);
        this.baseDamage = 9;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
        if (m != null && m.hasPower(com.megacrit.cardcrawl.powers.PoisonPower.POWER_ID)) {
            addToBot(new BanePoisonAction(m, p));
        }
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeDamage(3);
        }
    }

    public AbstractCard makeCopy() {
        return new Bane();
    }
}
