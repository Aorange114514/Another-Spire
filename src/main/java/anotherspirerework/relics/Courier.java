package anotherspirerework.relics;

import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.shop.StoreRelic;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

/**
 * The Courier: keeps its vanilla description and shop discount. Hidden extra effect: a relic bought
 * from the merchant is restocked at the same spot as another shop relic, falling back to the normal
 * tier only when the shop pool is empty.
 *
 * <p>Replaces the vanilla prototype through {@code patches.RelicSwapPatch}; the vanilla shop pulse
 * is inherited. The restock happens inside the merchant's own purchase code, so it is instrumented.
 */
public class Courier extends com.megacrit.cardcrawl.relics.Courier {
    public Courier() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new Courier();
    }

    /**
     * Vanilla asks for a relic of the bought relic's tier, which for a shop relic can be any tier.
     * Restocking the shop rug with a shop relic keeps the merchant's own layout.
     */
    @SpirePatch(clz = StoreRelic.class, method = "purchaseRelic")
    public static class ShopRestock {
        @SpireInstrumentPatch
        public static ExprEditor edit() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall call) throws CannotCompileException {
                    if (call.getMethodName().equals("returnRandomRelicEnd")) {
                        call.replace("{ $_ = $proceed(this.relic.tier"
                                + " == com.megacrit.cardcrawl.relics.AbstractRelic.RelicTier.SHOP"
                                + " && !com.megacrit.cardcrawl.dungeons.AbstractDungeon.shopRelicPool.isEmpty()"
                                + " ? com.megacrit.cardcrawl.relics.AbstractRelic.RelicTier.SHOP : $1); }");
                    }
                }
            };
        }
    }
}
