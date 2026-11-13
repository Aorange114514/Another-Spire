package anotherspirerework.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;

/**
 * The per-card Sly mark. ModTheSpire moves the field onto {@link AbstractCard}, so the mark follows
 * the card instance through the fight without touching the saved deck.
 */
@SpirePatch(clz = AbstractCard.class, method = SpirePatch.CLASS)
public class SlyFieldPatch {
    public static SpireField<Boolean> SLY = new SpireField<>(() -> false);
}
