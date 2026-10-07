package anotherspirerework.patches;

import anotherspirerework.AnotherSpireRework;
import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.screens.SingleCardViewPopup;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Card art the artist drops into the mod's own resources folder. Art is never generated, cropped
 * or scaled here: when the file is there it replaces the base game image, when it is not the
 * vanilla atlas is used exactly as before.
 *
 * <p>Files live next to the other resources:
 * <pre>
 * anotherspirerework/images/cards/&lt;name&gt;.png     250x190, the card face
 * anotherspirerework/images/cards/&lt;name&gt;_p.png  500x380, the zoomed card view
 * </pre>
 *
 * {@code <name>} is the card ID without spaces ({@code DieDieDie}) or the file name of the vanilla
 * art ({@code die_die_die}); both spellings are accepted. Only one of the two sizes is needed: the
 * big image is used for the card face as well when the small one is missing (and the other way
 * around in the zoomed view), because both are the same picture at the same position. The image is
 * sampled as a whole, so a 2x file simply scales down on the face.
 */
public class CardArtLoaderPatch {
    /** Where card images may be dropped, the mod's own sub folder first. */
    private static final String[] FOLDERS = {"cards/", ""};

    private static final Map<String, Texture> CACHE = new HashMap<>();

    /** The file names a card image may use, most specific first. */
    public static List<String> candidateNames(String cardID, String assetUrl) {
        String asset = assetUrl == null ? "" : assetUrl;
        int slash = asset.lastIndexOf('/');
        return Arrays.asList(
                cardID.replace(" ", ""),
                asset.replace('/', '_'),
                slash < 0 ? asset : asset.substring(slash + 1));
    }

    private static List<String> names(AbstractCard card) {
        return candidateNames(card.cardID, card.assetUrl);
    }

    /**
     * Looks up the image and picks the best match: the smallest file for the card face and the
     * largest for the zoomed view, so having both a 1x and a 2x file always uses each on the
     * surface it was made for. Only one of the two sizes is enough on its own.
     */
    private static Texture texture(AbstractCard card, boolean preferLarge) {
        Texture best = null;
        for (String suffix : new String[]{"", "_p"}) {
            for (String folder : FOLDERS) {
                for (String name : names(card)) {
                    if (name.isEmpty()) {
                        continue;
                    }
                    Texture candidate = load(folder + name + suffix);
                    if (candidate == null) {
                        continue;
                    }
                    if (best == null) {
                        best = candidate;
                    } else if (preferLarge == (candidate.getWidth() > best.getWidth())) {
                        best = candidate;
                    }
                }
            }
        }
        return best;
    }

    private static Texture load(String relativePath) {
        if (CACHE.containsKey(relativePath)) {
            return CACHE.get(relativePath);
        }
        FileHandle file = Gdx.files.internal(AnotherSpireRework.imagePath(relativePath + ".png"));
        Texture loaded = file.exists() ? new Texture(file) : null;
        CACHE.put(relativePath, loaded);
        return loaded;
    }

    private static Color renderColor(AbstractCard card) {
        Color color = ReflectionHacks.getPrivate(card, AbstractCard.class, "renderColor");
        return color == null ? Color.WHITE : color;
    }

    @SpirePatch(clz = AbstractCard.class, method = "renderPortrait")
    public static class Face {
        @SpirePrefixPatch
        public static SpireReturn<Void> before(AbstractCard __instance, SpriteBatch sb) {
            Texture img = __instance.isLocked ? null : texture(__instance, false);
            if (img == null) {
                return SpireReturn.Continue();
            }
            float drawX = __instance.current_x - 125.0F;
            float drawY = __instance.current_y - 95.0F;
            sb.setColor(renderColor(__instance));
            sb.draw(img, drawX, drawY + 72.0F, 125.0F, 23.0F, 250.0F, 190.0F,
                    __instance.drawScale * Settings.scale, __instance.drawScale * Settings.scale,
                    __instance.angle, 0, 0, sourceWidth(img), sourceHeight(img), false, false);
            sb.setColor(Color.WHITE);
            return SpireReturn.Return(null);
        }
    }

    @SpirePatch(clz = AbstractCard.class, method = "renderJokePortrait")
    public static class BetaFace {
        @SpirePrefixPatch
        public static SpireReturn<Void> before(AbstractCard __instance, SpriteBatch sb) {
            return Face.before(__instance, sb);
        }
    }

    @SpirePatch(clz = SingleCardViewPopup.class, method = "renderPortrait")
    public static class Popup {
        @SpirePrefixPatch
        public static SpireReturn<Void> before(SingleCardViewPopup __instance, SpriteBatch sb) {
            AbstractCard card = ReflectionHacks.getPrivate(__instance, SingleCardViewPopup.class, "card");
            Texture img = card == null || card.isLocked ? null : texture(card, true);
            if (img == null) {
                return SpireReturn.Continue();
            }
            sb.draw(img, Settings.WIDTH / 2.0F - 250.0F, Settings.HEIGHT / 2.0F - 190.0F + 136.0F * Settings.scale,
                    250.0F, 190.0F, 500.0F, 380.0F, Settings.scale, Settings.scale, 0.0F,
                    0, 0, sourceWidth(img), sourceHeight(img), false, false);
            return SpireReturn.Return(null);
        }
    }

    /** The whole image is drawn into the box, whichever of the two sizes was supplied. */
    private static int sourceWidth(Texture img) {
        return Math.min(img.getWidth(), 500);
    }

    private static int sourceHeight(Texture img) {
        return Math.min(img.getHeight(), 380);
    }
}
