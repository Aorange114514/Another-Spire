package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.DrawCardNextTurnPower;

/**
 * Nirvana: the fifth card played in a turn turns into "draw next turn".
 *
 * <p>Copies stack in {@code amount} - that is the number of Nirvanas running, so the pay out is
 * doubled per copy - while a separate counter tracks the cards played this turn. All copies share
 * that one counter, and the icon shows it instead of the stack level, zero included, which the
 * base game's amount rendering would not draw.
 */
public class NirvanaCounterPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpireRework.makeID("Nirvana");
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("Nirvana");

    /** Cards played in one turn needed to trigger. */
    public static final int CARDS_PER_TURN = 5;

    /** Cards played this turn; reset at the start of every turn. */
    private int played;
    private boolean triggered;

    public NirvanaCounterPower(AbstractCreature owner, int amount) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;
        updateDescription();
        loadRegion("nirvana");
    }

    @Override
    public void onUseCard(AbstractCard card, com.megacrit.cardcrawl.actions.utility.UseCardAction action) {
        if (this.triggered) {
            return;
        }
        this.played++;
        if (this.played >= CARDS_PER_TURN) {
            this.triggered = true;
            flash();
            int draw = this.amount * 2;
            addToBot(new ApplyPowerAction(this.owner, this.owner,
                    new DrawCardNextTurnPower(this.owner, draw), draw));
        }
    }

    @Override
    public void atStartOfTurn() {
        this.played = 0;
        this.triggered = false;
    }

    @Override
    public void updateDescription() {
        this.description = powerStrings.DESCRIPTIONS[0] + (this.amount * 2) + powerStrings.DESCRIPTIONS[1];
    }

    /** The icon shows the cards played this turn, including zero. */
    @Override
    public void renderAmount(SpriteBatch sb, float x, float y, Color c) {
        FontHelper.renderFontRightTopAligned(sb, FontHelper.powerAmountFont, Integer.toString(this.played),
                x, y, this.fontScale, c);
    }
}
