package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PotionHelper;
import com.megacrit.cardcrawl.potions.PotionSlot;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import java.util.ArrayList;

/**
 * Cauldron: on pickup it gains one potion slot and brews five potions into the current room's
 * rewards, drawing from the player's own potion pool plus Fruit Juice (the vanilla relic handed out
 * five random potions and removed a single card reward; this one removes all of them).
 *
 * <p>Replaces the vanilla prototype through {@code patches.RelicSwapPatch}. While the reward screen
 * this relic opens is being built, {@link #isBrewing()} is true so Busted Crown's card-reward
 * replacement does not touch Cauldron's own screens. The second line of the vanilla relic strings
 * stays the header of that screen.
 */
public class Cauldron extends com.megacrit.cardcrawl.relics.Cauldron {
    private static boolean brewing;

    public Cauldron() {
        super();
    }

    /** True while Cauldron is opening its own reward screen; read by Busted Crown. */
    public static boolean isBrewing() {
        return brewing;
    }

    @Override
    public AbstractRelic makeCopy() {
        return new Cauldron();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** The vanilla pickup is replaced entirely: a slot, a brew and a screen without a card reward. */
    @Override
    public void onEquip() {
        int slots = AbstractDungeon.player.potionSlots;
        AbstractDungeon.player.potionSlots += 1;
        AbstractDungeon.player.potions.add(new PotionSlot(slots));
        ArrayList<String> ids = new ArrayList<>(
                PotionHelper.getPotions(AbstractDungeon.player.chosenClass, false));
        ids.add("Fruit Juice");
        for (int i = 0; i < 5; i++) {
            AbstractDungeon.getCurrRoom().addPotionToRewards(
                    PotionHelper.getPotion(ids.get(AbstractDungeon.potionRng.random(ids.size() - 1))));
        }
        brewing = true;
        try {
            AbstractDungeon.combatRewardScreen.open(DESCRIPTIONS[1]);
        } finally {
            brewing = false;
        }
        AbstractDungeon.getCurrRoom().rewardPopOutTimer = 0.0F;
        AbstractDungeon.combatRewardScreen.rewards.removeIf(r -> r.type == RewardItem.RewardType.CARD);
        AbstractDungeon.combatRewardScreen.positionRewards();
    }
}
