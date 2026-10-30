package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.actions.common.ExhaustSpecificCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Frozen Eye: every basic Strike and Defend gains Ethereal. The vanilla relic only let the player
 * see the draw pile order; that behavior is not expressible as a hook here and is simply not
 * replaced, while the new Ethereal effect is added on top.
 *
 * <p>Cards the player already owns are marked in place when the relic is picked up and unmarked
 * when it is lost ({@link #onEquip()} / {@link #onUnequip()}). Cards made later are caught by the
 * nested constructor patch, and the end-of-turn exhaust is the nested prefix patch below - both
 * live here because the relic class itself has no hook for them.
 *
 * <p>Like every reworked relic it replaces the vanilla prototype through
 * {@code patches.RelicSwapPatch}, keeping the vanilla ID, art and acquisition path.
 */
public class FrozenEye extends com.megacrit.cardcrawl.relics.FrozenEye {
    /** Strike and Defend cards of every character, the ones Frozen Eye hands Ethereal to. */
    private static final String[] BASIC_ATTACK_AND_DEFEND = {
            "Strike_R", "Strike_G", "Strike_B", "Strike_P",
            "Defend_R", "Defend_G", "Defend_B", "Defend_P"
    };

    public FrozenEye() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new FrozenEye();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** Cards already in the deck when the relic is picked up are updated in place. */
    @Override
    public void onEquip() {
        markEverywhere(true);
    }

    @Override
    public void onUnequip() {
        markEverywhere(false);
    }

    public static boolean isStrikeOrDefend(AbstractCard card) {
        return card != null && Arrays.asList(BASIC_ATTACK_AND_DEFEND).contains(card.cardID);
    }

    /** True when the player holds a Frozen Eye; safe outside a run. */
    public static boolean owned() {
        return AbstractDungeon.player != null && AbstractDungeon.player.hasRelic("Frozen Eye");
    }

    /** Marks one card Ethereal, text included, so the keyword is visible on the card. */
    public static void applyFrozenEye(AbstractCard card) {
        if (!isStrikeOrDefend(card) || card.isEthereal) {
            return;
        }
        card.isEthereal = true;
        card.rawDescription += " NL " + GameDictionary.ETHEREAL.NAMES[0] + keywordSuffix();
        card.initializeDescription();
    }

    public static void removeFrozenEye(AbstractCard card) {
        if (!isStrikeOrDefend(card) || !card.isEthereal) {
            return;
        }
        card.isEthereal = false;
        card.rawDescription = card.rawDescription
                .replace(" NL " + GameDictionary.ETHEREAL.NAMES[0] + keywordSuffix(), "");
        card.initializeDescription();
    }

    /**
     * The keyword has to be its own word: the description parser splits on spaces, so a Chinese
     * "虚无。" would be treated as one unknown word and never highlighted. Latin text is fine with
     * the period attached, the parser strips punctuation from the end of a word there.
     */
    public static String keywordSuffix(String languageName) {
        return anotherspirerework.util.RelicChecks.keywordSuffix(languageName);
    }

    private static String keywordSuffix() {
        return keywordSuffix(Settings.language.name());
    }

    /** Covers every basic Strike / Defend in the deck, hand and piles at the moment of the swap. */
    private static void markEverywhere(boolean ethereal) {
        if (AbstractDungeon.player == null) {
            return;
        }
        java.util.List<CardGroup> groups = new ArrayList<>();
        groups.add(AbstractDungeon.player.masterDeck);
        groups.add(AbstractDungeon.player.hand);
        groups.add(AbstractDungeon.player.drawPile);
        groups.add(AbstractDungeon.player.discardPile);
        groups.add(AbstractDungeon.player.exhaustPile);
        for (CardGroup group : groups) {
            if (group == null) {
                continue;
            }
            for (AbstractCard card : new ArrayList<>(group.group)) {
                if (ethereal) {
                    applyFrozenEye(card);
                } else {
                    removeFrozenEye(card);
                }
            }
        }
    }

    /** Covers every basic Strike / Defend the player builds or picks up while the relic is held. */
    @SpirePatch(clz = AbstractCard.class, method = SpirePatch.CONSTRUCTOR,
            paramtypez = {String.class, String.class, String.class, int.class, String.class,
                    AbstractCard.CardType.class, AbstractCard.CardColor.class,
                    AbstractCard.CardRarity.class, AbstractCard.CardTarget.class})
    public static class NewCards {
        @SpirePostfixPatch
        public static void after(AbstractCard __instance) {
            if (owned()) {
                applyFrozenEye(__instance);
            }
        }
    }

    /** Ethereal cards are exhausted at the end of the player's turn; mirror that for our cards. */
    @SpirePatch(clz = AbstractCard.class, method = "triggerOnEndOfPlayerTurn")
    public static class EndOfTurn {
        @SpirePrefixPatch
        public static void before(AbstractCard __instance) {
            if (!__instance.isEthereal && owned() && isStrikeOrDefend(__instance)) {
                AbstractDungeon.actionManager.addToTop(
                        new ExhaustSpecificCardAction(__instance, AbstractDungeon.player.hand));
            }
        }
    }
}
