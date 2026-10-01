package anotherspire.powers;

import anotherspire.AnotherSpire;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

/** Devotion: draw a card whenever you gain Mantra. The trigger lives in the mod's PostPowerApply subscriber. */
public class DevotionDrawPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpire.makeID("DevotionDraw");
    private static final PowerStrings powerStrings = AnotherSpire.getPowerStrings("DevotionDraw");

    public DevotionDrawPower(AbstractCreature owner, int amount) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;
        updateDescription();
        loadRegion("devotion");
    }

    @Override
    public void stackPower(int stackAmount) {
        this.fontScale = 8.0F;
        this.amount += stackAmount;
        updateDescription();
    }

    @Override
    public void updateDescription() {
        if (this.amount > 1) {
            this.description = powerStrings.DESCRIPTIONS[0] + this.amount + powerStrings.DESCRIPTIONS[1];
        } else {
            this.description = powerStrings.DESCRIPTIONS[0] + this.amount + powerStrings.DESCRIPTIONS[2];
        }
    }
}
