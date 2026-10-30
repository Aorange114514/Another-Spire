package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.screens.CombatRewardScreen;

/**
 * Busted Crown: the extra energy is gone entirely; instead every card reward is replaced by a
 * random relic reward of a random tier.
 *
 * <p>Replaces the vanilla prototype through {@code patches.RelicSwapPatch}. The card reward is
 * still generated the way the base game generates it and only converted when the reward screen is
 * laid out; the nested patch owns that step because the screen lives outside the relic. The vanilla
 * class rewrites its own description on a class change, so both string hooks are overridden.
 */
public class BustedCrown extends com.megacrit.cardcrawl.relics.BustedCrown {
    public BustedCrown() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new BustedCrown();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    @Override
    public void updateDescription(AbstractPlayer.PlayerClass c) {
        this.description = getUpdatedDescription();
        this.tips.clear();
        this.tips.add(new PowerTip(this.name, this.description));
        initializeTips();
    }

    /** The vanilla energy is gone entirely: the reward conversion is the whole relic. */
    @Override
    public void onEquip() {
    }

    @Override
    public void onUnequip() {
    }

    /**
     * Vanilla shrank every card reward by two cards; the rework generates the reward untouched and
     * converts the whole entry to a relic reward, so the card count is irrelevant.
     */
    @Override
    public int changeNumberOfCardsInReward(int numberOfCards) {
        return numberOfCards;
    }

    /**
     * Convert only after the callers have inspected the generated card reward: the screen is laid
     * out last. Cauldron's own pickup screen must be left alone while it is being opened, because
     * it removes its card reward itself.
     */
    @SpirePatch(clz = CombatRewardScreen.class, method = "positionRewards")
    public static class CrownRewards {
        @SpirePrefixPatch
        public static void before(CombatRewardScreen __instance) {
            if (Cauldron.isBrewing() || AbstractDungeon.player == null
                    || !AbstractDungeon.player.hasRelic(ID)) {
                return;
            }
            for (int i = 0; i < __instance.rewards.size(); i++) {
                RewardItem old = __instance.rewards.get(i);
                if (old.type != RewardItem.RewardType.CARD) {
                    continue;
                }
                RewardItem replacement = new RewardItem(
                        AbstractDungeon.returnRandomRelic(AbstractDungeon.returnRandomRelicTier()));
                replacement.isDone = old.isDone;
                replacement.ignoreReward = old.ignoreReward;
                __instance.rewards.set(i, replacement);
            }
        }
    }
}
