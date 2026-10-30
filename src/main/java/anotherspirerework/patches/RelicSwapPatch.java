package anotherspirerework.patches;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.relics.*;
import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Replaces the vanilla relic prototypes with this mod's own classes.
 *
 * <p>This is the relic counterpart of {@link CardSwapPatch}: every reworked relic is one class in
 * {@code anotherspirerework.relics} that extends the vanilla relic, keeping the vanilla ID, art and
 * acquisition path. The class only overrides the hooks whose behavior changed, so everything else is
 * inherited from the vanilla relic instead of having to be forwarded by hand.
 *
 * <p>{@code RelicLibrary.initialize()} has just built the vanilla prototypes, the tier lists read by
 * the compendium and the pools a new run is filled from. The postfix puts our instance into the same
 * map, replaces it in the tier and colour lists, and carries {@code isSeen} over. Every acquisition
 * in the game goes through {@code RelicLibrary.getRelic(id).makeCopy()} (pools, chests, rewards,
 * shops, events and save loading), so replacing the prototype here reaches all of them.
 *
 * <p>Relics the game constructs directly would bypass this swap; the only one in the base game is
 * Nloth's Gift in the N'loth event, which its own relic class patches.
 */
@SpirePatch(clz = RelicLibrary.class, method = "initialize")
public class RelicSwapPatch {
    @SpirePostfixPatch
    public static void Postfix() {
        for (AbstractRelic relic : replacements()) {
            swap(relic);
        }
    }

    /** One instance of every reworked relic; grouped the way the README lists them. */
    private static List<AbstractRelic> replacements() {
        return Arrays.<AbstractRelic>asList(
                new PrismaticShard(), new Sling(), new HandDrill(), new CultistMask(), new Cauldron(),
                new FusionHammer(), new MarkOfPain(), new VelvetChoker(), new Ectoplasm(),
                new BlackBlood(), new BustedCrown(), new SacredBark(), new Sozu(),
                new RingOfTheSerpent(), new Girya(), new BlackStar(),
                new NinjaScroll(), new Duality(), new Enchiridion(), new MedicalKit(),
                new BlueCandle(), new StrangeSpoon(), new Orrery(), new FrozenEye(),
                new Inserter(), new Matryoshka(),
                new Astrolabe(), new TinyHouse(), new VioletLotus(), new FaceOfCleric(),
                new SneckoEye(), new TwistedFunnel(), new Melange(), new TheSpecimen(),
                new PrayerWheel(), new GoldenEye(), new ThreadAndNeedle(), new NlothsGift(),
                new CallingBell(), new Courier(), new ChemicalX(), new Waffle(),
                new BottledFlame(), new BottledLightning(), new BottledTornado());
    }

    /** The five maps RelicLibrary keeps its prototypes in. */
    private static final String[] MAP_FIELDS = {
            "sharedRelics", "redRelics", "greenRelics", "blueRelics", "purpleRelics"
    };

    private static void swap(AbstractRelic replacement) {
        String id = replacement.relicId;
        for (int mapIndex = 0; mapIndex < MAP_FIELDS.length; mapIndex++) {
            HashMap<String, AbstractRelic> map =
                    ReflectionHacks.getPrivateStatic(RelicLibrary.class, MAP_FIELDS[mapIndex]);
            if (map == null || !map.containsKey(id)) {
                continue;
            }
            AbstractRelic original = map.get(id);
            replacement.isSeen = original.isSeen;
            map.put(id, replacement);
            replaceInLists(original, replacement, mapIndex);
            return;
        }
        AnotherSpireRework.logger.error("Could not find vanilla relic \"" + id + "\" to replace.");
    }

    /** Moves the list entries the compendium reads over to our instance and its own tier. */
    private static void replaceInLists(AbstractRelic original, AbstractRelic replacement, int mapIndex) {
        for (ArrayList<AbstractRelic> list : allLists()) {
            list.remove(original);
        }
        ArrayList<AbstractRelic> tierList = tierList(replacement.tier);
        if (tierList != null) {
            tierList.add(replacement);
        }
        // Keep the per-colour lists in step with the map the prototype came from.
        if (mapIndex == 1) {
            RelicLibrary.redList.add(replacement);
        } else if (mapIndex == 2) {
            RelicLibrary.greenList.add(replacement);
        } else if (mapIndex == 3) {
            RelicLibrary.blueList.add(replacement);
        } else if (mapIndex == 4) {
            RelicLibrary.whiteList.add(replacement);
        }
    }

    private static List<ArrayList<AbstractRelic>> allLists() {
        return Arrays.asList(
                RelicLibrary.starterList, RelicLibrary.commonList, RelicLibrary.uncommonList,
                RelicLibrary.rareList, RelicLibrary.bossList, RelicLibrary.specialList,
                RelicLibrary.shopList, RelicLibrary.redList, RelicLibrary.greenList,
                RelicLibrary.blueList, RelicLibrary.whiteList);
    }

    private static ArrayList<AbstractRelic> tierList(AbstractRelic.RelicTier tier) {
        switch (tier) {
            case STARTER:
                return RelicLibrary.starterList;
            case COMMON:
                return RelicLibrary.commonList;
            case UNCOMMON:
                return RelicLibrary.uncommonList;
            case RARE:
                return RelicLibrary.rareList;
            case BOSS:
                return RelicLibrary.bossList;
            case SPECIAL:
                return RelicLibrary.specialList;
            case SHOP:
                return RelicLibrary.shopList;
            default:
                return null;
        }
    }
}
