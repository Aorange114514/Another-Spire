package anotherspirerework.patches;

import anotherspirerework.cards.blue.Stack;
import anotherspirerework.powers.DrawPileShuffleListener;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.EmptyDeckShuffleAction;
import com.megacrit.cardcrawl.actions.common.PutOnDeckAction;
import com.megacrit.cardcrawl.actions.common.ShuffleAction;
import com.megacrit.cardcrawl.actions.defect.ShuffleAllAction;
import com.megacrit.cardcrawl.cards.SoulGroup;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import java.util.ArrayList;

/**
 * Fires "the draw pile was shuffled" for Stack, Chrysalis, Metamorphosis and Matryoshka.
 *
 * <p>The base game shuffles the discard pile and then flies every card into the draw pile over
 * several frames; the shuffle action reports itself as done long before that animation is.
 * Interrupting it would cut the animation the player expects to see, so the event is deferred:
 *
 * <ol>
 *   <li>EmptyDeckShuffleAction finishing only marks a shuffle as pending. ShuffleAllAction (Reboot)
 *       is the same, except that it leaves the hand behind: on its first update it queues a
 *       PutOnDeckAction that moves the whole hand into the draw pile, and the card playing it -
 *       Reboot - queues its own ShuffleAction right behind that one. The event waits for the whole
 *       flow, so {@code waitingForHandOnDeck} and {@code waitingForFinalShuffle} keep it pending
 *       until the hand is really on the draw pile and the draw pile has really been shuffled.
 *       Arming the event when ShuffleAllAction ended opened the Metamorphosis picker before
 *       PutOnDeckAction ran, so the card the player had just picked was moved into the draw pile
 *       together with the rest of the hand and the pick had no effect.</li>
 *   <li>The card draw that follows waits for {@link SoulGroup#isActive()} to go false - exactly
 *       like the base game already does before drawing - and the event fires right there, after
 *       the animation. That draw is the one the shuffle queued behind itself, so it is put back at
 *       the top of the queue with its remaining amount and left empty for this pass; the queue then
 *       reads {@code [event actions..., draw]} and the search screen really does come before the
 *       remaining cards instead of after them.</li>
 * </ol>
 *
 * <p>The closing ShuffleAction is only watched while the flow armed by ShuffleAllAction is running:
 * the plain ShuffleAction that Deep Breath queues after EmptyDeckShuffleAction must not fire a
 * second event for the same shuffle.
 */
public class ShuffleTriggerPatch {
    // Package private like flush(): a private flag would be reached from the nested patch classes
    // through a javac synthetic accessor, so the contract test could no longer see which step of
    // the shuffle flow writes which flag.
    static boolean pendingShuffle;

    /** ShuffleAllAction is done, but the hand has not been moved onto the draw pile yet. */
    static boolean waitingForHandOnDeck;

    /** The hand is on the draw pile, but the draw pile has not been shuffled by ShuffleAction yet. */
    static boolean waitingForFinalShuffle;

    /** Cards the interrupted draw still has to draw once the event is over; 0 outside fire(). */
    private static int drawsAfterShuffle;

    /** For listeners: how many cards will be drawn right after this shuffle event. */
    public static int drawsAfterShuffle() {
        return drawsAfterShuffle;
    }

    public static void fire(int pendingDraws) {
        if (AbstractDungeon.player == null || AbstractDungeon.actionManager == null) {
            return;
        }
        AbstractRoom room = AbstractDungeon.getCurrRoom();
        if (room == null || room.phase != AbstractRoom.RoomPhase.COMBAT) {
            return;
        }
        // First, so that anything the event draws or searches already sees Stack on top.
        Stack.moveToTopOfDrawPile();
        drawsAfterShuffle = pendingDraws;
        try {
            for (AbstractPower power : new ArrayList<>(AbstractDungeon.player.powers)) {
                if (power instanceof DrawPileShuffleListener) {
                    ((DrawPileShuffleListener) power).onDrawPileShuffled();
                }
            }
        } finally {
            drawsAfterShuffle = 0;
        }
        anotherspirerework.relics.Matryoshka.drawOnShuffle();
    }

    /** Cleared when a fight starts or ends, so a shuffle that is never followed by a draw expires. */
    public static void reset() {
        pendingShuffle = false;
        waitingForHandOnDeck = false;
        waitingForFinalShuffle = false;
    }

    @SpirePatch(clz = EmptyDeckShuffleAction.class, method = "update")
    public static class Empty {
        @SpirePostfixPatch
        public static void after(EmptyDeckShuffleAction __instance) {
            if (__instance.isDone) {
                pendingShuffle = true;
            }
        }
    }

    @SpirePatch(clz = ShuffleAllAction.class, method = "update")
    public static class All {
        @SpirePostfixPatch
        public static void after(ShuffleAllAction __instance) {
            if (__instance.isDone) {
                // The discard pile is on its way, but the hand is not: ShuffleAllAction queued a
                // PutOnDeckAction for it on its first update and that action runs next.
                waitingForHandOnDeck = true;
            }
        }
    }

    /**
     * The hand -> draw pile half of ShuffleAllAction. Only after this may the shuffle count as
     * done as far as the hand is concerned: a pick added to the hand before it runs would be
     * shuffled straight back into the draw pile.
     */
    @SpirePatch(clz = PutOnDeckAction.class, method = "update")
    public static class HandOnDeck {
        @SpirePostfixPatch
        public static void after(PutOnDeckAction __instance) {
            if (__instance.isDone && waitingForHandOnDeck) {
                waitingForHandOnDeck = false;
                // Reboot queues its draw pile shuffle right behind the hand move.
                waitingForFinalShuffle = true;
            }
        }
    }

    /**
     * The shuffle that really reorders the draw pile, queued by the card behind the hand move.
     * Deep Breath's ShuffleAction is not part of such a flow and is left alone.
     */
    @SpirePatch(clz = ShuffleAction.class, method = "update")
    public static class FinalShuffle {
        @SpirePostfixPatch
        public static void after(ShuffleAction __instance) {
            if (__instance.isDone && waitingForFinalShuffle) {
                waitingForFinalShuffle = false;
                pendingShuffle = true;
            }
        }
    }

    /**
     * The moment the animation is over: the event fires here, ahead of the draws the shuffle
     * interrupted.
     *
     * <p>Firing while the following draw runs is not enough on its own. Whatever the event queues
     * with {@code addToTop} only becomes the next action once the draw action that is running
     * finishes, so the picker used to open after every remaining card had been drawn. The draw is
     * therefore emptied and put back at the top of the queue under the event's own actions: the
     * picker (and the card it adds to the hand) resolves first, the remaining cards afterwards.
     *
     * <p>While the player has a screen open - a deck view, the map, a card inspection, a reward
     * screen - the event is left pending. Anything it does (drawing, opening a search screen) moves
     * cards, and the base game's own screens animate the very same card objects, so firing in the
     * middle of that makes cards drift or appear stuck inside the screen that is open.
     */
    @SpirePatch(clz = DrawCardAction.class, method = "update")
    public static class Deferred {
        @SpirePrefixPatch
        public static void before(DrawCardAction __instance) {
            // Every action queued ahead of this draw has already run, so a ShuffleAllAction flow
            // that queued no closing ShuffleAction cannot be waited for any longer.
            if (waitingForFinalShuffle) {
                waitingForFinalShuffle = false;
                pendingShuffle = true;
            }
            flush(__instance);
        }
    }

    /**
     * The frame tick for a shuffle whose draw ended before the animation did.
     *
     * <p>"No Draw" and a full hand both end the draw that follows a shuffle on its very first
     * update, long before {@link SoulGroup#isActive()} goes false. Nothing polls the pending event
     * after that, so it used to sit there until some later draw happened to run - Deep Breath under
     * Battle Trance shuffled, triggered nothing, and then fired at the next unrelated draw. This is
     * called once per frame after the dungeon update, so the event fires as soon as the animation
     * is over whether or not a draw is still around to carry it.
     */
    public static void tick() {
        flush(AbstractDungeon.actionManager == null
                ? null : AbstractDungeon.actionManager.currentAction);
    }

    /**
     * Fires the event once the animation is over and no screen is in the way. Package private on
     * purpose: the nested patch above calls it directly, while a private method would compile to a
     * synthetic accessor instead and hide the real call from the contract test.
     *
     * @param interrupted the draw the event interrupts, or null when nothing is being interrupted
     *                    (the queue is idle, or busy with something that is not a draw).
     */
    static void flush(AbstractGameAction interrupted) {
        if (!pendingShuffle || waitingForHandOnDeck || waitingForFinalShuffle) {
            return;
        }
        if (AbstractDungeon.getCurrRoom() == null
                || AbstractDungeon.player == null || AbstractDungeon.actionManager == null) {
            return;
        }
        if (AbstractDungeon.isScreenUp || com.megacrit.cardcrawl.core.CardCrawlGame.isPopupOpen) {
            return;
        }
        if (SoulGroup.isActive()) {
            return;
        }
        pendingShuffle = false;
        int pendingDraws = 0;
        if (interrupted instanceof DrawCardAction && !interrupted.isDone) {
            DrawCardAction draw = (DrawCardAction) interrupted;
            int remaining = draw.amount;
            if (remaining > 0) {
                // Queued with clearDrawHistory = false, like the continuation the shuffle branch of
                // DrawCardAction builds itself; the pass that is running right now has already
                // handled the history for this draw.
                draw.amount = 0;
                AbstractDungeon.actionManager.addToTop(new DrawCardAction(remaining, false));
                // Only the cards that will really arrive: the draw stops once the hand is full.
                pendingDraws = Math.min(remaining,
                        Math.max(0, basemod.BaseMod.MAX_HAND_SIZE - AbstractDungeon.player.hand.size()));
            }
        }
        fire(pendingDraws);
    }
}
