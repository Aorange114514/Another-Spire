package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

public class ShivPoisonPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpireRework.makeID("ShivPoison");

    /**
     * Envenom's buff. The field has to be named {@code powerStrings}: every other power in this mod
     * uses that name, and the power list screens read it by reflection to build the entry title.
     * Without it the title is never resolved and the base game logs
     * "PowerString: anotherspirerework.powers.ShivPoisonPower not found".
     */
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("ShivPoison");

    public ShivPoisonPower(AbstractCreature owner, int amount) {
        this.owner = owner;
        this.amount = amount;
        ID = POWER_ID;
        name = powerStrings.NAME;
        type = PowerType.BUFF;
        loadRegion("envenom");
        updateDescription();
    }

    public void updateDescription() {
        description = powerStrings.DESCRIPTIONS[0] + amount + powerStrings.DESCRIPTIONS[1];
    }
}