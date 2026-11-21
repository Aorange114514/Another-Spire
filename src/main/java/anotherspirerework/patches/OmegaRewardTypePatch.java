package anotherspirerework.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.megacrit.cardcrawl.rewards.RewardItem;

/** The reward type BaseMod saves and loads the Omega removal reward under. */
public class OmegaRewardTypePatch {
    @SpireEnum
    public static RewardItem.RewardType OMEGA_REMOVAL;
}
