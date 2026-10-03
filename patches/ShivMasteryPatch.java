package anotherspirerework.patches;

import anotherspirerework.powers.ShivMasteryPower;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;

/**
 * Unload: the first Shiv played each turn deals extra damage. The damage itself lives in the
 * Shiv's baseDamage (so the card face shows it), and this hook only has to make sure the Shiv
 * that is actually being played carries the bonus before its DamageAction is built.
 */
@SpirePatch(clz = Shiv.class, method = "use", paramtypez = {AbstractPlayer.class, AbstractMonster.class})
public class ShivMasteryPatch {
    @SpirePrefixPatch
    public static void Prefix(Shiv __instance) {
        if (AbstractDungeon.player == null) {
            return;
        }
        AbstractPower mastery = AbstractDungeon.player.getPower(ShivMasteryPower.POWER_ID);
        if (mastery instanceof ShivMasteryPower) {
            ShivMasteryPower power = (ShivMasteryPower) mastery;
            if (!power.shivPlayedThisTurn) {
                power.shivPlayedThisTurn = true;
                // The other Shivs lose the bonus first, then this one gets it (even if Accuracy
                // rewrote its base damage in the meantime).
                power.refreshShivs();
                __instance.baseDamage = ShivMasteryPower.shivBaseDamage(__instance) + power.amount;
                __instance.applyPowers();
                power.flash();
            }
        }
    }
}
