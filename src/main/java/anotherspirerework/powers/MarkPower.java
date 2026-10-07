package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

/**
 * Path to Victory's Mark. Unlike the vanilla MarkPower (which only reacted to playing Path to
 * Victory again) this debuff keeps no trigger of its own: marked enemies lose HP equal to their
 * Mark whenever they are attacked, which is handled by MarkAttackPatch.
 */
public class MarkPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpireRework.makeID("Mark");
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("Mark");

    public MarkPower(AbstractCreature owner, int amount) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.DEBUFF;
        updateDescription();
        loadRegion("pressure_points");
    }

    @Override
    public void updateDescription() {
        this.description = powerStrings.DESCRIPTIONS[0] + this.amount + powerStrings.DESCRIPTIONS[1];
    }

    @Override
    public void stackPower(int stackAmount) {
        this.fontScale = 8.0F;
        this.amount += stackAmount;
        updateDescription();
    }
}
