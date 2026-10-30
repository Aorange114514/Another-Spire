package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.evacipated.cardcrawl.modthespire.lib.LineFinder;
import com.evacipated.cardcrawl.modthespire.lib.Matcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertLocator;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;

/**
 * Calling Bell: keeps the whole vanilla pickup (three relic rewards plus the unique curse) and adds
 * two more relic rewards, so the relic grants five in total.
 *
 * <p>Replaces the vanilla prototype through {@code patches.RelicSwapPatch}. The extra rewards are
 * added by the nested patch at the point where the vanilla {@code update} method has built its
 * three rewards and is about to position them, so they are laid out with the rest.
 */
public class CallingBell extends com.megacrit.cardcrawl.relics.CallingBell {
    public CallingBell() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new CallingBell();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** Two extra relic rewards, inserted before the vanilla screen positions its rewards. */
    @SpirePatch(clz = com.megacrit.cardcrawl.relics.CallingBell.class, method = "update")
    public static class Bell {
        @SpireInsertPatch(locator = PositionRewards.class)
        public static void insert() {
            for (int i = 0; i < 2; i++) {
                AbstractDungeon.combatRewardScreen.rewards.add(new RewardItem(
                        AbstractDungeon.returnRandomScreenlessRelic(
                                AbstractDungeon.returnRandomRelicTier())));
            }
        }
    }

    /** The insertion point: the call that lays the vanilla rewards out. */
    public static class PositionRewards extends SpireInsertLocator {
        public int[] Locate(javassist.CtBehavior method) throws Exception {
            return LineFinder.findInOrder(method, new Matcher.MethodCallMatcher(
                    com.megacrit.cardcrawl.screens.CombatRewardScreen.class, "positionRewards"));
        }
    }
}
