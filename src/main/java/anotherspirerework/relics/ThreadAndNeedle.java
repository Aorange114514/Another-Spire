package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.PlatedArmorPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;

/**
 * Thread and Needle: gain 2 Plated Armor at the start of every turn. The vanilla relic granted 4
 * once per combat; that battle-start grant is gone.
 *
 * <p>Like every reworked relic it replaces the vanilla prototype through
 * {@code patches.RelicSwapPatch}, keeping the vanilla ID, art and acquisition path.
 */
public class ThreadAndNeedle extends com.megacrit.cardcrawl.relics.ThreadAndNeedle {
    /** Plated Armor granted each turn. */
    public static final int ARMOR = 2;

    public ThreadAndNeedle() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new ThreadAndNeedle();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** The vanilla battle-start armor applies no more; the armor is a per-turn grant now. */
    @Override
    public void atBattleStart() {
    }

    /** Flashes the relic above the player and grants the Plated Armor for this turn. */
    @Override
    public void atTurnStart() {
        flash();
        addToBot(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player,
                new PlatedArmorPower(AbstractDungeon.player, ARMOR), ARMOR));
    }
}
