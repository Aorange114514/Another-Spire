package anotherspirerework.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import java.util.ArrayList;
import java.util.function.IntConsumer;

/**
 * Exhausts every Status card the player is holding or has in their piles.
 *
 * <p>Shared by Reboot and Rip and Tear. The callback is how Rip and Tear learns how many cards were
 * actually exhausted: that count only exists once the piles have been walked, so it is reported
 * from inside the same pass that moves the cards.
 */
public class ExhaustAllStatusAction extends AbstractGameAction {

    /** Notified with the number of exhausted cards, or null when the caller does not care. */
    private final IntConsumer onExhausted;

    public ExhaustAllStatusAction() {
        this(null);
    }

    public ExhaustAllStatusAction(IntConsumer onExhausted) {
        this.onExhausted = onExhausted;
        this.actionType = ActionType.EXHAUST;
        this.duration = this.startDuration;
    }

    @Override
    public void update() {
        int count = 0;
        for (AbstractCard c : new ArrayList<>(AbstractDungeon.player.hand.group)) {
            count += exhaustIfStatus(c, AbstractDungeon.player.hand);
        }
        for (AbstractCard c : new ArrayList<>(AbstractDungeon.player.drawPile.group)) {
            count += exhaustIfStatus(c, AbstractDungeon.player.drawPile);
        }
        for (AbstractCard c : new ArrayList<>(AbstractDungeon.player.discardPile.group)) {
            count += exhaustIfStatus(c, AbstractDungeon.player.discardPile);
        }
        if (this.onExhausted != null && count > 0) {
            this.onExhausted.accept(count);
        }
        this.isDone = true;
    }

    private static int exhaustIfStatus(AbstractCard card, com.megacrit.cardcrawl.cards.CardGroup pile) {
        if (card.type == AbstractCard.CardType.STATUS) {
            pile.moveToExhaustPile(card);
            return 1;
        }
        return 0;
    }
}
