package anotherspirerework.patches;

import anotherspirerework.actions.ReturnCardToHandAction;
import anotherspirerework.cards.purple.FlurryOfBlows;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import java.util.ArrayList;

/**
 * Flurry of Blows comes back to the hand from anywhere, not just the discard pile.
 *
 * <p>The base game's stance change walks the discard pile and hands every card to
 * {@code triggerExhaustedCardsOnStanceChange}, which the card itself uses; the draw pile and the
 * exhaust pile are never walked, so they are handled here, on the stance change the player reports.
 * The copies are taken before anything is queued, so no pile is modified while the game iterates it.
 */
public class FlurryOfBlowsPatch {

    @SpirePatch(clz = AbstractPlayer.class, method = "onStanceChange")
    public static class Anywhere {
        @SpirePostfixPatch
        public static void after(AbstractPlayer __instance, String id) {
            if (__instance == null || __instance != AbstractDungeon.player) {
                return;
            }
            returnFrom(__instance.drawPile.group, false);
            returnFrom(__instance.exhaustPile.group, true);
        }
    }

    /** Package private: the hook above is a nested class, and a private method would be reached
     *  through a javac synthetic bridge instead of a direct call. */
    static void returnFrom(ArrayList<AbstractCard> pile, boolean fromExhaust) {
        for (AbstractCard card : new ArrayList<>(pile)) {
            if (card instanceof FlurryOfBlows) {
                AbstractDungeon.actionManager.addToBottom(new ReturnCardToHandAction(card, fromExhaust));
            }
        }
    }
}
