package anotherspirerework.actions;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.util.SlyMark;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;

import java.util.ArrayList;

/** Survivor: the picked cards gain Sly for the rest of the fight and go back to hand. */
public class ApplySlyAction extends AbstractGameAction {
    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings("Survivor");

    private static final float DURATION = Settings.ACTION_DUR_XFAST;

    public ApplySlyAction(int amount) {
        this.amount = amount;
        this.duration = DURATION;
        this.actionType = ActionType.CARD_MANIPULATION;
    }

    @Override
    public void update() {
        CardGroup hand = AbstractDungeon.player.hand;
        if (this.duration == DURATION) {
            if (hand.isEmpty() || AbstractDungeon.getMonsters().areMonstersBasicallyDead()) {
                this.isDone = true;
                return;
            }
            if (hand.size() <= this.amount) {
                for (AbstractCard card : new ArrayList<>(hand.group)) {
                    SlyMark.apply(card);
                }
                this.isDone = true;
                return;
            }
            AbstractDungeon.handCardSelectScreen.open(
                    AnotherSpireRework.extendedDescription(cardStrings, 0), this.amount, false, false);
            hand.refreshHandLayout();
            tickDuration();
            return;
        }
        if (!AbstractDungeon.handCardSelectScreen.wereCardsRetrieved) {
            for (AbstractCard card : AbstractDungeon.handCardSelectScreen.selectedCards.group) {
                SlyMark.apply(card);
                // The select screen already took the card out of the hand; return it like the
                // vanilla retain/upgrade screens do, otherwise the cleared group loses it.
                hand.addToTop(card);
            }
            AbstractDungeon.handCardSelectScreen.wereCardsRetrieved = true;
            AbstractDungeon.handCardSelectScreen.selectedCards.clear();
            hand.refreshHandLayout();
        }
        this.isDone = true;
    }
}
