package anotherspirerework.cards.red;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.util.SaturatingMath;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.vfx.combat.VerticalImpactEffect;

public class HeavyBlade extends AbstractCard {
    public static final String ID = "Heavy Blade";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public HeavyBlade() {
        super(ID, cardStrings.NAME, "red/attack/heavy_blade", 2, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.RED, CardRarity.UNCOMMON, CardTarget.ENEMY);
        this.baseDamage = 15;
        this.baseMagicNumber = 5;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        if (m != null) {
            addToBot(new VFXAction(new VerticalImpactEffect(m.hb.cX + m.hb.width / 4.0F, m.hb.cY - m.hb.height / 4.0F)));
        }
        addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn), AbstractGameAction.AttackEffect.BLUNT_HEAVY));
    }

    public void applyPowers() {
        AbstractPower strength = AbstractDungeon.player.getPower("Strength");
        int original = strength == null ? 0 : strength.amount;
        if (strength != null) {
            // The base game multiplies the power and divides it back afterwards, which wraps around
            // once Strength gets big (uncapped Strength easily passes 400 million, and the divide
            // cannot undo the overflow, leaving the player with negative Strength). Saturate the
            // temporary value instead, and restore the exact original afterwards.
            strength.amount = SaturatingMath.multiply(original, this.magicNumber);
        }
        try {
            super.applyPowers();
        } finally {
            if (strength != null) {
                strength.amount = original;
            }
        }
    }

    public void calculateCardDamage(AbstractMonster mo) {
        AbstractPower strength = AbstractDungeon.player.getPower("Strength");
        int original = strength == null ? 0 : strength.amount;
        if (strength != null) {
            strength.amount = SaturatingMath.multiply(original, this.magicNumber);
        }
        try {
            super.calculateCardDamage(mo);
        } finally {
            if (strength != null) {
                strength.amount = original;
            }
        }
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeDamage(5);
            upgradeMagicNumber(2);
        }
    }

    public AbstractCard makeCopy() {
        return new HeavyBlade();
    }
}
