package anotherspirerework.actions;

import anotherspirerework.powers.GlassKnifeMarkPower;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;

import java.util.List;

/**
 * Die Die Die: one Shiv hits every enemy in the same frame (like Cleave) instead of one Shiv per
 * enemy in sequence. The attack effect is still the Shiv's own slash, and Glass Knife pays out for
 * every enemy that really loses health, just like the single-target ShivDamageAction.
 */
public class ShivDamageAllAction extends DamageAllEnemiesAction {
    private final AbstractCreature attacker;

    private int[] healthBefore;
    private boolean captured;
    private boolean rewarded;

    public ShivDamageAllAction(AbstractCreature source, int[] amount, DamageInfo.DamageType type, AttackEffect effect) {
        super(source, amount, type, effect);
        this.attacker = source;
    }

    @Override
    public void update() {
        if (!captured) {
            healthBefore = captureHealth();
            captured = true;
        }
        super.update();
        if (isDone && !rewarded) {
            rewarded = true;
            rewardGlassKnife();
        }
    }

    private int[] captureHealth() {
        List<AbstractMonster> monsters = AbstractDungeon.getCurrRoom().monsters.monsters;
        int[] health = new int[monsters.size()];
        for (int i = 0; i < monsters.size(); i++) {
            health[i] = monsters.get(i).currentHealth;
        }
        return health;
    }

    private void rewardGlassKnife() {
        if (attacker == null || healthBefore == null) {
            return;
        }
        List<AbstractMonster> monsters = AbstractDungeon.getCurrRoom().monsters.monsters;
        for (int i = 0; i < monsters.size() && i < healthBefore.length; i++) {
            AbstractMonster monster = monsters.get(i);
            AbstractPower mark = monster.getPower(GlassKnifeMarkPower.POWER_ID);
            if (mark != null && mark.amount > 0 && monster.currentHealth < healthBefore[i]) {
                addToTop(new GainBlockAction(attacker, attacker, mark.amount));
            }
        }
    }
}
