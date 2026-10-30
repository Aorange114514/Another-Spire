package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.util.RelicChecks;
import anotherspirerework.util.SaturatingMath;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.unique.PoisonLoseHpAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.PoisonPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

/**
 * Twisted Funnel: Poison triggers twice more than usual. The vanilla relic poisoned every enemy at
 * the start of the fight; that handout is gone, and the extra ticks are queued on every poisoned
 * monster instead. The health bar is patched to preview the extra ticks, so the number it shows is
 * the damage the poison will actually deal.
 *
 * <p>Like every reworked relic it replaces the vanilla prototype through
 * {@code patches.RelicSwapPatch}, keeping the vanilla ID, art and acquisition path.
 */
public class TwistedFunnel extends com.megacrit.cardcrawl.relics.TwistedFunnel {
    /** Extra poison ticks on top of the one the base game runs. */
    public static final int EXTRA_TICKS = 2;

    public TwistedFunnel() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new TwistedFunnel();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** The vanilla battle-start poison applies no more; only the extra ticks below are left. */
    @Override
    public void atBattleStart() {
    }

    /** The extra ticks are appended to the one the base game already queued. */
    @SpirePatch(clz = PoisonPower.class, method = "atStartOfTurn")
    public static class FunnelTicks {
        @SpirePostfixPatch
        public static void after(PoisonPower __instance) {
            if (!RelicChecks.owned(ID) || __instance.owner == null || __instance.owner.isPlayer) {
                return;
            }
            AbstractRoom room = AbstractDungeon.getCurrRoom();
            if (room == null || room.phase != AbstractRoom.RoomPhase.COMBAT
                    || AbstractDungeon.getMonsters().areMonstersBasicallyDead()) {
                return;
            }
            for (int i = 0; i < EXTRA_TICKS; i++) {
                AbstractDungeon.actionManager.addToBottom(new PoisonExtraTickAction(__instance.owner));
            }
        }
    }

    /** One more poison tick at the layer's current size, on top of the base game's own tick. */
    private static class PoisonExtraTickAction extends AbstractGameAction {
        PoisonExtraTickAction(AbstractCreature target) {
            this.target = target;
            this.actionType = ActionType.DAMAGE;
        }

        @Override
        public void update() {
            if (this.target != null && this.target.currentHealth > 0) {
                AbstractPower poison = this.target.getPower(PoisonPower.POWER_ID);
                if (poison != null && poison.amount > 0) {
                    addToTop(new PoisonLoseHpAction(this.target, this.target, poison.amount, AttackEffect.POISON));
                }
            }
            this.isDone = true;
        }
    }

    /** The health the poisoned bar shows: the vanilla tick plus the ticks this relic adds. */
    public static int previewPoison(int amount) {
        return SaturatingMath.poisonPreview(amount, EXTRA_TICKS);
    }

    /** Only the amount is ever read from the power the health bar asks for. */
    private static class PreviewPower extends AbstractPower {
    }

    private static final PreviewPower BAR_POWER = new PreviewPower();

    /** Swaps the power the health bar reads for one that carries the previewed amount. */
    public static AbstractPower poisonForBar(AbstractPower original) {
        if (original == null || !(original.owner instanceof AbstractMonster) || !RelicChecks.owned(ID)) {
            return original;
        }
        int preview = previewPoison(original.amount);
        if (preview == original.amount) {
            return original;
        }
        BAR_POWER.amount = preview;
        return BAR_POWER;
    }

    @SpirePatch(clz = AbstractCreature.class, method = "renderRedHealthBar")
    public static class FunnelHealthBar {
        @SpireInstrumentPatch
        public static ExprEditor edit() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("getPower")
                            && m.getClassName().equals("com.megacrit.cardcrawl.core.AbstractCreature")) {
                        m.replace("{ $_ = anotherspirerework.relics.TwistedFunnel.poisonForBar((com.megacrit.cardcrawl.powers.AbstractPower)$proceed($$)); }");
                    }
                }
            };
        }
    }
}
