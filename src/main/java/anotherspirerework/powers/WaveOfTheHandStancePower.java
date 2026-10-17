package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.WeakPower;
import com.megacrit.cardcrawl.powers.watcher.WaveOfTheHandPower;
import com.megacrit.cardcrawl.stances.AbstractStance;

/**
 * Wave of the Hand: the vanilla power (same ID, icon and end-of-round removal), triggered by stance
 * changes instead of gaining Block.
 */
public class WaveOfTheHandStancePower extends WaveOfTheHandPower {
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings(POWER_ID);

    public WaveOfTheHandStancePower(AbstractCreature owner, int amount) {
        super(owner, amount);
        this.name = powerStrings.NAME;
    }

    @Override
    public void onGainedBlock(float blockAmount) {
    }

    @Override
    public void onChangeStance(AbstractStance oldStance, AbstractStance newStance) {
        if (!oldStance.ID.equals(newStance.ID)) {
            flash();
            for (AbstractMonster mo : AbstractDungeon.getCurrRoom().monsters.monsters) {
                addToBot(new ApplyPowerAction(mo, this.owner, new WeakPower(mo, this.amount, false), this.amount, true, AbstractGameAction.AttackEffect.NONE));
            }
        }
    }

    @Override
    public void updateDescription() {
        this.description = powerStrings.DESCRIPTIONS[0] + this.amount + powerStrings.DESCRIPTIONS[1];
    }
}
