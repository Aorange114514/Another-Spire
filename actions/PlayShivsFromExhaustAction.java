package anotherspire.actions;

import anotherspire.powers.ShivMasteryPower;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.utility.UnlimboAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardQueueItem;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;

/**
 * Die Die Die: plays a copy of every Shiv sitting in the exhaust pile against the target. The
 * copies keep the Shiv's own Exhaust, so they land back in the exhaust pile and the player ends
 * up with twice as many Shivs in there.
 *
 * The copies are parked in limbo while they are being played (the same thing the base game does
 * in PlayTopCardAction), but they must not stay there: the base game treats limbo as scratch space,
 * and at the end of a turn it hands everything left in there that still has Retain back to the
 * player (DiscardAtEndOfTurnAction -> RestoreRetainedCardsAction) after marking it as fading out
 * (GameActionManager.cleanCardQueue). Since Unload gives Shivs Retain, leftovers would come back to
 * hand as invisible cards, so each copy gets an UnlimboAction right after its replay and loses the
 * Retain flags up front.
 */
public class PlayShivsFromExhaustAction extends AbstractGameAction {

    public PlayShivsFromExhaustAction(AbstractCreature target) {
        this.target = target;
        this.source = AbstractDungeon.player;
        this.duration = Settings.ACTION_DUR_FAST;
        this.actionType = ActionType.WAIT;
    }

    @Override
    public void update() {
        if (this.target == null || !(this.target instanceof AbstractMonster)) {
            this.isDone = true;
            return;
        }
        AbstractMonster monster = (AbstractMonster) this.target;
        boolean hasShivMastery = AbstractDungeon.player.hasPower(ShivMasteryPower.POWER_ID);
        for (AbstractCard c : new ArrayList<>(AbstractDungeon.player.exhaustPile.group)) {
            if (!(c instanceof Shiv)) {
                continue;
            }
            AbstractCard copy = c.makeStatEquivalentCopy();
            if (hasShivMastery) {
                // Copies start from the plain Shiv damage; the first one played gets the
                // Shiv mastery bonus from ShivMasteryPatch, the rest do not.
                copy.baseDamage = ShivMasteryPower.shivBaseDamage(copy);
            }
            copy.current_x = c.current_x;
            copy.current_y = c.current_y;
            copy.target_x = (float) Settings.WIDTH / 2.0F - 300.0F * Settings.scale;
            copy.target_y = (float) Settings.HEIGHT / 2.0F;
            copy.freeToPlayOnce = true;
            copy.calculateCardDamage(monster);
            // The copy is a transient replay, not a card the player keeps: no Retain, and out of
            // limbo as soon as the replay is over (the action queue only runs once the card queue
            // is empty, so the copies are still in limbo while they fly at the enemy).
            copy.retain = false;
            copy.selfRetain = false;
            AbstractDungeon.player.limbo.addToBottom(copy);
            AbstractDungeon.actionManager.addCardQueueItem(new CardQueueItem(copy, monster, 0, true, true), true);
            addToBot(new UnlimboAction(copy));
        }
        this.isDone = true;
    }
}
