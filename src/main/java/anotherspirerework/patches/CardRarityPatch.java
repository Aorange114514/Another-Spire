package anotherspirerework.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.green.AllOutAttack;
import com.megacrit.cardcrawl.cards.green.Dash;
import com.megacrit.cardcrawl.cards.green.EndlessAgony;

/**
 * Rarity-only reworks: these three keep their vanilla effect and only move to Common. Every pool,
 * shop price and compendium entry reads the prototype's rarity field, so setting it when the card
 * is constructed is the whole change; they are not in CardSwapPatch because nothing else moves.
 */
public class CardRarityPatch {
    static void makeCommon(AbstractCard card) {
        card.rarity = AbstractCard.CardRarity.COMMON;
    }

    @SpirePatch(clz = AllOutAttack.class, method = SpirePatch.CONSTRUCTOR)
    public static class AllOutAttackRarity {
        @SpirePostfixPatch
        public static void after(AllOutAttack __instance) {
            makeCommon(__instance);
        }
    }

    @SpirePatch(clz = Dash.class, method = SpirePatch.CONSTRUCTOR)
    public static class DashRarity {
        @SpirePostfixPatch
        public static void after(Dash __instance) {
            makeCommon(__instance);
        }
    }

    @SpirePatch(clz = EndlessAgony.class, method = SpirePatch.CONSTRUCTOR)
    public static class EndlessAgonyRarity {
        @SpirePostfixPatch
        public static void after(EndlessAgony __instance) {
            makeCommon(__instance);
        }
    }
}
