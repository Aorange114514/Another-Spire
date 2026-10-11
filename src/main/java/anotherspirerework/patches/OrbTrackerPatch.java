package anotherspirerework.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.EmptyOrbSlot;

/** Remembers the last orb channeled during the current combat (used by Rebound). */
@SpirePatch(clz = AbstractPlayer.class, method = "channelOrb", paramtypez = {AbstractOrb.class})
public class OrbTrackerPatch {
    public static AbstractOrb lastOrb = null;

    @SpirePostfixPatch
    public static void Postfix(AbstractPlayer __instance, AbstractOrb orbToSet) {
        if (!(orbToSet instanceof EmptyOrbSlot)) {
            lastOrb = orbToSet.makeCopy();
            // Dark charges itself at the end of every turn; that charge is part of the orb the
            // player sees, so it has to survive the copy onto the new instance.
            lastOrb.evokeAmount = orbToSet.evokeAmount;
        }
    }

    /**
     * A copy of the last orb channeled, with its charged values intact, or null when nothing has
     * been channeled yet this combat. Dark is the only orb the base game charges over time.
     */
    public static AbstractOrb copyLast() {
        if (lastOrb == null) {
            return null;
        }
        AbstractOrb copy = lastOrb.makeCopy();
        copy.evokeAmount = lastOrb.evokeAmount;
        return copy;
    }
}
