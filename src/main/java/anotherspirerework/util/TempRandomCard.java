package anotherspirerework.util;

import basemod.cardmods.EtherealMod;
import basemod.cardmods.ExhaustMod;
import basemod.helpers.CardModifierManager;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

/** The random card Swift Strike and Good Instincts hand out: temporary, Ethereal and Exhausting. */
public final class TempRandomCard {

    private TempRandomCard() {
    }

    /**
     * Adds one random card of the given type to the player's hand, marked Ethereal and Exhaust.
     *
     * <p>The card comes from the current character's own pools, the same way the base game's
     * random-card cards do. BaseMod's modifiers are used instead of setting the two flags by hand so
     * the keywords also show up in the added card's text.
     */
    public static void addEtherealExhaust(AbstractCard.CardType type) {
        AbstractCard source = AbstractDungeon.returnTrulyRandomCardInCombat(type);
        if (source == null) {
            return;
        }
        AbstractCard card = source.makeCopy();
        CardModifierManager.addModifier(card, new EtherealMod());
        CardModifierManager.addModifier(card, new ExhaustMod());
        AbstractDungeon.actionManager.addToBottom(new MakeTempCardInHandAction(card, 1));
    }
}
