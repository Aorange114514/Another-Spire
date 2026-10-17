package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

/**
 * Sadistic Nature: the Weak and Vulnerable on enemies are twice as strong.
 *
 * <p>This power only carries the switch and its text; the doubling itself is applied where the base
 * game reads those two powers, in {@code SadisticNaturePatch}, so it stacks with Paper Crane and
 * Paper Frog instead of replacing them.
 */
public class SadisticNaturePower extends AbstractPower {
    public static final String POWER_ID = AnotherSpireRework.makeID("Sadistic Nature");
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("Sadistic Nature");

    /** -1 keeps the icon but draws no stack number, the way the base game's Barricade does. */
    public static final int ICON_AMOUNT = -1;

    public SadisticNaturePower(AbstractCreature owner, int amount) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = ICON_AMOUNT;
        this.type = PowerType.BUFF;
        updateDescription();
        loadRegion("sadistic");
    }

    /** True while the player owns this power; the damage patches ask before they double. */
    public static boolean active() {
        return AbstractDungeon.player != null && AbstractDungeon.player.hasPower(POWER_ID);
    }

    /** Extra copies change nothing, so the icon never shows a stack level. */
    @Override
    public void stackPower(int stackAmount) {
        this.amount = ICON_AMOUNT;
    }

    @Override
    public void updateDescription() {
        this.description = powerStrings.DESCRIPTIONS[0];
    }
}
