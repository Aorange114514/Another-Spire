package anotherspirerework.actions;

import anotherspirerework.powers.GlassKnifeMarkPower;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;

/** Glass Knife rewards actual Shiv damage, including each target of an area Shiv. */
public class ShivDamageAction extends DamageAction {
    private final AbstractCreature attacker;

    public ShivDamageAction(AbstractCreature target, DamageInfo info, AttackEffect effect) {
        super(target, info, effect);
        attacker = info.owner;
    }

    public void update() {
        int before = target == null ? 0 : target.currentHealth;
        AbstractPower mark = target == null ? null : target.getPower(GlassKnifeMarkPower.POWER_ID);
        int block = mark == null ? 0 : mark.amount;
        super.update();
        if (target != null && target.currentHealth < before && block > 0) {
            addToTop(new GainBlockAction(attacker, attacker, block));
        }
    }
}