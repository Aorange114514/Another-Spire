package anotherspirerework.patches;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.actions.SlyPlayAction;
import anotherspirerework.util.SlyMark;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

/**
 * Sly: the two hooks that make the keyword work. A manual discard resolves the card's effect, and a
 * text rebuild keeps the keyword line at the top of a marked card.
 */
public class SlyPatch {
    /**
     * A discarded Sly card resolves its own effect. An ordinary card waits in the action queue until
     * the whole discard has finished; a Power is resolved right here, because it has to leave combat
     * instead of reaching the discard pile.
     */
    @SpirePatch(clz = AbstractCard.class, method = "triggerOnManualDiscard")
    public static class Discard {
        @SpirePostfixPatch
        public static void after(AbstractCard __instance) {
            if (!SlyMark.isSly(__instance) || !AnotherSpireRework.inCombat()) {
                return;
            }
            if (__instance.type == AbstractCard.CardType.POWER) {
                // moveToDiscardPile() has already put the card in the discard pile and started a soul
                // flying it there. Cancel that flight and undo the darkening, so hand.empower() can
                // run the single smooth "Power flies into the player" animation from the hand.
                AbstractDungeon.getCurrRoom().souls.remove(__instance);
                AbstractDungeon.player.discardPile.group.remove(__instance);
                __instance.lighten(true);
                SlyPlayAction.playNow(__instance);
                return;
            }
            AbstractDungeon.actionManager.addToBottom(new SlyPlayAction(__instance));
        }
    }

    /**
     * A card whose text is rebuilt from scratch (a Shiv, for one) must not lose the keyword line;
     * the mark lives in {@link SlyMark}, the description only mirrors it.
     */
    @SpirePatch(clz = AbstractCard.class, method = "initializeDescription")
    public static class Description {
        @SpirePrefixPatch
        public static void before(AbstractCard __instance) {
            if (SlyMark.isSly(__instance) && !SlyMark.showsKeyword(__instance)
                    && __instance.rawDescription != null) {
                __instance.rawDescription = SlyMark.keywordLine() + __instance.rawDescription;
            }
        }
    }
}
