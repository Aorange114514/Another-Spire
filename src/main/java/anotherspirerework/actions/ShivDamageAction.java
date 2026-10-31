package anotherspirerework.actions;

import anotherspirerework.powers.GlassKnifeMarkPower;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.powers.AbstractPower;

/**
 * A Shiv hit. Glass Knife pays Block whenever the hit lands, including when Block or Intangible
 * stops the damage; a cancelled attack (target or attacker already gone) pays nothing.
 */
public class ShivDamageAction extends DamageAction {
    private final AbstractCreature attacker;

    private boolean started;
    private boolean hit;
    private AbstractPower mark;
    private int glassKnifeBlock;
    private boolean rewarded;

    public ShivDamageAction(AbstractCreature target, DamageInfo info, AttackEffect effect) {
        super(target, info, effect);
        this.attacker = info.owner;
    }

    @Override
    public void update() {
        if (!started) {
            started = true;
            hit = target != null && !target.isDeadOrEscaped();
            mark = hit ? target.getPower(GlassKnifeMarkPower.POWER_ID) : null;
            glassKnifeBlock = mark == null ? 0 : mark.amount;
        }
        super.update();
        if (isDone && !rewarded) {
            rewarded = true;
            if (hit && mark != null && glassKnifeBlock > 0 && attacker != null
                    && !attacker.isDying && !attacker.halfDead) {
                mark.flash();
                addToTop(new GainBlockAction(attacker, glassKnifeBlock, Settings.FAST_MODE));
            }
        }
    }
}
