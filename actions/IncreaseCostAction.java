package anotherspire.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.GetAllInBattleInstances;

import java.util.UUID;

/**
 * Core Surge: raises this card's cost by 1 for the rest of the combat.
 * The mirror image of the vanilla ReduceCostAction (Blood for Blood / Streamline).
 */
public class IncreaseCostAction extends AbstractGameAction {
    private AbstractCard card = null;
    private UUID uuid;

    public IncreaseCostAction(AbstractCard card) {
        this.card = card;
    }

    public IncreaseCostAction(UUID targetUUID, int amount) {
        this.uuid = targetUUID;
        this.amount = amount;
        this.duration = Settings.ACTION_DUR_XFAST;
    }

    @Override
    public void update() {
        if (this.card == null) {
            for (AbstractCard c : GetAllInBattleInstances.get(this.uuid)) {
                c.updateCost(1);
            }
        } else {
            this.card.updateCost(1);
        }
        this.isDone = true;
    }
}
