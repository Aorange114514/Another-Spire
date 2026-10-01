package anotherspire.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.unique.RestoreRetainedCardsAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

/**
 * Safety net for cards that were left in limbo and then handed back to the player at the end of a
 * turn. GameActionManager.cleanCardQueue marks every card still in limbo as fading out, and
 * RestoreRetainedCardsAction moves the retained ones back into the hand without clearing that flag,
 * so they would come back invisible (only their playable glow shows). Anything that has just
 * arrived in hand is supposed to be visible, so this fades it back in.
 *
 * The mod's own limbo leftovers are cleaned up where they are created
 * (PlayShivsFromExhaustAction); this only has to repair cards that are already stuck, for example
 * in a save made before that fix.
 */
@SpirePatch(clz = RestoreRetainedCardsAction.class, method = "update")
public class RetainedCardFadeInPatch {
    @SpirePostfixPatch
    public static void Postfix() {
        if (AbstractDungeon.player == null) {
            return;
        }
        for (AbstractCard c : AbstractDungeon.player.hand.group) {
            if (c.fadingOut) {
                c.unfadeOut();
            }
        }
    }
}
