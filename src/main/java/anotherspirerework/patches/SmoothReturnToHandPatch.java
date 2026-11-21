package anotherspirerework.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;

/**
 * returnToHand cards (Protect) glide straight back to the hand from the position the play
 * animation left them. The vanilla {@code moveToHand} first teleports the card to the draw-pile
 * corner and then lets it glide to the hand slot, which reads as a detour through the bottom
 * left; keeping the card's current position turns it into one pull-back motion.
 */
public class SmoothReturnToHandPatch {
    private static float keepX;
    private static float keepY;
    private static float keepScale;

    @SpirePatch(clz = CardGroup.class, method = "moveToHand", paramtypez = {AbstractCard.class})
    public static class MoveToHand {
        @SpirePrefixPatch
        public static void before(CardGroup __instance, AbstractCard c) {
            keepX = c.current_x;
            keepY = c.current_y;
            keepScale = c.drawScale;
        }

        @SpirePostfixPatch
        public static void after(CardGroup __instance, AbstractCard c) {
            c.current_x = keepX;
            c.current_y = keepY;
            c.drawScale = keepScale;
        }
    }
}
