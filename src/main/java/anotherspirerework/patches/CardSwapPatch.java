package anotherspirerework.patches;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.cards.blue.Stack;
import anotherspirerework.cards.blue.ThunderStrike;
import anotherspirerework.cards.colorless.Chrysalis;
import anotherspirerework.cards.colorless.Mayhem;
import anotherspirerework.cards.colorless.Metamorphosis;
import anotherspirerework.cards.colorless.SecretTechnique;
import anotherspirerework.cards.colorless.SecretWeapon;
import anotherspirerework.cards.colorless.ThinkingAhead;
import anotherspirerework.cards.red.InfernalBlade;
import anotherspirerework.cards.green.StormOfSteel;
import anotherspirerework.cards.green.Envenom;
import anotherspirerework.cards.green.Distraction;
import anotherspirerework.cards.green.CloakAndDagger;
import anotherspirerework.cards.green.Concentrate;
import anotherspirerework.cards.blue.Barrage;
import anotherspirerework.cards.blue.Blizzard;
import anotherspirerework.cards.blue.Chill;
import anotherspirerework.cards.blue.Claw;
import anotherspirerework.cards.blue.CoreSurge;
import anotherspirerework.cards.blue.CreativeAI;
import anotherspirerework.cards.blue.Dualcast;
import anotherspirerework.cards.blue.ForceField;
import anotherspirerework.cards.blue.Fusion;
import anotherspirerework.cards.blue.Heatsinks;
import anotherspirerework.cards.blue.HelloWorld;
import anotherspirerework.cards.blue.Hyperbeam;
import anotherspirerework.cards.blue.Leap;
import anotherspirerework.cards.blue.Melter;
import anotherspirerework.cards.blue.MultiCast;
import anotherspirerework.cards.blue.MeteorStrike;
import anotherspirerework.cards.blue.Rainbow;
import anotherspirerework.cards.blue.Rebound;
import anotherspirerework.cards.blue.Reboot;
import anotherspirerework.cards.blue.Reprogram;
import anotherspirerework.cards.blue.RipAndTear;
import anotherspirerework.cards.blue.SteamBarrier;
import anotherspirerework.cards.blue.Streamline;
import anotherspirerework.cards.blue.SweepingBeam;
import anotherspirerework.cards.colorless.Purity;
import anotherspirerework.cards.green.AThousandCuts;
import anotherspirerework.cards.green.AfterImage;
import anotherspirerework.cards.green.Backflip;
import anotherspirerework.cards.green.Choke;
import anotherspirerework.cards.green.CripplingPoison;
import anotherspirerework.cards.green.DaggerSpray;
import anotherspirerework.cards.green.DaggerThrow;
import anotherspirerework.cards.green.DieDieDie;
import anotherspirerework.cards.green.DodgeAndRoll;
import anotherspirerework.cards.green.Expertise;
import anotherspirerework.cards.green.GlassKnife;
import anotherspirerework.cards.green.MasterfulStab;
import anotherspirerework.cards.green.Outmaneuver;
import anotherspirerework.cards.green.PoisonedStab;
import anotherspirerework.cards.green.QuickSlash;
import anotherspirerework.cards.green.RiddleWithHoles;
import anotherspirerework.cards.green.Unload;
import anotherspirerework.cards.purple.BattleHymn;
import anotherspirerework.cards.purple.CarveReality;
import anotherspirerework.cards.purple.Consecrate;
import anotherspirerework.cards.purple.DeceiveReality;
import anotherspirerework.cards.purple.Devotion;
import anotherspirerework.cards.purple.FollowUp;
import anotherspirerework.cards.purple.JustLucky;
import anotherspirerework.cards.purple.LikeWater;
import anotherspirerework.cards.purple.PathToVictory;
import anotherspirerework.cards.purple.Perseverance;
import anotherspirerework.cards.purple.Sanctity;
import anotherspirerework.cards.purple.Study;
import anotherspirerework.cards.purple.Swivel;
import anotherspirerework.cards.purple.ThirdEye;
import anotherspirerework.cards.purple.Vengeance;
import anotherspirerework.cards.purple.Weave;
import anotherspirerework.cards.purple.WheelKick;
import anotherspirerework.cards.purple.WindmillStrike;
import anotherspirerework.cards.red.Berserk;
import anotherspirerework.cards.red.Bloodletting;
import anotherspirerework.cards.red.BurningPact;
import anotherspirerework.cards.red.Clash;
import anotherspirerework.cards.red.Cleave;
import anotherspirerework.cards.red.Clothesline;
import anotherspirerework.cards.red.Entrench;
import anotherspirerework.cards.red.Flex;
import anotherspirerework.cards.red.HeavyBlade;
import anotherspirerework.cards.red.IronWave;
import anotherspirerework.cards.red.Metallicize;
import anotherspirerework.cards.red.ExpectAFight;
import anotherspirerework.cards.red.Rampage;
import anotherspirerework.cards.red.SearingBlow;
import anotherspirerework.cards.red.SeeingRed;
import anotherspirerework.cards.red.Sentinel;
import anotherspirerework.cards.red.SeverSoul;
import anotherspirerework.cards.red.SwordBoomerang;
import anotherspirerework.cards.red.ThunderClap;
import anotherspirerework.cards.red.Uppercut;
import anotherspirerework.cards.red.Whirlwind;
import anotherspirerework.cards.red.WildStrike;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;

import java.util.Arrays;
import java.util.List;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.CardLibrary;

/**
 * Replaces the vanilla cards with our reworked versions, keeping their original IDs so that
 * card pools, starting decks, shops, rewards, transforms and existing saves all keep working.
 * Direct vanilla constructors bypass this map and need separate patches when used.
 */
@SpirePatch(clz = CardLibrary.class, method = "initialize")
public class CardSwapPatch {
    @SpirePostfixPatch
    public static void Postfix() {
        for (AbstractCard card : replacements()) {
            swap(card);
        }
    }

    /** The order is kept grouped by character so the replacement list is easy to audit. */
    private static List<AbstractCard> replacements() {
        return Arrays.<AbstractCard>asList(
                new Clash(), new WildStrike(), new HeavyBlade(), new Clothesline(), new IronWave(),
                new Flex(), new ThunderClap(), new Cleave(), new SwordBoomerang(), new Uppercut(),
                new Sentinel(), new Entrench(), new Bloodletting(), new SeverSoul(), new Whirlwind(),
                new Rampage(), new SearingBlow(), new Berserk(), new Metallicize(), new ExpectAFight(),
                new BurningPact(), new SeeingRed(), new InfernalBlade(),
                new DaggerSpray(), new Backflip(), new PoisonedStab(), new QuickSlash(), new DaggerThrow(),
                new Outmaneuver(), new Choke(), new RiddleWithHoles(), new GlassKnife(), new AThousandCuts(),
                new Unload(), new MasterfulStab(), new CripplingPoison(), new Expertise(), new DieDieDie(),
                new AfterImage(), new DodgeAndRoll(), new StormOfSteel(), new Envenom(),
                new Distraction(), new CloakAndDagger(), new Concentrate(),
                new Dualcast(), new Rebound(), new Barrage(), new SweepingBeam(), new Claw(), new Streamline(),
                new SteamBarrier(), new Leap(), new HelloWorld(), new Chill(), new ForceField(), new Blizzard(),
                new RipAndTear(), new Melter(), new Fusion(), new Reprogram(), new MultiCast(), new Hyperbeam(),
                new Rainbow(), new Heatsinks(), new CoreSurge(), new Reboot(), new CreativeAI(), new MeteorStrike(), new Stack(),
                new ThunderStrike(),
                new PathToVictory(), new Consecrate(), new FollowUp(), new Perseverance(), new ThirdEye(),
                new BattleHymn(), new LikeWater(), new Vengeance(), new Swivel(), new WindmillStrike(),
                new DeceiveReality(), new Study(), new Weave(), new Devotion(), new Sanctity(), new CarveReality(),
                new WheelKick(), new JustLucky(), new Purity(),
                new Mayhem(), new ThinkingAhead(), new SecretTechnique(), new SecretWeapon(),
                new Chrysalis(), new Metamorphosis());
    }

    private static void swap(AbstractCard card) {
        AbstractCard original = CardLibrary.cards.get(card.cardID);
        if (original != null) {
            card.isSeen = original.isSeen;
        } else {
            AnotherSpireRework.logger.error("Could not find vanilla card \"" + card.cardID + "\" to replace.");
        }
        CardLibrary.cards.put(card.cardID, card);
    }
}
