package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomElite;
import com.megacrit.cardcrawl.screens.CombatRewardScreen;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.NewExpr;

import java.util.ArrayList;

/**
 * Nloth's Gift: elite fights drop an additional Rare card reward, and the vanilla tripled Rare card
 * chance is gone.
 *
 * <p>Like every reworked relic it replaces the vanilla prototype through
 * {@code patches.RelicSwapPatch}, keeping the vanilla ID, art and acquisition path. The N'loth event
 * is the one place the base game constructs a relic directly, so this class carries a patch that
 * replaces that construction with its own type.
 */
public class NlothsGift extends com.megacrit.cardcrawl.relics.NlothsGift {
    /** Cards in the extra Rare reward. */
    private static final int RARE_REWARD_CARDS = 3;

    public NlothsGift() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new NlothsGift();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** The vanilla relic multiplied the Rare chance by 3; a Rare reward is guaranteed instead. */
    @Override
    public int changeRareCardRewardChance(int rareCardChance) {
        return rareCardChance;
    }

    /** Three distinct Rare cards out of the player's own pools. */
    private static RewardItem rareCardReward() {
        RewardItem item = new RewardItem();
        item.cards.clear();
        ArrayList<String> picked = new ArrayList<>();
        for (int guard = 0; item.cards.size() < RARE_REWARD_CARDS && guard < RARE_REWARD_CARDS * 20; guard++) {
            AbstractCard card = AbstractDungeon.getCard(AbstractCard.CardRarity.RARE);
            if (card != null && !picked.contains(card.cardID)) {
                picked.add(card.cardID);
                item.cards.add(card.makeCopy());
            }
        }
        for (AbstractCard c : item.cards) {
            for (AbstractRelic r : AbstractDungeon.player.relics) {
                r.onPreviewObtainCard(c);
            }
        }
        // The reward screen draws the gold card icon for a reward that calls itself a boss reward,
        // which is how the base game shows a Rare group; the elite drop has to look the same even
        // though it was not dropped by a boss.
        ReflectionHacks.setPrivate(item, RewardItem.class, "isBoss", true);
        return item;
    }

    /**
     * Appends the Rare card group after the elite reward screen has laid out its own rewards, so the
     * extra entry is positioned exactly like the rewards it joins. A new Rare group is drawn only
     * when there is at least one card in it.
     */
    @SpirePatch(clz = CombatRewardScreen.class, method = "setupItemReward")
    public static class ExtraCardReward {
        @SpirePostfixPatch
        public static void after(CombatRewardScreen __instance) {
            AbstractRoom room = AbstractDungeon.getCurrRoom();
            if (AbstractDungeon.player == null || room == null || !(room instanceof MonsterRoomElite)) {
                return;
            }
            if (!AbstractDungeon.player.hasRelic("Nloth's Gift")) {
                return;
            }
            addCardReward(__instance, rareCardReward());
        }

        private static void addCardReward(CombatRewardScreen screen, RewardItem item) {
            if (item.cards == null || item.cards.isEmpty()) {
                return;
            }
            screen.rewards.add(item);
            screen.positionRewards();
        }
    }

    /**
     * The N'loth event does {@code new NlothsGift()} instead of going through the relic library, so
     * the swap installed by {@code patches.RelicSwapPatch} never sees it. Every construction of the
     * vanilla type inside the event's constructor is replaced with this class; the vanilla event has
     * exactly one constructor, and no other class constructs the relic directly.
     */
    @SpirePatch(clz = com.megacrit.cardcrawl.events.shrines.Nloth.class, method = SpirePatch.CONSTRUCTOR)
    public static class NlothEvent {
        @SpireInstrumentPatch
        public static ExprEditor edit() {
            return new ExprEditor() {
                @Override
                public void edit(NewExpr newExpr) throws CannotCompileException {
                    if (newExpr.getClassName().equals("com.megacrit.cardcrawl.relics.NlothsGift")) {
                        newExpr.replace("{ $_ = new anotherspirerework.relics.NlothsGift(); }");
                    }
                }
            };
        }
    }
}
