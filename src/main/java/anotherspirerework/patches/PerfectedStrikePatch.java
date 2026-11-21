package anotherspirerework.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.red.PerfectedStrike;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

/**
 * Perfected Strike: the vanilla damage count walks hand, draw pile and discard pile. Strikes in the
 * exhaust pile now count as well, so exhausting a Strike no longer lowers the card's damage.
 */
public class PerfectedStrikePatch {

    @SpirePatch(clz = PerfectedStrike.class, method = "countCards")
    public static class CountExhaustedStrikes {
        @SpirePostfixPatch
        public static int after(int __result) {
            if (AbstractDungeon.player == null) {
                return __result;
            }
            int extra = 0;
            for (AbstractCard card : AbstractDungeon.player.exhaustPile.group) {
                if (PerfectedStrike.isStrike(card)) {
                    extra++;
                }
            }
            return __result + extra;
        }
    }
}
