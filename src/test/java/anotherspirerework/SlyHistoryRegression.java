package anotherspirerework;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.Loader;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/** Real history-reading action bytecode with headless world and effect-queue substitutes. */
public final class SlyHistoryRegression {
    private static final String ROOT = "com.megacrit.cardcrawl.";
    private static final String ACTION = ROOT + "actions.AbstractGameAction";
    private static final String CARD = ROOT + "cards.AbstractCard";
    private static final String MANAGER = ROOT + "actions.GameActionManager";
    private static final String DUNGEON = ROOT + "dungeons.AbstractDungeon";
    private static final String WRAPPER = "anotherspirerework.actions.SlyHistoryAction";
    private static final String FINISHER = ROOT + "actions.unique.DamagePerAttackPlayedAction";
    private static final String FTL = ROOT + "actions.defect.FTLAction";
    private static final String CRUSH = ROOT + "actions.watcher.CrushJointsAction";
    private static final String SANCTITY = "anotherspirerework.actions.SanctityEnergyAction";

    public static void run() throws Exception {
        ClassPool pool = new ClassPool(true);
        stub(pool, ROOT + "helpers.Hitbox", null,
                "public float cX;", "public float cY;");
        stub(pool, ROOT + "core.AbstractCreature", null,
                "public int currentHealth = 100;",
                "public " + ROOT + "helpers.Hitbox hb = new " + ROOT + "helpers.Hitbox();");
        stub(pool, ROOT + "characters.AbstractPlayer", ROOT + "core.AbstractCreature");
        stub(pool, ROOT + "monsters.AbstractMonster", ROOT + "core.AbstractCreature");
        stub(pool, CARD, null, "public " + CARD + "$CardType type;");
        stub(pool, ROOT + "cards.DamageInfo", null);
        stub(pool, MANAGER, null,
                "public java.util.ArrayList actions = new java.util.ArrayList();",
                "public java.util.ArrayList cardsPlayedThisTurn = new java.util.ArrayList();",
                "public java.util.ArrayList cardsPlayedThisCombat = new java.util.ArrayList();");
        stub(pool, DUNGEON, null,
                "public static " + MANAGER + " actionManager;",
                "public static " + ROOT + "characters.AbstractPlayer player;");
        CtClass base = stub(pool, ACTION, null,
                "public boolean isDone;", "public float duration;", "public int amount;",
                "public " + ROOT + "core.AbstractCreature target;",
                "public " + ACTION + "$ActionType actionType;",
                "public " + ACTION + "$AttackEffect attackEffect;");
        base.addMethod(CtNewMethod.make("public void update() { this.isDone = true; }", base));
        base.addMethod(CtNewMethod.make("public void setValues(" + ROOT + "core.AbstractCreature t, "
                + ROOT + "cards.DamageInfo i) { this.target = t; }", base));
        base.addMethod(CtNewMethod.make("public void addToTop(" + ACTION + " a) { "
                + DUNGEON + ".actionManager.actions.add(0, a); }", base));
        base.addMethod(CtNewMethod.make("public void addToBot(" + ACTION + " a) { "
                + DUNGEON + ".actionManager.actions.add(a); }", base));
        stub(pool, ROOT + "powers.AbstractPower", null);
        stub(pool, ROOT + "vfx.AbstractGameEffect", null);
        for (String output : new String[] {"actions.common.DamageAction", "actions.common.DrawCardAction",
                "actions.common.ApplyPowerAction", "actions.common.GainEnergyAction",
                "actions.animations.VFXAction", "actions.utility.SFXAction"}) {
            effectStub(pool, ROOT + output, ACTION);
        }
        effectStub(pool, ROOT + "powers.VulnerablePower", ROOT + "powers.AbstractPower");
        effectStub(pool, ROOT + "vfx.BorderFlashEffect", ROOT + "vfx.AbstractGameEffect");
        effectStub(pool, ROOT + "vfx.combat.SanctityEffect", ROOT + "vfx.AbstractGameEffect");
        CtClass probe = stub(pool, "slytest.Probe", ACTION,
                "public boolean fail;", "public boolean sawSource;", "public " + CARD + " source;");
        probe.addMethod(CtNewMethod.make("public void update() { java.util.ArrayList t = " + DUNGEON
                + ".actionManager.cardsPlayedThisTurn; java.util.ArrayList c = " + DUNGEON
                + ".actionManager.cardsPlayedThisCombat; this.sawSource = t.get(t.size()-1) == this.source"
                + " && c.get(c.size()-1) == this.source; t.clear(); c.clear();"
                + " if (this.fail) throw new IllegalStateException(\"probe\"); this.isDone = true; }", probe));
        CtClass subclass = pool.makeClass("slytest.FTLSubclass", pool.get(FTL));
        subclass.addConstructor(CtNewConstructor.make("public FTLSubclass() { super(null, null, 3); }", subclass));

        Loader loader = new Loader(SlyHistoryRegression.class.getClassLoader(), pool);
        Harness h = new Harness(loader);
        h.finisher();
        h.ftl();
        h.previousCard();
        h.restoration();
        h.queueSelection();
        System.out.println("PASS: Sly history real-action counts, thresholds, previous-card checks and isolation");
    }

    private static CtClass stub(ClassPool pool, String name, String parent, String... fields) throws Exception {
        CtClass old = pool.getOrNull(name);
        if (old != null) old.detach();
        CtClass result = pool.makeClass(name);
        if (parent != null) result.setSuperclass(pool.get(parent));
        for (String field : fields) result.addField(CtField.make(field, result));
        result.addConstructor(CtNewConstructor.defaultConstructor(result));
        return result;
    }

    private static void effectStub(ClassPool pool, String name, String parent) throws Exception {
        CtConstructor[] original = pool.get(name).getDeclaredConstructors();
        CtClass[][] signatures = new CtClass[original.length][];
        for (int i = 0; i < original.length; i++) signatures[i] = original[i].getParameterTypes();
        CtClass result = stub(pool, name, parent);
        for (CtClass[] signature : signatures) {
            if (signature.length == 0) continue;
            CtConstructor constructor = new CtConstructor(signature, result);
            constructor.setModifiers(javassist.Modifier.PUBLIC);
            String body = "{ super();";
            if (ACTION.equals(parent)) {
                for (int i = 0; i < signature.length; i++) {
                    if (signature[i].equals(CtClass.intType)) {
                        body += "this.amount = $" + (i + 1) + ";";
                        break;
                    }
                }
            }
            constructor.setBody(body + "}");
            result.addConstructor(constructor);
        }
    }

    private static final class Harness {
        private final Loader loader;
        private final Class<?> cardClass;
        private final Class<?> actionClass;
        private final Class<?> wrapperClass;
        private final Class<?> dungeonClass;
        private final Object attack;
        private final Object skill;
        private final Object power;
        private Object manager;
        private Object source;
        private List<Object> turn;
        private List<Object> combat;

        Harness(Loader loader) throws Exception {
            this.loader = loader;
            cardClass = loader.loadClass(CARD);
            actionClass = loader.loadClass(ACTION);
            wrapperClass = loader.loadClass(WRAPPER);
            dungeonClass = loader.loadClass(DUNGEON);
            Class<?> types = loader.loadClass(CARD + "$CardType");
            attack = types.getField("ATTACK").get(null);
            skill = types.getField("SKILL").get(null);
            power = types.getField("POWER").get(null);
        }

        @SuppressWarnings("unchecked")
        void reset(Object sourceType, Object... history) throws Exception {
            manager = loader.loadClass(MANAGER).newInstance();
            dungeonClass.getField("actionManager").set(null, manager);
            dungeonClass.getField("player").set(null,
                    loader.loadClass(ROOT + "characters.AbstractPlayer").newInstance());
            source = card(sourceType);
            turn = (List<Object>) manager.getClass().getField("cardsPlayedThisTurn").get(manager);
            combat = (List<Object>) manager.getClass().getField("cardsPlayedThisCombat").get(manager);
            for (Object type : history) {
                Object card = card(type);
                turn.add(card);
                combat.add(card);
            }
        }

        Object card(Object type) throws Exception {
            Object card = cardClass.newInstance();
            cardClass.getField("type").set(card, type);
            return card;
        }

        Object construct(String name, Object... arguments) throws Exception {
            for (java.lang.reflect.Constructor<?> c : loader.loadClass(name).getConstructors()) {
                if (c.getParameterTypes().length == arguments.length) return c.newInstance(arguments);
            }
            throw new AssertionError("Missing constructor " + name);
        }

        @SuppressWarnings("unchecked")
        List<Object> queue() throws Exception {
            return (List<Object>) manager.getClass().getField("actions").get(manager);
        }

        void execute(Object action) throws Exception {
            List<Object> turnBefore = new ArrayList<>(turn);
            List<Object> combatBefore = new ArrayList<>(combat);
            Object wrapper = wrapperClass.getConstructor(actionClass, cardClass).newInstance(action, source);
            wrapperClass.getMethod("update").invoke(wrapper);
            require((Boolean) actionClass.getField("isDone").get(wrapper), "Wrapper completion");
            restored(turnBefore, combatBefore);
        }

        void restored(List<Object> expectedTurn, List<Object> expectedCombat) throws Exception {
            require(manager.getClass().getField("cardsPlayedThisTurn").get(manager) == turn,
                    "Turn history reference must be restored");
            require(manager.getClass().getField("cardsPlayedThisCombat").get(manager) == combat,
                    "Combat history reference must be restored");
            require(turn.equals(expectedTurn) && combat.equals(expectedCombat), "History contents must be preserved");
        }

        int outputs(String simpleName) throws Exception {
            int count = 0;
            for (Object action : queue()) if (action.getClass().getSimpleName().equals(simpleName)) count++;
            return count;
        }

        void finisher() throws Exception {
            for (int count : new int[] {0, 1, 4}) {
                Object[] history = new Object[count + 1];
                java.util.Arrays.fill(history, attack);
                history[0] = skill;
                reset(attack, history);
                Object monster = loader.loadClass(ROOT + "monsters.AbstractMonster").newInstance();
                Object info = loader.loadClass(ROOT + "cards.DamageInfo").newInstance();
                execute(construct(FINISHER, monster, info));
                require(outputs("DamageAction") == count, "Finisher must hit once per real Attack: " + count);
            }
            reset(attack, attack);
            // A previously played instance can itself gain Sly: identity must never remove its old history entry.
            turn.add(source);
            combat.add(source);
            execute(construct(FINISHER, loader.loadClass(ROOT + "monsters.AbstractMonster").newInstance(),
                    loader.loadClass(ROOT + "cards.DamageInfo").newInstance()));
            require(outputs("DamageAction") == 2, "Repeated source identity remains a real prior Attack");
        }

        void ftl() throws Exception {
            for (int threshold : new int[] {3, 4}) {
                for (int count : new int[] {0, threshold - 1, threshold, threshold + 1}) {
                    Object[] history = new Object[count];
                    java.util.Arrays.fill(history, skill);
                    reset(attack, history);
                    execute(construct(FTL, null, null, threshold));
                    require(outputs("DamageAction") == 1, "FTL always queues its damage");
                    require(outputs("DrawCardAction") == (count < threshold ? 1 : 0),
                            "FTL threshold must count real plays: " + count + "/" + threshold);
                }
            }
        }

        void previousCard() throws Exception {
            for (Object[] history : new Object[][] {{}, {skill}, {attack}, {skill, attack}, {attack, skill}, {power}}) {
                boolean previousSkill = history.length > 0 && history[history.length - 1] == skill;
                reset(attack, history);
                execute(construct(CRUSH, null, 2));
                require(outputs("ApplyPowerAction") == (previousSkill ? 1 : 0), "Crush Joints checks latest real card");
                reset(skill, history);
                execute(construct(SANCTITY, 2));
                require(outputs("GainEnergyAction") == (previousSkill ? 1 : 0), "Sanctity checks latest real card");
                for (Object output : queue()) {
                    if (output.getClass().getSimpleName().equals("GainEnergyAction")) {
                        require(actionClass.getField("amount").getInt(output) == 2, "Sanctity preserves energy amount");
                    }
                }
            }
        }

        void restoration() throws Exception {
            for (boolean fail : new boolean[] {false, true}) {
                reset(attack, skill);
                List<Object> turnBefore = new ArrayList<>(turn);
                List<Object> combatBefore = new ArrayList<>(combat);
                Object probe = loader.loadClass("slytest.Probe").newInstance();
                probe.getClass().getField("source").set(probe, source);
                probe.getClass().getField("fail").setBoolean(probe, fail);
                Object wrapper = wrapperClass.getConstructor(actionClass, cardClass).newInstance(probe, source);
                try {
                    wrapperClass.getMethod("update").invoke(wrapper);
                    require(!fail, "Probe must throw");
                } catch (InvocationTargetException ex) {
                    require(fail && ex.getCause() instanceof IllegalStateException, "Original failure must propagate");
                }
                require(probe.getClass().getField("sawSource").getBoolean(probe), "Both temporary histories append source");
                restored(turnBefore, combatBefore);
            }
        }

        void queueSelection() throws Exception {
            reset(attack, skill);
            Object existing = construct(FTL, null, null, 3);
            queue().add(existing);
            List<Object> before = new ArrayList<>(queue());
            Object unrelated = actionClass.newInstance();
            Object subclass = loader.loadClass("slytest.FTLSubclass").newInstance();
            queue().add(0, construct(FINISHER, null, null));
            queue().add(unrelated);
            queue().add(construct(FTL, null, null, 3));
            queue().add(construct(CRUSH, null, 1));
            queue().add(construct(SANCTITY, 1));
            queue().add(subclass);
            wrapperClass.getMethod("wrapNewActions", cardClass, List.class).invoke(null, source, before);
            require(queue().get(1) == existing, "Existing historical action stays unchanged");
            require(queue().get(2) == unrelated && queue().get(6) == subclass, "Unrelated actions and subclasses stay unchanged");
            for (int index : new int[] {0, 3, 4, 5}) {
                require(queue().get(index).getClass() == wrapperClass, "New whitelisted action must be wrapped");
            }
            List<Object> once = new ArrayList<>(queue());
            wrapperClass.getMethod("wrapNewActions", cardClass, List.class).invoke(null, source, before);
            require(queue().equals(once), "Wrapping must be idempotent");
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
