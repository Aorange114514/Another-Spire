package anotherspirerework.patches;

import anotherspirerework.effects.ChooseDeckCardsEffect;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;

/**
 * Drops the cards a picker still had selected when the run was torn down.
 *
 * <p>The game has one {@link GridCardSelectScreen} for the whole session and it outlives a run:
 * only the code that consumes a pick clears {@code selectedCards}, while
 * {@code AbstractDungeon.reset()} resets the dungeon but not that list. The reworked pickers drop
 * their pick as soon as their grid closes ({@link ChooseDeckCardsEffect.SettleOnClose}), so an
 * ordinary "leave the screen" no longer leaves anything behind; this hook covers the teardown that
 * clears the effect list without closing the grid first (a room restart mod, "Save and Quit", the
 * death screen returning to the menu).
 *
 * <p>After the reload the room updates from that same list. In a shop it is read as the card
 * removal service: the shop charges Gold, raises the purge price and plays the removal animation
 * for a card of the run that is already gone. Clearing the list with the run means no room can ever
 * see a selection that belongs to a run that no longer exists.
 */
@SpirePatch(clz = AbstractDungeon.class, method = "reset")
public class AbandonedPickPatch {
    @SpirePostfixPatch
    public static void Postfix() {
        ChooseDeckCardsEffect.forget();
        GridCardSelectScreen screen = AbstractDungeon.gridSelectScreen;
        if (screen == null) {
            return;
        }
        // The cards are stale objects once the deck is gone; dropping the glow keeps a card that
        // was still shown as picked from staying lit if any view keeps it around.
        for (AbstractCard card : screen.selectedCards) {
            card.stopGlowing();
            card.unhover();
            card.untip();
        }
        screen.selectedCards.clear();
    }
}
