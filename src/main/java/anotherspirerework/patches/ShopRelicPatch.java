package anotherspirerework.patches;

import anotherspirerework.relics.KeepInPool;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import java.util.ArrayList;

/**
 * Relic pool draws.
 *
 * <p>A conditional relic does not use up the run's one chance at it: when the pool hands out an ID
 * that cannot be offered right now, the entry is left where it is instead of being removed, so the
 * next shop, chest or reward can try again. The base game removes the entry and recurses instead.
 *
 * <ul>
 *   <li>A shop slot only accepts a relic whose own tier is SHOP. A run saved before the tier change
 *       still lists Prismatic Shard in the shop pool, and selling it from the rug would show the
 *       boss price of #b999 Gold; {@link RelicTierPatch} also moves it out of the pool.</li>
 *   <li>{@link KeepInPool} relics (the bottled ones before the third act) answer whether the pool
 *       has to keep them; the relic class owns that condition, not this patch.</li>
 *   <li>Chemical X and Lee's Waffle carry their spawn condition in their own {@code canSpawn()}, so
 *       they are handled by the same "leave it in the pool" branch.</li>
 * </ul>
 */
public class ShopRelicPatch {

    /** True when a shop slot may offer this relic right now. */
    public static boolean sellable(String id) {
        AbstractRelic relic = RelicLibrary.getRelic(id);
        return relic != null && relic.tier == AbstractRelic.RelicTier.SHOP && relic.canSpawn();
    }

    private static SpireReturn<String> select(AbstractRelic.RelicTier tier, boolean end) {
        if (tier == AbstractRelic.RelicTier.UNCOMMON) {
            return selectUncommon(end);
        }
        if (tier != AbstractRelic.RelicTier.SHOP) {
            return SpireReturn.Continue();
        }
        ArrayList<String> pool = AbstractDungeon.shopRelicPool;
        for (int n = 0; n < pool.size(); n++) {
            int i = end ? pool.size() - 1 - n : n;
            String id = pool.get(i);
            if (sellable(id)) {
                pool.remove(i);
                return SpireReturn.Return(id);
            }
        }
        return SpireReturn.Return(AbstractDungeon.returnRandomRelicKey(AbstractRelic.RelicTier.UNCOMMON));
    }

    /**
     * The act gate of the bottled relics and any other condition that only means "not yet". An entry
     * that is merely gated stays in the pool; an entry whose condition is permanent is dropped and
     * the draw is retried from the other end, exactly like the base game does. An empty pool falls
     * back to the next tier.
     */
    private static SpireReturn<String> selectUncommon(boolean end) {
        ArrayList<String> pool = AbstractDungeon.uncommonRelicPool;
        for (int n = 0; n < pool.size(); n++) {
            int i = end ? pool.size() - 1 - n : n;
            String id = pool.get(i);
            AbstractRelic relic = RelicLibrary.getRelic(id);
            if (relic instanceof KeepInPool && ((KeepInPool) relic).keepInPool()) {
                continue;
            }
            pool.remove(i);
            if (relic == null || !relic.canSpawn()) {
                return SpireReturn.Return(end
                        ? AbstractDungeon.returnEndRandomRelicKey(AbstractRelic.RelicTier.RARE)
                        : AbstractDungeon.returnRandomRelicKey(AbstractRelic.RelicTier.RARE));
            }
            return SpireReturn.Return(id);
        }
        // Only gated entries are left, which the base game would have drawn; the tier's own
        // empty-pool fallback is used instead, so the gate still holds.
        return SpireReturn.Return(end
                ? AbstractDungeon.returnEndRandomRelicKey(AbstractRelic.RelicTier.RARE)
                : AbstractDungeon.returnRandomRelicKey(AbstractRelic.RelicTier.RARE));
    }

    @SpirePatch(clz = AbstractDungeon.class, method = "returnRandomRelicKey")
    public static class Front {
        @SpirePrefixPatch
        public static SpireReturn<String> before(AbstractRelic.RelicTier tier) {
            return select(tier, false);
        }
    }

    @SpirePatch(clz = AbstractDungeon.class, method = "returnEndRandomRelicKey")
    public static class Back {
        @SpirePrefixPatch
        public static SpireReturn<String> before(AbstractRelic.RelicTier tier) {
            return select(tier, true);
        }
    }
}
