package anotherspirerework.actions;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.util.SlyMark;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;

/**
 * Setup: the chosen card gains Sly for the rest of the fight and goes to the top of the draw pile.
 * The picker follows the vanilla SetupAction: a hand with a single card skips the screen. The mark
 * stays on the card itself, so the keyword line survives the trip through the draw pile.
 */
public class SetupSlyAction extends AbstractGameAction {
    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings("Setup");

    public SetupSlyAction() {
        this.duration = Settings.ACTION_DUR_FAST;
        this.actionType = ActionType.CARD_MANIPULATION;
    }

    @Override
    public void update() {
        CardGroup hand = AbstractDungeon.player.hand;
        if (this.duration == Settings.ACTION_DUR_FAST) {
            if (hand.isEmpty()) {
                this.isDone = true;
                return;
            }
            if (hand.size() == 1) {
                AbstractCard card = hand.getTopCard();
                SlyMark.apply(card);
                hand.moveToDeck(card, false);
                hand.refreshHandLayout();
                this.isDone = true;
                return;
            }
            AbstractDungeon.handCardSelectScreen.open(
                    AnotherSpireRework.extendedDescription(cardStrings, 0), 1, false, false);
            tickDuration();
            return;
        }
        if (!AbstractDungeon.handCardSelectScreen.wereCardsRetrieved) {
            for (AbstractCard card : AbstractDungeon.handCardSelectScreen.selectedCards.group) {
                SlyMark.apply(card);
                // The select screen already took the card out of the hand, so the vanilla
                // moveToDeck() animation is what puts it on top of the draw pile.
                hand.moveToDeck(card, false);
            }
            hand.refreshHandLayout();
            AbstractDungeon.handCardSelectScreen.wereCardsRetrieved = true;
            AbstractDungeon.handCardSelectScreen.selectedCards.clear();
        }
        tickDuration();
    }
}
