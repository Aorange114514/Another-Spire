package anotherspirerework.relics;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;

/**
 * Bottled Lightning: keeps its complete vanilla behavior (bottle a Skill, draw it at combat start).
 * Only the acquisition is gated: before the third act the pool draw leaves it in the pool, so the
 * run can still be offered it later instead of losing the slot.
 *
 * <p>Replaces the vanilla prototype through {@code patches.RelicSwapPatch}.
 */
public class BottledLightning extends com.megacrit.cardcrawl.relics.BottledLightning implements KeepInPool {
    public BottledLightning() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new BottledLightning();
    }

    @Override
    public boolean canSpawn() {
        return AbstractDungeon.actNum >= 3 && super.canSpawn();
    }

    @Override
    public boolean keepInPool() {
        return AbstractDungeon.actNum < 3;
    }
}
