package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.util.RelicChecks;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.LoseHPAction;
import com.megacrit.cardcrawl.actions.unique.PoisonLoseHpAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

/**
 * The Specimen: whenever an enemy loses HP from Poison, every other enemy loses the same amount. The
 * vanilla poison transfer on death is gone.
 *
 * <p>Like every reworked relic it replaces the vanilla prototype through
 * {@code patches.RelicSwapPatch}, keeping the vanilla ID, art and acquisition path. The spread has
 * to run on the poison damage itself rather than on a relic hook, so it is instrumented into
 * {@code PoisonLoseHpAction}.
 */
public class TheSpecimen extends com.megacrit.cardcrawl.relics.TheSpecimen {
    public TheSpecimen() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new TheSpecimen();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** The vanilla poison transfer applies no more; the spread below replaces it. */
    @Override
    public void onMonsterDeath(AbstractMonster monster) {
    }

    @SpirePatch(clz = PoisonLoseHpAction.class, method = "update")
    public static class SpecimenSpread {
        @SpireInstrumentPatch
        public static ExprEditor edit() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("damage")
                            && m.getClassName().equals("com.megacrit.cardcrawl.core.AbstractCreature")) {
                        m.replace("{ $proceed($$); anotherspirerework.relics.TheSpecimen.spreadPoison(this.target, (com.megacrit.cardcrawl.cards.DamageInfo)$1); }");
                    }
                }
            };
        }
    }

    /**
     * Makes every other enemy lose the health the poisoned one just lost.
     *
     * <p>The loss is plain HP loss, the same kind the base game uses for poison, so it cannot be
     * blocked and cannot start a chain of spreads of its own.
     */
    public static void spreadPoison(AbstractCreature victim, DamageInfo info) {
        if (victim == null || info == null || info.type != DamageInfo.DamageType.HP_LOSS
                || !RelicChecks.owned(ID) || AbstractDungeon.getCurrRoom() == null
                || AbstractDungeon.getMonsters() == null) {
            return;
        }
        int lost = info.output;
        if (lost <= 0) {
            return;
        }
        for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
            if (m == victim || m.isDead || m.isDying || m.currentHealth <= 0) {
                continue;
            }
            AbstractDungeon.actionManager.addToTop(
                    new LoseHPAction(m, victim, lost, AbstractGameAction.AttackEffect.POISON));
        }
    }
}
