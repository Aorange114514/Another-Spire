package anotherspirerework.actions;

import anotherspirerework.cards.blue.Claw;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

/**
 * Vanilla GashAction, but matching our reworked Claw class instead of the vanilla one
 * (which is replaced at runtime and therefore never seen by the vanilla action).
 */
public class GashClawAction extends AbstractGameAction {
    private final AbstractCard card;

    public GashClawAction(AbstractCard card, int amount) {
        this.card = card;
        this.amount = amount;
    }

    @Override
    public void update() {
        this.card.baseDamage += this.amount;
        this.card.applyPowers();
        increaseClaws(AbstractDungeon.player.discardPile.group);
        increaseClaws(AbstractDungeon.player.drawPile.group);
        increaseClaws(AbstractDungeon.player.hand.group);
        this.isDone = true;
    }

    private void increaseClaws(java.util.ArrayList<AbstractCard> pile) {
        for (AbstractCard c : pile) {
            if (c instanceof Claw) {
                c.baseDamage += this.amount;
                c.applyPowers();
            }
        }
    }
}
