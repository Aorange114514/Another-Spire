package anotherspirerework.actions;

import anotherspirerework.powers.ShivMasteryPower;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;

/**
 * Die Die Die: plays a copy of every Shiv sitting in the exhaust pile against the target. The
 * copies keep the Shiv's own Exhaust, so they land back in the exhaust pile and the player ends
 * up with twice as many Shivs in there.
 *
 * Pending copies stay outside limbo/cardQueue. Each completed UseCardAction schedules only the
 * next copy, so Time Warp cannot clear future plays. Transient copies do not retain.
 */
public class PlayShivsFromExhaustAction extends AbstractGameAction {
    private static final IdentityHashMap<AbstractCard, AbstractGameAction> continuations = new IdentityHashMap<>();

    public static void reset() { continuations.clear(); }

    @SpirePatch(clz = UseCardAction.class, method = "update")
    public static class ContinueReplay {
        @SpirePostfixPatch
        public static void after(UseCardAction __instance, AbstractCard ___targetCard) {
            if (__instance.isDone) {
                AbstractGameAction next = continuations.remove(___targetCard);
                if (next != null) { AbstractDungeon.actionManager.addToBottom(next); }
            }
        }
    }

    private static class ReplayNext extends AbstractGameAction {
        private final ArrayList<AbstractCard> copies;
        private final AbstractMonster monster;
        private final int index;
        ReplayNext(ArrayList<AbstractCard> copies, AbstractMonster monster, int index) {
            this.copies = copies;
            this.monster = monster;
            this.index = index;
        }
        public void update() {
            if (index < copies.size() && !monster.isDeadOrEscaped() && !AbstractDungeon.getMonsters().areMonstersBasicallyDead()) {
                AbstractCard copy = copies.get(index);
                copy.calculateCardDamage(monster);
                continuations.put(copy, new ReplayNext(copies, monster, index + 1));
                addToBot(new NewQueueCardAction(copy, monster, false, true) {
                    public void update() {
                        super.update();
                        // Time Warp queues an end-turn marker before our continuation.
                        // Finish this replay before allowing that marker to execute.
                        ArrayList<com.megacrit.cardcrawl.cards.CardQueueItem> endMarkers = new ArrayList<>();
                        for (com.megacrit.cardcrawl.cards.CardQueueItem item : AbstractDungeon.actionManager.cardQueue) {
                            if (item.card == null) endMarkers.add(item);
                        }
                        AbstractDungeon.actionManager.cardQueue.removeAll(endMarkers);
                        AbstractDungeon.actionManager.cardQueue.addAll(endMarkers);
                    }
                });
            }
            isDone = true;
        }
    }

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
        ArrayList<AbstractCard> copies = new ArrayList<>();
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
            // A transient replay must never return to hand through end-of-turn Retain.
            copy.retain = false;
            copy.selfRetain = false;
            // Like Omniscience, keep pending plays in the action queue, not cardQueue.
            // Time Warp clears cardQueue, but does not cancel these later actions.
            copies.add(copy);
        }
        addToBot(new ReplayNext(copies, monster, 0));
        this.isDone = true;
    }
}
