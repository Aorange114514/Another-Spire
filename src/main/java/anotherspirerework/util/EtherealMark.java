package anotherspirerework.util;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.GameDictionary;

/** Marks a card Ethereal at runtime, keyword line included so the card itself shows it. */
public final class EtherealMark {
    private EtherealMark() {
    }

    /** The description line the keyword needs; Chinese needs the space before the full stop. */
    public static String keywordLine() {
        return " NL " + GameDictionary.ETHEREAL.NAMES[0] + RelicChecks.keywordSuffix(Settings.language.name());
    }

    /** Marks one combat card and refreshes its text; a card that is already Ethereal is left alone. */
    public static void apply(AbstractCard card) {
        if (card == null || card.isEthereal) {
            return;
        }
        card.isEthereal = true;
        card.rawDescription += keywordLine();
        card.initializeDescription();
    }
}
