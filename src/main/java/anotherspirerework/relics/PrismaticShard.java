package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import basemod.abstracts.CustomSavable;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;

/**
 * Prismatic Shard: a boss relic now. It keeps the vanilla "rewards may contain colourless and other
 * colors" behavior (the base game tests it by ID) and gains [E] and a card every turn through the
 * vanilla permanent energy/draw fields.
 *
 * <p>Replaces the vanilla prototype through {@code patches.RelicSwapPatch}, so the tier shown by
 * the compendium, used by the pools and read by the shop is the one set here.
 */
public class PrismaticShard extends com.megacrit.cardcrawl.relics.PrismaticShard {
    public PrismaticShard() {
        super();
        this.tier = AbstractRelic.RelicTier.BOSS;
    }

    @Override
    public AbstractRelic makeCopy() {
        return new PrismaticShard();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    @Override
    public void onEquip() {
        super.onEquip();
        AbstractDungeon.player.energy.energyMaster++;
        AbstractDungeon.player.masterHandSize++;
    }

    @Override
    public void onUnequip() {
        AbstractDungeon.player.energy.energyMaster--;
        AbstractDungeon.player.masterHandSize--;
    }

    /**
     * A run saved before the rework holds a Prismatic Shard that never received the permanent energy
     * and draw slot. The field is written once and never again, so the bonus cannot stack.
     */
    public static class StatMigration implements CustomSavable<Boolean> {
        @Override
        public Boolean onSave() {
            return true;
        }

        @Override
        public void onLoad(Boolean migrated) {
            if (Boolean.TRUE.equals(migrated) || AbstractDungeon.player == null) {
                return;
            }
            if (AbstractDungeon.player.hasRelic(PrismaticShard.ID)) {
                AbstractDungeon.player.energy.energyMaster++;
                AbstractDungeon.player.masterHandSize++;
            }
        }
    }
}
