package anotherspire.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;

/**
 * Energy is capped at 999 in EnergyPanel.addEnergy. totalCount is not just the panel's label: it
 * is the energy the player actually has (EnergyPanel.getCurrentEnergy() is what card costs are
 * paid with), so lifting this clamp lifts the cap on gaining energy too.
 *
 * Doubling effects (Double Energy -> AbstractPlayer.gainEnergy(EnergyPanel.totalCount)) add the
 * current energy to itself, which wraps around once the total passes the int limit, so the addition
 * is checked the same way the power and block patches check theirs (a result lower than the value it
 * started from means the int wrapped) and saturates instead of leaving the player with negative
 * energy.
 */
@SpirePatch(clz = EnergyPanel.class, method = "addEnergy", paramtypez = {int.class})
public class NoEnergyCapPatch {
    private static long uncapped;

    /** totalCount before the energy was added, used to spot int overflow. */
    private static int before;

    @SpirePrefixPatch
    public static void Prefix(int e) {
        before = EnergyPanel.totalCount;
        uncapped = (long) before + e;
    }

    @SpirePostfixPatch
    public static void Postfix() {
        if (before > 0 && EnergyPanel.totalCount < before) {
            EnergyPanel.totalCount = NoPowerCapPatch.saturate(uncapped);
        } else if (EnergyPanel.totalCount == 999 && uncapped > 999) {
            EnergyPanel.totalCount = NoPowerCapPatch.saturate(uncapped);
        }
    }
}
