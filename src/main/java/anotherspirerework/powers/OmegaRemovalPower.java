package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.rewards.OmegaRemovalReward;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

/**
 * Omega: a combat-only marker counting how many Omega cards were played. Once the fight is over
 * the main class turns the stack count into that many removal rewards on the reward screen.
 */
public class OmegaRemovalPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpireRework.makeID("OmegaRemoval");
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("OmegaRemoval");

    public OmegaRemovalPower(AbstractCreature owner, int amount) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;
        updateDescription();
        loadRegion("omega");
    }

    @Override
    public void stackPower(int stackAmount) {
        this.fontScale = 8.0F;
        this.amount += stackAmount;
        updateDescription();
    }

    /** Called at the end of a won combat, before the reward screen is filled. */
    public static void addRewardsIfPending(AbstractRoom room) {
        if (AbstractDungeon.player == null || room == null) {
            return;
        }
        AbstractPower power = AbstractDungeon.player.getPower(POWER_ID);
        if (power == null || power.amount <= 0) {
            return;
        }
        for (int i = 0; i < power.amount; i++) {
            room.rewards.add(new OmegaRemovalReward());
        }
    }

    @Override
    public void updateDescription() {
        if (this.amount > 1) {
            this.description = powerStrings.DESCRIPTIONS[0] + this.amount + powerStrings.DESCRIPTIONS[2];
        } else {
            this.description = powerStrings.DESCRIPTIONS[0] + this.amount + powerStrings.DESCRIPTIONS[1];
        }
    }
}
