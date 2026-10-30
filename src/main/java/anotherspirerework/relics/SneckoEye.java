package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import basemod.abstracts.CustomSavable;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;

/**
 * Snecko Eye: draw 3 additional cards each turn instead of the vanilla 2. The Confusion applied at
 * the start of every combat is inherited unchanged.
 *
 * <p>Like every reworked relic it replaces the vanilla prototype through
 * {@code patches.RelicSwapPatch}, keeping the vanilla ID, art and acquisition path. Because the
 * vanilla {@code onEquip}/{@code onUnequip} only change {@code masterHandSize}, replacing them with
 * the new size is enough and calling {@code super} there would add the vanilla 2 on top.
 */
public class SneckoEye extends com.megacrit.cardcrawl.relics.SneckoEye {
    /** Extra cards drawn each turn. */
    public static final int HAND_SIZE = 3;

    public SneckoEye() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new SneckoEye();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    @Override
    public void onEquip() {
        AbstractDungeon.player.masterHandSize += HAND_SIZE;
    }

    @Override
    public void onUnequip() {
        AbstractDungeon.player.masterHandSize -= HAND_SIZE;
    }

    /**
     * A run saved before the rework holds a Snecko Eye that only ever added the vanilla 2 hand slots.
     * The field is written once and never again, so the missing third slot is granted exactly once.
     */
    public static class Migration implements CustomSavable<Boolean> {
        @Override
        public Boolean onSave() {
            return true;
        }

        @Override
        public void onLoad(Boolean migrated) {
            if (Boolean.TRUE.equals(migrated) || AbstractDungeon.player == null) {
                return;
            }
            if (AbstractDungeon.player.hasRelic(ID)) {
                AbstractDungeon.player.masterHandSize += 1;
            }
        }
    }
}
