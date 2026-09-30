package anotherspire.patches;

import anotherspire.AnotherSpire;
import anotherspire.cards.blue.Barrage;
import anotherspire.cards.blue.Blizzard;
import anotherspire.cards.blue.Chill;
import anotherspire.cards.blue.Claw;
import anotherspire.cards.blue.Dualcast;
import anotherspire.cards.blue.ForceField;
import anotherspire.cards.blue.Fusion;
import anotherspire.cards.blue.HelloWorld;
import anotherspire.cards.blue.Hyperbeam;
import anotherspire.cards.blue.Leap;
import anotherspire.cards.blue.Melter;
import anotherspire.cards.blue.MultiCast;
import anotherspire.cards.blue.Rebound;
import anotherspire.cards.blue.Reprogram;
import anotherspire.cards.blue.RipAndTear;
import anotherspire.cards.blue.SteamBarrier;
import anotherspire.cards.blue.Streamline;
import anotherspire.cards.blue.SweepingBeam;
import anotherspire.cards.green.AThousandCuts;
import anotherspire.cards.green.Backflip;
import anotherspire.cards.green.Choke;
import anotherspire.cards.green.DaggerSpray;
import anotherspire.cards.green.DaggerThrow;
import anotherspire.cards.green.GlassKnife;
import anotherspire.cards.green.Outmaneuver;
import anotherspire.cards.green.PoisonedStab;
import anotherspire.cards.green.QuickSlash;
import anotherspire.cards.green.RiddleWithHoles;
import anotherspire.cards.purple.BattleHymn;
import anotherspire.cards.purple.Consecrate;
import anotherspire.cards.purple.DeceiveReality;
import anotherspire.cards.purple.Devotion;
import anotherspire.cards.purple.FollowUp;
import anotherspire.cards.purple.LikeWater;
import anotherspire.cards.purple.PathToVictory;
import anotherspire.cards.purple.Perseverance;
import anotherspire.cards.purple.Study;
import anotherspire.cards.purple.Swivel;
import anotherspire.cards.purple.ThirdEye;
import anotherspire.cards.purple.Vengeance;
import anotherspire.cards.purple.Weave;
import anotherspire.cards.purple.WindmillStrike;
import anotherspire.cards.red.Berserk;
import anotherspire.cards.red.Bloodletting;
import anotherspire.cards.red.Clash;
import anotherspire.cards.red.Cleave;
import anotherspire.cards.red.Clothesline;
import anotherspire.cards.red.Entrench;
import anotherspire.cards.red.Flex;
import anotherspire.cards.red.HeavyBlade;
import anotherspire.cards.red.IronWave;
import anotherspire.cards.red.Metallicize;
import anotherspire.cards.red.Rampage;
import anotherspire.cards.red.SearingBlow;
import anotherspire.cards.red.Sentinel;
import anotherspire.cards.red.SeverSoul;
import anotherspire.cards.red.SwordBoomerang;
import anotherspire.cards.red.ThunderClap;
import anotherspire.cards.red.Uppercut;
import anotherspire.cards.red.Whirlwind;
import anotherspire.cards.red.WildStrike;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.CardLibrary;

/**
 * Replaces the vanilla cards with our reworked versions, keeping their original IDs so that
 * card pools, starting decks, shops, rewards, transforms and existing saves all keep working.
 * Only the library map needs updating: every source in the base game builds its cards from it.
 */
@SpirePatch(clz = CardLibrary.class, method = "initialize")
public class CardSwapPatch {
    @SpirePostfixPatch
    public static void Postfix() {
        // Ironclad
        swap(new Clash());
        swap(new WildStrike());
        swap(new HeavyBlade());
        swap(new Clothesline());
        swap(new IronWave());
        swap(new Flex());
        swap(new ThunderClap());
        swap(new Cleave());
        swap(new SwordBoomerang());
        swap(new Uppercut());
        swap(new Sentinel());
        swap(new Entrench());
        swap(new Bloodletting());
        swap(new SeverSoul());
        swap(new Whirlwind());
        swap(new Rampage());
        swap(new SearingBlow());
        swap(new Berserk());
        swap(new Metallicize());
        // Silent
        swap(new DaggerSpray());
        swap(new Backflip());
        swap(new PoisonedStab());
        swap(new QuickSlash());
        swap(new DaggerThrow());
        swap(new Outmaneuver());
        swap(new Choke());
        swap(new RiddleWithHoles());
        swap(new GlassKnife());
        swap(new AThousandCuts());
        // Defect
        swap(new Dualcast());
        swap(new Rebound());
        swap(new Barrage());
        swap(new SweepingBeam());
        swap(new Claw());
        swap(new Streamline());
        swap(new SteamBarrier());
        swap(new Leap());
        swap(new HelloWorld());
        swap(new Chill());
        swap(new ForceField());
        swap(new Blizzard());
        swap(new RipAndTear());
        swap(new Melter());
        swap(new Fusion());
        swap(new Reprogram());
        swap(new MultiCast());
        swap(new Hyperbeam());
        // Watcher
        swap(new PathToVictory());
        swap(new Consecrate());
        swap(new FollowUp());
        swap(new Perseverance());
        swap(new ThirdEye());
        swap(new BattleHymn());
        swap(new LikeWater());
        swap(new Vengeance());
        swap(new Swivel());
        swap(new WindmillStrike());
        swap(new DeceiveReality());
        swap(new Study());
        swap(new Weave());
        swap(new Devotion());
    }

    private static void swap(AbstractCard card) {
        AbstractCard original = CardLibrary.cards.get(card.cardID);
        if (original != null) {
            card.isSeen = original.isSeen;
        } else {
            AnotherSpire.logger.error("Could not find vanilla card \"" + card.cardID + "\" to replace.");
        }
        CardLibrary.cards.put(card.cardID, card);
    }
}
