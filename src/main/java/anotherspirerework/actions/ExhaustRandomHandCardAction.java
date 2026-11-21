package anotherspirerework.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

/** Exhausts one random card from the hand; an empty hand does nothing. */
public class ExhaustRandomHandCardAction extends AbstractGameAction {

    public ExhaustRandomHandCardAction() {
        this.actionType = ActionType.EXHAUST;
        this.duration = this.startDuration;
    }

    @Override
    public void update() {
        if (!AbstractDungeon.player.hand.isEmpty()) {
            AbstractCard card = AbstractDungeon.player.hand.getRandomCard(AbstractDungeon.cardRandomRng);
            AbstractDungeon.player.hand.moveToExhaustPile(card);
        }
        this.isDone = true;
    }
}
