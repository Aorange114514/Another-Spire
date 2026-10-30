package anotherspirerework.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.core.Settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

/**
 * Repair of the two relic pools whose tier the rework changed.
 *
 * <p>{@code relicId -> tier} now lives in the relic classes themselves
 * ({@code relics.PrismaticShard} is BOSS, {@code relics.TinyHouse} is SHOP), so a new run builds its
 * pools correctly. A run saved before the change, however, carries its pools around in the save file
 * and {@code loadSave} reads them back verbatim: Prismatic Shard would sit in {@code shopRelicPool}
 * (where its BOSS tier prices it at #b999 Gold) and Tiny House in {@code bossRelicPool}, out of reach
 * of every shop. Both are moved to the pool their tier says they belong in.
 *
 * <p>The new position is drawn from the run seed instead of {@code Math.random}, so saving and
 * reloading in front of a shop or a boss chest cannot be used to reroll the pool. An entry the player
 * already owns is dropped rather than offered a second time, and nothing is added twice.
 */
public class RelicTierPatch {
    /** The pool a relic belongs in after the tier change; null for every other relic. */
    public static AbstractRelic.RelicTier poolFor(String relicId) {
        if ("PrismaticShard".equals(relicId)) {
            return AbstractRelic.RelicTier.BOSS;
        }
        if ("Tiny House".equals(relicId)) {
            return AbstractRelic.RelicTier.SHOP;
        }
        return null;
    }

    public static void normalizePools(ArrayList<String> shop, ArrayList<String> boss,
            Collection<String> owned, boolean diverse, Random rng) {
        for (int i = shop.size() - 1; i >= 0; i--) {
            String id = shop.get(i);
            if (poolFor(id) == AbstractRelic.RelicTier.BOSS) {
                shop.remove(i);
                if (!diverse && !owned.contains(id) && !boss.contains(id)) {
                    boss.add(rng.nextInt(boss.size() + 1), id);
                }
            }
        }
        for (int i = boss.size() - 1; i >= 0; i--) {
            String id = boss.get(i);
            if (poolFor(id) == AbstractRelic.RelicTier.SHOP) {
                boss.remove(i);
                if (!owned.contains(id) && !shop.contains(id)) {
                    shop.add(rng.nextInt(shop.size() + 1), id);
                }
            }
        }
    }

    /** Repairs the two pools of the run being loaded, with the player's relics already known. */
    public static void normalizeRelicPools() {
        if (AbstractDungeon.player == null || AbstractDungeon.shopRelicPool == null
                || AbstractDungeon.bossRelicPool == null) {
            return;
        }
        ArrayList<String> owned = new ArrayList<>();
        for (AbstractRelic relic : AbstractDungeon.player.relics) {
            owned.add(relic.relicId);
        }
        long seed = Settings.seed == null ? 0L : Settings.seed;
        // The Diverse daily modifier hands out Prismatic Shard itself, and the base game keeps it
        // out of the pools there; read the flag defensively so a run without mod data still loads.
        boolean diverse = ModHelper.enabledMods != null && ModHelper.isModEnabled("Diverse");
        normalizePools(AbstractDungeon.shopRelicPool, AbstractDungeon.bossRelicPool, owned, diverse,
                new Random(seed));
    }

    /**
     * {@code loadSave} installs the pools straight from the save file. It runs inside the dungeon
     * constructor, after the player and its relics were rebuilt, which is what the ownership check
     * needs. A new run builds its pools from the relic tiers instead, so it needs no repair.
     */
    @SpirePatch(clz = AbstractDungeon.class, method = "loadSave")
    public static class LoadedPools {
        @SpirePostfixPatch
        public static void after() {
            normalizeRelicPools();
        }
    }
}
