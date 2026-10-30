package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.effects.ChooseDeckCardsEffect;
import anotherspirerework.util.RelicChecks;
import basemod.abstracts.CustomSavable;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import javassist.expr.ExprEditor;

import java.util.HashMap;
import java.util.Map;

/**
 * Strange Spoon: on pickup, choose up to two cards and remove their Exhaust for good. The vanilla
 * 50% "do not Exhaust" coin flip is switched off, otherwise the relic would keep its old effect on
 * top of the new one.
 *
 * <p>A card's own flag does not survive long: the base game saves a deck card as nothing but its
 * ID, upgrade count and {@code misc} value, and the combat deck is built from fresh copies. So:
 *
 * <ul>
 *   <li>Copies inherit the change, because a card that no longer Exhausts while its base game
 *       version does is recognised whenever a copy is made (nested {@link Copies}).</li>
 *   <li>On save, the deck is scanned and how many cards of each identity lost their Exhaust is
 *       written down - the only thing a save file can carry.</li>
 *   <li>After loading, that count is applied to the rebuilt deck, so exactly as many copies of the
 *       same card lose Exhaust again.</li>
 * </ul>
 *
 * <p>Like every reworked relic it replaces the vanilla prototype through
 * {@code patches.RelicSwapPatch}, keeping the vanilla ID, art and acquisition path. The hooks the
 * vanilla relic cannot express live in the nested patch classes below; the save field is
 * {@link SaveData}, registered by the main class.
 */
public class StrangeSpoon extends com.megacrit.cardcrawl.relics.StrangeSpoon {
    /** identity -> how many copies of it still have to be restored after a load. */
    private static final Map<String, Integer> PENDING = new HashMap<>();

    public StrangeSpoon() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new StrangeSpoon();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** The pickup screen replaces the vanilla coin flip entirely. */
    @Override
    public void onEquip() {
        flash();
        ChooseDeckCardsEffect.start(ChooseDeckCardsEffect.Choice.REMOVE_EXHAUST,
                AnotherSpireRework.getRelicDescription(relicId, 1), 2);
    }

    /** ID, upgrade count and misc: everything the base game keeps when it saves a deck card. */
    private static String key(AbstractCard card) {
        return card.cardID + "|" + card.timesUpgraded + "|" + card.misc;
    }

    /** True when a card no longer Exhausts although its base game version does. */
    public static boolean removed(AbstractCard card) {
        if (card == null || card.exhaust) {
            return false;
        }
        AbstractCard prototype = CardLibrary.getCard(card.cardID);
        return prototype != null && prototype.exhaust;
    }

    /** Called for every card the player picks in the Spoon screen. */
    public static void mark(AbstractCard card) {
        strip(card);
    }

    /** Drops Exhaust from a card, text included, so the card reads correctly right away. */
    public static void strip(AbstractCard card) {
        card.exhaust = false;
        card.rawDescription = RelicChecks.withoutExhaustText(card.rawDescription, GameDictionary.EXHAUST.NAMES[0]);
        card.initializeDescription();
    }

    /** Everything a save file carries about the removals: one line, written by {@link SaveData}. */
    public static String onSave() {
        if (AbstractDungeon.player == null || AbstractDungeon.player.masterDeck == null) {
            return "";
        }
        Map<String, Integer> counts = new HashMap<>();
        for (AbstractCard card : AbstractDungeon.player.masterDeck.group) {
            if (removed(card)) {
                String id = key(card);
                counts.put(id, counts.containsKey(id) ? counts.get(id) + 1 : 1);
            }
        }
        return anotherspirerework.util.RelicSave.formatCounts(counts);
    }

    /** Loads the counts; they are applied to the deck later, once it exists. */
    public static void onLoad(String value) {
        PENDING.clear();
        PENDING.putAll(anotherspirerework.util.RelicSave.parseCounts(value));
    }

    /** Applies the saved counts to the deck once it exists; cheap while nothing is pending. */
    public static void restorePending() {
        if (PENDING.isEmpty() || AbstractDungeon.player == null) {
            return;
        }
        CardGroup deck = AbstractDungeon.player.masterDeck;
        if (deck == null || deck.size() == 0) {
            return;
        }
        for (Map.Entry<String, Integer> entry : PENDING.entrySet()) {
            int left = entry.getValue() == null ? 0 : entry.getValue();
            for (AbstractCard card : deck.group) {
                if (left <= 0) {
                    break;
                }
                if (!removed(card) && key(card).equals(entry.getKey())) {
                    strip(card);
                    left--;
                }
            }
        }
        PENDING.clear();
    }

    /** Copies made for combat and by card effects inherit the change. */
    @SpirePatch(clz = AbstractCard.class, method = "makeStatEquivalentCopy")
    public static class Copies {
        /**
         * ModTheSpire hands a value returning postfix the original result first and the card
         * instance second - by position, not by parameter name (verified against the shipped
         * patcher). Declaring them the other way round means returning the source card instead of
         * the copy, which makes the combat deck share objects with the master deck: a card
         * Exhausted in a fight then disappears from the deck view and comes back invisible.
         */
        @SpirePostfixPatch
        public static AbstractCard after(AbstractCard __result, AbstractCard __instance) {
            if (__result != null && removed(__instance)) {
                strip(__result);
            }
            return __result;
        }
    }

    /** Vanilla's coin flip is replaced by the pickup screen. */
    @SpirePatch(clz = UseCardAction.class, method = "update")
    public static class NoVanillaRoll {
        @SpireInstrumentPatch
        public static ExprEditor edit() {
            return RelicChecks.ignoreHasRelic("Strange Spoon");
        }
    }

    /** The save field registered by the main class; the deck scan lives on the relic class. */
    public static class SaveData implements CustomSavable<String> {
        @Override
        public String onSave() {
            return StrangeSpoon.onSave();
        }

        @Override
        public void onLoad(String value) {
            StrangeSpoon.onLoad(value);
        }
    }
}
