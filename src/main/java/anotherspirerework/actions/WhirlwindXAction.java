package anotherspirerework.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;
import com.megacrit.cardcrawl.vfx.combat.CleaveEffect;
import com.megacrit.cardcrawl.vfx.combat.WhirlwindEffect;

/** Vanilla WhirlwindAction, hitting X (X + 1 once upgraded) times. */
public class WhirlwindXAction extends AbstractGameAction {
    public int[] multiDamage;

    private boolean freeToPlayOnce = false;
    private DamageInfo.DamageType damageType;
    private AbstractPlayer p;
    private boolean upgraded;
    private int energyOnUse = -1;

    public WhirlwindXAction(AbstractPlayer p, int[] multiDamage, DamageInfo.DamageType damageType,
                            boolean freeToPlayOnce, int energyOnUse, boolean upgraded) {
        this.multiDamage = multiDamage;
        this.damageType = damageType;
        this.p = p;
        this.freeToPlayOnce = freeToPlayOnce;
        this.upgraded = upgraded;
        this.duration = Settings.ACTION_DUR_XFAST;
        this.actionType = ActionType.SPECIAL;
        this.energyOnUse = energyOnUse;
    }

    @Override
    public void update() {
        int effect = EnergyPanel.totalCount;
        if (this.energyOnUse != -1) {
            effect = this.energyOnUse;
        }
        if (this.p.hasRelic("Chemical X")) {
            effect += 2;
            this.p.getRelic("Chemical X").flash();
        }
        if (this.upgraded) {
            effect++;
        }
        if (effect > 0) {
            for (int i = 0; i < effect; i++) {
                if (i == 0) {
                    addToBot(new SFXAction("ATTACK_WHIRLWIND"));
                    addToBot(new VFXAction(new WhirlwindEffect(), 0.0F));
                }
                addToBot(new SFXAction("ATTACK_HEAVY"));
                addToBot(new VFXAction(this.p, new CleaveEffect(), 0.0F));
                addToBot(new DamageAllEnemiesAction(this.p, this.multiDamage, this.damageType, AttackEffect.NONE, true));
            }
            if (!this.freeToPlayOnce) {
                this.p.energy.use(EnergyPanel.totalCount);
            }
        }
        this.isDone = true;
    }
}
