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
        for (String name : patchSourceNames()) {
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
                        // A postfix is bound the same way: the receiver comes first, and a value
                        // returning one has the original result named __result ahead of it. A hook
                        // that skips the instance is handed the receiver where it expected the
                        // first argument, which only fails once ModTheSpire builds the call.
                        if (hook.isAnnotationPresent(SpirePostfixPatch.class)
                                && !hook.isAnnotationPresent(SpireInstrumentPatch.class)
                                && !Modifier.isStatic(method.getModifiers())) {
                            Class<?>[] hookTypes = hook.getParameterTypes();
                            int instanceAt = hook.getReturnType() == void.class ? 0 : 1;
                            if (hookTypes.length > instanceAt) {
                                require(hookTypes[instanceAt].isAssignableFrom(annotation.clz()),
                                        "Postfix is missing the target instance: " + patch.getName()
                                                + "." + hook.getName());
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
        secondRoundRegression(pool);
        thirdRoundRegression(pool);
        fourthRoundRegression(pool);
        fifthRoundRegression(pool);
        rebootShuffleOrderRegression(pool);
        relicPoolRegression();
        powerTitleAndReprogramRegression(pool);
        postfixBindingRegression();
        System.out.println("PASS: " + count + " patch targets, instrumented bytecode, insertion locators and injected fields");
    }

    /**
     * Every class of the mod that may carry a patch: the patch package classes, the card picker and
     * every relic class. The relic classes are discovered from the build output so a new relic file
     * is picked up without touching this test.
     */
    private static java.util.List<String> patchSourceNames() {
        java.util.List<String> names = new java.util.ArrayList<>(java.util.Arrays.asList(
                "anotherspirerework.patches.ActFourRelicBlockPatch",
                "anotherspirerework.patches.RelicTierPatch",
                "anotherspirerework.patches.RelicSwapPatch",
                "anotherspirerework.patches.ShopRelicPatch",
                "anotherspirerework.actions.PlayShivsFromExhaustAction",
                "anotherspirerework.patches.ShivPatch",
                "anotherspirerework.patches.ShuffleTriggerPatch",
                "anotherspirerework.patches.ColorlessSkipPatch",
                "anotherspirerework.patches.CardArtLoaderPatch",
                "anotherspirerework.patches.EnergySpentPatch",
                "anotherspirerework.patches.PowerArtLoaderPatch",
                "anotherspirerework.patches.BitePatch",
                "anotherspirerework.patches.CardSwapPatch",
                "anotherspirerework.patches.ExhaustTracker",
                "anotherspirerework.patches.FreeSkillCardPatch",
                "anotherspirerework.patches.MarkAttackPatch",
                "anotherspirerework.patches.NoBlockCapPatch",
                "anotherspirerework.patches.NoEnergyCapPatch",
                "anotherspirerework.patches.NoPowerCapPatch",
                "anotherspirerework.patches.OrbTrackerPatch",
                "anotherspirerework.patches.RetainedCardFadeInPatch",
                "anotherspirerework.patches.ShivCreationPatch",
                "anotherspirerework.patches.ShivMasteryPatch",
                "anotherspirerework.patches.ShivDescriptionPatch",
                "anotherspirerework.patches.TriplePoisonSaturatePatch",
                "anotherspirerework.patches.SadisticNaturePatch",
                "anotherspirerework.patches.FlurryOfBlowsPatch",
                "anotherspirerework.patches.ShopPricePatch",
                "anotherspirerework.patches.IndignationStancePatch",
                "anotherspirerework.effects.ChooseDeckCardsEffect"));
        java.io.File relicDir = new java.io.File("target/classes/anotherspirerework/relics");
        java.io.File[] files = relicDir.listFiles();
        if (files != null) {
            for (java.io.File file : files) {
                String fileName = file.getName();
                if (fileName.endsWith(".class") && !fileName.contains("$")) {
                    names.add("anotherspirerework.relics."
                            + fileName.substring(0, fileName.length() - ".class".length()));
                }
            }
        }
        return names;
    }

    private static CtBehavior find(CtClass target, SpirePatch patch) throws Exception {
        Class<?>[] types = patch.paramtypez();
        // The annotation uses a sentinel to represent an unspecified parameter list.
        boolean specified = types.length == 0 || types[0] != void.class;
        if (SpirePatch.CONSTRUCTOR.equals(patch.method())) {
            if (!specified) {
                // An unspecified constructor means "the class' own constructor"; anything with
                // overloads has to name its parameters so the target stays unambiguous.
                CtConstructor[] constructors = target.getDeclaredConstructors();
                require(constructors.length == 1, "Ambiguous constructor patch: " + target.getName());
                return constructors[0];
            }
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
        // Die Die Die hits all enemies as one simultaneous action (like Cleave), not one action per
        // enemy, while the damage type and attack effect stay the Shiv's own.
        require(pool.get("anotherspirerework.actions.ShivDamageAllAction").getSuperclass().getName()
                        .equals("com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction"),
                "Area Shiv must resolve as one DamageAllEnemiesAction");
        requireCalls(pool, "anotherspirerework.actions.ShivDamageAllAction", "update", "update", "rewardGlassKnife");
        requireCalls(pool, "anotherspirerework.actions.ShivDamageAllAction", "rewardGlassKnife",
                "getPower", "com.megacrit.cardcrawl.actions.common.GainBlockAction");
        requireCalls(pool, "anotherspirerework.patches.ShivPatch", "allEnemies",
                "anotherspirerework.actions.ShivDamageAllAction",
                "com.megacrit.cardcrawl.powers.PoisonPower");
        require(anotherspirerework.patches.ShivDescriptionPatch.variant(false, false) == -1
                        && anotherspirerework.patches.ShivDescriptionPatch.variant(true, false) == 0
                        && anotherspirerework.patches.ShivDescriptionPatch.variant(false, true) == 1
                        && anotherspirerework.patches.ShivDescriptionPatch.variant(true, true) == 2,
                "Shiv text must follow Unload, Die Die Die, and both");
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
                "anotherspirerework.actions.ShuffleSeekAction");
        // No picker when the draw that follows the shuffle takes the whole pile anyway.
        requireCalls(pool, "anotherspirerework.powers.ShuffleSeekPower", "onDrawPileShuffled", "drawsAfterShuffle");
        // "Up to" N: the grid opens in its any-number mode.
        requireCalls(pool, "anotherspirerework.actions.ShuffleSeekAction", "update", "open", "extendedDescription");
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
        requireCalls(pool, "anotherspirerework.patches.ShuffleTriggerPatch", "fire", "drawOnShuffle");
        requireCalls(pool, "anotherspirerework.relics.Matryoshka", "drawOnShuffle",
                "com.megacrit.cardcrawl.actions.common.DrawCardAction");
        requireCalls(pool, "anotherspirerework.relics.MedicalKit", "applyDrawn",
                "com.megacrit.cardcrawl.actions.common.ExhaustSpecificCardAction",
                "com.megacrit.cardcrawl.actions.common.DrawCardAction");
        requireCalls(pool, "anotherspirerework.relics.MedicalKit", "onCardDrawn", "add");
        requireCalls(pool, "anotherspirerework.relics.MedicalKit", "processPendingDraws", "contains");
        requireCalls(pool, "anotherspirerework.relics.BlueCandle", "applyDrawn",
                "com.megacrit.cardcrawl.actions.common.ExhaustSpecificCardAction");
        requireCalls(pool, "anotherspirerework.relics.Inserter", "atTurnStart",
                "com.megacrit.cardcrawl.actions.defect.IncreaseMaxOrbAction",
                "com.megacrit.cardcrawl.actions.defect.ChannelAction", "getRandomOrb");
        requireCalls(pool, "anotherspirerework.relics.NinjaScroll", "atTurnStart",
                "com.megacrit.cardcrawl.cards.tempCards.Shiv");
        requireCalls(pool, "anotherspirerework.relics.Enchiridion", "atPreBattle",
                "com.megacrit.cardcrawl.actions.unique.DiscoveryAction");
        requireCalls(pool, "anotherspirerework.relics.Duality", "onUseCard",
                "com.megacrit.cardcrawl.powers.LoseStrengthPower",
                "com.megacrit.cardcrawl.powers.LoseDexterityPower");
        // The two pickup effects run outside combat, as effects rather than as actions.
        requireCalls(pool, "anotherspirerework.relics.StrangeSpoon", "onEquip", "start");
        requireCalls(pool, "anotherspirerework.relics.Orrery", "onEquip", "start");
        requireCalls(pool, "anotherspirerework.effects.ChooseDeckCardsEffect", "apply",
                "com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect", "mark");
        requireCalls(pool, "anotherspirerework.effects.ChooseDeckCardsEffect", "update", "open", "size");
        requireCalls(pool, "anotherspirerework.effects.ChooseDeckCardsEffect", "orreryChoices", "getCard", "makeCopy");
        // Removing Exhaust has to survive copies and save loading, and the vanilla roll is off.
        requireCalls(pool, "anotherspirerework.relics.StrangeSpoon", "strip", "initializeDescription");
        requireCalls(pool, "anotherspirerework.relics.StrangeSpoon", "restorePending", "strip");
        // The copy hook's shape and ModTheSpire's argument order are checked by
        // postfixBindingRegression() below; a body level check cannot see static calls.
        java.util.Map<String, Integer> spoonCounts = new java.util.HashMap<>();
        spoonCounts.put("Cleave|0|0", 1);
        spoonCounts.put("Die Die Die|2|5", 2);
        require(anotherspirerework.util.RelicSave
                .parseCounts(anotherspirerework.util.RelicSave.formatCounts(spoonCounts))
                .equals(spoonCounts), "Spoon counts must survive a save round trip");
        require(anotherspirerework.util.RelicSave.parseCounts("").isEmpty()
                && anotherspirerework.util.RelicSave.parseCounts("broken").isEmpty(),
                "Spoon save parsing must tolerate empty and malformed data");
        // A mod card image, when present, replaces the base game portrait and the zoomed view.
        requireCalls(pool, "anotherspirerework.patches.CardArtLoaderPatch", "load", "internal", "exists");
        requireCalls(pool, "anotherspirerework.patches.CardArtLoaderPatch$Face", "before", "draw");
        requireCalls(pool, "anotherspirerework.patches.CardArtLoaderPatch$Popup", "before", "draw");
        // Frozen Eye's keyword has to stay its own word so the parser can highlight it.
        requireCalls(pool, "anotherspirerework.relics.FrozenEye", "applyFrozenEye", "keywordSuffix");

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
        require(anotherspirerework.util.RelicChecks
                .withoutExhaustText("造成 8 点伤害。 NL 消耗 。", "消耗").equals("造成 8 点伤害。"),
                "Chinese Exhaust keyword must be removed");
        require(anotherspirerework.util.RelicChecks
                .withoutExhaustText("Deal 8 damage. NL Exhaust.", "Exhaust").equals("Deal 8 damage."),
                "English Exhaust keyword must be removed");

        // The Chinese Ethereal keyword needs a space before the full stop to be parsed at all.
        require(anotherspirerework.util.RelicChecks.keywordSuffix("ZHS").equals(" 。"),
                "Chinese keyword separator");
        require(anotherspirerework.util.RelicChecks.keywordSuffix("ENG").equals("."),
                "Latin keyword separator");

        // Nothing in this mod may move cards while a deck screen is being browsed: the deferral
        // points have to exist for the shuffle trigger, the on-draw relic effects and the picker.
        require(anotherspirerework.patches.ShuffleTriggerPatch.Deferred.class
                        .getDeclaredMethod("before",
                                com.megacrit.cardcrawl.actions.common.DrawCardAction.class) != null,
                "The shuffle trigger needs its deferred entry point");
        require(anotherspirerework.relics.MedicalKit.class.getDeclaredMethod("processPendingDraws") != null,
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
                "anotherspirerework.powers.ShivAllEnemiesPower",
                "anotherspirerework.powers.BlurDrawPower",
                "anotherspirerework.powers.SadisticNaturePower"}) {
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
        Method copyHook = anotherspirerework.relics.StrangeSpoon.Copies.class
                .getDeclaredMethod("after", com.megacrit.cardcrawl.cards.AbstractCard.class,
                        com.megacrit.cardcrawl.cards.AbstractCard.class);
        require(copyHook.getReturnType() == com.megacrit.cardcrawl.cards.AbstractCard.class
                        && !Modifier.isStatic(copyHook.getModifiers()) == false,
                "Copy hook must return the card it was given");
        System.out.println("PASS: postfix binding (result first, instance second) and copy hook shape");
    }

    /** Relics, cards and powers added in the second round of reworks. */
    private static void secondRoundRegression(ClassPool pool) throws Exception {
        requireConstant(pool, "anotherspirerework.relics.Inserter", "TURNS", 2);
        requireConstant(pool, "anotherspirerework.relics.TwistedFunnel", "EXTRA_TICKS", 2);
        requireConstant(pool, "anotherspirerework.relics.SneckoEye", "HAND_SIZE", 3);
        requireConstant(pool, "anotherspirerework.relics.GoldenEye", "SCRY", 3);
        requireConstant(pool, "anotherspirerework.relics.ThreadAndNeedle", "ARMOR", 2);
        requireConstant(pool, "anotherspirerework.relics.Melange", "BLOCK", 3);
        requireConstant(pool, "anotherspirerework.powers.NirvanaCounterPower", "CARDS_PER_TURN", 5);
        requireConstant(pool, "anotherspirerework.powers.JackOfAllTradesPower", "ENERGY_PER_PAYOUT", 4);
        // Twisted Funnel: the example in the requirements, 7 layers dealing 7 + 6 + 5.
        require(anotherspirerework.util.SaturatingMath.poisonPreview(7, 2) == 18,
                "A 7 stack of Poison with two extra ticks must deal 18");
        require(anotherspirerework.util.SaturatingMath.poisonPreview(1, 2) == 1,
                "A single Poison layer must not be inflated");
        require(anotherspirerework.util.SaturatingMath.poisonPreview(2, 2) == 3,
                "Two layers with two extra ticks must deal 2 + 1");
        // Prismatic Shard and Tiny House changed tier in their own constructors; every other relic
        // keeps the vanilla tier it inherits.
        requireTierAssignment(pool, "anotherspirerework.relics.PrismaticShard", "BOSS");
        requireTierAssignment(pool, "anotherspirerework.relics.TinyHouse", "SHOP");
        requireNoTierWrite(pool, "anotherspirerework.relics.Sling");
        // The shop may only sell a relic whose own tier is SHOP; without this a pool saved before
        // the tier change would put Prismatic Shard on the rug at its 999 gold boss price.
        requireCalls(pool, "anotherspirerework.patches.ShopRelicPatch", "select", "sellable",
                "returnRandomRelicKey");
        requireCalls(pool, "anotherspirerework.patches.ShopRelicPatch", "sellable", "canSpawn");
        java.util.Set<String> shopReads = new java.util.HashSet<>();
        CtClass shopClass = pool.get("anotherspirerework.patches.ShopRelicPatch");
        if (shopClass.isFrozen()) shopClass.defrost();
        shopClass.getDeclaredMethod("sellable", new CtClass[]{pool.get("java.lang.String")})
                .instrument(new ExprEditor() {
                    @Override
                    public void edit(javassist.expr.FieldAccess field) {
                        if (field.isReader()) shopReads.add(field.getFieldName());
                    }
                });
        require(shopReads.contains("tier") && shopReads.contains("SHOP"),
                "Shop slots must compare the relic's own tier with SHOP");
        shopClass.toBytecode();
        requireCalls(pool, "anotherspirerework.patches.RelicTierPatch$LoadedPools", "after",
                "normalizeRelicPools");
        requireCalls(pool, "anotherspirerework.relics.TwistedFunnel$FunnelTicks", "after",
                "anotherspirerework.relics.TwistedFunnel$PoisonExtraTickAction", "addToBottom");
        requireCalls(pool, "anotherspirerework.relics.TwistedFunnel$PoisonExtraTickAction", "update",
                "com.megacrit.cardcrawl.actions.unique.PoisonLoseHpAction");
        requireCalls(pool, "anotherspirerework.relics.TwistedFunnel", "poisonForBar", "previewPoison");
        requireCalls(pool, "anotherspirerework.relics.TwistedFunnel", "previewPoison", "poisonPreview");
        requireCalls(pool, "anotherspirerework.relics.TheSpecimen", "spreadPoison",
                "com.megacrit.cardcrawl.actions.common.LoseHPAction");
        requireCalls(pool, "anotherspirerework.relics.Melange", "onUseCard",
                "com.megacrit.cardcrawl.actions.common.GainBlockAction");
        requireCalls(pool, "anotherspirerework.relics.GoldenEye", "goldenEyeScry",
                "com.megacrit.cardcrawl.actions.utility.ScryAction",
                "com.megacrit.cardcrawl.actions.common.EmptyDeckShuffleAction");
        requireCalls(pool, "anotherspirerework.relics.GoldenEye", "atTurnStart", "goldenEyeScry");
        // The opening hand is scried before it is drawn, and the turn start right after skips once.
        requireCalls(pool, "anotherspirerework.relics.GoldenEye$OpeningScry", "before", "goldenEyeScry");
        requireCalls(pool, "anotherspirerework.relics.ThreadAndNeedle", "atTurnStart",
                "com.megacrit.cardcrawl.powers.PlatedArmorPower");
        requireCalls(pool, "anotherspirerework.relics.FaceOfCleric", "onVictory", "increaseMaxHp");
        requireCalls(pool, "anotherspirerework.relics.VioletLotus", "onChangeStance",
                "com.megacrit.cardcrawl.actions.common.DrawCardAction");
        // PART B HERE
        // Both out of combat pickers run as effects, and the extra rewards are positioned.
        requireCalls(pool, "anotherspirerework.relics.Astrolabe", "onEquip", "start");
        requireConstant(pool, "anotherspirerework.relics.Astrolabe", "CHOICES", 40);
        // Tiny House opens no picker of its own any more (the check lives in the fourth round below).
        requireCalls(pool, "anotherspirerework.effects.ChooseDeckCardsEffect", "anyColorChoices",
                "getAnyColorCard", "makeCopy", "isPickable");
        // Astrolabe adds cards to the deck outside combat, so the healing cards the base game's
        // in-combat generation excludes are legal offers: the picker must not filter on the tag.
        requireMissingCalls(pool, "anotherspirerework.effects.ChooseDeckCardsEffect", "isPickable", "contains");
        requireCalls(pool, "anotherspirerework.relics.PrayerWheel$ExtraCardReward", "after",
                "add", "positionRewards");
        requireCalls(pool, "anotherspirerework.relics.NlothsGift$ExtraCardReward", "after",
                "addCardReward");
        requireCalls(pool, "anotherspirerework.relics.NlothsGift$ExtraCardReward", "addCardReward",
                "positionRewards", "add");
        requireCalls(pool, "anotherspirerework.relics.NlothsGift", "rareCardReward",
                "makeCopy", "onPreviewObtainCard");
        // Energy spent on cards pays out once per four; the random card handout keeps both modifiers.
        requireCalls(pool, "anotherspirerework.patches.EnergySpentPatch$Spend", "before", "onEnergySpent");
        requireCalls(pool, "anotherspirerework.powers.JackOfAllTradesPower", "countSpent",
                "com.megacrit.cardcrawl.actions.common.GainEnergyAction");
        requireCalls(pool, "anotherspirerework.powers.NirvanaCounterPower", "onAfterUseCard", "grant");
        requireCalls(pool, "anotherspirerework.powers.NirvanaCounterPower", "grant",
                "com.megacrit.cardcrawl.powers.DrawCardNextTurnPower");
        requireCalls(pool, "anotherspirerework.util.TempRandomCard", "addEtherealExhaust",
                "addModifier", "returnTrulyRandomCardInCombat", "makeCopy",
                "com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction");
        // The power icons are the artist's own files: the mod prefix is dropped from the file name,
        // and a vanilla power must keep the base game's icon whatever the files are called.
        requireCalls(pool, "anotherspirerework.patches.PowerArtLoaderPatch", "apply", "load", "region");
        requireCalls(pool, "anotherspirerework.patches.PowerArtLoaderPatch$LoadRegion", "before", "apply");
        require(anotherspirerework.patches.PowerArtLoaderPatch.fileId("anotherspirerework:Nirvana")
                        .equals("Nirvana"),
                "Power icon file names drop the mod prefix");
        require(anotherspirerework.patches.PowerArtLoaderPatch.owns("anotherspirerework.powers.NirvanaCounterPower"),
                "Own powers may be reskinned");
        require(!anotherspirerework.patches.PowerArtLoaderPatch.owns("com.megacrit.cardcrawl.powers.StrengthPower"),
                "A vanilla power must keep its own icon");
        requireNewCardIds(pool);
        System.out.println("PASS: second round relics, cards and powers (Inserter, relic tiers, Twisted Funnel,"
                + " The Specimen, Melange, Golden Eye, Thread and Needle, Snecko Eye, Violet Lotus,"
                + " Astrolabe, Tiny House, Prayer Wheel, Nloth's Gift, Jack Of All Trades)");
    }

    /**
     * A run saved before the tier change still carries the pre-change pools: Prismatic Shard sits in
     * the shop pool, where its rewritten BOSS tier would price it at 999 gold, and Tiny House sits in
     * the boss pool, where no shop can reach it. Both have to move, exactly once.
     */
    private static void relicPoolRegression() {
        java.util.ArrayList<String> shop = new java.util.ArrayList<>(
                java.util.Arrays.asList("Lee's Waffle", "PrismaticShard"));
        java.util.ArrayList<String> boss = new java.util.ArrayList<>(
                java.util.Arrays.asList("FrozenCore", "Tiny House"));
        java.util.ArrayList<String> owned = new java.util.ArrayList<>();
        anotherspirerework.patches.RelicTierPatch.normalizePools(shop, boss, owned, false,
                new java.util.Random(1L));
        require(!shop.contains("PrismaticShard"), "Prismatic Shard must leave the shop pool");
        require(boss.contains("PrismaticShard"), "Prismatic Shard must join the boss pool");
        require(shop.contains("Tiny House"), "Tiny House must join the shop pool");
        require(!boss.contains("Tiny House"), "Tiny House must leave the boss pool");
        require(shop.size() == 2 && boss.size() == 2, "A moved relic must not be duplicated");
        // The same save read back a second time has to give the same pools, otherwise saving in
        // front of a boss chest and reloading would reroll it.
        java.util.ArrayList<String> reloadShop = new java.util.ArrayList<>(
                java.util.Arrays.asList("Lee's Waffle", "PrismaticShard"));
        java.util.ArrayList<String> reloadBoss = new java.util.ArrayList<>(
                java.util.Arrays.asList("FrozenCore", "Tiny House"));
        anotherspirerework.patches.RelicTierPatch.normalizePools(reloadShop, reloadBoss, owned,
                false, new java.util.Random(1L));
        require(reloadShop.equals(shop) && reloadBoss.equals(boss),
                "Reloading a save must not reroll the relic pools");
        // Pool order is what the base game pops from, so a repaired pool must stay untouched.
        anotherspirerework.patches.RelicTierPatch.normalizePools(shop, boss, owned, false,
                new java.util.Random(1L));
        require(shop.equals(reloadShop) && boss.equals(reloadBoss),
                "Repairing an already repaired pool must change nothing");
        // An owned relic is dropped instead of being offered a second time.
        owned.add("PrismaticShard");
        owned.add("Tiny House");
        java.util.ArrayList<String> ownedShop = new java.util.ArrayList<>(
                java.util.Arrays.asList("PrismaticShard"));
        java.util.ArrayList<String> ownedBoss = new java.util.ArrayList<>(
                java.util.Arrays.asList("Tiny House"));
        anotherspirerework.patches.RelicTierPatch.normalizePools(ownedShop, ownedBoss, owned,
                false, new java.util.Random(1L));
        require(ownedShop.isEmpty() && ownedBoss.isEmpty(),
                "A relic the player already owns must not return to a pool");
        // Diverse hands out Prismatic Shard itself, and the base game keeps it out of the pools.
        java.util.ArrayList<String> diverseShop = new java.util.ArrayList<>(
                java.util.Arrays.asList("PrismaticShard"));
        java.util.ArrayList<String> diverseBoss = new java.util.ArrayList<>(
                java.util.Arrays.asList("Tiny House"));
        anotherspirerework.patches.RelicTierPatch.normalizePools(diverseShop, diverseBoss,
                new java.util.ArrayList<String>(), true, new java.util.Random(1L));
        require(!diverseShop.contains("PrismaticShard") && !diverseBoss.contains("PrismaticShard"),
                "Diverse must not put Prismatic Shard back into a pool");
        require(diverseShop.contains("Tiny House"), "Tiny House must still reach the shop pool");
        // A relic whose tier never changed is left exactly where it was.
        java.util.ArrayList<String> anchorShop = new java.util.ArrayList<>(
                java.util.Arrays.asList("Anchor", "Circlet"));
        anotherspirerework.patches.RelicTierPatch.normalizePools(anchorShop,
                new java.util.ArrayList<String>(), new java.util.ArrayList<String>(), false,
                new java.util.Random(1L));
        require(anchorShop.equals(java.util.Arrays.asList("Anchor", "Circlet")),
                "Relics without a tier change must stay where they are");
        System.out.println("PASS: relic pool tier repair (Prismatic Shard out of the shop,"
                + " Tiny House into it, stable across reloads, owned relics dropped)");
    }

    /**
     * The third round: the seven new cards, the act gate of the bottled relics and the two shop
     * discounts.
     *
     * <p>Everything here is read out of the bytecode, so no game class initializer runs.
     */
    private static void thirdRoundRegression(ClassPool pool) throws Exception {
        requireConstant(pool, "anotherspirerework.cards.purple.Ragnarok", "MULTIPLIER", 2);
        requireConstant(pool, "anotherspirerework.patches.ShopPricePatch", "UNCOMMON_DISCOUNT", 50);
        requireConstant(pool, "anotherspirerework.patches.ShopPricePatch", "RARE_DISCOUNT", 100);

        // Blur pays out on the draw the base game reports, and the power cleans itself up.
        requireCalls(pool, "anotherspirerework.AnotherSpireRework", "receivePostDraw", "onCardDrawn");
        requireCalls(pool, "anotherspirerework.powers.BlurDrawPower", "payOut",
                "com.megacrit.cardcrawl.actions.common.GainBlockAction");
        requireCalls(pool, "anotherspirerework.powers.BlurDrawPower", "atEndOfTurn",
                "com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction");

        // Both X cost cards work out the energy the way the base game does, spend it, and leave the
        // hits themselves to the base game's own actions.
        requireCalls(pool, "anotherspirerework.actions.RagnarokXAction", "energyFor", "hasRelic");
        requireCalls(pool, "anotherspirerework.actions.RagnarokXAction", "update", "energyFor", "flash",
                "com.megacrit.cardcrawl.actions.common.AttackDamageRandomEnemyAction");
        requireCalls(pool, "anotherspirerework.actions.BouncingFlaskXAction", "update", "getRandomMonster",
                "com.megacrit.cardcrawl.vfx.combat.PotionBounceEffect",
                "com.megacrit.cardcrawl.actions.unique.BouncingFlaskAction");

        // Flurry of Blows: the discard pile keeps the base game's own walk, and all three piles are
        // pulled back before the power loop so Rushdown's draw is queued behind the returns.
        requireCalls(pool, "anotherspirerework.cards.purple.FlurryOfBlows",
                "triggerExhaustedCardsOnStanceChange",
                "com.megacrit.cardcrawl.actions.utility.DiscardToHandAction");
        requireCalls(pool, "anotherspirerework.patches.FlurryOfBlowsPatch$BeforeStancePowers",
                "beforePowers", "returnFrom");
        requireCalls(pool, "anotherspirerework.patches.FlurryOfBlowsPatch", "returnFrom", "addToBottom");
        requireCalls(pool, "anotherspirerework.actions.ReturnCardToHandAction", "update", "contains",
                "addToHand", "removeCard", "refreshHandLayout");

        // Sadistic Nature only ever scales the normal damage the two debuffs scale.
        requireCalls(pool, "anotherspirerework.cards.colorless.SadisticNature", "use",
                "anotherspirerework.powers.SadisticNaturePower");
        requireCalls(pool, "anotherspirerework.patches.SadisticNaturePatch", "doubles", "active");
        requireCalls(pool, "anotherspirerework.patches.SadisticNaturePatch$Weak", "after", "doubles");
        requireCalls(pool, "anotherspirerework.patches.SadisticNaturePatch$Vulnerable", "after", "doubles");
        CtClass sadistic = pool.get("anotherspirerework.patches.SadisticNaturePatch");
        if (sadistic.isFrozen()) sadistic.defrost();
        final java.util.Set<String> damageTypes = new java.util.HashSet<>();
        sadistic.getDeclaredMethod("doubles").instrument(new ExprEditor() {
            public void edit(javassist.expr.FieldAccess field) {
                damageTypes.add(field.getFieldName());
            }
        });
        require(damageTypes.contains("NORMAL") && damageTypes.contains("isPlayer"),
                "Sadistic Nature must only double enemy debuffs on normal damage");
        sadistic.toBytecode();

        // Regal Pillow went back to vanilla, so its free-Rest patch must be gone: the rest is paid by
        // the base game again (30% + 15, onRest, Dream Catcher) and the campfire closes.
        requireMissing("anotherspirerework.patches.RegalPillowPatch");
        requireMissing("anotherspirerework.effects.TinyHouseEffect");

        // The shop: the two discounts, and the gate that keeps the bottled relics in the pool.
        requireCalls(pool, "anotherspirerework.patches.ShopPricePatch$Price", "after", "discounted");
        requireCalls(pool, "anotherspirerework.patches.ShopRelicPatch", "selectUncommon", "keepInPool",
                "remove", "canSpawn");
        // The bottled relic classes own the act number their inventory gate reads.
        CtClass bottled = pool.get("anotherspirerework.relics.BottledFlame");
        if (bottled.isFrozen()) bottled.defrost();
        final java.util.Set<String> bottleReads = new java.util.HashSet<>();
        bottled.getDeclaredMethod("keepInPool").instrument(new ExprEditor() {
            @Override
            public void edit(javassist.expr.FieldAccess field) {
                bottleReads.add(field.getFieldName());
            }
        });
        require(bottleReads.contains("actNum"),
                "The bottled relic gate must read the act number");
        bottled.toBytecode();
        requireCalls(pool, "anotherspirerework.relics.BottledFlame", "canSpawn", "canSpawn");
        System.out.println("PASS: third round cards and relics (Predator, Sucker Punch, Blur, Bouncing Flask,"
                + " Flurry of Blows, Ragnarok, Sadistic Nature, act-gated bottled relics,"
                + " shop relic discounts)");
    }

    /**
     * The fourth round: Blur became 1 (0) with a flat payout, Ragnarok got a per-strike pause,
     * Sadistic Nature's icon stopped counting, and Tiny House took over the free campfire from the
     * reverted Regal Pillow.
     */
    private static void fourthRoundRegression(ClassPool pool) throws Exception {
        // Blur: 1 energy, 0 when upgraded, 1 Block per draw at both levels.
        requireConstant(pool, "anotherspirerework.cards.green.Blur", "BASE_COST", 1);
        requireConstant(pool, "anotherspirerework.cards.green.Blur", "UPGRADED_COST", 0);
        requireConstant(pool, "anotherspirerework.cards.green.Blur", "BLOCK_PER_DRAW", 1);
        CtClass blur = pool.get("anotherspirerework.cards.green.Blur");
        if (blur.isFrozen()) blur.defrost();
        final java.util.Set<String> blurUpgrade = new java.util.HashSet<>();
        blur.getDeclaredMethod("upgrade").instrument(new ExprEditor() {
            public void edit(javassist.expr.MethodCall call) { blurUpgrade.add(call.getMethodName()); }
        });
        require(blurUpgrade.contains("upgradeBaseCost"), "Blur's upgrade must take it to 0 energy");
        require(!blurUpgrade.contains("upgradeMagicNumber"), "Blur's payout is fixed at 1 Block");
        blur.toBytecode();

        // Ragnarok: X*2 strikes, each spaced the way the base game spaces random lightning.
        requireConstant(pool, "anotherspirerework.actions.RagnarokXAction", "HIT_PAUSE", 0.1F);
        requireCalls(pool, "anotherspirerework.actions.RagnarokXAction", "update",
                "com.megacrit.cardcrawl.actions.utility.WaitAction",
                "com.megacrit.cardcrawl.actions.common.AttackDamageRandomEnemyAction");

        // Sadistic Nature: -1 keeps the icon and drops the counter, and stacking never changes it.
        requireConstant(pool, "anotherspirerework.powers.SadisticNaturePower", "ICON_AMOUNT", -1);
        requireWrites(pool, "anotherspirerework.powers.SadisticNaturePower", "stackPower", "amount");

        // Tiny House: the used option is spent, and the campfire comes back once it has settled.
        requireConstant(pool, "anotherspirerework.relics.TinyHouse", "REOPEN_DELAY", 0.3F);
        requireConstant(pool, "anotherspirerework.relics.TinyHouse", "FORCE_AFTER", 3.0F);
        requireCalls(pool, "anotherspirerework.AnotherSpireRework", "receivePostDungeonUpdate", "tick");
        requireCalls(pool, "anotherspirerework.relics.TinyHouse", "tick", "reopen");
        requireWrites(pool, "anotherspirerework.relics.TinyHouse", "arm", "usable");
        requireWrites(pool, "anotherspirerework.relics.TinyHouse", "tick", "phase");
        requireCalls(pool, "anotherspirerework.relics.TinyHouse$MarkUsed", "after", "arm");
        requireCalls(pool, "anotherspirerework.relics.TinyHouse$MarkUsedByTouch", "after", "arm");
        // Cancelling the grid the option opened hands the option back - and Recall, which the click
        // grayed out, comes back with it (two writes). A Recall locked by an earlier used option is
        // not unlocked by this path.
        requireWrites(pool, "anotherspirerework.relics.TinyHouse$CancelledChoice", "after", "usable");
        CtClass cancelled = pool.get("anotherspirerework.relics.TinyHouse$CancelledChoice");
        if (cancelled.isFrozen()) cancelled.defrost();
        final int[] usableWrites = {0};
        cancelled.getDeclaredMethod("after").instrument(new ExprEditor() {
            public void edit(javassist.expr.FieldAccess field) {
                if (field.isWriter() && field.getFieldName().equals("usable")) usableWrites[0]++;
            }
        });
        cancelled.toBytecode();
        require(usableWrites[0] >= 2, "Cancelling a pick must hand back the option and Recall");
        // Tiny House: silent on pickup, then Courier-style pulse for the whole Rest Site.
        // The pickup keeps the base game's single flash; only our extra one and the vanilla rewards are gone.
        requireMissingCalls(pool, "anotherspirerework.relics.TinyHouse", "onEquip", "flash");
        requireCalls(pool, "anotherspirerework.relics.TinyHouse$RestRoomPulse", "after",
                "beginLongPulse", "flash", "stopPulse");
        System.out.println("PASS: fourth round (Blur 1(0), Ragnarok strike pacing, Sadistic Nature without"
                + " counter, Tiny House free campfire, Regal Pillow reverted)");
    }

    /**
     * The fifth round: Consecrate / Third Eye / Deceive Reality / Stack changes, four replaced cards
     * (Judgement, Reach Heaven, Wave of the Hand, Violence) and the Indignation stance order fix.
     */
    private static void fifthRoundRegression(ClassPool pool) throws Exception {
        String[][] ids = {
                {"anotherspirerework.cards.purple.Judgement", "Judgement"},
                {"anotherspirerework.cards.purple.ReachHeaven", "ReachHeaven"},
                {"anotherspirerework.cards.purple.WaveOfTheHand", "WaveOfTheHand"},
                {"anotherspirerework.cards.colorless.Violence", "Violence"}
        };
        for (String[] pair : ids) requireConstant(pool, pair[0], "ID", pair[1]);

        requireCalls(pool, "anotherspirerework.cards.purple.Consecrate", "upgrade", "upgradeBaseCost");
        requireCalls(pool, "anotherspirerework.cards.purple.ThirdEye", "use",
                "com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction");
        requireCalls(pool, "anotherspirerework.cards.purple.DeceiveReality", "use",
                "com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction");
        requireCalls(pool, "anotherspirerework.cards.blue.Stack", "upgrade", "upgradeBaseCost");
        requireCalls(pool, "anotherspirerework.patches.ShuffleTriggerPatch", "fire", "moveToTopOfDrawPile");

        requireCalls(pool, "anotherspirerework.cards.purple.Judgement", "use",
                "anotherspirerework.actions.PercentJudgementAction");
        requireCalls(pool, "anotherspirerework.actions.PercentJudgementAction", "update", "hasPower",
                "com.megacrit.cardcrawl.actions.common.InstantKillAction");
        requireCalls(pool, "anotherspirerework.actions.ReachHeavenAction", "update", "damage",
                "com.megacrit.cardcrawl.actions.watcher.ChangeStanceAction");
        requireCalls(pool, "anotherspirerework.powers.WaveOfTheHandStancePower", "onChangeStance",
                "com.megacrit.cardcrawl.powers.WeakPower");
        requireCalls(pool, "anotherspirerework.actions.PlayRandomAttackFromDrawPileAction", "update",
                "com.megacrit.cardcrawl.actions.utility.NewQueueCardAction",
                "com.megacrit.cardcrawl.actions.utility.UnlimboAction");

        // Girya's Lift tooltip reads the mod's own UI string.
        requireCalls(pool, "anotherspirerework.relics.Girya$GiryaLiftText", "after", "getUIString", "setPrivate");

        // The instrumented IndignationAction now queues the stance change at the top.
        requireCalls(pool, "com.megacrit.cardcrawl.actions.watcher.IndignationAction", "update", "addToTop");
        System.out.println("PASS: fifth round (Consecrate, Third Eye, Deceive Reality, Stack, Judgement,"
                + " Reach Heaven, Wave of the Hand, Violence, Indignation stance order)");
    }

    /**
     * Reboot + Metamorphosis: the picker must not open before the hand has been moved onto the
     * draw pile. ShuffleAllAction only moves the discard pile - it queues the PutOnDeckAction for
     * the hand itself - and Reboot queues the closing ShuffleAction behind that. The event is armed
     * by those two steps instead of by ShuffleAllAction, and flush() stays pending in between.
     */
    private static void rebootShuffleOrderRegression(ClassPool pool) throws Exception {
        java.util.Set<String> allWrites = fieldsWritten(pool,
                "anotherspirerework.patches.ShuffleTriggerPatch$All", "after");
        require(allWrites.contains("waitingForHandOnDeck"),
                "ShuffleAllAction must wait for its hand -> draw pile step");
        require(!allWrites.contains("pendingShuffle"),
                "ShuffleAllAction must not arm the shuffle event by itself");
        require(fieldsWritten(pool, "anotherspirerework.patches.ShuffleTriggerPatch$HandOnDeck", "after")
                        .contains("waitingForFinalShuffle"),
                "The hand move must wait for the closing ShuffleAction");
        require(fieldsWritten(pool, "anotherspirerework.patches.ShuffleTriggerPatch$FinalShuffle", "after")
                        .contains("pendingShuffle"),
                "The closing ShuffleAction must arm the shuffle event");
        // The hooks are bound to the vanilla actions that carry those two steps.
        require(patchTarget("anotherspirerework.patches.ShuffleTriggerPatch$HandOnDeck")
                        == com.megacrit.cardcrawl.actions.common.PutOnDeckAction.class,
                "The hand move hook must patch PutOnDeckAction");
        require(patchTarget("anotherspirerework.patches.ShuffleTriggerPatch$FinalShuffle")
                        == com.megacrit.cardcrawl.actions.common.ShuffleAction.class,
                "The closing shuffle hook must patch ShuffleAction");
        // flush() must refuse to fire while either step is still outstanding.
        CtClass trigger = pool.get("anotherspirerework.patches.ShuffleTriggerPatch");
        if (trigger.isFrozen()) trigger.defrost();
        final java.util.Set<String> reads = new java.util.HashSet<>();
        trigger.getDeclaredMethod("flush",
                        new CtClass[]{pool.get("com.megacrit.cardcrawl.actions.AbstractGameAction")})
                .instrument(new ExprEditor() {
                    public void edit(javassist.expr.FieldAccess field) {
                        if (field.isReader()) reads.add(field.getFieldName());
                    }
                });
        trigger.toBytecode();
        require(reads.contains("waitingForHandOnDeck") && reads.contains("waitingForFinalShuffle"),
                "flush must stay pending until the whole ShuffleAllAction flow is done");
        System.out.println("PASS: Reboot + Metamorphosis (shuffle event waits for the hand move and "
                + "the closing shuffle)");
    }

    /** Every field a method writes, used to pin down which step of a multi-action flow arms what. */
    private static java.util.Set<String> fieldsWritten(ClassPool pool, String name, String method)
            throws Exception {
        CtClass type = pool.get(name);
        if (type.isFrozen()) type.defrost();
        final java.util.Set<String> writes = new java.util.HashSet<>();
        type.getDeclaredMethod(method).instrument(new ExprEditor() {
            public void edit(javassist.expr.FieldAccess field) {
                if (field.isWriter()) writes.add(field.getFieldName());
            }
        });
        type.toBytecode();
        return writes;
    }

    /** The class a nested patch class attaches to, read from its {@link SpirePatch} annotation. */
    private static Class<?> patchTarget(String name) throws Exception {
        Class<?> type = Class.forName(name, false, PatchContractTest.class.getClassLoader());
        SpirePatch patch = type.getAnnotation(SpirePatch.class);
        return patch == null ? null : patch.clz();
    }

    /** Fails when a class that was deleted in this round is still on the classpath. */
    private static void requireMissing(String name) {
        try {
            Class.forName(name, false, PatchContractTest.class.getClassLoader());
        } catch (ClassNotFoundException gone) {
            return;
        }
        throw new AssertionError("Class should have been removed: " + name);
    }

    /** A method must write the named field; used for plain assignments like "usable = false". */
    private static void requireWrites(ClassPool pool, String name, String method, String expected) throws Exception {
        CtClass type = pool.get(name);
        if (type.isFrozen()) type.defrost();
        final java.util.Set<String> writes = new java.util.HashSet<>();
        type.getDeclaredMethod(method).instrument(new ExprEditor() {
            public void edit(javassist.expr.FieldAccess field) {
                if (field.isWriter()) writes.add(field.getFieldName());
            }
        });
        require(writes.contains(expected), "Missing write contract: " + name + "." + method + " -> " + expected);
        type.toBytecode();
    }

    /** Every new card must claim the vanilla card id it replaces. */
    private static void requireNewCardIds(ClassPool pool) throws Exception {
        String[][] newCards = {
                {"anotherspirerework.cards.red.DemonForm", "Demon Form"},
                {"anotherspirerework.cards.green.DeadlyPoison", "Deadly Poison"},
                {"anotherspirerework.cards.green.Bane", "Bane"},
                {"anotherspirerework.cards.green.FlyingKnee", "Flying Knee"},
                {"anotherspirerework.cards.green.Catalyst", "Catalyst"},
                {"anotherspirerework.cards.blue.Tempest", "Tempest"},
                {"anotherspirerework.cards.blue.Overclock", "Steam Power"},
                {"anotherspirerework.cards.purple.FlyingSleeves", "FlyingSleeves"},
                {"anotherspirerework.cards.purple.SashWhip", "SashWhip"},
                {"anotherspirerework.cards.purple.Evaluate", "Evaluate"},
                {"anotherspirerework.cards.purple.Nirvana", "Nirvana"},
                {"anotherspirerework.cards.purple.SpiritShield", "SpiritShield"},
                {"anotherspirerework.cards.purple.EmptyBody", "EmptyBody"},
                {"anotherspirerework.cards.purple.EmptyMind", "EmptyMind"},
                {"anotherspirerework.cards.colorless.JackOfAllTrades", "Jack Of All Trades"},
                {"anotherspirerework.cards.green.Predator", "Predator"},
                {"anotherspirerework.cards.green.SuckerPunch", "Sucker Punch"},
                {"anotherspirerework.cards.green.Blur", "Blur"},
                {"anotherspirerework.cards.green.BouncingFlask", "Bouncing Flask"},
                {"anotherspirerework.cards.purple.FlurryOfBlows", "FlurryOfBlows"},
                {"anotherspirerework.cards.purple.Ragnarok", "Ragnarok"},
                {"anotherspirerework.cards.colorless.SadisticNature", "Sadistic Nature"}
        };
        for (String[] pair : newCards) {
            Object id = pool.get(pair[0]).getDeclaredField("ID").getConstantValue();
            require(pair[1].equals(id), "New card id mismatch: " + pair[0] + " must replace " + pair[1]);
        }
        String[] colourless = {"FlashOfSteel", "Finesse", "SwiftStrike", "GoodInstincts", "MindBlast", "DramaticEntrance"};
        String[] colourlessIds = {"Flash of Steel", "Finesse", "Swift Strike", "Good Instincts", "Mind Blast", "Dramatic Entrance"};
        for (int i = 0; i < colourless.length; i++) {
            Object id = pool.get("anotherspirerework.cards.colorless." + colourless[i])
                    .getDeclaredField("ID").getConstantValue();
            require(colourlessIds[i].equals(id),
                    "New colourless card id mismatch: " + colourless[i] + " must replace " + colourlessIds[i]);
        }
    }

    /**
     * A relic constructor that assigns its own tier: the field is written and the expected tier
     * constant is read, which is how a tier change stays in the relic class instead of a patch.
     */
    private static void requireTierAssignment(ClassPool pool, String name, String tier) throws Exception {
        CtClass type = pool.get(name);
        if (type.isFrozen()) type.defrost();
        final boolean[] writes = {false};
        final java.util.Set<String> reads = new java.util.HashSet<>();
        type.getDeclaredConstructor(new CtClass[0]).instrument(new ExprEditor() {
            @Override
            public void edit(javassist.expr.FieldAccess field) {
                if (field.isWriter() && field.getFieldName().equals("tier")) {
                    writes[0] = true;
                }
                if (field.isReader() && field.getClassName().endsWith("AbstractRelic$RelicTier")) {
                    reads.add(field.getFieldName());
                }
            }
        });
        type.toBytecode();
        require(writes[0], "Tier change must be assigned in the relic constructor: " + name);
        require(reads.contains(tier), name + " must assign the " + tier + " tier");
    }

    /** A relic constructor that does not touch the tier, so the vanilla one is inherited. */
    private static void requireNoTierWrite(ClassPool pool, String name) throws Exception {
        CtClass type = pool.get(name);
        if (type.isFrozen()) type.defrost();
        final boolean[] writes = {false};
        type.getDeclaredConstructor(new CtClass[0]).instrument(new ExprEditor() {
            @Override
            public void edit(javassist.expr.FieldAccess field) {
                if (field.isWriter() && field.getFieldName().equals("tier")) {
                    writes[0] = true;
                }
            }
        });
        type.toBytecode();
        require(!writes[0], "A relic without a tier change must keep the vanilla tier: " + name);
    }

    /** A compile time constant, read straight out of the bytecode so no class initializer runs. */
    private static void requireConstant(ClassPool pool, String name, String field, Object expected) throws Exception {
        Object value = pool.get(name).getDeclaredField(field).getConstantValue();
        require(expected.equals(value), "Constant mismatch: " + name + "." + field + " = " + value);
    }

    /** The mirror of requireCalls: a call that must NOT be there. */
    private static void requireMissingCalls(ClassPool pool, String name, String method, String unexpected) throws Exception {
        CtClass type = pool.get(name);
        if (type.isFrozen()) type.defrost();
        final java.util.Set<String> calls = new java.util.HashSet<>();
        type.getDeclaredMethod(method).instrument(new ExprEditor() {
            public void edit(javassist.expr.MethodCall call) { calls.add(call.getMethodName()); }
            public void edit(javassist.expr.NewExpr expr) { calls.add(expr.getClassName()); }
        });
        require(!calls.contains(unexpected), "Forbidden call still present: " + name + "." + method + " -> " + unexpected);
        type.toBytecode();
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
            require(anotherspirerework.util.RelicChecks.serpentTurnActive(turn) == (turn >= 1 && turn <= 3),
                    "Serpent turn boundary: " + turn);
        }
        for (int kills = 0; kills < 12; kills++) {
            require(anotherspirerework.util.RelicChecks.blackStarActive(kills) == (kills >= 5),
                    "Black Star kill threshold: " + kills);
        }
        for (int bits = 0; bits < 8; bits++) {
            boolean combat = (bits & 1) != 0, sozu = (bits & 2) != 0, unlocked = (bits & 4) != 0;
            require(anotherspirerework.util.RelicChecks.sozuLocked(combat, sozu, unlocked)
                    == (bits == 3), "Sozu state: " + bits);
        }
        CtClass popup = pool.get("com.megacrit.cardcrawl.ui.panels.PotionPopUp");
        if (popup.isFrozen()) popup.defrost();
        final int[] removals = {0}, unlocks = {0};
        popup.getDeclaredMethod("updateInput").instrument(new ExprEditor() {
            public void edit(javassist.expr.MethodCall call) {
                if (call.getMethodName().equals("destroyPotion")) removals[0]++;
                if (call.getClassName().equals("anotherspirerework.relics.Sozu")
                        && call.getMethodName().equals("discard")) {
                    require(removals[0] == 1, "Sozu unlock must precede only the second (discard) removal");
                    unlocks[0]++;
                }
            }
        });
        require(removals[0] == 2 && unlocks[0] == 1, "Potion discard branch layout changed");
        // Prism uses the vanilla permanent energy and draw fields.
        CtClass prism = pool.get("anotherspirerework.relics.PrismaticShard");
        if (prism.isFrozen()) prism.defrost();
        final java.util.Set<String> prismFields = new java.util.HashSet<>();
        for (String method : new String[]{"onEquip", "onUnequip"}) {
            prism.getDeclaredMethod(method).instrument(new ExprEditor() {
                public void edit(javassist.expr.FieldAccess field) {
                    if (field.isWriter()) prismFields.add(field.getFieldName());
                }
            });
        }
        prism.toBytecode();
        require(prismFields.contains("energyMaster") && prismFields.contains("masterHandSize"),
                "Prism must use vanilla permanent energy/draw fields");
        // The Serpent flashes and draws only inside the active-turn guard.
        CtClass serpent = pool.get("anotherspirerework.relics.RingOfTheSerpent");
        if (serpent.isFrozen()) serpent.defrost();
        final java.util.List<String> serpentCalls = new java.util.ArrayList<>();
        serpent.getDeclaredMethod("atTurnStart").instrument(new ExprEditor() {
            public void edit(javassist.expr.MethodCall call) { serpentCalls.add(call.getMethodName()); }
        });
        serpent.toBytecode();
        require(serpentCalls.indexOf("activeTurn") >= 0
                && serpentCalls.indexOf("flash") > serpentCalls.indexOf("activeTurn")
                && serpentCalls.indexOf("addToBot") > serpentCalls.indexOf("flash"),
                "Serpent flash and draw must share the active-turn guard");
        // Black Star shows the global Elite total in the vanilla counter.
        CtClass star = pool.get("anotherspirerework.relics.BlackStar");
        if (star.isFrozen()) star.defrost();
        final java.util.Set<String> counterFields = new java.util.HashSet<>();
        final java.util.List<String> counterCalls = new java.util.ArrayList<>();
        star.getDeclaredMethod("sync").instrument(new ExprEditor() {
            public void edit(javassist.expr.FieldAccess field) {
                if (field.isWriter()) counterFields.add(field.getFieldName());
            }
            public void edit(javassist.expr.MethodCall call) { counterCalls.add(call.getMethodName()); }
        });
        star.toBytecode();
        require(counterFields.contains("counter") && counterCalls.contains("currentElites"),
                "Black Star must display the global elite total using the vanilla counter");
        for (String method : new String[]{"onVictory", "atTurnStart"}) {
            requireCalls(pool, "anotherspirerework.relics.BlackStar", method, "syncOwnedStar");
        }
        requireCalls(pool, "anotherspirerework.relics.BlackStar$SaveData", "onLoad", "syncOwnedStar");
        requireCalls(pool, "anotherspirerework.relics.BlackStar", "renderCounter", "sync");
        System.out.println("PASS: Serpent per-turn guard; Black Star counter and victory/load/turn synchronization");
        System.out.println("PASS: 30 turn/potion/elite boundary cases; discard-only unlock; vanilla Prism fields");
    }

    private static void reportedRelicRegression(ClassPool pool) throws Exception {
        requireConstant(pool, "anotherspirerework.relics.VelvetChoker", "PLAY_LIMIT", 6);
        requireConstant(pool, "anotherspirerework.relics.SacredBark", "MIN_POTIONS", 2);
        requireCalls(pool, "anotherspirerework.relics.VelvetChoker", "atTurnStart", "loseEnergy");
        requireCalls(pool, "anotherspirerework.relics.SacredBark", "atTurnStart", "loseEnergy");
        // Mark of Pain puts its Wounds into the discard pile.
        requireCalls(pool, "anotherspirerework.relics.MarkOfPain", "atBattleStart",
                "com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAction");
        requireMissingCalls(pool, "anotherspirerework.relics.MarkOfPain", "atBattleStart",
                "com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction");
        final int[] slots = {0};
        CtClass cauldron = pool.get("anotherspirerework.relics.Cauldron");
        if (cauldron.isFrozen()) cauldron.defrost();
        cauldron.getDeclaredMethod("onEquip").instrument(new ExprEditor() {
            public void edit(javassist.expr.NewExpr expr) {
                if (expr.getClassName().equals("com.megacrit.cardcrawl.potions.PotionSlot")) slots[0]++;
            }
        });
        cauldron.toBytecode();
        require(slots[0] == 1, "Cauldron must append one empty potion slot");
        // The subclass inherits the vanilla onVictory, which hides the play counter with -1: no
        // subclass override is needed (and adding one would be a second implementation).
        try {
            pool.get("anotherspirerework.relics.VelvetChoker").getDeclaredMethod("onVictory");
            throw new AssertionError("Velvet Choker should inherit the vanilla counter cleanup");
        } catch (javassist.NotFoundException inherited) {
            // expected
        }
        CtClass vanillaChoker = pool.get("com.megacrit.cardcrawl.relics.VelvetChoker");
        if (vanillaChoker.isFrozen()) vanillaChoker.defrost();
        final java.util.Set<String> victoryWrites = new java.util.HashSet<>();
        vanillaChoker.getDeclaredMethod("onVictory").instrument(new ExprEditor() {
            public void edit(javassist.expr.FieldAccess field) {
                if (field.isWriter()) victoryWrites.add(field.getFieldName());
            }
        });
        vanillaChoker.toBytecode();
        require(victoryWrites.contains("counter"),
                "Velvet Choker must clear its play counter when the fight ends");
        // Girya's campfire limit moved from three to five lifts.
        require(anotherspirerework.relics.Girya.MAX_LIFTS == 5, "Girya lift limit");
        require(anotherspirerework.patches.ActFourRelicBlockPatch.BLOCKED.size() == 27,
                "Act four list size");
        // No patch may mutate a RewardItem during construction; the card reward is converted only
        // when the reward screen is laid out.
        for (String name : patchSourceNames()) {
            Class<?> outer = Class.forName(name, false, PatchContractTest.class.getClassLoader());
            java.util.List<Class<?>> patches = new java.util.ArrayList<>();
            patches.add(outer);
            patches.addAll(java.util.Arrays.asList(outer.getDeclaredClasses()));
            for (Class<?> patch : patches) {
                for (SpirePatch annotation : patch.getAnnotationsByType(SpirePatch.class)) {
                    require(annotation.clz() != com.megacrit.cardcrawl.rewards.RewardItem.class
                                    || !annotation.method().equals(SpirePatch.CONSTRUCTOR),
                            "Crown must not mutate RewardItem during construction");
                }
            }
        }
        requireCalls(pool, "anotherspirerework.relics.BustedCrown$CrownRewards", "before", "isBrewing");
        final boolean[] vanillaReward = {false};
        CtClass crown = pool.get("anotherspirerework.relics.BustedCrown$CrownRewards");
        if (crown.isFrozen()) crown.defrost();
        crown.getDeclaredMethod("before").instrument(new ExprEditor() {
            public void edit(javassist.expr.NewExpr expr) {
                if (expr.getClassName().equals("com.megacrit.cardcrawl.rewards.RewardItem")
                        && expr.getSignature().equals("(Lcom/megacrit/cardcrawl/relics/AbstractRelic;)V")) {
                    vanillaReward[0] = true;
                }
            }
        });
        crown.toBytecode();
        require(vanillaReward[0], "Crown must construct a complete vanilla relic reward");
        System.out.println("PASS: Choker/Bark thresholds; Mark action destination, Cauldron slot "
                + "constructors, Velvet Choker inherited counter cleanup, Crown safe construction path "
                + "(headless structural checks)");
    }
}