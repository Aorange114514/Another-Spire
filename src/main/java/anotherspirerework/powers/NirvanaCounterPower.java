package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.DrawCardNextTurnPower;

/**
 * Nirvana: once 5 cards have been played this turn, draw 2 more cards next turn per Nirvana.
 *
 * <p>{@code amount} is the number of Nirvanas. The counter is the base game's own list of cards played
 * this turn, so it is shared by every copy and counts the cards played before the first Nirvana -
 * the Nirvana itself included. The check runs in {@code onAfterUseCard}, after the played Nirvana has
 * already been stacked, so a Nirvana played as the 5th card pays for itself too.
 */
public class NirvanaCounterPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpireRework.makeID("Nirvana");
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("Nirvana");

    /** Cards played in one turn needed to trigger. */
    public static final int CARDS_PER_TURN = 5;
    public static final int DRAW_PER_STACK = 2;

    /** Whether this turn's payout has already been granted. */
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

    private static int playedThisTurn() {
        return Math.min(AbstractDungeon.actionManager.cardsPlayedThisTurn.size(), CARDS_PER_TURN);
    }

    private void grant(int stacks) {
        flash();
        int draw = stacks * DRAW_PER_STACK;
        addToBot(new ApplyPowerAction(this.owner, this.owner, new DrawCardNextTurnPower(this.owner, draw), draw));
    }

    @Override
    public void onAfterUseCard(AbstractCard card, UseCardAction action) {
        if (!this.triggered && playedThisTurn() >= CARDS_PER_TURN) {
            this.triggered = true;
            grant(this.amount);
        }
    }

    /** A Nirvana played after this turn's payout still adds its own share. */
    @Override
    public void stackPower(int stackAmount) {
        super.stackPower(stackAmount);
        if (this.triggered) {
            grant(stackAmount);
        }
    }

    @Override
    public void atStartOfTurn() {
        this.triggered = false;
    }

    @Override
    public void updateDescription() {
        this.description = powerStrings.DESCRIPTIONS[0] + (this.amount * DRAW_PER_STACK) + powerStrings.DESCRIPTIONS[1];
    }

    /** The icon shows the cards played this turn (stops at 5), including zero. */
    @Override
    public void renderAmount(SpriteBatch sb, float x, float y, Color c) {
        FontHelper.renderFontRightTopAligned(sb, FontHelper.powerAmountFont, Integer.toString(playedThisTurn()),
                x, y, this.fontScale, c);
    }
}
