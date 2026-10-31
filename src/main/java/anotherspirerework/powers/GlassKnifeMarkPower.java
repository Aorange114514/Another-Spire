package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

/**
 * Glass Knife's mark, shown on the enemy with the vanilla "Talk to the Hand" icon.
 * The mark only records the payout; the Shiv damage actions trigger it on a hit, so blocked hits
 * still pay. Talk to the Hand's BlockReturnPower fires on every attack, so it cannot be reused here.
 */
public class GlassKnifeMarkPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpireRework.makeID("GlassKnifeMark");
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("GlassKnifeMark");

    public GlassKnifeMarkPower(AbstractCreature owner, int blockAmount) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = blockAmount;
        this.type = PowerType.DEBUFF;
        updateDescription();
        loadRegion("talk_to_hand");
    }

    @Override
    public void stackPower(int stackAmount) {
        this.fontScale = 8.0F;
        this.amount += stackAmount;
        updateDescription();
    }

    @Override
    public void updateDescription() {
        this.description = powerStrings.DESCRIPTIONS[0] + this.amount + powerStrings.DESCRIPTIONS[1];
    }
}
