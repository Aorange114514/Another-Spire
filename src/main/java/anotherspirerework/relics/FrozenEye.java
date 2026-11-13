package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.util.EtherealMark;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.actions.common.ExhaustSpecificCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Frozen Eye: every basic Strike and Defend gains Ethereal.
 *
 * <p>The keyword is a property of the fight, never of the saved deck: the cards the player owns are
 * only given its text while a fight is on, and the fight's copies are the ones that actually become
 * Ethereal. Nothing has to be stored, so a save load cannot lose it either - every fight marks its
 * own copies again.
 *
 * <p>Like every reworked relic it replaces the vanilla prototype through
 * {@code patches.RelicSwapPatch}, keeping the vanilla ID, art and acquisition path.
 */
public class FrozenEye extends com.megacrit.cardcrawl.relics.FrozenEye {
    /** Strike and Defend cards of every character, the ones Frozen Eye hands Ethereal to. */
    private static final Set<String> BASIC_ATTACK_AND_DEFEND = new HashSet<>(Arrays.asList(
            "Strike_R", "Strike_G", "Strike_B", "Strike_P",
            "Defend_R", "Defend_G", "Defend_B", "Defend_P"));

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

    /**
     * The fight's cards are the copies the room transition made, so the start of the fight has every
     * one of them. The deck's own cards are only given the text here: while the fight is on the deck
     * view should read like the cards in the player's hand, but those cards are not themselves
     * Ethereal and are never exhausted.
     */
    @Override
    public void atBattleStart() {
        markCombatCards(AbstractDungeon.player);
        showInDeck(true);
    }

    /** The fight is over, so the deck view goes back to the cards as the player owns them. */
    @Override
    public void onVictory() {
        showInDeck(false);
    }

    public static boolean isStrikeOrDefend(AbstractCard card) {
        return card != null && BASIC_ATTACK_AND_DEFEND.contains(card.cardID);
    }

    /** True when the player holds a Frozen Eye; safe outside a run. */
    public static boolean owned() {
        return AbstractDungeon.player != null && AbstractDungeon.player.hasRelic("Frozen Eye");
    }

    /** True inside a fight, the only place the effect applies. Safe outside a run. */
    public static boolean inCombat() {
        // getCurrRoom() is just currMapNode.getRoom(); there is no map node in the main menu or in
        // the compendium, and the card text build runs there as well.
        if (AbstractDungeon.player == null || AbstractDungeon.currMapNode == null) {
            return false;
        }
        AbstractRoom room = AbstractDungeon.getCurrRoom();
        return room != null && room.phase == AbstractRoom.RoomPhase.COMBAT;
    }

    /** Marks one card Ethereal, text included, so the keyword is visible on the combat card. */
    public static void applyFrozenEye(AbstractCard card) {
        if (isStrikeOrDefend(card)) {
            EtherealMark.apply(card);
        }
    }

    /** Every basic Strike / Defend the player can draw or hold in this fight. */
    static void markCombatCards(AbstractPlayer player) {
        if (player == null) {
            return;
        }
        for (CardGroup group : new CardGroup[]{player.drawPile, player.hand,
                player.discardPile, player.exhaustPile}) {
            for (AbstractCard card : new ArrayList<>(group.group)) {
                applyFrozenEye(card);
            }
        }
    }

    /** Shows the keyword on the deck's own cards while the fight is on, and takes it away after. */
    static void showInDeck(boolean show) {
        AbstractPlayer player = AbstractDungeon.player;
        if (player == null) {
            return;
        }
        for (AbstractCard card : new ArrayList<>(player.masterDeck.group)) {
            if (!isStrikeOrDefend(card)) {
                continue;
            }
            if (show) {
                EtherealMark.show(card);
            } else {
                EtherealMark.hide(card);
            }
        }
    }

    /** True when the card is one of the player's own deck cards rather than a fight copy. */
    static boolean inMasterDeck(AbstractCard card) {
        AbstractPlayer player = AbstractDungeon.player;
        return player != null && player.masterDeck.group.contains(card);
    }

    /**
     * A card's text can be built after the opening sweep already ran (a view screen refreshes it, and
     * a combat effect that creates a Strike or Defend marks it in its constructor). The line is part
     * of the text build as well, so no copy can ever be shown without it while the relic is held.
     *
     * <p>The deck's own cards are excluded: they only carry the text, never the keyword.
     */
    @SpirePatch(clz = AbstractCard.class, method = "initializeDescription")
    public static class Description {
        @SpirePrefixPatch
        public static void before(AbstractCard __instance) {
            if (owned() && inCombat() && isStrikeOrDefend(__instance) && !inMasterDeck(__instance)) {
                EtherealMark.ensure(__instance);
            }
        }
    }

    /** The fight's cards are copies made just before this point, so marking them here is enough. */
    @SpirePatch(clz = AbstractPlayer.class, method = "preBattlePrep")
    public static class CombatStart {
        @SpirePostfixPatch
        public static void after(AbstractPlayer __instance) {
            if (owned()) {
                markCombatCards(__instance);
            }
        }
    }

    /** Covers a basic Strike / Defend a combat effect creates after the opening hand. */
    @SpirePatch(clz = AbstractCard.class, method = SpirePatch.CONSTRUCTOR,
            paramtypez = {String.class, String.class, String.class, int.class, String.class,
                    AbstractCard.CardType.class, AbstractCard.CardColor.class,
                    AbstractCard.CardRarity.class, AbstractCard.CardTarget.class})
    public static class NewCards {
        @SpirePostfixPatch
        public static void after(AbstractCard __instance) {
            if (owned() && inCombat()) {
                applyFrozenEye(__instance);
            }
        }
    }

    /** Fallback for a card that was missed: Ethereal cards are exhausted at the end of the turn. */
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
