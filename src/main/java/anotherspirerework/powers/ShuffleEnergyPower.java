package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

/** Chrysalis: gain energy whenever the draw pile is shuffled. */
public class ShuffleEnergyPower extends AbstractPower implements DrawPileShuffleListener {
    public static final String POWER_ID = AnotherSpireRework.makeID("Chrysalis");
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("Chrysalis");

    public ShuffleEnergyPower(AbstractCreature owner, int amount) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;
        updateDescription();
        loadRegion("energized_blue");
    }

    @Override
    public void onDrawPileShuffled() {
        flash();
        addToBot(new GainEnergyAction(this.amount));
    }

    @Override
    public void updateDescription() {
        this.description = powerStrings.DESCRIPTIONS[0];
    }
}
