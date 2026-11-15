package anotherspirerework.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.defect.FTLAction;
import com.megacrit.cardcrawl.actions.unique.DamagePerAttackPlayedAction;
import com.megacrit.cardcrawl.actions.watcher.CrushJointsAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import java.util.ArrayList;
import java.util.List;

/** Adapts actions that expect their source card to already be in the play history. */
public class SlyHistoryAction extends AbstractGameAction {
    private final AbstractGameAction action;
    private final AbstractCard card;

    public SlyHistoryAction(AbstractGameAction action, AbstractCard card) {
        this.action = action;
        this.card = card;
        this.actionType = action.actionType;
    }

    public static void wrapNewActions(AbstractCard card, List<AbstractGameAction> before) {
        ArrayList<AbstractGameAction> queued = AbstractDungeon.actionManager.actions;
        for (int i = 0; i < queued.size(); i++) {
            AbstractGameAction action = queued.get(i);
            // Only these single-update actions read history and then queue their effects.
            Class<?> type = action.getClass();
            if (!before.contains(action) && (type == DamagePerAttackPlayedAction.class
                    || type == FTLAction.class || type == CrushJointsAction.class
                    || type == SanctityEnergyAction.class)) {
                queued.set(i, new SlyHistoryAction(action, card));
            }
        }
    }

    @Override
    public void update() {
        GameActionManager manager = AbstractDungeon.actionManager;
        ArrayList<AbstractCard> turn = manager.cardsPlayedThisTurn;
        ArrayList<AbstractCard> combat = manager.cardsPlayedThisCombat;
        manager.cardsPlayedThisTurn = new ArrayList<>(turn);
        manager.cardsPlayedThisCombat = new ArrayList<>(combat);
        manager.cardsPlayedThisTurn.add(this.card);
        manager.cardsPlayedThisCombat.add(this.card);
        try {
            this.action.update();
            this.isDone = this.action.isDone;
        } finally {
            // Sly never contributes to real play counts, including when the action throws.
            manager.cardsPlayedThisTurn = turn;
            manager.cardsPlayedThisCombat = combat;
        }
    }
}
