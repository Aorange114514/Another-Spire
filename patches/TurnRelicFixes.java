package anotherspirerework.patches;

import basemod.abstracts.CustomSavable;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.DexterityPower;
import com.megacrit.cardcrawl.relics.*;
import com.megacrit.cardcrawl.rooms.MonsterRoomElite;
import com.megacrit.cardcrawl.ui.panels.PotionPopUp;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

/** Direct vanilla-instance hooks; wrappers call the same helpers where necessary. */
public class TurnRelicFixes implements CustomSavable<Integer> {
    private static boolean potionUnlocked;
    private static int elites;
    @SpirePatch(clz = AbstractRelic.class, method = SpirePatch.CLASS)
    public static class State {
        public static SpireField<Integer> turns = new SpireField<>(() -> 0);
    }

    public static boolean serpentActive(int turn) { return turn >= 1 && turn <= 3; }
    public static boolean starActive(int kills) { return kills >= 5; }
    public static boolean potionLocked(boolean combat, boolean sozu, boolean unlocked) {
        return combat && sozu && !unlocked;
    }
    public static boolean potionsLocked() {
        return potionLocked(anotherspirerework.AnotherSpireRework.inCombat(),
                AbstractDungeon.player != null && AbstractDungeon.player.hasRelic("Sozu"), potionUnlocked);
    }
    public static void resetBattle() { potionUnlocked = false; }
    public static void discard(int slot) {
        if (potionsLocked() && slot >= 0 && slot < AbstractDungeon.player.potions.size()
                && !(AbstractDungeon.player.potions.get(slot) instanceof com.megacrit.cardcrawl.potions.PotionSlot)) {
            potionUnlocked = true;
            AbstractDungeon.player.getRelic("Sozu").flash();
        }
    }
    public static void prismEquip(int delta) {
        AbstractDungeon.player.energy.energyMaster += delta;
        AbstractDungeon.player.masterHandSize += delta;
    }
    public static void serpentTurn(AbstractRelic relic, int turn) {
        if (!serpentActive(turn)) return;
        relic.flash();
        AbstractDungeon.actionManager.addToBottom(new DrawCardAction(2));
    }
    public static int vanillaElites() {
        return CardCrawlGame.elites1Slain + CardCrawlGame.elites2Slain + CardCrawlGame.elites3Slain;
    }
    public static void victory(com.megacrit.cardcrawl.rooms.AbstractRoom room) {
        if (room instanceof MonsterRoomElite) elites++;
        syncOwnedStar();
    }
    public static void startGame() { elites = 0; resetBattle(); }
    public static int currentElites() { return Math.max(elites, vanillaElites()); }
    public static void syncStar(AbstractRelic relic) {
        if ("Black Star".equals(relic.relicId)) relic.counter = currentElites();
    }
    private static void syncOwnedStar() {
        if (AbstractDungeon.player == null) return;
        AbstractRelic relic = AbstractDungeon.player.getRelic("Black Star");
        if (relic != null) syncStar(relic);
    }
    public Integer onSave() { return currentElites(); }
    public void onLoad(Integer value) {
        elites = value == null ? vanillaElites() : value;
        syncOwnedStar();
    }
    /** Vanilla saves retain energy/draw fields but do not replay onEquip. */
    public static class StatMigration implements CustomSavable<Boolean> {
        public Boolean onSave() { return true; }
        public void onLoad(Boolean migrated) {
            if (Boolean.TRUE.equals(migrated) || AbstractDungeon.player == null) return;
            if (AbstractDungeon.player.hasRelic("PrismaticShard")) prismEquip(1);
            // Do not guess whether an old Serpent was vanilla or a wrapper: the save
            // contains only its ID, not its class or the source of hand-size bonuses.
        }
    }
    public static void starTurn() {
        syncOwnedStar();
        if (starActive(currentElites()))
            AbstractDungeon.actionManager.addToBottom(new GainEnergyAction(1));
    }

    @SpirePatch(clz = Girya.class, method = "atBattleStart")
    public static class GiryaDexterity {
        @SpirePostfixPatch public static void after(Girya __instance) {
            if (__instance.counter > 0) AbstractDungeon.actionManager.addToTop(new ApplyPowerAction(
                    AbstractDungeon.player, AbstractDungeon.player,
                    new DexterityPower(AbstractDungeon.player, __instance.counter), __instance.counter));
        }
    }
    @SpirePatch(clz = PrismaticShard.class, method = "onEquip")
    public static class PrismEquip {
        @SpirePostfixPatch public static void after() { prismEquip(1); }
    }
    @SpirePatch(clz = AbstractRelic.class, method = "onUnequip")
    public static class PrismUnequip {
        @SpirePostfixPatch public static void after(AbstractRelic __instance) {
            if (__instance instanceof PrismaticShard) prismEquip(-1);
        }
    }
    @SpirePatch(clz = RingOfTheSerpent.class, method = "onEquip")
    @SpirePatch(clz = RingOfTheSerpent.class, method = "onUnequip")
    public static class SerpentHandSize {
        @SpirePrefixPatch public static SpireReturn<Void> before() { return SpireReturn.Return(null); }
    }
    @SpirePatch(clz = RingOfTheSerpent.class, method = "atTurnStart")
    public static class SerpentTurn {
        @SpirePrefixPatch public static SpireReturn<Void> before(RingOfTheSerpent __instance) {
            int turn = State.turns.get(__instance) + 1;
            State.turns.set(__instance, turn);
            serpentTurn(__instance, turn);
            return SpireReturn.Return(null);
        }
    }
    @SpirePatch(clz = AbstractRelic.class, method = "atBattleStart")
    public static class SozuBattle {
        @SpirePostfixPatch public static void after(AbstractRelic __instance) {
            if (__instance instanceof Sozu) resetBattle();
            if (__instance instanceof RingOfTheSerpent) State.turns.set(__instance, 0);
        }
    }
    @SpirePatch(clz = AbstractRelic.class, method = "atTurnStart")
    public static class StarTurn {
        @SpirePostfixPatch public static void after(AbstractRelic __instance) {
            if (__instance instanceof BlackStar) starTurn();
        }
    }
    @SpirePatch(clz = AbstractRelic.class, method = "onEquip")
    public static class StarEquip {
        @SpirePostfixPatch public static void after(AbstractRelic __instance) { syncStar(__instance); }
    }
    // Load restores relic_counters after acquisition. Refresh just before drawing the
    // vanilla counter so both wrappers and vanilla instances show the global total.
    @SpirePatch(clz = AbstractRelic.class, method = "renderCounter")
    public static class StarCounter {
        @SpirePrefixPatch public static void before(AbstractRelic __instance) { syncStar(__instance); }
    }
    @SpirePatch(clz = MonsterRoomElite.class, method = "dropReward")
    public static class StarReward {
        @SpireInstrumentPatch public static ExprEditor edit() { return RelicReworkPatch.ignoreRelic("Black Star"); }
    }
    // Only the second destroyPotion call in updateInput is the discard branch.
    // Drinking, throwing and automatic revival must never unlock Sozu.
    @SpirePatch(clz = PotionPopUp.class, method = "updateInput")
    public static class PotionDiscard {
        @SpireInstrumentPatch public static ExprEditor edit() {
            return new ExprEditor() {
                private int removals;
                public void edit(MethodCall call) throws CannotCompileException {
                    if (call.getClassName().equals("com.megacrit.cardcrawl.ui.panels.TopPanel")
                            && call.getMethodName().equals("destroyPotion") && ++removals == 2) {
                        call.replace("{ anotherspirerework.patches.TurnRelicFixes.discard($1); $proceed($$); }");
                    }
                }
            };
        }
    }
    @SpirePatch(clz = Girya.class, method = "getUpdatedDescription")
    @SpirePatch(clz = PrismaticShard.class, method = "getUpdatedDescription")
    @SpirePatch(clz = RingOfTheSerpent.class, method = "getUpdatedDescription")
    @SpirePatch(clz = Sozu.class, method = "getUpdatedDescription")
    @SpirePatch(clz = BlackStar.class, method = "getUpdatedDescription")
    public static class Description {
        @SpirePostfixPatch public static String after(String __result, AbstractRelic __instance) {
            return CardCrawlGame.languagePack.getRelicStrings(
                    anotherspirerework.AnotherSpireRework.makeID(__instance.relicId)).DESCRIPTIONS[0];
        }
    }
    @SpirePatch(clz = Sozu.class, method = "updateDescription")
    public static class SozuDescription {
        @SpirePostfixPatch public static void after(Sozu __instance) {
            __instance.description = Description.after(__instance.description, __instance);
            __instance.tips.clear();
            __instance.tips.add(new com.megacrit.cardcrawl.helpers.PowerTip(__instance.name, __instance.description));
        }
    }
}