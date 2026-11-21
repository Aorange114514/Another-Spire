package anotherspirerework.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.ExhaustSpecificCardAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import java.util.ArrayList;
import java.util.List;

/**
 * Exhausts every Status card in the player's hand one by one, using the same ExhaustSpecificCardAction
 * animation Second Wind uses, then grants the energy and the cards for the whole batch. The exhausts
 * run first and the gains afterwards, so every card visibly flies to the exhaust pile.
 */
public class ExhaustHandStatusesAction extends AbstractGameAction {
    private final int energyPerCard;
    private final int drawPerCard;

    public ExhaustHandStatusesAction(int energyPerCard, int drawPerCard) {
        this.energyPerCard = energyPerCard;
        this.drawPerCard = drawPerCard;
        this.actionType = ActionType.EXHAUST;
        this.duration = this.startDuration;
    }

    @Override
    public void update() {
        List<AbstractCard> statuses = new ArrayList<>();
        for (AbstractCard card : AbstractDungeon.player.hand.group) {
            if (card.type == AbstractCard.CardType.STATUS) {
                statuses.add(card);
            }
        }
        if (!statuses.isEmpty()) {
            addToTop(new DrawCardAction(AbstractDungeon.player, this.drawPerCard * statuses.size()));
            addToTop(new GainEnergyAction(this.energyPerCard * statuses.size()));
            for (AbstractCard card : statuses) {
                addToTop(new ExhaustSpecificCardAction(card, AbstractDungeon.player.hand));
            }
        }
        this.isDone = true;
    }
}
