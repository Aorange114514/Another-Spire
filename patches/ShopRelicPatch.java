package anotherspirerework.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.shop.StoreRelic;
import java.util.ArrayList;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class ShopRelicPatch {

    private static boolean eligible(String id) {
        if (id.equals("Lee's Waffle")) {
            return (long) AbstractDungeon.player.currentHealth * 2 < AbstractDungeon.player.maxHealth;
        }
        if (id.equals("Chemical X")) {
            for (AbstractCard c : AbstractDungeon.player.masterDeck.group) { if (c.cost == -1) return true; }
            if (AbstractDungeon.shopScreen != null) {
                for (AbstractCard c : AbstractDungeon.shopScreen.coloredCards) { if (c.cost == -1) return true; }
                for (AbstractCard c : AbstractDungeon.shopScreen.colorlessCards) { if (c.cost == -1) return true; }
            }
            return false;
        }
        return true;
    }

    // Leave ineligible entries in the saved vanilla pool, so retry survives save/load.
    private static SpireReturn<String> select(AbstractRelic.RelicTier tier, boolean end) {
        if (tier != AbstractRelic.RelicTier.SHOP) return SpireReturn.Continue();
        ArrayList<String> pool = AbstractDungeon.shopRelicPool;
        for (int n = 0; n < pool.size(); n++) {
            int i = end ? pool.size() - 1 - n : n;
            String id = pool.get(i);
            if (eligible(id) && com.megacrit.cardcrawl.helpers.RelicLibrary.getRelic(id).canSpawn()) {
                pool.remove(i);
                return SpireReturn.Return(id);
            }
        }
        return SpireReturn.Return(AbstractDungeon.returnRandomRelicKey(AbstractRelic.RelicTier.UNCOMMON));
    }

    @SpirePatch(clz = AbstractDungeon.class, method = "returnRandomRelicKey")
    public static class Front {
        @SpirePrefixPatch public static SpireReturn<String> before(AbstractRelic.RelicTier tier) {
            return select(tier, false);
        }
    }
    @SpirePatch(clz = AbstractDungeon.class, method = "returnEndRandomRelicKey")
    public static class Back {
        @SpirePrefixPatch public static SpireReturn<String> before(AbstractRelic.RelicTier tier) {
            return select(tier, true);
        }
    }

    @SpirePatch(clz = StoreRelic.class, method = "purchaseRelic")
    public static class Courier {
        @SpireInstrumentPatch public static ExprEditor edit() {
            return new ExprEditor() {
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("returnRandomRelicEnd")) {
                        m.replace("{ $_ = $proceed(this.relic.tier == com.megacrit.cardcrawl.relics.AbstractRelic.RelicTier.SHOP && !com.megacrit.cardcrawl.dungeons.AbstractDungeon.shopRelicPool.isEmpty() ? com.megacrit.cardcrawl.relics.AbstractRelic.RelicTier.SHOP : $1); }");
                    }
                }
            };
        }
    }

    @SpirePatch(clz = com.megacrit.cardcrawl.relics.CallingBell.class, method = "update")
    public static class Bell {
        @SpireInsertPatch(locator = PositionRewards.class)
        public static void insert() {
            for (int i = 0; i < 2; i++) {
                AbstractDungeon.combatRewardScreen.rewards.add(new com.megacrit.cardcrawl.rewards.RewardItem(
                        AbstractDungeon.returnRandomScreenlessRelic(AbstractDungeon.returnRandomRelicTier())));
            }
        }
    }
    public static class PositionRewards extends SpireInsertLocator {
        public int[] Locate(javassist.CtBehavior method) throws Exception {
            return LineFinder.findInOrder(method, new Matcher.MethodCallMatcher(
                    com.megacrit.cardcrawl.screens.CombatRewardScreen.class, "positionRewards"));
        }
    }

    @SpirePatch(clz = com.megacrit.cardcrawl.relics.CallingBell.class, method = "getUpdatedDescription")
    public static class BellText {
        @SpirePostfixPatch public static String after(String __result) {
            return com.megacrit.cardcrawl.core.CardCrawlGame.languagePack.getRelicStrings(
                    anotherspirerework.AnotherSpireRework.makeID("Calling Bell")).DESCRIPTIONS[0];
        }
    }
}