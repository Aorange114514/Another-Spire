package anotherspirerework.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.status.Wound;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PotionHelper;
import com.megacrit.cardcrawl.potions.PotionSlot;
import com.megacrit.cardcrawl.relics.*;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.screens.CombatRewardScreen;
import java.util.ArrayList;

/** Shared behavior for vanilla instances and the legacy wrapper. */
public class ReportedRelicFixes {
    private static boolean brewingRewards;
    public static boolean penalizeChoker(int cards) { return cards > 6; }
    public static boolean penalizeBark(int potions) { return potions < 2; }

    public static void loseEnergy() {
        AbstractDungeon.actionManager.addToBottom(new AbstractGameAction() {
            public void update() {
                AbstractDungeon.player.energy.use(1);
                isDone = true;
            }
        });
    }

    public static void markStart() {
        AbstractDungeon.actionManager.addToBottom(new MakeTempCardInDiscardAction(new Wound(), 2));
    }

    public static void chokerStart(AbstractRelic relic) {
        if (penalizeChoker(relic.counter)) loseEnergy();
        relic.counter = 0;
    }

    public static void barkStart() {
        int count = 0;
        for (com.megacrit.cardcrawl.potions.AbstractPotion potion : AbstractDungeon.player.potions) {
            if (!(potion instanceof PotionSlot)) count++;
        }
        if (penalizeBark(count)) loseEnergy();
    }

    public static void cauldronEquip(AbstractRelic relic) {
        int slots = AbstractDungeon.player.potionSlots;
        AbstractDungeon.player.potionSlots += 1;
        AbstractDungeon.player.potions.add(new PotionSlot(slots));
        ArrayList<String> ids = new ArrayList<>(PotionHelper.getPotions(AbstractDungeon.player.chosenClass, false));
        ids.add("Fruit Juice");
        for (int i = 0; i < 5; i++) {
            AbstractDungeon.getCurrRoom().addPotionToRewards(
                    PotionHelper.getPotion(ids.get(AbstractDungeon.potionRng.random(ids.size() - 1))));
        }
        brewingRewards = true;
        try {
            AbstractDungeon.combatRewardScreen.open(relic.DESCRIPTIONS[1]);
        } finally {
            brewingRewards = false;
        }
        AbstractDungeon.getCurrRoom().rewardPopOutTimer = 0.0F;
        AbstractDungeon.combatRewardScreen.rewards.removeIf(r -> r.type == RewardItem.RewardType.CARD);
        AbstractDungeon.combatRewardScreen.positionRewards();
    }

    @SpirePatch(clz = MarkOfPain.class, method = "atBattleStart")
    public static class Mark {
        @SpirePrefixPatch public static SpireReturn<Void> before() {
            markStart();
            return SpireReturn.Return(null);
        }
    }

    @SpirePatch(clz = Cauldron.class, method = "onEquip")
    public static class CauldronPickup {
        @SpirePrefixPatch public static SpireReturn<Void> before(Cauldron __instance) {
            cauldronEquip(__instance);
            return SpireReturn.Return(null);
        }
    }

    @SpirePatch(clz = VelvetChoker.class, method = "canPlay")
    public static class ChokerLimit {
        @SpirePrefixPatch public static SpireReturn<Boolean> before() { return SpireReturn.Return(true); }
    }
    @SpirePatch(clz = VelvetChoker.class, method = "onPlayCard")
    public static class ChokerCount {
        @SpirePrefixPatch public static SpireReturn<Void> before(VelvetChoker __instance) {
            if (__instance.counter < Integer.MAX_VALUE) __instance.counter++;
            return SpireReturn.Return(null);
        }
    }
    @SpirePatch(clz = VelvetChoker.class, method = "atTurnStart")
    public static class ChokerTurn {
        @SpirePrefixPatch public static SpireReturn<Void> before(VelvetChoker __instance) {
            chokerStart(__instance);
            return SpireReturn.Return(null);
        }
    }
    @SpirePatch(clz = VelvetChoker.class, method = "getUpdatedDescription")
    public static class ChokerDescription {
        @SpirePostfixPatch public static String after(String __result) {
            return com.megacrit.cardcrawl.core.CardCrawlGame.languagePack.getRelicStrings(
                    anotherspirerework.AnotherSpireRework.makeID("Velvet Choker")).DESCRIPTIONS[0];
        }
    }
    @SpirePatch(clz = VelvetChoker.class, method = "updateDescription")
    public static class ChokerRefresh {
        @SpirePostfixPatch public static void after(VelvetChoker __instance) {
            __instance.description = ChokerDescription.after(__instance.description);
            __instance.tips.clear();
            __instance.tips.add(new com.megacrit.cardcrawl.helpers.PowerTip(__instance.name, __instance.description));
        }
    }
    @SpirePatch(clz = SacredBark.class, method = "onEquip")
    public static class BarkEquip {
        @SpirePostfixPatch public static void after() { AbstractDungeon.player.energy.energyMaster++; }
    }
    @SpirePatch(clz = AbstractRelic.class, method = "onUnequip")
    public static class BarkUnequip {
        @SpirePostfixPatch public static void after(AbstractRelic __instance) {
            if (__instance instanceof SacredBark) AbstractDungeon.player.energy.energyMaster--;
        }
    }
    @SpirePatch(clz = AbstractRelic.class, method = "atTurnStart")
    public static class BarkTurn {
        @SpirePostfixPatch public static void after(AbstractRelic __instance) {
            if (__instance instanceof SacredBark) barkStart();
        }
    }

    // Convert only after callers have inspected cardReward.cards.size(). A complete vanilla
    // relic reward initializes the hitbox as well as type/text. Subsequent positioning is idempotent.
    @SpirePatch(clz = CombatRewardScreen.class, method = "positionRewards")
    public static class CrownRewards {
        @SpirePrefixPatch public static void before(CombatRewardScreen __instance) {
            if (brewingRewards || AbstractDungeon.player == null || !AbstractDungeon.player.hasRelic("Busted Crown")) return;
            for (int i = 0; i < __instance.rewards.size(); i++) {
                RewardItem old = __instance.rewards.get(i);
                if (old.type == RewardItem.RewardType.CARD) {
                    RewardItem replacement = new RewardItem(AbstractDungeon.returnRandomRelic(AbstractDungeon.returnRandomRelicTier()));
                    replacement.isDone = old.isDone;
                    replacement.ignoreReward = old.ignoreReward;
                    __instance.rewards.set(i, replacement);
                }
            }
        }
    }
}