package anotherspirerework.actions;

import anotherspirerework.AnotherSpireRework;
import basemod.BaseMod;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;

/**
 * Metamorphosis: vanilla SeekAction, except that the player picks up to {@code amount} cards
 * (the grid's "any number" mode) and the prompt shows that number.
 */
public class ShuffleSeekAction extends AbstractGameAction {
    private final AbstractPlayer p = AbstractDungeon.player;

    public ShuffleSeekAction(int amount) {
        setValues(this.p, this.p, amount);
        this.actionType = ActionType.CARD_MANIPULATION;
        this.duration = Settings.ACTION_DUR_MED;
    }

    public void update() {
        if (this.duration == Settings.ACTION_DUR_MED) {
            if (this.p.drawPile.isEmpty()) {
                this.isDone = true;
                return;
            }
            CardGroup tmp = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
            for (AbstractCard c : this.p.drawPile.group) {
                tmp.addToRandomSpot(c);
            }
            CardStrings strings = AnotherSpireRework.getCardStrings("Metamorphosis");
            String msg = AnotherSpireRework.extendedDescription(strings, 0) + this.amount
                    + AnotherSpireRework.extendedDescription(strings, 1);
            AbstractDungeon.gridSelectScreen.open(tmp, Math.min(this.amount, tmp.size()), true, msg);
            tickDuration();
            return;
        }
        for (AbstractCard c : AbstractDungeon.gridSelectScreen.selectedCards) {
            c.unhover();
            c.stopGlowing();
            if (this.p.hand.size() >= BaseMod.MAX_HAND_SIZE) {
                this.p.drawPile.moveToDiscardPile(c);
                this.p.createHandIsFullDialog();
            } else {
                this.p.drawPile.removeCard(c);
                this.p.hand.addToTop(c);
            }
        }
        AbstractDungeon.gridSelectScreen.selectedCards.clear();
        this.p.hand.refreshHandLayout();
        this.p.hand.applyPowers();
        tickDuration();
    }
}
