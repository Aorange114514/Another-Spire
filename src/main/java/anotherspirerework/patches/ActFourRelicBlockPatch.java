package anotherspirerework.patches;

import anotherspirerework.util.RelicChecks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.AncientTeaSet;
import com.megacrit.cardcrawl.relics.CeramicFish;
import com.megacrit.cardcrawl.relics.Courier;
import com.megacrit.cardcrawl.relics.DarkstonePeriapt;
import com.megacrit.cardcrawl.relics.DreamCatcher;
import com.megacrit.cardcrawl.relics.Girya;
import com.megacrit.cardcrawl.relics.JuzuBracelet;
import com.megacrit.cardcrawl.relics.MealTicket;
import com.megacrit.cardcrawl.relics.MeatOnTheBone;
import com.megacrit.cardcrawl.relics.OldCoin;
import com.megacrit.cardcrawl.relics.Omamori;
import com.megacrit.cardcrawl.relics.PeacePipe;
import com.megacrit.cardcrawl.relics.PrayerWheel;
import com.megacrit.cardcrawl.relics.PreservedInsect;
import com.megacrit.cardcrawl.relics.QuestionCard;
import com.megacrit.cardcrawl.relics.RegalPillow;
import com.megacrit.cardcrawl.relics.Shovel;
import com.megacrit.cardcrawl.relics.SingingBowl;
import com.megacrit.cardcrawl.relics.SmilingMask;
import com.megacrit.cardcrawl.relics.TinyChest;
import com.megacrit.cardcrawl.relics.WingBoots;

import java.util.Arrays;
import java.util.List;

/**
 * Relics that must never be offered inside the Ending, where the Heart sits.
 *
 * <p>Most of the list is blocked from one patch on {@link AbstractRelic#canSpawn()}; the relics that
 * override {@code canSpawn()} themselves never reach the base implementation, so each of them needs
 * its own prefix. This stays a single list instead of one class per relic: it is a rule about where
 * a relic may spawn, not a rewrite of the relic itself.
 */
public class ActFourRelicBlockPatch {
    /** The relics the requirements hide inside the Ending. */
    public static final List<String> BLOCKED = Arrays.asList(
            "White Beast Statue", "Darkstone Periapt", "PreservedInsect", "Omamori", "Smiling Mask",
            "Juzu Bracelet", "Ancient Tea Set", "Tiny Chest", "Dream Catcher", "Regal Pillow",
            "CeramicFish", "MealTicket", "Gremlin Horn", "Meat on the Bone", "Ninja Scroll",
            "Eternal Feather", "The Courier", "Question Card", "Singing Bowl", "StoneCalendar",
            "Old Coin", "Girya", "Peace Pipe", "The Specimen", "WingedGreaves", "Prayer Wheel", "Shovel"
    );

    public static boolean blocked(String relicId) {
        return RelicChecks.inActFour() && BLOCKED.contains(relicId);
    }

    /** Relics that do not override canSpawn themselves. */
    @SpirePatch(clz = AbstractRelic.class, method = "canSpawn")
    public static class Base {
        @SpirePostfixPatch
        public static boolean after(boolean __result, AbstractRelic __instance) {
            return __result && !blocked(__instance.relicId);
        }
    }

    /** Relics that do override canSpawn, so the base class patch never runs for them. */
    @SpirePatch(clz = DarkstonePeriapt.class, method = "canSpawn")
    @SpirePatch(clz = PreservedInsect.class, method = "canSpawn")
    @SpirePatch(clz = Omamori.class, method = "canSpawn")
    @SpirePatch(clz = SmilingMask.class, method = "canSpawn")
    @SpirePatch(clz = JuzuBracelet.class, method = "canSpawn")
    @SpirePatch(clz = AncientTeaSet.class, method = "canSpawn")
    @SpirePatch(clz = TinyChest.class, method = "canSpawn")
    @SpirePatch(clz = DreamCatcher.class, method = "canSpawn")
    @SpirePatch(clz = RegalPillow.class, method = "canSpawn")
    @SpirePatch(clz = CeramicFish.class, method = "canSpawn")
    @SpirePatch(clz = MealTicket.class, method = "canSpawn")
    @SpirePatch(clz = MeatOnTheBone.class, method = "canSpawn")
    @SpirePatch(clz = Courier.class, method = "canSpawn")
    @SpirePatch(clz = QuestionCard.class, method = "canSpawn")
    @SpirePatch(clz = SingingBowl.class, method = "canSpawn")
    @SpirePatch(clz = OldCoin.class, method = "canSpawn")
    @SpirePatch(clz = Girya.class, method = "canSpawn")
    @SpirePatch(clz = PeacePipe.class, method = "canSpawn")
    @SpirePatch(clz = PrayerWheel.class, method = "canSpawn")
    @SpirePatch(clz = Shovel.class, method = "canSpawn")
    @SpirePatch(clz = WingBoots.class, method = "canSpawn")
    public static class Overriding {
        @SpirePrefixPatch
        public static SpireReturn<Boolean> before(AbstractRelic __instance) {
            return blocked(__instance.relicId) ? SpireReturn.Return(false) : SpireReturn.Continue();
        }
    }
}
