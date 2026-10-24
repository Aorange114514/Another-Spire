package anotherspirerework.patches;

import anotherspirerework.actions.ReturnCardToHandAction;
import anotherspirerework.cards.purple.FlurryOfBlows;
import com.evacipated.cardcrawl.modthespire.lib.LineFinder;
import com.evacipated.cardcrawl.modthespire.lib.Matcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertLocator;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.watcher.ChangeStanceAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import java.util.ArrayList;
import javassist.CtBehavior;

/**
 * Flurry of Blows comes back to the hand from anywhere, not just the discard pile.
 *
 * <p>The base game's stance change walks the discard pile and hands every card to
 * {@code triggerExhaustedCardsOnStanceChange}, which the card itself uses; the draw pile and the
 * exhaust pile are never walked, so they are handled here. The hook goes into
 * {@code ChangeStanceAction} rather than {@code AbstractPlayer.onStanceChange}, because the Watcher
 * overrides that method and the override would hide a patch on the base class. The insertion point
 * is the base game's own power loop, so the returns are queued before Rushdown's draw is queued and
 * therefore resolve first.
 */
public class FlurryOfBlowsPatch {

    @SpirePatch(clz = ChangeStanceAction.class, method = "update")
    public static class BeforeStancePowers {
        @SpireInsertPatch(locator = BeforePowersLocator.class)
        public static void beforePowers() {
            AbstractPlayer player = AbstractDungeon.player;
            if (player == null) {
                return;
            }
            returnFrom(player.drawPile);
            returnFrom(player.exhaustPile);
            returnFrom(player.discardPile);
        }
    }

    /** The first read of {@code player.powers} in the update is the stance-change power loop. */
    public static class BeforePowersLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception {
            return LineFinder.findInOrder(ctBehavior,
                    new Matcher.FieldAccessMatcher(AbstractPlayer.class, "powers"));
        }
    }

    /** Package private: the hook above is a nested class, and a private method would be reached
     *  through a javac synthetic bridge instead of a direct call. */
    static void returnFrom(CardGroup pile) {
        for (AbstractCard card : new ArrayList<>(pile.group)) {
            if (card instanceof FlurryOfBlows) {
                AbstractDungeon.actionManager.addToBottom(new ReturnCardToHandAction(card, pile));
            }
        }
    }
}
