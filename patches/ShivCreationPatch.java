package anotherspire.patches;

import anotherspire.powers.ShivMasteryPower;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;

/**
 * Unload: a Shiv made while the Shiv mastery power is up is born Retaining and already shows the
 * bonus damage it would deal if it turns out to be the first Shiv played this turn.
 */
@SpirePatch(clz = Shiv.class, method = SpirePatch.CONSTRUCTOR)
public class ShivCreationPatch {
    @SpirePostfixPatch
    public static void Postfix(Shiv __instance) {
        if (AbstractDungeon.player == null) {
            return;
        }
        AbstractPower mastery = AbstractDungeon.player.getPower(ShivMasteryPower.POWER_ID);
        if (mastery instanceof ShivMasteryPower) {
            ShivMasteryPower power = (ShivMasteryPower) mastery;
            power.retainShiv(__instance);
            power.setShivDamage(__instance);
        }
    }
}
