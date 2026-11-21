package anotherspirerework.actions;

import anotherspirerework.AnotherSpireRework;
import basemod.BaseMod;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.UIStrings;

import java.util.ArrayList;

/**
 * Alpha: at the end of the turn the player chooses cards from the discard pile, puts them into
 * the hand and gives them Retain. Mirrors the base game's BetterDiscardPileToHandAction, except
 * that the moved cards keep themselves through the end-of-turn discard via {@code retain}.
 */
public class AlphaDiscardToHandAction extends AbstractGameAction {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack
            .getUIString(AnotherSpireRework.makeID("AlphaDiscard"));

    private final int numberOfCards;

    public AlphaDiscardToHandAction(int numberOfCards) {
        this.actionType = ActionType.CARD_MANIPULATION;
        this.duration = this.startDuration = Settings.ACTION_DUR_FAST;
        this.numberOfCards = numberOfCards;
    }

    @Override
    public void update() {
        if (this.duration == this.startDuration) {
            if (AbstractDungeon.player.discardPile.isEmpty() || this.numberOfCards <= 0) {
                this.isDone = true;
                return;
            }
            if (AbstractDungeon.player.discardPile.size() <= this.numberOfCards) {
                moveAllToHand(new ArrayList<>(AbstractDungeon.player.discardPile.group));
                this.isDone = true;
                return;
            }
            String message = this.numberOfCards == 1
                    ? uiStrings.TEXT[0]
                    : uiStrings.TEXT[1] + this.numberOfCards + uiStrings.TEXT[2];
            AbstractDungeon.gridSelectScreen.open(AbstractDungeon.player.discardPile, this.numberOfCards, message, false);
            tickDuration();
            return;
        }
        if (!AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {
            moveAllToHand(new ArrayList<>(AbstractDungeon.gridSelectScreen.selectedCards));
            for (AbstractCard card : AbstractDungeon.player.discardPile.group) {
                card.unhover();
                card.target_x = CardGroup.DISCARD_PILE_X;
                card.target_y = 0.0F;
            }
            AbstractDungeon.gridSelectScreen.selectedCards.clear();
            AbstractDungeon.player.hand.refreshHandLayout();
        }
        tickDuration();
    }

    private void moveAllToHand(ArrayList<AbstractCard> cards) {
        for (AbstractCard card : cards) {
            if (AbstractDungeon.player.hand.size() >= BaseMod.MAX_HAND_SIZE) {
                break;
            }
            card.retain = true;
            card.lighten(false);
            card.unhover();
            card.applyPowers();
            AbstractDungeon.player.hand.addToHand(card);
            AbstractDungeon.player.discardPile.removeCard(card);
        }
    }
}
