package anotherspirerework.actions;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import java.util.ArrayList;

/** Replace selected combat cards in place; neither exhaust nor change the master deck. */
public class TransformDrawPileToShivsAction extends AbstractGameAction {
    private boolean opened;

    public TransformDrawPileToShivsAction(int amount) {
        this.amount = amount;
        duration = Settings.ACTION_DUR_FAST;
        actionType = ActionType.CARD_MANIPULATION;
    }

    public void update() {
        CardGroup draw = AbstractDungeon.player.drawPile;
        if (!opened) {
            opened = true;
            if (draw.isEmpty()) { isDone = true; return; }
            if (draw.size() <= amount) {
                for (AbstractCard card : new ArrayList<>(draw.group)) transform(card);
                isDone = true;
                return;
            }
            CardGroup choices = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
            choices.group.addAll(draw.group);
            AbstractDungeon.gridSelectScreen.open(choices, amount,
                    AnotherSpireRework.extendedDescription(AnotherSpireRework.getCardStrings("Cloak And Dagger"), 0), false);
            return;
        }
        if (!AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {
            for (AbstractCard card : AbstractDungeon.gridSelectScreen.selectedCards) transform(card);
            AbstractDungeon.gridSelectScreen.selectedCards.clear();
            isDone = true;
        }
    }

    private void transform(AbstractCard card) {
        CardGroup draw = AbstractDungeon.player.drawPile;
        int index = draw.group.indexOf(card);
        if (index < 0) return;
        AbstractCard shiv = new Shiv();
        if (AbstractDungeon.player.hasPower("MasterRealityPower")) shiv.upgrade();
        shiv.current_x = card.current_x;
        shiv.current_y = card.current_y;
        draw.group.set(index, shiv);
        shiv.applyPowers();
    }
}