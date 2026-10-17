package anotherspirerework.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StoreRelic;

/**
 * Shop prices: an Uncommon relic costs 50 less and a Rare relic 100 less than the base game asks.
 *
 * <p>Only the shop's own slot is affected, and the tier read here is the one the relic carries,
 * which is also what {@code RareRelicFixes} rewrote for the two relics whose tier changed.
 */
public class ShopPricePatch {
    public static final int UNCOMMON_DISCOUNT = 50;
    public static final int RARE_DISCOUNT = 100;

    /** The base price after this mod's discount. */
    public static int discounted(AbstractRelic.RelicTier tier, int price) {
        if (tier == AbstractRelic.RelicTier.UNCOMMON) {
            return price - UNCOMMON_DISCOUNT;
        }
        if (tier == AbstractRelic.RelicTier.RARE) {
            return price - RARE_DISCOUNT;
        }
        return price;
    }

    @SpirePatch(clz = StoreRelic.class, method = SpirePatch.CONSTRUCTOR,
            paramtypez = {AbstractRelic.class, int.class, ShopScreen.class})
    public static class Price {
        @SpirePostfixPatch
        public static void after(StoreRelic __instance) {
            __instance.price = discounted(__instance.relic.tier, __instance.price);
        }
    }
}
