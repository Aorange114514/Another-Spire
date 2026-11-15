package anotherspirerework.util;

import anotherspirerework.AnotherSpireRework;
import basemod.BaseMod;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.gson.Gson;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.localization.UIStrings;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.function.Consumer;

/** Loads English first, then overlays the selected language. */
public final class Localization {
    private static final String DEFAULT_LANGUAGE = "eng";

    private Localization() {
    }

    public static void loadStrings() {
        loadWithFallback(Localization::loadStrings, "strings");
    }

    public static void loadKeywords() {
        loadWithFallback(Localization::loadKeywords, "keywords");
    }

    private static void loadWithFallback(Consumer<String> loader, String kind) {
        loader.accept(DEFAULT_LANGUAGE);
        String language = Settings.language.name().toLowerCase(Locale.ROOT);
        if (DEFAULT_LANGUAGE.equals(language)) {
            return;
        }
        try {
            loader.accept(language);
        } catch (GdxRuntimeException e) {
            AnotherSpireRework.logger.warn(AnotherSpireRework.modID + " does not support " + language + " " + kind + ".");
        }
    }

    private static void loadStrings(String language) {
        BaseMod.loadCustomStringsFile(CardStrings.class, AnotherSpireRework.localizationPath(language, "CardStrings.json"));
        BaseMod.loadCustomStringsFile(PowerStrings.class, AnotherSpireRework.localizationPath(language, "PowerStrings.json"));
        BaseMod.loadCustomStringsFile(RelicStrings.class, AnotherSpireRework.localizationPath(language, "RelicStrings.json"));
        BaseMod.loadCustomStringsFile(UIStrings.class, AnotherSpireRework.localizationPath(language, "UIStrings.json"));
    }

    private static void loadKeywords(String language) {
        String json = Gdx.files.internal(AnotherSpireRework.localizationPath(language, "Keywords.json"))
                .readString(StandardCharsets.UTF_8.name());
        KeywordInfo[] keywords = new Gson().fromJson(json, KeywordInfo[].class);
        if (keywords == null) {
            return;
        }
        for (KeywordInfo keyword : keywords) {
            for (int i = 0; i < keyword.NAMES.length; i++) {
                keyword.NAMES[i] = keyword.NAMES[i].toLowerCase(Locale.ROOT).replace('_', ' ');
            }
            // BaseMod prefixes the names; vanilla card text also needs the plain words.
            String[] plainNames = keyword.NAMES.clone();
            BaseMod.addKeyword(AnotherSpireRework.modID, keyword.PROPER_NAME, keyword.NAMES, keyword.DESCRIPTION);
            for (String name : plainNames) {
                GameDictionary.keywords.put(name, keyword.DESCRIPTION);
                GameDictionary.parentWord.put(name, plainNames[0]);
            }
        }
    }

    private static class KeywordInfo {
        public String PROPER_NAME;
        public String[] NAMES;
        public String DESCRIPTION;
    }
}
