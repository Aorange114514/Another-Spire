package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

import java.util.ArrayList;

/**
 * Jack Of All Trades: every four energy spent pays one back.
 *
 * <p>Each copy of the card adds its own instance of this power instead of stacking, which is what
 * makes every copy show its own counter. The counter is kept out of {@code amount} because the icon
 * has to draw it - zero included, which the base game's amount rendering would not draw.
 */
public class JackOfAllTradesPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpireRework.makeID("JackOfAllTrades");
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("JackOfAllTrades");

    /** Energy spent per payout. */
    public static final int ENERGY_PER_PAYOUT = 4;

    /** Energy spent since the last payout; lives for the current combat only. */
    private int spent;

    public JackOfAllTradesPower(AbstractCreature owner) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = 1;
        this.type = PowerType.BUFF;
        updateDescription();
        loadRegion("energized_blue");
    }

    /** Called by {@code EnergySpentPatch} with the energy a card really cost. */
    public static void onEnergySpent(int amount) {
        if (amount <= 0 || AbstractDungeon.player == null || AbstractDungeon.player.powers == null) {
            return;
        }
        for (AbstractPower power : new ArrayList<>(AbstractDungeon.player.powers)) {
            if (power instanceof JackOfAllTradesPower) {
                ((JackOfAllTradesPower) power).countSpent(amount);
            }
        }
    }

    private void countSpent(int amount) {
        this.spent += amount;
        boolean paid = false;
        while (this.spent >= ENERGY_PER_PAYOUT) {
            this.spent -= ENERGY_PER_PAYOUT;
            paid = true;
            addToBot(new GainEnergyAction(1));
        }
        if (paid) {
            flash();
        }
        updateDescription();
    }

    @Override
    public void updateDescription() {
        this.description = powerStrings.DESCRIPTIONS[0];
    }

    /** The icon shows the energy towards the next payout, including zero. */
    @Override
    public void renderAmount(SpriteBatch sb, float x, float y, Color c) {
        FontHelper.renderFontRightTopAligned(sb, FontHelper.powerAmountFont, Integer.toString(this.spent),
                x, y, this.fontScale, c);
    }
}
