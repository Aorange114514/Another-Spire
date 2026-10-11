package anotherspirerework.patches;

import anotherspirerework.powers.JackOfAllTradesPower;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.core.EnergyManager;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;

/**
 * Reports the energy a card actually cost to Jack Of All Trades.
 *
 * <p>{@link EnergyManager#use(int)} is the one place the base game charges energy for a card, both
 * for normal costs (from {@code useCard}) and for X-cost cards, which pay with their own action.
 * Energy handed back or taken away elsewhere - Ice Cream, the Velvet Choker penalty, potions - goes
 * through the energy panel directly and is deliberately not counted, because the card counts what
 * the player spent, not what the panel moved.
 */
public class EnergySpentPatch {

    @SpirePatch(clz = EnergyManager.class, method = "use", paramtypez = {int.class})
    public static class Spend {
        @SpirePrefixPatch
        public static void before(EnergyManager __instance, int e) {
            int available = EnergyPanel.totalCount;
            int spent = Math.min(e, available);
            if (spent > 0) {
                JackOfAllTradesPower.onEnergySpent(spent);
            }
        }
    }
}
