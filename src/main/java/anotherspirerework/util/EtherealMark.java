package anotherspirerework.util;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.GameDictionary;

/**
 * Gives a card the Ethereal keyword at runtime: the keyword line goes into the card's description
 * and the card is exhausted at the end of the turn.
 *
 * <p>Every operation is idempotent. A card's text is built more than once (a fight rebuilds its
 * cards from the deck, a view screen can build the text again, and a card can be marked on the turn
 * it is drawn), so the line must never be added twice.
 */
public final class EtherealMark {
    private EtherealMark() {
    }

    /**
     * The keyword line, placed at the top of the description. Chinese needs the space before the
     * full stop; the Latin name is stored lowercase but reads as a capitalised keyword on the card.
     */
    public static String keywordLine() {
        String language = Settings.language.name();
        String name = GameDictionary.ETHEREAL.NAMES[0];
        if (!RelicChecks.isCjk(language)) {
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
        return name + RelicChecks.keywordSuffix(language) + " NL ";
    }

    /** True when the card's text already carries the keyword line. */
    public static boolean showsKeyword(AbstractCard card) {
        return card != null && card.rawDescription != null && card.rawDescription.contains(keywordLine());
    }

    /** Marks one card and refreshes its text; a card that already shows everything is left alone. */
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
        boolean changed = !card.isEthereal;
        card.isEthereal = true;
        if (!showsKeyword(card)) {
            card.rawDescription = keywordLine()
                    + (card.rawDescription == null ? "" : card.rawDescription);
            changed = true;
        }
        return changed;
    }

    /** Adds the keyword line to the text only; used on a card that is not part of the fight. */
    public static void show(AbstractCard card) {
        if (card == null || showsKeyword(card)) {
            return;
        }
        card.rawDescription = keywordLine()
                + (card.rawDescription == null ? "" : card.rawDescription);
        card.initializeDescription();
    }

    /** Takes the keyword line out of the text again. */
    public static void hide(AbstractCard card) {
        if (!showsKeyword(card)) {
            return;
        }
        card.rawDescription = card.rawDescription.replace(keywordLine(), "");
        card.initializeDescription();
    }
}
