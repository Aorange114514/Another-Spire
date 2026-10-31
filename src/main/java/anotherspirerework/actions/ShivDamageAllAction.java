package anotherspirerework.actions;

import anotherspirerework.powers.GlassKnifeMarkPower;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;

import java.util.List;

/**
 * Die Die Die: one Shiv hits every enemy in the same frame (like Cleave) instead of one Shiv per
 * enemy in sequence. The attack effect is still the Shiv's own slash, and Glass Knife pays out for
 * every marked enemy the hit lands on, even if Block or Intangible stops the damage.
 */
public class ShivDamageAllAction extends DamageAllEnemiesAction {
    private final AbstractCreature attacker;

    private boolean[] hittable;
    private boolean captured;
    private boolean rewarded;

    public ShivDamageAllAction(AbstractCreature source, int[] amount, DamageInfo.DamageType type, AttackEffect effect) {
        super(source, amount, type, effect);
        this.attacker = source;
    }

    @Override
    public void update() {
        if (!captured) {
            captured = true;
            hittable = captureHittable();
        }
        super.update();
        if (isDone && !rewarded) {
            rewarded = true;
            rewardGlassKnife();
        }
    }

    /** The enemies the base action is about to hit: alive and not escaping when the hit starts. */
    private boolean[] captureHittable() {
        List<AbstractMonster> monsters = AbstractDungeon.getCurrRoom().monsters.monsters;
        boolean[] result = new boolean[monsters.size()];
        for (int i = 0; i < monsters.size(); i++) {
            result[i] = !monsters.get(i).isDeadOrEscaped();
        }
        return result;
    }

    private void rewardGlassKnife() {
        if (attacker == null || hittable == null) {
            return;
        }
        List<AbstractMonster> monsters = AbstractDungeon.getCurrRoom().monsters.monsters;
        for (int i = 0; i < monsters.size() && i < hittable.length; i++) {
            if (!hittable[i]) {
                continue;
            }
            AbstractPower mark = monsters.get(i).getPower(GlassKnifeMarkPower.POWER_ID);
            if (mark != null && mark.amount > 0) {
                mark.flash();
                addToTop(new GainBlockAction(attacker, mark.amount, Settings.FAST_MODE));
            }
        }
    }
}
