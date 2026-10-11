package anotherspirerework.patches;

import anotherspirerework.AnotherSpireRework;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.powers.AbstractPower;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Power icons the artist drops into the mod's own resources folder, the same deal the card art gets:
 * when the file is there it replaces the icon this mod borrowed from the base game, when it is not
 * the borrowed icon is used exactly as before.
 *
 * <p>Files live next to the other resources:
 * <pre>
 * anotherspirerework/images/powers/&lt;name&gt;_48.png    48x48,  the buff bar icon
 * anotherspirerework/images/powers/&lt;name&gt;_128.png  128x128, the icon of the gain/flash effect
 * </pre>
 *
 * {@code <name>} is the power's ID (the string the mod passes to {@code makeID}, e.g. {@code Nirvana})
 * or the power class' simple name ({@code NirvanaCounterPower}). The two sizes are independent: with
 * only the 48 supplied the flash keeps the base game's icon, and the other way round the bar keeps
 * it. {@code loadRegion} draws its two icons at fixed scales, so a file of the wrong size would not
 * be scaled into place - which is why each size has to be supplied as its own file.
 *
 * <p>Only this mod's own powers are touched. A vanilla power keeps the base game's icon even when a
 * file happens to be named after it.
 */
public class PowerArtLoaderPatch {
    /** The only folder power images are looked up in. */
    private static final String FOLDER = "powers/";

    private static final Map<String, Texture> CACHE = new HashMap<>();

    /** The ID as it can appear in a file name: the mod prefix holds a colon, which files cannot. */
    public static String fileId(String powerId) {
        if (powerId == null) {
            return "";
        }
        int colon = powerId.indexOf(':');
        return colon < 0 ? powerId : powerId.substring(colon + 1);
    }

    /** The names a power image may use, the power ID first and the class name second. */
    public static List<String> candidateNames(String powerId, String className) {
        return Arrays.asList(fileId(powerId), className == null ? "" : className);
    }

    /** Own powers only: a borrowed vanilla icon must stay the vanilla icon. */
    public static boolean owns(String className) {
        return className != null && className.startsWith("anotherspirerework.powers.");
    }

    /**
     * Loads one size of a power's icon, or null when the artist has not drawn it.
     *
     * <p>Deliberately not cached per power: the file name is the cache key, so the two sizes and both
     * spellings share one entry each.
     */
    public static Texture load(String name, int size) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        String path = FOLDER + name + "_" + size;
        if (CACHE.containsKey(path)) {
            return CACHE.get(path);
        }
        FileHandle file = Gdx.files.internal(AnotherSpireRework.imagePath(path + ".png"));
        Texture loaded = file.exists() ? new Texture(file) : null;
        CACHE.put(path, loaded);
        return loaded;
    }

    /** Wraps a plain texture so the base game's power rendering can draw it. */
    private static TextureAtlas.AtlasRegion region(Texture texture) {
        return new TextureAtlas.AtlasRegion(texture, 0, 0, texture.getWidth(), texture.getHeight());
    }

    /**
     * Replaces whichever of the two icons the artist supplied.
     *
     * @return true when at least one of them was replaced, which also means the borrowed icon is no
     *         longer needed.
     */
    static boolean apply(AbstractPower power) {
        if (power == null || !owns(power.getClass().getName())) {
            return false;
        }
        boolean replaced = false;
        for (String name : candidateNames(power.ID, power.getClass().getSimpleName())) {
            Texture small = load(name, 48);
            Texture large = load(name, 128);
            if (small != null) {
                power.region48 = region(small);
                replaced = true;
            }
            if (large != null) {
                power.region128 = region(large);
                replaced = true;
            }
            if (replaced) {
                break;
            }
        }
        return replaced;
    }

    /**
     * The game reads the icons through {@code loadRegion} while a power is being built, after the
     * power has set its ID. Returning without calling the base method is what makes the file win over
     * the borrowed icon; the base method runs untouched whenever there is no file.
     */
    @SpirePatch(clz = AbstractPower.class, method = "loadRegion", paramtypez = {String.class})
    public static class LoadRegion {
        @SpirePrefixPatch
        public static SpireReturn<Void> before(AbstractPower __instance, String fileName) {
            if (!apply(__instance)) {
                return SpireReturn.Continue();
            }
            AnotherSpireRework.logger.info("Power icon replaced: " + __instance.ID);
            return SpireReturn.Return(null);
        }
    }
}
