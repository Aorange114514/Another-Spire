package anotherspirerework;

import anotherspirerework.actions.PlayShivsFromExhaustAction;
import anotherspirerework.patches.BitePatch;
import anotherspirerework.patches.ExhaustTracker;
import anotherspirerework.patches.OmegaRewardTypePatch;
import anotherspirerework.patches.OrbTrackerPatch;
import anotherspirerework.patches.ShuffleTriggerPatch;
import anotherspirerework.powers.DevotionDrawPower;
import anotherspirerework.powers.OmegaRemovalPower;
import anotherspirerework.relics.BlackBlood;
import anotherspirerework.relics.BlackStar;
import anotherspirerework.relics.BlueCandle;
import anotherspirerework.relics.MedicalKit;
import anotherspirerework.relics.PrismaticShard;
import anotherspirerework.relics.SneckoEye;
import anotherspirerework.relics.Sozu;
import anotherspirerework.relics.StrangeSpoon;
import anotherspirerework.relics.TinyHouse;
import anotherspirerework.rewards.OmegaRemovalReward;
import anotherspirerework.util.Localization;
import basemod.BaseMod;
import basemod.interfaces.EditKeywordsSubscriber;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostBattleSubscriber;
import basemod.interfaces.PostDrawSubscriber;
import basemod.interfaces.PostDungeonUpdateSubscriber;
import basemod.interfaces.PostExhaustSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import basemod.interfaces.PostPowerApplySubscriber;
import basemod.interfaces.StartGameSubscriber;
import basemod.interfaces.PreStartGameSubscriber;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFileHandle;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.Patcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.watcher.MantraPower;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scannotation.AnnotationDB;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@SpireInitializer
public class AnotherSpireRework implements
        EditStringsSubscriber,
        EditKeywordsSubscriber,
        PostInitializeSubscriber,
        PostBattleSubscriber,
        PostDrawSubscriber,
        PostDungeonUpdateSubscriber,
        PostExhaustSubscriber,
        StartGameSubscriber,
        PreStartGameSubscriber,
        PostPowerApplySubscriber {

    public static ModInfo info;
    public static String modID;
    static { loadModInfo(); }

    private static final String resourcesFolder = checkResourcesPath();
    public static final Logger logger = LogManager.getLogger(modID);

    public static String makeID(String id) {
        return modID + ":" + id;
    }

    public static void initialize() {
        new AnotherSpireRework();
    }

    public AnotherSpireRework() {
        BaseMod.subscribe(this);
        BaseMod.addSaveField(makeID("EliteKills"), new BlackStar.SaveData());
        BaseMod.addSaveField(makeID("TurnRelicStatsV1"), new PrismaticShard.StatMigration());
        BaseMod.addSaveField(makeID("SpoonNoExhaust"), new StrangeSpoon.SaveData());
        BaseMod.addSaveField(makeID("SneckoHandV1"), new SneckoEye.Migration());
        logger.info(modID + " subscribed to BaseMod.");
    }

    @Override
    public void receivePostInitialize() {
        BaseMod.registerCustomReward(
                OmegaRewardTypePatch.OMEGA_REMOVAL,
                rewardSave -> new OmegaRemovalReward(),
                customReward -> new com.megacrit.cardcrawl.rewards.RewardSave(customReward.type.toString(), null));
        BitePatch.applyStrings();
        Texture badgeTexture = new Texture(Gdx.files.internal(imagePath("badge.png")));
        BaseMod.registerModBadge(badgeTexture, info.Name, String.join(", ", info.Authors), info.Description, null);
    }

    @Override
    public void receivePostBattle(AbstractRoom room) {
        OmegaRemovalPower.addRewardsIfPending(room);
        BlackStar.onVictory(room);
        resetCombatState();
    }

    @Override
    public void receiveStartGame() {
        resetCombatState();
        TinyHouse.reset();
    }

    private static void resetCombatState() {
        BlackBlood.reset();
        PlayShivsFromExhaustAction.reset();
        ShuffleTriggerPatch.reset();
        OrbTrackerPatch.lastOrb = null;
        ExhaustTracker.reset();
        OmegaRemovalReward.reset();
    }

    @Override
    public void receivePreStartGame() {
        BlackStar.resetRun();
        Sozu.resetBattle();
    }

    /** Medical Kit, Blue Candle, Mayhem and Blur all watch the cards the player draws. */
    @Override
    public void receivePostDraw(AbstractCard card) {
        MedicalKit.onCardDrawn(card);
        BlueCandle.onCardDrawn(card);
        anotherspirerework.powers.ScuffleDrawPower.onCardDrawn(card);
        anotherspirerework.powers.BlurDrawPower.onCardDrawn(card);
    }

    /**
     * The Strange Spoon save field already applies itself when it is read (before combat prep builds
     * the combat deck); this per-frame pass is only the fallback for a load where the deck did not
     * exist yet. The same pass also finishes the on-draw relic effects that were held back while the
     * player was looking at a pile.
     */
    @Override
    public void receivePostDungeonUpdate() {
        StrangeSpoon.restorePending();
        MedicalKit.processPendingDraws();
        BlueCandle.processPendingDraws();
        anotherspirerework.patches.ShuffleTriggerPatch.tick();
        TinyHouse.tick();
        OmegaRemovalReward.tick();
    }

    /** Seeing Red needs to know whether a card was exhausted on the turn it is played. */
    @Override
    public void receivePostExhaust(AbstractCard card) {
        ExhaustTracker.onCardExhausted();
    }

    @Override
    public void receivePostPowerApplySubscriber(AbstractPower power, AbstractCreature target, AbstractCreature source) {
        if (!(power instanceof MantraPower) || AbstractDungeon.player == null) {
            return;
        }
        if (target != AbstractDungeon.player) {
            return;
        }
        AbstractPower devotion = AbstractDungeon.player.getPower(DevotionDrawPower.POWER_ID);
        if (devotion != null && devotion.amount > 0) {
            AbstractDungeon.actionManager.addToBottom(new DrawCardAction(AbstractDungeon.player, devotion.amount));
        }
    }

    /*----------Localization----------*/

    @Override
    public void receiveEditStrings() {
        Localization.loadStrings();
    }

    @Override
    public void receiveEditKeywords() {
        Localization.loadKeywords();
    }

    /*----------Helpers----------*/

    /**
     * True only while a fight is really going on. Cards that show a live counter use this, because
     * the hand, the exhaust pile and the player all outlive the fight (the card reward screen still
     * updates its cards), and those counters should be gone by then.
     */
    public static boolean inCombat() {
        // getCurrRoom() is just currMapNode.getRoom(), and there is no map node in the main menu or
        // in the compendium, so it has to be checked before it is called (card.update() runs there).
        if (AbstractDungeon.player == null || AbstractDungeon.currMapNode == null) {
            return false;
        }
        AbstractRoom room = AbstractDungeon.getCurrRoom();
        return room != null && room.phase == AbstractRoom.RoomPhase.COMBAT && !room.isBattleEnding();
    }

    /**
     * The extra description line a card counter uses. Lives in the localization file, and is allowed
     * to be missing (a translation that has not been updated simply has no counter line).
     */
    public static String extendedDescription(CardStrings strings, int index) {
        if (index < 0 || strings == null || strings.EXTENDED_DESCRIPTION == null || strings.EXTENDED_DESCRIPTION.length <= index) {
            return "";
        }
        return strings.EXTENDED_DESCRIPTION[index] == null ? "" : strings.EXTENDED_DESCRIPTION[index];
    }

    /** Card text lives under the mod prefixed key, so overwriting vanilla strings is never required. */
    public static CardStrings getCardStrings(String vanillaID) {
        CardStrings strings = CardCrawlGame.languagePack.getCardStrings(makeID(vanillaID));
        if (strings == null || strings.NAME == null || strings.DESCRIPTION == null) {
            logger.error("Missing CardStrings entry for " + makeID(vanillaID));
            return CardStrings.getMockCardString();
        }
        return strings;
    }

    public static PowerStrings getPowerStrings(String powerID) {
        PowerStrings strings = CardCrawlGame.languagePack.getPowerStrings(makeID(powerID));
        if (strings == null || strings.NAME == null || strings.DESCRIPTIONS == null) {
            logger.error("Missing PowerStrings entry for " + makeID(powerID));
            strings = new PowerStrings();
            strings.NAME = "MISSING POWER";
            strings.DESCRIPTIONS = new String[]{"Missing description.", " Missing description.", " Missing description."};
        }
        return strings;
    }

    /** Relic text lives under the mod prefixed key, so overwriting vanilla strings is never required. */
    public static String getRelicDescription(String vanillaID) {
        return getRelicDescription(vanillaID, 0);
    }

    /**
     * The n-th line of a relic's own description. Relics use the extra lines for the header of a
     * card picker (Spoon, Orrery, Astrolabe, Tiny House), so a missing line must not be fatal.
     */
    public static String getRelicDescription(String vanillaID, int index) {
        com.megacrit.cardcrawl.localization.RelicStrings strings =
                CardCrawlGame.languagePack.getRelicStrings(makeID(vanillaID));
        if (strings == null || strings.DESCRIPTIONS == null || index < 0
                || index >= strings.DESCRIPTIONS.length || strings.DESCRIPTIONS[index] == null) {
            logger.error("Missing RelicStrings entry for " + makeID(vanillaID) + " line " + index);
            return "";
        }
        return strings.DESCRIPTIONS[index];
    }

    public static String localizationPath(String lang, String file) {
        return resourcesFolder + "/localization/" + lang + "/" + file;
    }

    public static String imagePath(String file) {
        return resourcesFolder + "/images/" + file;
    }

    private static String checkResourcesPath() {
        String name = AnotherSpireRework.class.getName();
        int separator = name.indexOf('.');
        if (separator > 0) {
            name = name.substring(0, separator);
        }

        FileHandle resources = new LwjglFileHandle(name, Files.FileType.Internal);
        if (!resources.exists()) {
            throw new RuntimeException("\n\tFailed to find resources folder; expected it to be at \"resources/" + name + "\".");
        }
        if (!resources.child("images").exists()) {
            throw new RuntimeException("\n\tFailed to find the 'images' folder in the mod's 'resources/" + name + "' folder.");
        }
        if (!resources.child("localization").exists()) {
            throw new RuntimeException("\n\tFailed to find the 'localization' folder in the mod's 'resources/" + name + "' folder.");
        }
        return name;
    }

    private static void loadModInfo() {
        Optional<ModInfo> infos = Arrays.stream(Loader.MODINFOS).filter((modInfo) -> {
            AnnotationDB annotationDB = Patcher.annotationDBMap.get(modInfo.jarURL);
            if (annotationDB == null) {
                return false;
            }
            Set<String> initializers = annotationDB.getAnnotationIndex()
                    .getOrDefault(SpireInitializer.class.getName(), Collections.emptySet());
            return initializers.contains(AnotherSpireRework.class.getName());
        }).findFirst();
        if (infos.isPresent()) {
            info = infos.get();
            modID = info.ID;
        } else {
            throw new RuntimeException("Failed to determine mod info/ID based on initializer.");
        }
    }
}
