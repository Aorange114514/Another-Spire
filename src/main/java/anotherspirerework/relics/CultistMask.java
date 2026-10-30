package anotherspirerework.relics;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.RitualPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;

/**
 * Cultist Mask: keeps the vanilla chant at the start of combat and additionally grants 1 Ritual.
 * The vanilla description is deliberately left untouched, because the new effect stays hidden.
 *
 * <p>Like every reworked relic it replaces the vanilla prototype through
 * {@code patches.RelicSwapPatch}, keeping the vanilla ID, art and acquisition path. Every hook this
 * class does not override is inherited from the vanilla relic unchanged.
 */
public class CultistMask extends com.megacrit.cardcrawl.relics.CultistMask {
    public CultistMask() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new CultistMask();
    }

    @Override
    public void atBattleStart() {
        super.atBattleStart();
        addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player,
                new RitualPower(AbstractDungeon.player, 1, true), 1));
    }
}
