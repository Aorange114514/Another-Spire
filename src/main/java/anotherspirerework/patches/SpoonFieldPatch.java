package anotherspirerework.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;

/**
 * The per-card "the Strange Spoon took its Exhaust away" mark. ModTheSpire moves the field onto
 * {@link AbstractCard}, so the mark follows the card instance through upgrades and previews; a new
 * copy starts unmarked and inherits the mark through the relic's copy hook.
 */
@SpirePatch(clz = AbstractCard.class, method = SpirePatch.CLASS)
public class SpoonFieldPatch {
    public static SpireField<Boolean> REMOVED = new SpireField<>(() -> false);
}
