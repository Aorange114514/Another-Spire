package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.actions.common.LoseHPAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

/**
 * Combust: the stack count is the HP lost at the end of the turn, and the amount is the total
 * damage dealt to every enemy whenever the player loses HP. Multiple stacks therefore read as
 * "lose N HP, deal 5N (7N) damage", and the icon shows the total damage.
 */
public class CombustPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpireRework.makeID("Combust");
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("Combust");

    private int hpLoss;

    public CombustPower(AbstractCreature owner, int damageAmount) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = damageAmount;
        this.hpLoss = 1;
        this.type = PowerType.BUFF;
        updateDescription();
        loadRegion("combust");
    }

    @Override
    public void stackPower(int stackAmount) {
        this.fontScale = 8.0F;
        this.amount += stackAmount;
        this.hpLoss++;
        updateDescription();
    }

    @Override
    public void atEndOfTurn(boolean isPlayer) {
        if (!isPlayer || AbstractDungeon.getMonsters().areMonstersBasicallyDead()) {
            return;
        }
        flash();
        addToBot(new LoseHPAction(this.owner, this.owner, this.hpLoss, AbstractGameAction.AttackEffect.FIRE));
    }

    @Override
    public int onLoseHp(int damageAmount) {
        if (damageAmount > 0 && !AbstractDungeon.getMonsters().areMonstersBasicallyDead()) {
            flash();
            addToBot(new DamageAllEnemiesAction(null,
                    DamageInfo.createDamageMatrix(this.amount, true),
                    DamageInfo.DamageType.THORNS, AbstractGameAction.AttackEffect.FIRE));
        }
        return damageAmount;
    }

    @Override
    public void updateDescription() {
        this.description = powerStrings.DESCRIPTIONS[0] + this.hpLoss + powerStrings.DESCRIPTIONS[1]
                + this.amount + powerStrings.DESCRIPTIONS[2];
    }
}
