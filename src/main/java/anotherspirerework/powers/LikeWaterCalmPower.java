package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.watcher.ChangeStanceAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

/** Like Water: enter Calm at the end of your turn. */
public class LikeWaterCalmPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpireRework.makeID("LikeWaterCalm");
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("LikeWaterCalm");

    public LikeWaterCalmPower(AbstractCreature owner, int amount) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = -1;
        this.type = PowerType.BUFF;
        updateDescription();
        loadRegion("like_water");
    }

    /** Extra copies change nothing, so the icon never shows a stack number (amount stays -1). */
    @Override
    public void stackPower(int stackAmount) {
    }

    @Override
    public void updateDescription() {
        this.description = powerStrings.DESCRIPTIONS[0];
    }

    @Override
    public void atEndOfTurn(boolean isPlayer) {
        if (isPlayer) {
            flash();
            addToBot(new ChangeStanceAction("Calm"));
        }
    }
}
