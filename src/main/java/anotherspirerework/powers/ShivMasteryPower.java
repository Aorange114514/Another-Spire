package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

import java.util.ArrayList;

/**
 * Unload: your Shivs gain Retain, and the first Shiv you play each turn deals extra damage.
 * The bonus is written into the Shiv's own baseDamage (the same thing AccuracyPower does), so the
 * number printed on the card is the damage it really deals. As soon as the first Shiv has been
 * played the bonus is taken off the remaining Shivs again (see ShivMasteryPatch).
 */
public class ShivMasteryPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpireRework.makeID("ShivMastery");
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("ShivMastery");

    private static final int SHIV_DAMAGE = 4;
    private static final int SHIV_UPGRADE_DAMAGE = 2;

    /** Reset at the start of every turn, so only the first Shiv of the turn gets the bonus. */
    public boolean shivPlayedThisTurn = false;

    public ShivMasteryPower(AbstractCreature owner, int amount) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;
        updateDescription();
        loadRegion("accuracy");
        makeShivsRetain();
        refreshShivs();
    }

    @Override
    public void updateDescription() {
        this.description = powerStrings.DESCRIPTIONS[0] + this.amount + powerStrings.DESCRIPTIONS[1];
    }

    @Override
    public void stackPower(int stackAmount) {
        this.fontScale = 8.0F;
        this.amount += stackAmount;
        updateDescription();
        makeShivsRetain();
        refreshShivs();
    }

    @Override
    public void atStartOfTurn() {
        this.shivPlayedThisTurn = false;
        refreshShivs();
    }

    @Override
    public void onCardDraw(AbstractCard card) {
        retainShiv(card);
        setShivDamage(card);
    }

    @Override
    public void onDrawOrDiscard() {
        refreshShivs();
    }

    /** Shivs created while this power is up keep Retain too (the constructor hook lives in ShivCreationPatch). */
    public static void retainShiv(AbstractCard card) {
        if (card instanceof Shiv) {
            card.selfRetain = true;
        }
    }

    /** What a Shiv deals without this power's bonus: the vanilla value plus Accuracy. */
    public static int shivBaseDamage(AbstractCard card) {
        int base = card.upgraded ? SHIV_DAMAGE + SHIV_UPGRADE_DAMAGE : SHIV_DAMAGE;
        if (AbstractDungeon.player != null && AbstractDungeon.player.hasPower("Accuracy")) {
            base += AbstractDungeon.player.getPower("Accuracy").amount;
        }
        return base;
    }

    /** Writes the bonus (while it is still up for grabs) into one Shiv's base damage. */
    public void setShivDamage(AbstractCard card) {
        if (!(card instanceof Shiv)) {
            return;
        }
        card.baseDamage = shivBaseDamage(card) + (this.shivPlayedThisTurn ? 0 : this.amount);
        card.applyPowers();
    }

    /** Keeps every Shiv the player has in hand, in the draw pile or in the discard pile in sync. */
    public void refreshShivs() {
        if (AbstractDungeon.player == null) {
            return;
        }
        for (AbstractCard c : new ArrayList<>(AbstractDungeon.player.hand.group)) {
            setShivDamage(c);
        }
        for (AbstractCard c : new ArrayList<>(AbstractDungeon.player.drawPile.group)) {
            setShivDamage(c);
        }
        for (AbstractCard c : new ArrayList<>(AbstractDungeon.player.discardPile.group)) {
            setShivDamage(c);
        }
    }

    private static void makeShivsRetain() {
        if (AbstractDungeon.player == null) {
            return;
        }
        for (AbstractCard c : AbstractDungeon.player.hand.group) {
            retainShiv(c);
        }
        for (AbstractCard c : AbstractDungeon.player.drawPile.group) {
            retainShiv(c);
        }
        for (AbstractCard c : AbstractDungeon.player.discardPile.group) {
            retainShiv(c);
        }
    }
}
