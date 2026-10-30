package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;

/**
 * Matryoshka: whenever the draw pile is shuffled, draw a card. The vanilla extra relic from
 * non-boss chests is gone, so its chest counter is hidden and {@code onChestOpen} does nothing.
 *
 * <p>The shuffle itself is detected by {@code patches.ShuffleTriggerPatch}, which calls
 * {@link #drawOnShuffle()} once the shuffle animation has really finished.
 *
 * <p>Like every reworked relic it replaces the vanilla prototype through
 * {@code patches.RelicSwapPatch}, keeping the vanilla ID, art and acquisition path. Every hook this
 * class does not override is inherited from the vanilla relic unchanged.
 */
public class Matryoshka extends com.megacrit.cardcrawl.relics.Matryoshka {
    public Matryoshka() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new Matryoshka();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** The vanilla extra relic from non-boss chests is gone. */
    @Override
    public void onChestOpen(boolean bossChest) {
    }

    /** Matryoshka no longer counts chests, so its vanilla counter is hidden on pickup. */
    @Override
    public void onEquip() {
        counter = -1;
    }

    /** Called by {@code patches.ShuffleTriggerPatch} once the shuffle really finished. */
    public static void drawOnShuffle() {
        if (AbstractDungeon.player == null) {
            return;
        }
        AbstractRelic relic = AbstractDungeon.player.getRelic("Matryoshka");
        if (relic == null) {
            return;
        }
        relic.flash();
        AbstractDungeon.actionManager.addToBottom(
                new RelicAboveCreatureAction(AbstractDungeon.player, relic));
        AbstractDungeon.actionManager.addToBottom(new DrawCardAction(AbstractDungeon.player, 1));
    }
}
