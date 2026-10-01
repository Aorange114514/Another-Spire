package anotherspire;

import anotherspire.patches.ExhaustTracker;
import anotherspire.patches.BitePatch;
import anotherspire.patches.OrbTrackerPatch;
import anotherspire.powers.DevotionDrawPower;
import basemod.BaseMod;
import basemod.interfaces.EditKeywordsSubscriber;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostBattleSubscriber;
import basemod.interfaces.PostExhaustSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import basemod.interfaces.PostPowerApplySubscriber;
import basemod.interfaces.StartGameSubscriber;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFileHandle;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.Patcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.Gson;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.watcher.MantraPower;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scannotation.AnnotationDB;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@SpireInitializer
public class AnotherSpire implements
        EditStringsSubscriber,
        EditKeywordsSubscriber,
        PostInitializeSubscriber,
        PostBattleSubscriber,
        PostExhaustSubscriber,
        StartGameSubscriber,
        PostPowerApplySubscriber {

    public static ModInfo info;
    public static String modID;
    static { loadModInfo(); }

    private static final String resourcesFolder = checkResourcesPath();
    private static final String defaultLanguage = "eng";
    public static final Logger logger = LogManager.getLogger(modID);

    public static String makeID(String id) {
        return modID + ":" + id;
    }

    public static void initialize() {
        new AnotherSpire();
    }

    public AnotherSpire() {
        BaseMod.subscribe(this);
        logger.info(modID + " subscribed to BaseMod.");
    }

    @Override
    public void receivePostInitialize() {
        BitePatch.applyStrings();
        Texture badgeTexture = new Texture(Gdx.files.internal(imagePath("badge.png")));
        BaseMod.registerModBadge(badgeTexture, info.Name, String.join(", ", info.Authors), info.Description, null);
    }

    @Override
    public void receivePostBattle(AbstractRoom room) {
        OrbTrackerPatch.lastOrb = null;
        ExhaustTracker.reset();
    }

    @Override
    public void receiveStartGame() {
        OrbTrackerPatch.lastOrb = null;
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

    private static String getLangString() {
        return Settings.language.name().toLowerCase();
    }

    @Override
    public void receiveEditStrings() {
        loadLocalization(defaultLanguage);
        if (!defaultLanguage.equals(getLangString())) {
            try {
                loadLocalization(getLangString());
            } catch (GdxRuntimeException e) {
                logger.warn(modID + " does not support " + getLangString() + " strings.");
            }
        }
    }

    private static void loadLocalization(String lang) {
        BaseMod.loadCustomStringsFile(CardStrings.class, localizationPath(lang, "CardStrings.json"));
        BaseMod.loadCustomStringsFile(PowerStrings.class, localizationPath(lang, "PowerStrings.json"));
    }

    @Override
    public void receiveEditKeywords() {
        loadKeywords(defaultLanguage);
        if (!defaultLanguage.equals(getLangString())) {
            try {
                loadKeywords(getLangString());
            } catch (GdxRuntimeException e) {
                logger.warn(modID + " does not support " + getLangString() + " keywords.");
            }
        }
    }

    private static void loadKeywords(String lang) {
        Gson gson = new Gson();
        String json = Gdx.files.internal(localizationPath(lang, "Keywords.json"))
                .readString(String.valueOf(StandardCharsets.UTF_8));
        KeywordInfo[] keywords = gson.fromJson(json, KeywordInfo[].class);
        if (keywords == null) {
            return;
        }
        for (KeywordInfo keyword : keywords) {
            keyword.prep();
            BaseMod.addKeyword(modID, keyword.PROPER_NAME, keyword.NAMES, keyword.DESCRIPTION);
        }
    }

    private static class KeywordInfo {
        public String PROPER_NAME;
        public String[] NAMES;
        public String DESCRIPTION;

        public void prep() {
            for (int i = 0; i < NAMES.length; i++) {
                NAMES[i] = NAMES[i].toLowerCase().replace('_', ' ');
            }
        }
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
        if (strings == null || strings.EXTENDED_DESCRIPTION == null || strings.EXTENDED_DESCRIPTION.length <= index) {
            return "";
        }
        return strings.EXTENDED_DESCRIPTION[index];
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

    public static String localizationPath(String lang, String file) {
        return resourcesFolder + "/localization/" + lang + "/" + file;
    }

    public static String imagePath(String file) {
        return resourcesFolder + "/images/" + file;
    }

    private static String checkResourcesPath() {
        String name = AnotherSpire.class.getName();
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
            return initializers.contains(AnotherSpire.class.getName());
        }).findFirst();
        if (infos.isPresent()) {
            info = infos.get();
            modID = info.ID;
        } else {
            throw new RuntimeException("Failed to determine mod info/ID based on initializer.");
        }
    }
}
