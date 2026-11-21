package anotherspirerework.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.status.Burn;
import com.megacrit.cardcrawl.core.AbstractCreature;

/**
 * Burn keeps its vanilla card: no Ethereal keyword, unplayable, damage at the end of the turn and
 * then the discard pile.
 *
 * <p>A Burn that another effect marks Ethereal (Slice picks cards in hand) still follows the vanilla
 * end-of-turn auto-play - the card pops out and deals its damage - but it must be consumed instead
 * of returning to the discard pile. The destination is decided in the {@link UseCardAction} built
 * for that play, after the damage action Burn itself queues, so the damage comes first and the
 * exhaust follows it.
 */
public class BurnEtherealPatch {

    @SpirePatch(clz = UseCardAction.class, method = SpirePatch.CONSTRUCTOR,
            paramtypez = {AbstractCard.class, AbstractCreature.class})
    public static class ExhaustInsteadOfDiscard {
        @SpirePostfixPatch
        public static void after(UseCardAction __instance, AbstractCard card) {
            if (card instanceof Burn && card.isEthereal) {
                __instance.exhaustCard = true;
                __instance.actionType = AbstractGameAction.ActionType.EXHAUST;
            }
        }
    }
}
