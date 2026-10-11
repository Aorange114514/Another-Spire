package anotherspirerework.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.defect.AnimateOrbAction;
import com.megacrit.cardcrawl.actions.defect.EvokeOrbAction;
import com.megacrit.cardcrawl.actions.defect.EvokeWithoutRemovingOrbAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.EmptyOrbSlot;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;

/**
 * Multi-Cast: evokes every orb the player has X times each (X * 2 once upgraded).
 *
 * <p>The base game evokes the rightmost orb over and over; evoking without removing walks the
 * orbs right to left, so looping once per filled slot evokes all of them exactly once per pass.
 */
public class MultiCastXAction extends AbstractGameAction {
    private boolean freeToPlayOnce = false;
    private AbstractPlayer p;
    private boolean upgraded;
    private int energyOnUse = -1;

    public MultiCastXAction(AbstractPlayer p, int energyOnUse, boolean upgraded, boolean freeToPlayOnce) {
        this.p = p;
        this.duration = Settings.ACTION_DUR_XFAST;
        this.actionType = ActionType.SPECIAL;
        this.energyOnUse = energyOnUse;
        this.upgraded = upgraded;
        this.freeToPlayOnce = freeToPlayOnce;
    }

    @Override
    public void update() {
        int orbs = filledOrbSlots();
        int effect = EnergyPanel.totalCount;
        if (this.energyOnUse != -1) {
            effect = this.energyOnUse;
        }
        if (this.p.hasRelic("Chemical X")) {
            effect += 2;
            this.p.getRelic("Chemical X").flash();
        }
        if (this.upgraded) {
            effect *= 2;
        }
        if (effect > 0 && orbs > 0) {
            for (int orb = 0; orb < orbs; orb++) {
                for (int i = 0; i < effect - 1; i++) {
                    addToBot(new EvokeWithoutRemovingOrbAction(1));
                }
                addToBot(new AnimateOrbAction(1));
                addToBot(new EvokeOrbAction(1));
            }
            if (!this.freeToPlayOnce) {
                this.p.energy.use(EnergyPanel.totalCount);
            }
        }
        this.isDone = true;
    }

    /** Empty slots are not orbs: the base game's own evoke actions skip them too. */
    private static int filledOrbSlots() {
        int count = 0;
        for (AbstractOrb orb : AbstractDungeon.player.orbs) {
            if (!(orb instanceof EmptyOrbSlot)) {
                count++;
            }
        }
        return count;
    }
}
