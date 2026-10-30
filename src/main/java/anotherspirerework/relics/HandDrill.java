package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.InvinciblePower;
import com.megacrit.cardcrawl.relics.AbstractRelic;

/**
 * Hand Drill: the player's normal Attack damage ignores enemy Block, including the Heart's
 * Invincible shield. The vanilla "apply Vulnerable when an enemy's Block breaks" effect is gone.
 *
 * <p>Replaces the vanilla prototype through {@code patches.RelicSwapPatch}. The bypass cannot live
 * on the relic itself because it has to interrupt the shared damage pipeline, so it is a static
 * check plus two nested patches; they stay in this file so the relic owns every behavior change it
 * introduces.
 */
public class HandDrill extends com.megacrit.cardcrawl.relics.HandDrill {
    public HandDrill() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new HandDrill();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** The vanilla Block-break Vulnerable is removed; the bypass below replaces it. */
    @Override
    public void onBlockBroken(AbstractCreature m) {
    }

    /** True for the player's normal Attack damage while Hand Drill is owned. */
    public static boolean bypass(DamageInfo info) {
        return info != null && info.owner == AbstractDungeon.player
                && info.type == DamageInfo.DamageType.NORMAL
                && AbstractDungeon.player != null && AbstractDungeon.player.hasRelic(ID);
    }

    /** Damage that reaches a monster's Block is applied in full instead of being blocked. */
    @SpirePatch(clz = AbstractCreature.class, method = "decrementBlock")
    public static class PierceBlock {
        @SpirePrefixPatch
        public static SpireReturn<Integer> before(AbstractCreature __instance, DamageInfo info,
                int damageAmount) {
            return __instance instanceof AbstractMonster && bypass(info)
                    ? SpireReturn.Return(damageAmount) : SpireReturn.Continue();
        }
    }

    /** The Heart's Invincible cap does not apply to the bypassing damage either. */
    @SpirePatch(clz = InvinciblePower.class, method = "onAttackedToChangeDamage")
    public static class PierceInvincible {
        @SpirePrefixPatch
        public static SpireReturn<Integer> before(InvinciblePower __instance, DamageInfo info,
                int damageAmount) {
            return bypass(info) ? SpireReturn.Return(damageAmount) : SpireReturn.Continue();
        }
    }
}
