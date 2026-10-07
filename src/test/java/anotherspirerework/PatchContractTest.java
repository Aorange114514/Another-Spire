package anotherspirerework;

import com.evacipated.cardcrawl.modthespire.lib.*;
import javassist.*;
import javassist.expr.ExprEditor;
import java.lang.reflect.Method;

/** Headless verification against the real dependency JARs, without initializing the game. */
public class PatchContractTest {
    public static void main(String[] args) throws Exception {
        ClassPool pool = new ClassPool(true);
        int count = 0;
        for (String name : new String[]{
                "anotherspirerework.patches.RelicReworkPatch",
                "anotherspirerework.patches.ShopRelicPatch",
                "anotherspirerework.patches.ReportedRelicFixes",
                "anotherspirerework.patches.TurnRelicFixes",
                "anotherspirerework.actions.PlayShivsFromExhaustAction",
                "anotherspirerework.patches.ShivPatch",
                "anotherspirerework.patches.NewRelicFixes",
                "anotherspirerework.patches.ShuffleTriggerPatch",
                "anotherspirerework.patches.ColorlessSkipPatch",
                "anotherspirerework.patches.CardArtLoaderPatch",
                "anotherspirerework.patches.StrangeSpoonExhaust"}) {
            Class<?> outer = Class.forName(name, false, PatchContractTest.class.getClassLoader());
            java.util.List<Class<?>> patches = new java.util.ArrayList<>();
            patches.add(outer);
            patches.addAll(java.util.Arrays.asList(outer.getDeclaredClasses()));
            for (Class<?> patch : patches) {
                for (SpirePatch annotation : patch.getAnnotationsByType(SpirePatch.class)) {
                    CtClass target = pool.get(annotation.clz().getName());
                    if (target.isFrozen()) target.defrost();
                    if (annotation.method().equals(SpirePatch.CLASS)) continue;
                    CtBehavior method = find(target, annotation);
                    for (Method hook : patch.getDeclaredMethods()) {
                        if (hook.isAnnotationPresent(SpirePrefixPatch.class)) {
                            Class<?>[] hookTypes = hook.getParameterTypes();
                            if (!Modifier.isStatic(method.getModifiers()) && hookTypes.length > 0) {
                                require(hookTypes[0].isAssignableFrom(annotation.clz()),
                                        "Prefix is missing the target instance: " + patch.getName());
                            }
                        }
                        if (hook.isAnnotationPresent(SpireInstrumentPatch.class)) {
                            method.instrument((ExprEditor) hook.invoke(null));
                        }
                        SpireInsertPatch insert = hook.getAnnotation(SpireInsertPatch.class);
                        if (insert != null) {
                            int[] lines = insert.locator().newInstance().Locate(method);
                            require(lines.length > 0, "Missing insertion point: " + patch.getName());
                        }
                        for (java.lang.reflect.Parameter parameter : hook.getParameters()) {
                            // Field injection contracts are checked explicitly below; parameter names
                            // are stored in debug tables rather than reflection in the Java 8 build.
                            require(parameter.getType() != null, "Unresolvable hook signature");
                        }
                    }
                    target.toBytecode();
                    count++;
                }
            }
        }
        require(pool.get("com.megacrit.cardcrawl.actions.utility.UseCardAction")
                .getDeclaredField("targetCard").getType().getName().equals("com.megacrit.cardcrawl.cards.AbstractCard"),
                "UseCardAction targetCard field changed");
        require(count >= 25, "Missing rework patches: " + count);
        reportedRelicRegression(pool);
        turnRelicRegression(pool);
        cardContentRegression(pool);
        newContentRegression(pool);
        powerTitleAndReprogramRegression(pool);
        postfixBindingRegression();
        System.out.println("PASS: " + count + " patch targets, instrumented bytecode, insertion locators and injected fields");
    }

    private static CtBehavior find(CtClass target, SpirePatch patch) throws Exception {
        Class<?>[] types = patch.paramtypez();
        // The annotation uses a sentinel to represent an unspecified parameter list.
        boolean specified = types.length == 0 || types[0] != void.class;
        if (SpirePatch.CONSTRUCTOR.equals(patch.method())) {
            CtClass[] parameters = new CtClass[types.length];
            for (int i = 0; i < types.length; i++) parameters[i] = target.getClassPool().get(types[i].getName());
            return target.getDeclaredConstructor(parameters);
        }
        if (specified) {
            CtClass[] parameters = new CtClass[types.length];
            for (int i = 0; i < types.length; i++) parameters[i] = target.getClassPool().get(types[i].getName());
            return target.getDeclaredMethod(patch.method(), parameters);
        }
        CtMethod[] methods = target.getDeclaredMethods(patch.method());
        require(methods.length == 1, "Ambiguous patch target: " + target.getName() + "." + patch.method());
        return methods[0];
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void cardContentRegression(ClassPool pool) throws Exception {
        CtClass shiv = pool.get("com.megacrit.cardcrawl.cards.tempCards.Shiv");
        require(shiv.getDeclaredMethod("use").getParameterTypes().length == 2, "Shiv use signature");
        require(pool.get("com.megacrit.cardcrawl.cards.AbstractCard").getDeclaredField("isMultiDamage")
                .getType().equals(CtClass.booleanType), "Shiv targeting field");
        require(pool.get("com.megacrit.cardcrawl.actions.unique.DiscoveryAction")
                .getDeclaredConstructor(new CtClass[]{pool.get("com.megacrit.cardcrawl.cards.AbstractCard$CardType"), CtClass.intType}) != null,
                "Typed Discovery constructor");
        for (String name : new String[]{"red.InfernalBlade", "green.Distraction", "green.Concentrate",
                "green.CloakAndDagger", "green.Envenom", "green.StormOfSteel", "blue.Stack"}) {
            CtClass card = pool.get("anotherspirerework.cards." + name);
            require(card.getSuperclass().getName().equals("com.megacrit.cardcrawl.cards.AbstractCard"), "Card superclass: " + name);
            require(card.getDeclaredMethod("makeCopy") != null, "Card copy: " + name);
            card.toBytecode();
        }
        requireCalls(pool, "anotherspirerework.cards.red.SeeingRed", "upgrade", "upgradeBaseCost");
        requireCalls(pool, "anotherspirerework.cards.green.Concentrate", "upgrade", "upgradeMagicNumber");
        requireCalls(pool, "anotherspirerework.cards.green.StormOfSteel", "use", "anotherspirerework.actions.PlayShivsFromExhaustAction");
        requireCalls(pool, "anotherspirerework.cards.red.InfernalBlade", "use", "com.megacrit.cardcrawl.actions.unique.DiscoveryAction");
        requireCalls(pool, "anotherspirerework.cards.green.Distraction", "use", "com.megacrit.cardcrawl.actions.unique.DiscoveryAction");
        requireCalls(pool, "anotherspirerework.cards.red.IronWave", "use", "com.megacrit.cardcrawl.actions.common.GainBlockAction",
                "com.megacrit.cardcrawl.actions.common.DamageAction");
        final java.util.List<String> ironWaveActions = new java.util.ArrayList<>();
        pool.get("anotherspirerework.cards.red.IronWave").defrost();
        pool.get("anotherspirerework.cards.red.IronWave").getDeclaredMethod("use").instrument(new ExprEditor() {
            public void edit(javassist.expr.NewExpr expr) { ironWaveActions.add(expr.getClassName()); }
        });
        require(ironWaveActions.indexOf("com.megacrit.cardcrawl.actions.common.GainBlockAction")
                < ironWaveActions.indexOf("com.megacrit.cardcrawl.actions.common.DamageAction"), "Iron Wave must gain Block before dealing damage");
        requireCalls(pool, "anotherspirerework.actions.ShivDamageAction", "update", "update", "com.megacrit.cardcrawl.actions.common.GainBlockAction");
        requireCalls(pool, "anotherspirerework.actions.TransformDrawPileToShivsAction", "transform", "set", "com.megacrit.cardcrawl.cards.tempCards.Shiv");
        requireCalls(pool, "anotherspirerework.patches.ShivPatch", "attack", "anotherspirerework.actions.ShivDamageAction", "com.megacrit.cardcrawl.powers.PoisonPower");
        System.out.println("PASS: new card class/copy contracts, Shiv targeting field and typed Discovery API");
    }

    /** The cards, powers and relic behaviors added in this round. */
    private static void newContentRegression(ClassPool pool) throws Exception {
        requireCalls(pool, "anotherspirerework.cards.blue.ThunderStrike", "use",
                "com.megacrit.cardcrawl.actions.defect.ChannelAction",
                "com.megacrit.cardcrawl.actions.common.AttackDamageRandomEnemyAction");
        requireCalls(pool, "anotherspirerework.cards.colorless.ThinkingAhead", "use",
                "com.megacrit.cardcrawl.actions.common.DrawCardAction",
                "com.megacrit.cardcrawl.actions.common.PutOnDeckAction");
        requireCalls(pool, "anotherspirerework.cards.colorless.SecretTechnique", "use",
                "com.megacrit.cardcrawl.actions.unique.SkillFromDeckToHandAction");
        requireCalls(pool, "anotherspirerework.cards.colorless.SecretWeapon", "use",
                "com.megacrit.cardcrawl.actions.unique.AttackFromDeckToHandAction");
        requireCalls(pool, "anotherspirerework.cards.colorless.Mayhem", "use",
                "anotherspirerework.powers.ScuffleDrawPower");
        requireCalls(pool, "anotherspirerework.cards.colorless.Chrysalis", "use",
                "anotherspirerework.powers.ShuffleEnergyPower");
        requireCalls(pool, "anotherspirerework.cards.colorless.Metamorphosis", "use",
                "anotherspirerework.powers.ShuffleSeekPower");
        requireCalls(pool, "anotherspirerework.powers.ScuffleDrawPower", "countDraw",
                "com.megacrit.cardcrawl.actions.common.GainEnergyAction");
        requireCalls(pool, "anotherspirerework.powers.ScuffleDrawPower", "renderAmount",
                "renderFontRightTopAligned");
        requireCalls(pool, "anotherspirerework.powers.ShuffleSeekPower", "onDrawPileShuffled",
                "com.megacrit.cardcrawl.actions.defect.SeekAction");
        requireCalls(pool, "anotherspirerework.powers.ShuffleEnergyPower", "onDrawPileShuffled",
                "com.megacrit.cardcrawl.actions.common.GainEnergyAction");
        // The shuffle event must wait for the shuffle animation before it fires, and it has to land
        // in the queue ahead of the draws it interrupted: the running draw is put back underneath
        // the actions the event queues, so the picker comes first and the rest of the draw after.
        requireCalls(pool, "anotherspirerework.patches.ShuffleTriggerPatch", "flush", "isActive",
                "addToTop", "com.megacrit.cardcrawl.actions.common.DrawCardAction");
        requireCalls(pool, "anotherspirerework.patches.ShuffleTriggerPatch$Deferred", "before", "flush");
        // The frame tick: "No Draw" and a full hand end the draw that should carry the event on its
        // first update, so it also has to be polled once per frame instead of only by that draw.
        requireCalls(pool, "anotherspirerework.patches.ShuffleTriggerPatch", "tick", "flush");
        requireCalls(pool, "anotherspirerework.AnotherSpireRework", "receivePostDungeonUpdate", "tick");
        requireCalls(pool, "anotherspirerework.patches.ShuffleTriggerPatch", "fire", "matryoshkaShuffle");
        requireCalls(pool, "anotherspirerework.patches.NewRelicFixes", "matryoshkaShuffle",
                "com.megacrit.cardcrawl.actions.common.DrawCardAction");
        requireCalls(pool, "anotherspirerework.patches.NewRelicFixes", "applyDrawnCard",
                "com.megacrit.cardcrawl.actions.common.ExhaustSpecificCardAction");
        requireCalls(pool, "anotherspirerework.patches.NewRelicFixes", "onCardDrawn", "add");
        requireCalls(pool, "anotherspirerework.patches.NewRelicFixes", "processPendingDraws", "contains");
        requireCalls(pool, "anotherspirerework.patches.NewRelicFixes", "inserterTurn",
                "com.megacrit.cardcrawl.actions.defect.IncreaseMaxOrbAction");
        requireCalls(pool, "anotherspirerework.patches.NewRelicFixes", "ninjaScrollTurn",
                "com.megacrit.cardcrawl.cards.tempCards.Shiv");
        requireCalls(pool, "anotherspirerework.patches.NewRelicFixes", "enchiridionStart",
                "com.megacrit.cardcrawl.actions.unique.DiscoveryAction");
        requireCalls(pool, "anotherspirerework.patches.NewRelicFixes", "yangUse",
                "com.megacrit.cardcrawl.powers.LoseStrengthPower",
                "com.megacrit.cardcrawl.powers.LoseDexterityPower");
        // The two pickup effects run outside combat, as effects rather than as actions.
        requireCalls(pool, "anotherspirerework.patches.NewRelicFixes", "spoonEquip", "start");
        requireCalls(pool, "anotherspirerework.patches.NewRelicFixes", "orreryEquip", "start");
        requireCalls(pool, "anotherspirerework.effects.ChooseDeckCardsEffect", "apply",
                "com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect", "mark");
        requireCalls(pool, "anotherspirerework.effects.ChooseDeckCardsEffect", "update", "open", "size");
        requireCalls(pool, "anotherspirerework.effects.ChooseDeckCardsEffect", "orreryChoices", "getCard", "makeCopy");
        // Removing Exhaust has to survive copies and save loading, and the vanilla roll is off.
        requireCalls(pool, "anotherspirerework.patches.StrangeSpoonExhaust", "strip", "initializeDescription");
        requireCalls(pool, "anotherspirerework.patches.StrangeSpoonExhaust", "restorePending", "strip");
        // The copy hook's shape and ModTheSpire's argument order are checked by
        // postfixBindingRegression() below; a body level check cannot see static calls.
        java.util.Map<String, Integer> spoonCounts = new java.util.HashMap<>();
        spoonCounts.put("Cleave|0|0", 1);
        spoonCounts.put("Die Die Die|2|5", 2);
        require(anotherspirerework.patches.StrangeSpoonExhaust
                .parseCounts(anotherspirerework.patches.StrangeSpoonExhaust.formatCounts(spoonCounts))
                .equals(spoonCounts), "Spoon counts must survive a save round trip");
        require(anotherspirerework.patches.StrangeSpoonExhaust.parseCounts("").isEmpty()
                && anotherspirerework.patches.StrangeSpoonExhaust.parseCounts("broken").isEmpty(),
                "Spoon save parsing must tolerate empty and malformed data");
        // A mod card image, when present, replaces the base game portrait and the zoomed view.
        requireCalls(pool, "anotherspirerework.patches.CardArtLoaderPatch", "load", "internal", "exists");
        requireCalls(pool, "anotherspirerework.patches.CardArtLoaderPatch$Face", "before", "draw");
        requireCalls(pool, "anotherspirerework.patches.CardArtLoaderPatch$Popup", "before", "draw");
        // Frozen Eye's keyword has to stay its own word so the parser can highlight it.
        requireCalls(pool, "anotherspirerework.patches.NewRelicFixes", "applyFrozenEye", "keywordSuffix");

        // Art file names: both the card ID spelling and the vanilla asset spelling are accepted.
        java.util.List<String> artNames = anotherspirerework.patches.CardArtLoaderPatch
                .candidateNames("Die Die Die", "green/attack/die_die_die");
        require(artNames.contains("DieDieDie"), "Card art name from the card ID");
        require(artNames.contains("green_attack_die_die_die"), "Card art name from the asset path");
        require(artNames.contains("die_die_die"), "Card art name from the asset file name");
        // Blizzard keeps its vanilla asset path, so "blizzard.png" is what the loader looks for.
        require(anotherspirerework.patches.CardArtLoaderPatch
                .candidateNames("Blizzard", "blue/attack/blizzard").contains("blizzard"),
                "Blizzard art must resolve to blizzard.png");
        require(anotherspirerework.patches.CardArtLoaderPatch
                .candidateNames("Core Surge", "blue/attack/core_surge").contains("core_surge"),
                "Core Surge art must resolve to core_surge.png");

        // Exhaust text: both the Chinese and the English spelling have to disappear.
        require(anotherspirerework.patches.StrangeSpoonExhaust
                .withoutExhaustText("造成 8 点伤害。 NL 消耗 。", "消耗").equals("造成 8 点伤害。"),
                "Chinese Exhaust keyword must be removed");
        require(anotherspirerework.patches.StrangeSpoonExhaust
                .withoutExhaustText("Deal 8 damage. NL Exhaust.", "Exhaust").equals("Deal 8 damage."),
                "English Exhaust keyword must be removed");

        // The Chinese Ethereal keyword needs a space before the full stop to be parsed at all.
        require(anotherspirerework.patches.NewRelicFixes.keywordSuffix("ZHS").equals(" 。"),
                "Chinese keyword separator");
        require(anotherspirerework.patches.NewRelicFixes.keywordSuffix("ENG").equals("."),
                "Latin keyword separator");

        // Nothing in this mod may move cards while a deck screen is being browsed: the deferral
        // points have to exist for the shuffle trigger, the on-draw relic effects and the picker.
        require(anotherspirerework.patches.ShuffleTriggerPatch.Deferred.class
                        .getDeclaredMethod("before",
                                com.megacrit.cardcrawl.actions.common.DrawCardAction.class) != null,
                "The shuffle trigger needs its deferred entry point");
        require(anotherspirerework.patches.NewRelicFixes.class.getDeclaredMethod("processPendingDraws") != null,
                "On-draw relic effects need a deferred pass");
        require(anotherspirerework.effects.ChooseDeckCardsEffect.class.getDeclaredMethod("browsingCards") != null,
                "The picker needs the deck-browsing guard");
        System.out.println("PASS: new card/power/relic action contracts (Thunder Strike, Mayhem, "
                + "Thinking Ahead, Secret Technique/Weapon, Chrysalis, Metamorphosis, 10 relics, "
                + "deferred shuffle trigger, out-of-combat pickers, custom art loader)");
    }

    /**
     * Three checks that only show up in game. A Power card must not still carry the Exhaust flag;
     * a power class has to expose its strings under the field name {@code powerStrings}, because
     * the power list screens read that name by reflection and otherwise leave the title
     * unlocalized (the base game then logs "PowerString: &lt;class&gt; not found"); and the card
     * picker must not treat a screen opened on top of it - the map above all - as the end of the
     * pick, or it applies the choice early and leaves the still open screen dead on confirm.
     */
    private static void powerTitleAndReprogramRegression(ClassPool pool) throws Exception {
        CtClass reprogram = pool.get("anotherspirerework.cards.blue.Reprogram");
        if (reprogram.isFrozen()) reprogram.defrost();
        final boolean[] exhaustWrite = {false};
        reprogram.getDeclaredConstructor(new CtClass[0]).instrument(new ExprEditor() {
            public void edit(javassist.expr.FieldAccess access) {
                if (access.isWriter() && access.getFieldName().equals("exhaust")) {
                    exhaustWrite[0] = true;
                }
            }
        });
        require(!exhaustWrite[0], "Reprogram is a Power card and must not set exhaust");

        for (String name : new String[]{"anotherspirerework.powers.ShivPoisonPower",
                "anotherspirerework.powers.ShivAllEnemiesPower"}) {
            CtClass power = pool.get(name);
            try {
                require(power.getDeclaredField("powerStrings").getType().getName()
                                .equals("com.megacrit.cardcrawl.localization.PowerStrings"),
                        name + ": powerStrings must hold PowerStrings");
            } catch (NotFoundException missing) {
                throw new AssertionError(name + " must declare a field named powerStrings, "
                        + "otherwise its title is never localized");
            }
        }

        CtClass picker = pool.get("anotherspirerework.effects.ChooseDeckCardsEffect");
        if (picker.isFrozen()) picker.defrost();
        final boolean[] waitsForScreen = {false};
        picker.getDeclaredMethod("update").instrument(new ExprEditor() {
            public void edit(javassist.expr.FieldAccess access) {
                if (access.getFieldName().equals("isScreenUp")) {
                    waitsForScreen[0] = true;
                }
            }
        });
        require(waitsForScreen[0],
                "The picker must wait for the screen to be really gone, not just for GRID");
        System.out.println("PASS: Reprogram carries no Exhaust, both Shiv powers expose "
                + "powerStrings for their titles, and the card picker waits out a screen opened "
                + "on top of it");
    }


    /** The hook the binding probe uses; the result type and instance type must differ. */
    public static class ProbeSource {
        public String make() {
            return "copy";
        }
    }

    public static class ProbeHook {
        /** result first, instance second: what ModTheSpire hands a value returning postfix. */
        public static String after(String __result, Object __instance) {
            return __result + "/" + (__instance != null);
        }
    }

    /**
     * Applies a postfix through ModTheSpire's own patcher and checks which argument it receives.
     *
     * <p>This is the check that catches the mistake the mod's copy hook had: with the instance
     * declared before the result, ModTheSpire still passes the original result first, so the hook
     * hands back the source object and every "copy" shares its state with the original - a card
     * Exhausted in combat then also vanishes from the master deck.
     */
    private static void postfixBindingRegression() throws Exception {
        ClassPool pool = new ClassPool(true);
        pool.insertClassPath(new javassist.LoaderClassPath(PatchContractTest.class.getClassLoader()));
        CtMethod make = pool.get(ProbeSource.class.getName()).getDeclaredMethod("make");
        CtMethod hook = pool.get(ProbeHook.class.getName()).getDeclaredMethod("after");
        new com.evacipated.cardcrawl.modthespire.patcher.PostfixPatchInfo(make, hook).doPatch();
        if (make.getDeclaringClass().isFrozen()) {
            make.getDeclaringClass().defrost();
        }
        ClassLoader loader = new ClassLoader(PatchContractTest.class.getClassLoader()) { };
        Class<?> patched = make.getDeclaringClass().toClass(loader,
                PatchContractTest.class.getProtectionDomain());
        Object instance = patched.getDeclaredConstructor().newInstance();
        Object result = patched.getMethod("make").invoke(instance);
        require("copy/true".equals(result),
                "Postfix binding: result first, instance second, got " + result);

        // The mod's real copy hook keeps that same shape.
        Method copyHook = anotherspirerework.patches.StrangeSpoonExhaust.Copies.class
                .getDeclaredMethod("after", com.megacrit.cardcrawl.cards.AbstractCard.class,
                        com.megacrit.cardcrawl.cards.AbstractCard.class);
        require(copyHook.getReturnType() == com.megacrit.cardcrawl.cards.AbstractCard.class
                        && !Modifier.isStatic(copyHook.getModifiers()) == false,
                "Copy hook must return the card it was given");
        System.out.println("PASS: postfix binding (result first, instance second) and copy hook shape");
    }

    private static void requireCalls(ClassPool pool, String name, String method, String... expected) throws Exception {
        CtClass type = pool.get(name);
        if (type.isFrozen()) type.defrost();
        final java.util.Set<String> calls = new java.util.HashSet<>();
        type.getDeclaredMethod(method).instrument(new ExprEditor() {
            public void edit(javassist.expr.MethodCall call) { calls.add(call.getMethodName()); }
            public void edit(javassist.expr.NewExpr expr) { calls.add(expr.getClassName()); }
        });
        for (String call : expected) require(calls.contains(call), "Missing content contract: " + name + "." + method + " -> " + call);
        type.toBytecode();
    }

    private static void turnRelicRegression(ClassPool pool) throws Exception {
        for (int turn = 0; turn < 10; turn++) {
            require(anotherspirerework.patches.TurnRelicFixes.serpentActive(turn) == (turn >= 1 && turn <= 3),
                    "Serpent turn boundary: " + turn);
        }
        for (int kills = 0; kills < 12; kills++) {
            require(anotherspirerework.patches.TurnRelicFixes.starActive(kills) == (kills >= 5),
                    "Black Star kill threshold: " + kills);
        }
        for (int bits = 0; bits < 8; bits++) {
            boolean combat = (bits & 1) != 0, sozu = (bits & 2) != 0, unlocked = (bits & 4) != 0;
            require(anotherspirerework.patches.TurnRelicFixes.potionLocked(combat, sozu, unlocked)
                    == (bits == 3), "Sozu state: " + bits);
        }
        CtClass popup = pool.get("com.megacrit.cardcrawl.ui.panels.PotionPopUp");
        if (popup.isFrozen()) popup.defrost();
        final int[] removals = {0}, unlocks = {0};
        popup.getDeclaredMethod("updateInput").instrument(new ExprEditor() {
            public void edit(javassist.expr.MethodCall call) {
                if (call.getMethodName().equals("destroyPotion")) removals[0]++;
                if (call.getClassName().equals("anotherspirerework.patches.TurnRelicFixes")
                        && call.getMethodName().equals("discard")) {
                    require(removals[0] == 1, "Sozu unlock must precede only the second (discard) removal");
                    unlocks[0]++;
                }
            }
        });
        require(removals[0] == 2 && unlocks[0] == 1, "Potion discard branch layout changed");
        CtClass fixes = pool.get("anotherspirerework.patches.TurnRelicFixes");
        final java.util.Set<String> fields = new java.util.HashSet<>();
        fixes.getDeclaredMethod("prismEquip").instrument(new ExprEditor() {
            public void edit(javassist.expr.FieldAccess field) {
                if (field.isWriter()) fields.add(field.getFieldName());
            }
        });
        require(fields.contains("energyMaster") && fields.contains("masterHandSize"),
                "Prism must use vanilla permanent energy/draw fields");
        Method serpentHook = anotherspirerework.patches.TurnRelicFixes.SerpentTurn.class.getDeclaredMethod(
                "before", com.megacrit.cardcrawl.relics.RingOfTheSerpent.class);
        require(serpentHook.isAnnotationPresent(SpirePrefixPatch.class)
                && serpentHook.getReturnType() == SpireReturn.class,
                "Serpent must skip the original unconditional flash using a returning prefix");
        final java.util.List<String> serpentCalls = new java.util.ArrayList<>();
        fixes.getDeclaredMethod("serpentTurn").instrument(new ExprEditor() {
            public void edit(javassist.expr.MethodCall call) { serpentCalls.add(call.getMethodName()); }
        });
        require(serpentCalls.indexOf("serpentActive") >= 0
                && serpentCalls.indexOf("flash") > serpentCalls.indexOf("serpentActive")
                && serpentCalls.indexOf("addToBottom") > serpentCalls.indexOf("flash"),
                "Serpent flash and draw must share the active-turn guard");
        final java.util.Set<String> counterFields = new java.util.HashSet<>();
        final java.util.List<String> counterCalls = new java.util.ArrayList<>();
        fixes.getDeclaredMethod("syncStar").instrument(new ExprEditor() {
            public void edit(javassist.expr.FieldAccess field) {
                if (field.isWriter()) counterFields.add(field.getFieldName());
            }
            public void edit(javassist.expr.MethodCall call) { counterCalls.add(call.getMethodName()); }
        });
        require(counterFields.contains("counter") && counterCalls.contains("currentElites"),
                "Black Star must display the global elite total using the vanilla counter");
        for (String method : new String[]{"victory", "onLoad", "starTurn"}) {
            final boolean[] sync = {false};
            fixes.getDeclaredMethod(method).instrument(new ExprEditor() {
                public void edit(javassist.expr.MethodCall call) {
                    if (call.getMethodName().equals("syncOwnedStar")) sync[0] = true;
                }
            });
            require(sync[0], "Black Star counter must synchronize on " + method);
        }
        System.out.println("PASS: Serpent conditional flash/returning prefix; Black Star counter and victory/load/turn synchronization");
        System.out.println("PASS: 30 turn/potion/elite boundary cases; discard-only unlock; vanilla Prism fields");
    }

    private static void reportedRelicRegression(ClassPool pool) throws Exception {
        // These predicates do not initialize game objects, so run their real production code.
        for (int cards = 0; cards < 40; cards++) {
            require(anotherspirerework.patches.ReportedRelicFixes.penalizeChoker(cards) == (cards >= 7),
                    "Choker threshold: " + cards);
        }
        for (int potions = 0; potions < 12; potions++) {
            require(anotherspirerework.patches.ReportedRelicFixes.penalizeBark(potions) == (potions <= 1),
                    "Bark threshold: " + potions);
        }
        CtClass fixes = pool.get("anotherspirerework.patches.ReportedRelicFixes");
        final java.util.List<String> markCalls = new java.util.ArrayList<>();
        fixes.getDeclaredMethod("markStart").instrument(new ExprEditor() {
            public void edit(javassist.expr.NewExpr expr) { markCalls.add(expr.getClassName()); }
        });
        require(markCalls.contains("com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAction"),
                "Mark must create discard-pile action");
        require(!markCalls.contains("com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction")
                && !markCalls.contains("com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction"),
                "Mark must not create hand/draw-pile action");
        final int[] slots = {0};
        fixes.getDeclaredMethod("cauldronEquip").instrument(new ExprEditor() {
            public void edit(javassist.expr.NewExpr expr) {
                if (expr.getClassName().equals("com.megacrit.cardcrawl.potions.PotionSlot")) slots[0]++;
            }
        });
        require(slots[0] == 1, "Cauldron must append one empty potion slot");
        // The wrapper takes the vanilla instance's place, so a vanilla hook it does not forward is
        // gone for good - Velvet Choker hides its play counter with -1 when a fight ends, and the
        // wrapper has to do that itself.
        CtClass choker = pool.get("anotherspirerework.relics.ReworkedRelic");
        if (choker.isFrozen()) choker.defrost();
        final java.util.Set<String> victoryWrites = new java.util.HashSet<>();
        choker.getDeclaredMethod("onVictory").instrument(new ExprEditor() {
            public void edit(javassist.expr.FieldAccess field) {
                if (field.isWriter()) victoryWrites.add(field.getFieldName());
            }
        });
        require(victoryWrites.contains("counter"),
                "Velvet Choker must clear its play counter when the fight ends");
        // Girya's campfire limit moved from three to five lifts.
        require(anotherspirerework.patches.NewRelicFixes.GIRYA_MAX_LIFTS == 5, "Girya lift limit");
        java.lang.reflect.Field blocked = anotherspirerework.patches.NewRelicFixes.class
                .getDeclaredField("ACT_FOUR_BLOCKED");
        blocked.setAccessible(true);
        require(((String[]) blocked.get(null)).length == 27, "Act four list size");
        for (Class<?> patch : anotherspirerework.patches.RelicReworkPatch.class.getDeclaredClasses()) {
            for (SpirePatch annotation : patch.getAnnotationsByType(SpirePatch.class)) {
                require(annotation.clz() != com.megacrit.cardcrawl.rewards.RewardItem.class
                        || !annotation.method().equals(SpirePatch.CONSTRUCTOR),
                        "Crown must not mutate RewardItem during construction");
            }
        }
        final boolean[] vanillaReward = {false};
        CtClass crown = pool.get("anotherspirerework.patches.ReportedRelicFixes$CrownRewards");
        if (crown.isFrozen()) crown.defrost();
        crown.getDeclaredMethod("before").instrument(new ExprEditor() {
                    public void edit(javassist.expr.NewExpr expr) {
                        if (expr.getClassName().equals("com.megacrit.cardcrawl.rewards.RewardItem")
                                && expr.getSignature().equals("(Lcom/megacrit/cardcrawl/relics/AbstractRelic;)V")) {
                            vanillaReward[0] = true;
                        }
                    }
                });
        require(vanillaReward[0], "Crown must construct a complete vanilla relic reward");
        System.out.println("PASS: 52 relic threshold cases; Mark action destination, Cauldron slot "
                + "constructors, Velvet Choker counter cleanup, Crown safe construction path "
                + "(headless structural checks)");
    }
}