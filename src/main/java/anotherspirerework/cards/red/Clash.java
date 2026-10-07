package anotherspirerework.cards.red;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.combat.ClashEffect;

public class Clash extends AbstractCard {
    public static final String ID = "Clash";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Clash() {
        super(ID, cardStrings.NAME, "red/attack/clash", 0, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.RED, CardRarity.COMMON, CardTarget.ENEMY);
        this.baseDamage = 14;
    }

    public static int countNonAttacks() {
        int count = 0;
        if (AbstractDungeon.player == null) {
            return count;
        }
        for (AbstractCard c : AbstractDungeon.player.hand.group) {
            if (c.type != CardType.ATTACK) {
                count++;
            }
        }
        return count;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        if (m != null) {
            addToBot(new VFXAction(new ClashEffect(m.hb.cX, m.hb.cY), 0.1F));
        }
        addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn), AbstractGameAction.AttackEffect.NONE));
    }

    public void calculateCardDamage(AbstractMonster mo) {
        int realBaseDamage = this.baseDamage;
        this.baseDamage -= 2 * countNonAttacks();
        try {
            super.calculateCardDamage(mo);
        } finally {
            this.baseDamage = realBaseDamage;
        }
        this.isDamageModified = this.damage != this.baseDamage;
    }

    public void applyPowers() {
        int realBaseDamage = this.baseDamage;
        this.baseDamage -= 2 * countNonAttacks();
        try {
            super.applyPowers();
        } finally {
            this.baseDamage = realBaseDamage;
        }
        this.isDamageModified = this.damage != this.baseDamage;
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeDamage(4);
        }
    }

    public AbstractCard makeCopy() {
        return new Clash();
    }
}
