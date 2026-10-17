package anotherspirerework.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.InstantKillAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.ArtifactPower;

/**
 * Vanilla JudgementAction with a percentage cutoff: an enemy without Artifact whose HP is below
 * {@code percent}% of its Max HP has its HP set to 0.
 */
public class PercentJudgementAction extends AbstractGameAction {
    private final int percent;

    public PercentJudgementAction(AbstractCreature target, int percent) {
        this.duration = Settings.ACTION_DUR_FAST;
        this.source = null;
        this.target = target;
        this.percent = percent;
    }

    public void update() {
        if (this.duration == Settings.ACTION_DUR_FAST && this.target instanceof AbstractMonster
                && !this.target.hasPower(ArtifactPower.POWER_ID)
                && this.target.currentHealth * 100L < (long) this.target.maxHealth * this.percent) {
            addToTop(new InstantKillAction(this.target));
        }
        this.isDone = true;
    }
}
