package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomBoss;
import com.megacrit.cardcrawl.rooms.MonsterRoomElite;
import com.megacrit.cardcrawl.screens.CombatRewardScreen;

/**
 * Prayer Wheel: a boss or elite fight drops an additional card reward on top of the vanilla one.
 * The relic itself has no hook the base game calls, so the reward is appended to the reward screen
 * from a patch that this class carries.
 *
 * <p>Like every reworked relic it replaces the vanilla prototype through
 * {@code patches.RelicSwapPatch}, keeping the vanilla ID, art and acquisition path; the vanilla
 * {@code canSpawn()} placement rule is inherited.
 */
public class PrayerWheel extends com.megacrit.cardcrawl.relics.PrayerWheel {
    public PrayerWheel() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new PrayerWheel();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /**
     * Appends the extra card reward after the reward screen has laid out its own rewards, so the
     * entry is positioned exactly like the rewards it joins. A boss room already rolls nothing but
     * Rares, which is what makes the extra reward there a second Rare group.
     */
    @SpirePatch(clz = CombatRewardScreen.class, method = "setupItemReward")
    public static class ExtraCardReward {
        @SpirePostfixPatch
        public static void after(CombatRewardScreen __instance) {
            AbstractRoom room = AbstractDungeon.getCurrRoom();
            if (AbstractDungeon.player == null || room == null) {
                return;
            }
            if (!(room instanceof MonsterRoomBoss) && !(room instanceof MonsterRoomElite)) {
                return;
            }
            if (!AbstractDungeon.player.hasRelic("Prayer Wheel")) {
                return;
            }
            RewardItem item = new RewardItem();
            if (item.cards == null || item.cards.isEmpty()) {
                return;
            }
            __instance.rewards.add(item);
            __instance.positionRewards();
        }
    }
}
