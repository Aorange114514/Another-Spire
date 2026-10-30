package anotherspirerework.relics;

/**
 * A relic whose {@code canSpawn()} can be temporarily false without the relic having left the run's
 * pool.
 *
 * <p>The base game draws an ID out of a relic pool, then deletes it when {@code canSpawn()} says no.
 * For the relics whose own condition is only a "not yet" (the bottled relics before the third act),
 * {@code patches.ShopRelicPatch} leaves the entry in the pool instead, so a later act can still
 * offer it. The relic class itself only has to answer {@link #keepInPool()}.
 */
public interface KeepInPool {
    /** True while the relic has to stay in the pool because of a temporary gate. */
    boolean keepInPool();
}
