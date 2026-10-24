package anotherspirerework.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import java.util.ArrayList;
import java.util.function.IntConsumer;

/**
 * Exhausts every card in the player's hand.
 *
 * <p>Transmutation uses the reported count to add one random card per card actually exhausted;
 * that count only exists after the hand has been walked, the same way
 * {@link ExhaustAllStatusAction} reports its own.
 */
public class ExhaustAllHandAction extends AbstractGameAction {

    /** Notified with the number of exhausted cards, or null when the caller does not care. */
    private final IntConsumer onExhausted;

    public ExhaustAllHandAction(IntConsumer onExhausted) {
        this.onExhausted = onExhausted;
        this.actionType = ActionType.EXHAUST;
        this.duration = this.startDuration;
    }

    @Override
    public void update() {
        int count = 0;
        for (AbstractCard c : new ArrayList<>(AbstractDungeon.player.hand.group)) {
            AbstractDungeon.player.hand.moveToExhaustPile(c);
            count++;
        }
        if (this.onExhausted != null && count > 0) {
            this.onExhausted.accept(count);
        }
        this.isDone = true;
    }
}
