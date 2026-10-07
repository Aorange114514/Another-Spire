package anotherspirerework.patches;

import anotherspirerework.relics.ReworkedRelic;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.powers.InvinciblePower;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rewards.RewardItem;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.CannotCompileException;

public class RelicReworkPatch {
    @SpirePatch(clz = AbstractRelic.class, method = SpirePatch.CONSTRUCTOR,
            paramtypez = {String.class, String.class, AbstractRelic.RelicTier.class, AbstractRelic.LandingSound.class})
    public static class VanillaTooltips {
        @SpirePostfixPatch public static void after(AbstractRelic __instance) {
            if (ReworkedRelic.supports(__instance.relicId) && !__instance.relicId.equals("CultistMask")) {
                __instance.description = com.megacrit.cardcrawl.core.CardCrawlGame.languagePack.getRelicStrings(
                        anotherspirerework.AnotherSpireRework.makeID(__instance.relicId)).DESCRIPTIONS[0];
                __instance.tips.clear();
                __instance.tips.add(new com.megacrit.cardcrawl.helpers.PowerTip(__instance.name, __instance.description));
            }
        }
    }
    public static boolean potionsLocked() {
        return TurnRelicFixes.potionsLocked();
    }
    public static boolean bypass(DamageInfo info) {
        return info.owner == AbstractDungeon.player && info.type == DamageInfo.DamageType.NORMAL
                && AbstractDungeon.player.hasRelic("HandDrill");
    }

    @SpirePatch(clz = RelicLibrary.class, method = "getRelic")
    public static class Swap {
        @SpirePostfixPatch
        public static AbstractRelic after(AbstractRelic __result) {
            return __result instanceof ReworkedRelic || !ReworkedRelic.supports(__result.relicId)
                    ? __result : new ReworkedRelic(__result);
        }
    }

    @SpirePatch(clz = com.megacrit.cardcrawl.relics.Sling.class, method = "atBattleStart")
    public static class DirectSling {
        @SpirePrefixPatch public static SpireReturn<Void> before() {
            AbstractDungeon.actionManager.addToBottom(new com.megacrit.cardcrawl.actions.common.ApplyPowerAction(
                    AbstractDungeon.player, AbstractDungeon.player, new com.megacrit.cardcrawl.powers.StrengthPower(AbstractDungeon.player, 2), 2));
            AbstractDungeon.actionManager.addToBottom(new com.megacrit.cardcrawl.actions.common.ApplyPowerAction(
                    AbstractDungeon.player, AbstractDungeon.player, new com.megacrit.cardcrawl.powers.DexterityPower(AbstractDungeon.player, 2), 2));
            return SpireReturn.Return(null);
        }
    }
    @SpirePatch(clz = com.megacrit.cardcrawl.relics.Sling.class, method = "getUpdatedDescription")
    public static class SlingText {
        @SpirePostfixPatch public static String after(String __result) {
            return com.megacrit.cardcrawl.core.CardCrawlGame.languagePack.getRelicStrings(
                    anotherspirerework.AnotherSpireRework.makeID("Sling")).DESCRIPTIONS[0];
        }
    }
    @SpirePatch(clz = com.megacrit.cardcrawl.relics.CultistMask.class, method = "atBattleStart")
    public static class DirectMask {
        @SpirePostfixPatch public static void after() {
            AbstractDungeon.actionManager.addToBottom(new com.megacrit.cardcrawl.actions.common.ApplyPowerAction(
                    AbstractDungeon.player, AbstractDungeon.player, new com.megacrit.cardcrawl.powers.RitualPower(AbstractDungeon.player, 1, true), 1));
        }
    }

    /** Disable the old ID-based restrictions without changing hasRelic globally. */
    public static ExprEditor ignoreRelic(final String id) {
        return new ExprEditor() {
            public void edit(MethodCall m) throws CannotCompileException {
                if (m.getMethodName().equals("hasRelic")) {
                    m.replace("{ $_ = $1.equals(\"" + id + "\") ? false : $proceed($$); }");
                }
            }
        };
    }

    @SpirePatch(clz = AbstractPlayer.class, method = "gainGold")
    public static class Gold {
        @SpireInstrumentPatch public static ExprEditor edit() { return ignoreRelic("Ectoplasm"); }
    }

    @SpirePatch(clz = AbstractPlayer.class, method = "obtainPotion", paramtypez = {AbstractPotion.class})
    public static class ObtainPotion {
        @SpireInstrumentPatch public static ExprEditor edit() { return ignoreRelic("Sozu"); }
    }

    @SpirePatch(clz = com.megacrit.cardcrawl.actions.common.ObtainPotionAction.class, method = "update")
    public static class ObtainPotionAction {
        @SpireInstrumentPatch public static ExprEditor edit() { return ignoreRelic("Sozu"); }
    }
    @SpirePatch(clz = RewardItem.class, method = "claimReward")
    public static class PotionReward {
        @SpireInstrumentPatch public static ExprEditor edit() { return ignoreRelic("Sozu"); }
    }
    @SpirePatch(clz = com.megacrit.cardcrawl.shop.StorePotion.class, method = "purchasePotion")
    public static class PotionShop {
        @SpireInstrumentPatch public static ExprEditor edit() { return ignoreRelic("Sozu"); }
    }

    @SpirePatch(clz = com.megacrit.cardcrawl.actions.utility.UseCardAction.class, method = "update")
    public static class BloodFinish {
        @SpirePostfixPatch public static void after(com.megacrit.cardcrawl.actions.utility.UseCardAction __instance) {
            if (__instance.isDone) bloodActive = false;
        }
    }

    @SpirePatch(clz = AbstractPotion.class, method = "getPotency", paramtypez = {})
    public static class Potency {
        @SpireInstrumentPatch public static ExprEditor edit() { return ignoreRelic("SacredBark"); }
    }

    @SpirePatch(clz = com.megacrit.cardcrawl.potions.AttackPotion.class, method = "initializeData")
    @SpirePatch(clz = com.megacrit.cardcrawl.potions.SkillPotion.class, method = "initializeData")
    @SpirePatch(clz = com.megacrit.cardcrawl.potions.PowerPotion.class, method = "initializeData")
    @SpirePatch(clz = com.megacrit.cardcrawl.potions.ColorlessPotion.class, method = "initializeData")
    @SpirePatch(clz = com.megacrit.cardcrawl.potions.DuplicationPotion.class, method = "initializeData")
    public static class PotionText {
        @SpireInstrumentPatch public static ExprEditor edit() { return ignoreRelic("SacredBark"); }
    }

    @SpirePatch(clz = com.megacrit.cardcrawl.potions.EntropicBrew.class, method = "use")
    @SpirePatch(clz = com.megacrit.cardcrawl.vfx.ObtainPotionEffect.class, method = "update")
    public static class Brew {
        @SpireInstrumentPatch public static ExprEditor edit() { return ignoreRelic("Sozu"); }
    }

    @SpirePatch(clz = com.megacrit.cardcrawl.potions.BloodPotion.class, method = "canUse")
    @SpirePatch(clz = com.megacrit.cardcrawl.potions.EntropicBrew.class, method = "canUse")
    @SpirePatch(clz = com.megacrit.cardcrawl.potions.FruitJuice.class, method = "canUse")
    @SpirePatch(clz = com.megacrit.cardcrawl.potions.SmokeBomb.class, method = "canUse")
    public static class OverridePotionUse {
        @SpirePostfixPatch public static boolean after(boolean __result) { return __result && !potionsLocked(); }
    }

    @SpirePatch(clz = AbstractPlayer.class, method = "damage")
    public static class Fairy {
        @SpireInstrumentPatch public static ExprEditor edit() {
            return new ExprEditor() {
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("hasPotion")) {
                        m.replace("{ $_ = $1.equals(\"FairyPotion\") && anotherspirerework.patches.RelicReworkPatch.potionsLocked() ? false : $proceed($$); }");
                    }
                }
            };
        }
    }

    @SpirePatch(clz = AbstractPotion.class, method = "canUse")
    public static class PotionUse {
        @SpirePostfixPatch
        public static boolean after(boolean __result) {
            return __result && !potionsLocked();
        }
    }

    @SpirePatch(clz = AbstractCreature.class, method = "decrementBlock")
    public static class PierceBlock {
        @SpirePrefixPatch
        public static SpireReturn<Integer> before(AbstractCreature __instance, DamageInfo info, int damageAmount) {
            return __instance instanceof AbstractMonster && bypass(info)
                    ? SpireReturn.Return(damageAmount) : SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = InvinciblePower.class, method = "onAttackedToChangeDamage")
    public static class PierceInvincible {
        @SpirePrefixPatch
        public static SpireReturn<Integer> before(InvinciblePower __instance, DamageInfo info, int damageAmount) {
            return bypass(info) ? SpireReturn.Return(damageAmount) : SpireReturn.Continue();
        }
    }

    public static boolean bloodActive;
    @SpirePatch(clz = AbstractPlayer.class, method = "useCard")
    public static class BloodCard {
        @SpirePrefixPatch
        public static void before(AbstractPlayer __instance, AbstractCard c) {
            bloodActive = false;
            AbstractRelic r = AbstractDungeon.player.getRelic("Black Blood");
            if (r instanceof ReworkedRelic && c.type == AbstractCard.CardType.ATTACK && c.costForTurn >= 2
                    && !((ReworkedRelic) r).bloodUsed) {
                ((ReworkedRelic) r).bloodUsed = true;
                bloodActive = true;
            }
        }
    }

    @SpirePatch(clz = AbstractMonster.class, method = "damage")
    public static class BloodHeal {
        @SpirePostfixPatch
        public static void after(AbstractMonster __instance, DamageInfo info) {
            if (bloodActive && info.owner == AbstractDungeon.player && info.type == DamageInfo.DamageType.NORMAL
                    && __instance.lastDamageTaken > 0 && AbstractDungeon.player.currentHealth > 0) {
                AbstractDungeon.player.heal(__instance.lastDamageTaken);
            }
        }
    }

}