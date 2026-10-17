package anotherspirerework.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.unique.BouncingFlaskAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;
import com.megacrit.cardcrawl.vfx.combat.PotionBounceEffect;

/**
 * Bouncing Flask: applies the potion's Poison X (X + 1 once upgraded) times, on random enemies.
 *
 * <p>The base game's own bouncing action is handed the count, so the bounce animation and the
 * random target per application come from the same code the vanilla card uses.
 */
public class BouncingFlaskXAction extends AbstractGameAction {
    private final AbstractPlayer p;
    private final int poison;
    private final boolean upgraded;
    private final boolean freeToPlayOnce;
    private final int energyOnUse;

    public BouncingFlaskXAction(AbstractPlayer p, int poison, boolean upgraded, boolean freeToPlayOnce, int energyOnUse) {
        this.p = p;
        this.poison = poison;
        this.upgraded = upgraded;
        this.freeToPlayOnce = freeToPlayOnce;
        this.energyOnUse = energyOnUse;
        this.duration = Settings.ACTION_DUR_XFAST;
        this.actionType = ActionType.SPECIAL;
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
            AbstractMonster target = AbstractDungeon.getMonsters()
                    .getRandomMonster(null, true, AbstractDungeon.cardRandomRng);
            if (target != null) {
                addToBot(new VFXAction(new PotionBounceEffect(this.p.hb.cX, this.p.hb.cY, target.hb.cX, this.p.hb.cY), 0.4F));
                addToBot(new BouncingFlaskAction(target, this.poison, effect));
            }
            if (!this.freeToPlayOnce) {
                this.p.energy.use(EnergyPanel.totalCount);
            }
        }
        this.isDone = true;
    }
}
