package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

/**
 * Blur: every card drawn for the rest of this turn also grants Block.
 *
 * <p>The trigger lives in the mod's PostDraw subscriber, so it sees exactly the draws the base game
 * counts. The power asks to be removed as the player's turn ends, which is what "this turn" means
 * here; its own icon shows the Block it pays out.
 */
public class BlurDrawPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpireRework.makeID("Blur");
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("Blur");

    public BlurDrawPower(AbstractCreature owner, int amount) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;
        updateDescription();
        loadRegion("blur");
    }

    /** Called once per card the player draws. */
    public static void onCardDrawn(AbstractCard card) {
        if (AbstractDungeon.player == null || AbstractDungeon.player.powers == null) {
            return;
        }
        for (AbstractPower power : AbstractDungeon.player.powers) {
            if (power instanceof BlurDrawPower) {
                // Typed on purpose: addToBot is protected, so it is only reachable through a
                // reference of this class rather than through AbstractPower.
                BlurDrawPower blur = (BlurDrawPower) power;
                if (blur.amount > 0) {
                    blur.payOut();
                }
            }
        }
    }

    private void payOut() {
        flash();
        addToBot(new GainBlockAction(AbstractDungeon.player, AbstractDungeon.player, this.amount));
    }

    @Override
    public void atEndOfTurn(boolean isPlayer) {
        if (isPlayer) {
            addToBot(new RemoveSpecificPowerAction(this.owner, this.owner, this));
        }
    }

    @Override
    public void updateDescription() {
        this.description = powerStrings.DESCRIPTIONS[0] + this.amount + powerStrings.DESCRIPTIONS[1];
    }
}
