package anotherspirerework.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import java.util.ArrayList;

/** Reboot: exhausts every Status card the player is holding or has in their piles. */
public class ExhaustAllStatusAction extends AbstractGameAction {

    public ExhaustAllStatusAction() {
        this.actionType = ActionType.EXHAUST;
        this.duration = this.startDuration;
    }

    @Override
    public void update() {
        for (AbstractCard c : new ArrayList<>(AbstractDungeon.player.hand.group)) {
            exhaustIfStatus(c, AbstractDungeon.player.hand);
        }
        for (AbstractCard c : new ArrayList<>(AbstractDungeon.player.drawPile.group)) {
            exhaustIfStatus(c, AbstractDungeon.player.drawPile);
        }
        for (AbstractCard c : new ArrayList<>(AbstractDungeon.player.discardPile.group)) {
            exhaustIfStatus(c, AbstractDungeon.player.discardPile);
        }
        this.isDone = true;
    }

    private static void exhaustIfStatus(AbstractCard card, com.megacrit.cardcrawl.cards.CardGroup pile) {
        if (card.type == AbstractCard.CardType.STATUS) {
            pile.moveToExhaustPile(card);
        }
    }
}
