package anotherspirerework.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.PoisonPower;

/**
 * Turns the damage a card just dealt into Poison on the same target, once.
 *
 * <p>It has to run after the damage action, because only then does the game know how much damage
 * got through the block; {@code lastDamageTaken} is the unblocked amount the base game also uses
 * for its own on-damage relics. Dead and escaping targets are skipped, and the Poison goes through
 * ApplyPowerAction so Weak/Focus style modifiers and the base game's stacking stay in charge.
 */
public class BanePoisonAction extends AbstractGameAction {

    public BanePoisonAction(AbstractCreature target, AbstractCreature source) {
        setValues(target, source);
        this.actionType = ActionType.DEBUFF;
        this.duration = this.startDuration;
    }

    @Override
    public void update() {
        if (this.target != null && !this.target.isDying && !this.target.isDeadOrEscaped() && this.target.currentHealth > 0) {
            int amount = this.target.lastDamageTaken;
            if (amount > 0) {
                addToTop(new ApplyPowerAction(this.target, this.source,
                        new PoisonPower(this.target, this.source, amount), amount, AttackEffect.POISON));
            }
        }
        this.isDone = true;
    }
}
