package anotherspirerework.powers;

/**
 * Implemented by powers that react to the player's draw pile being shuffled. Fired by
 * {@code anotherspirerework.patches.ShuffleTriggerPatch} once the shuffle has really finished,
 * so an effect that searches the draw pile sees every card that was just shuffled in.
 */
public interface DrawPileShuffleListener {
    void onDrawPileShuffled();
}
