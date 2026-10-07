package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

/**
 * Mayhem: every 10 cards drawn pays out energy.
 *
 * <p>The pay out is kept in {@code amount}, which is the number of copies running and is what the
 * base game saves, so it survives a reload; the description shows it. The card counter lives in
 * its own field, because the icon has to show it instead of the stack level - zero included, which
 * the base game's amount rendering would not draw.
 */
public class ScuffleDrawPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpireRework.makeID("Scuffle");
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("Scuffle");

    public static final int CARDS_PER_ENERGY = 10;

    /** Cards drawn since the last payout; only lives for the current combat. */
    private int drawn;

    public ScuffleDrawPower(AbstractCreature owner, int amount) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;
        updateDescription();
        loadRegion("mayhem");
    }

    /** Called once per card the player draws. */
    public static void onCardDrawn(AbstractCard card) {
        if (AbstractDungeon.player == null || AbstractDungeon.player.powers == null) {
            return;
        }
        for (AbstractPower power : AbstractDungeon.player.powers) {
            if (power instanceof ScuffleDrawPower) {
                ((ScuffleDrawPower) power).countDraw();
            }
        }
    }

    private void countDraw() {
        this.drawn++;
        if (this.drawn >= CARDS_PER_ENERGY) {
            this.drawn = 0;
            flash();
            addToBot(new GainEnergyAction(this.amount));
        }
        updateDescription();
    }

    @Override
    public void updateDescription() {
        this.description = powerStrings.DESCRIPTIONS[0] + this.amount + powerStrings.DESCRIPTIONS[1];
    }

    /** The icon shows the cards towards the next payout, including zero. */
    @Override
    public void renderAmount(SpriteBatch sb, float x, float y, Color c) {
        FontHelper.renderFontRightTopAligned(sb, FontHelper.powerAmountFont, Integer.toString(this.drawn),
                x, y, this.fontScale, c);
    }
}

