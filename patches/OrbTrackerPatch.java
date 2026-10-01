package anotherspire.patches;

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
        }
    }
}
