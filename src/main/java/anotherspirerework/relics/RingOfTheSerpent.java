package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.relics.AbstractRelic;

/**
 * Ring of the Serpent: draw 2 extra cards at the start of each of the first three turns of a combat.
 * The vanilla permanent +1 card per turn is gone.
 *
 * <p>Replaces the vanilla prototype through {@code patches.RelicSwapPatch}; {@code canSpawn()}
 * (needs Ring of the Snake) is inherited.
 */
public class RingOfTheSerpent extends com.megacrit.cardcrawl.relics.RingOfTheSerpent {
    /** Number of cards drawn at the start of each of the first three turns. */
    public static final int CARDS = 2;

    /** Turns of this combat so far, reset when a fight starts. */
    private int turns;

    public RingOfTheSerpent() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new RingOfTheSerpent();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** The vanilla pickup grants +1 card per turn permanently; the new effect is per-combat. */
    @Override
    public void onEquip() {
    }

    @Override
    public void onUnequip() {
    }

    @Override
    public void atBattleStart() {
        this.turns = 0;
    }

    @Override
    public void atTurnStart() {
        this.turns++;
        if (activeTurn(this.turns)) {
            flash();
            addToBot(new DrawCardAction(CARDS));
        }
    }

    /** True only on the first three turns of a combat. */
    public static boolean activeTurn(int turn) {
        return anotherspirerework.util.RelicChecks.serpentTurnActive(turn);
    }
}
