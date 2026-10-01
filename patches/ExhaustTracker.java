package anotherspire.patches;

import com.megacrit.cardcrawl.actions.GameActionManager;

/**
 * Remembers the combat turn on which the player last exhausted a card, which is what Seeing Red
 * asks about. It is reset when a combat ends so a stale turn number can never leak into the
 * next fight (both fights start counting at turn 1).
 */
public class ExhaustTracker {

    private static int lastExhaustTurn = -1;

    /** Called from AnotherSpire.receivePostExhaust. */
    public static void onCardExhausted() {
        lastExhaustTurn = GameActionManager.turn;
    }

    public static boolean exhaustedThisTurn() {
        return GameActionManager.turn == lastExhaustTurn;
    }

    /** Called from AnotherSpire.receivePostBattle. */
    public static void reset() {
        lastExhaustTurn = -1;
    }
}
