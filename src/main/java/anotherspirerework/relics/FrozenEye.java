package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
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

/**
 * Frozen Eye: every basic Strike and Defend gains Ethereal.
 *
 * <p>The effect lives in the fight, never in the saved deck: the cards the player owns are left
 * untouched, and the combat copies are marked when a battle starts (plus any basic Strike or
 * Defend a combat effect creates afterwards). Nothing has to be stored, so a save load cannot lose
 * it either - the next battle marks its own copies again.
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

    public static boolean isStrikeOrDefend(AbstractCard card) {
        return card != null && Arrays.asList(BASIC_ATTACK_AND_DEFEND).contains(card.cardID);
    }

    /** True when the player holds a Frozen Eye; safe outside a run. */
    public static boolean owned() {
        return AbstractDungeon.player != null && AbstractDungeon.player.hasRelic("Frozen Eye");
    }

    /** True inside a fight, the only place the effect applies. */
    public static boolean inCombat() {
        AbstractRoom room = AbstractDungeon.getCurrRoom();
        return room != null && room.phase == AbstractRoom.RoomPhase.COMBAT;
    }

    /** Marks one card Ethereal, text included, so the keyword is visible on the combat card. */
    public static void applyFrozenEye(AbstractCard card) {
        if (isStrikeOrDefend(card)) {
            anotherspirerework.util.EtherealMark.apply(card);
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
