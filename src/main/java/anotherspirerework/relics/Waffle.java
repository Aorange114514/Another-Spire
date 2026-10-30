package anotherspirerework.relics;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;

/**
 * Lee's Waffle: keeps its vanilla max-HP-and-heal pickup and text. Only the acquisition changes: it
 * may spawn only while the player is below half of their max HP, so the heal is never wasted.
 *
 * <p>Replaces the vanilla prototype through {@code patches.RelicSwapPatch}.
 */
public class Waffle extends com.megacrit.cardcrawl.relics.Waffle {
    public Waffle() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new Waffle();
    }

    @Override
    public boolean canSpawn() {
        return AbstractDungeon.player != null
                && (long) AbstractDungeon.player.currentHealth * 2
                        < AbstractDungeon.player.maxHealth;
    }
}
