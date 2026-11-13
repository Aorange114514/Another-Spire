package anotherspirerework.util;

import anotherspirerework.patches.SlyFieldPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;

/**
 * Gives a card the Sly keyword at runtime: the keyword line goes to the top of the description and
 * the card's effect is triggered when it is discarded from the hand.
 *
 * <p>The mark is a property of the fight, not of the saved deck, so nothing is stored. The five
 * reworked cards carry it from their constructor, Survivor and Tools of the Trade add it to other
 * cards; every operation is idempotent because a card's text is built more than once.
 */
public final class SlyMark {
    private SlyMark() {
    }

    /** True for the one language this mod ships card text in; every other one falls back to eng. */
    private static boolean chinese() {
        return Settings.language.name().equals("ZHS");
    }

    /** The keyword as card text writes it; Chinese has to stay one word for the parser. */
    public static String keywordName() {
        return chinese() ? "奇巧" : "Sly";
    }

    /** The first line of a marked card. */
    public static String keywordLine() {
        return keywordName() + (chinese() ? " 。" : ".") + " NL ";
    }

    public static boolean isSly(AbstractCard card) {
        return card != null && SlyFieldPatch.SLY.get(card);
    }

    /** True when the text already carries the keyword line, wherever a caller put it. */
    public static boolean showsKeyword(AbstractCard card) {
        return card != null && card.rawDescription != null
                && card.rawDescription.contains(keywordLine());
    }

    /** Marks a card whose own CardStrings already start with the keyword line. */
    public static void mark(AbstractCard card) {
        if (card != null) {
            SlyFieldPatch.SLY.set(card, true);
        }
    }

    /** Marks one card and rebuilds its text; a card that already shows everything is left alone. */
    public static void apply(AbstractCard card) {
        if (ensure(card)) {
            card.initializeDescription();
        }
    }

    /**
     * Marks one card without rebuilding its text: a caller that is already inside the text build
     * cannot call {@code initializeDescription()} again.
     *
     * @return true when the card changed
     */
    public static boolean ensure(AbstractCard card) {
        if (card == null) {
            return false;
        }
        boolean changed = !isSly(card);
        SlyFieldPatch.SLY.set(card, true);
        if (!showsKeyword(card)) {
            card.rawDescription = keywordLine()
                    + (card.rawDescription == null ? "" : card.rawDescription);
            changed = true;
        }
        return changed;
    }
}
