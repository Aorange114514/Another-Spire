package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.saveAndContinue.SaveAndContinue;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import com.megacrit.cardcrawl.screens.CombatRewardScreen;

import java.util.ArrayList;

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
    /** The current reward screen's relic roll, kept until the post-combat save is written. */
    static Roll roll;

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
                if (roll == null || !roll.shown()) {
                    roll = new Roll();
                }
                RewardItem replacement = new RewardItem(
                        AbstractDungeon.returnRandomRelic(AbstractDungeon.returnRandomRelicTier()));
                replacement.isDone = old.isDone;
                replacement.ignoreReward = old.ignoreReward;
                roll.rewards.add(replacement);
                __instance.rewards.set(i, replacement);
            }
        }
    }

    /**
     * The post-combat save is written immediately after the screen was laid out, so it would record
     * the relic RNG and pools after this roll. The base game rewinds {@code card_seed_count} for the
     * same reason, because it regenerates the card reward from the save; a load would otherwise roll
     * the relic again and show a different one (the save/load reroll). Putting back only what the
     * roll consumed keeps the screen reproducible while every later roll still advances normally.
     */
    @SpirePatch(clz = SaveAndContinue.class, method = "save")
    public static class PostCombatSave {
        @SpirePrefixPatch
        public static void before(SaveFile save) {
            if (save.post_combat && roll != null && roll.shown()) {
                roll.rewind(save);
            }
        }
    }

    /** The relic RNG counter and pools from just before a reward screen rolled its relic. */
    static final class Roll {
        private final int relicCounter;
        private final ArrayList<String> common;
        private final ArrayList<String> uncommon;
        private final ArrayList<String> rare;
        private final ArrayList<String> shop;
        private final ArrayList<String> boss;
        private final ArrayList<RewardItem> rewards = new ArrayList<>();

        Roll() {
            relicCounter = AbstractDungeon.relicRng.counter;
            common = new ArrayList<>(AbstractDungeon.commonRelicPool);
            uncommon = new ArrayList<>(AbstractDungeon.uncommonRelicPool);
            rare = new ArrayList<>(AbstractDungeon.rareRelicPool);
            shop = new ArrayList<>(AbstractDungeon.shopRelicPool);
            boss = new ArrayList<>(AbstractDungeon.bossRelicPool);
        }

        /** True while a reward of this roll is still shown: the screen it belongs to is up. */
        boolean shown() {
            for (RewardItem reward : rewards) {
                if (AbstractDungeon.combatRewardScreen.rewards.contains(reward)) {
                    return true;
                }
            }
            return false;
        }

        /** Puts back what the roll consumed, so a load replays the same relic reward. */
        void rewind(SaveFile save) {
            save.relic_seed_count = relicCounter;
            save.common_relics = new ArrayList<>(common);
            save.uncommon_relics = new ArrayList<>(uncommon);
            save.rare_relics = new ArrayList<>(rare);
            save.shop_relics = new ArrayList<>(shop);
            save.boss_relics = new ArrayList<>(boss);
        }
    }
}
