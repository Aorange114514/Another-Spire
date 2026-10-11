package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

/**
 * Spirit Shield: while in Calm, incoming attack damage is halved per copy.
 *
 * <p>Each copy halves again, so the multiplier is 1/2^n rather than 1 - 0.5n. Only NORMAL damage
 * is scaled: that is what monsters attack with, while Poison, HP loss and Thorns are untouched.
 */
public class SpiritShieldPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpireRework.makeID("SpiritShield");
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("SpiritShield");

    public SpiritShieldPower(AbstractCreature owner, int amount) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;
        updateDescription();
        loadRegion("mental_fortress");
    }

    @Override
    public float atDamageReceive(float damage, DamageInfo.DamageType type) {
        if (type != DamageInfo.DamageType.NORMAL || !inCalm()) {
            return damage;
        }
        return damage * (float) Math.pow(0.5D, this.amount);
    }

    private static boolean inCalm() {
        return AbstractDungeon.player != null && AbstractDungeon.player.stance != null
                && "Calm".equals(AbstractDungeon.player.stance.ID);
    }

    @Override
    public void updateDescription() {
        this.description = powerStrings.DESCRIPTIONS[0] + this.amount + powerStrings.DESCRIPTIONS[1];
    }
}
