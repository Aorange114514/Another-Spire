package anotherspirerework.actions;

import basemod.BaseMod;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

/**
 * Moves a card out of the draw pile, the exhaust pile or the discard pile into the hand.
 *
 * <p>The base game only ships this for the discard pile ({@code DiscardToHandAction}); the other
 * piles are handled the same way here, including the icon the exhaust pile leaves on the card.
 */
public class ReturnCardToHandAction extends AbstractGameAction {
    private final AbstractCard card;
    private final CardGroup source;

    public ReturnCardToHandAction(AbstractCard card, CardGroup source) {
        this.actionType = ActionType.CARD_MANIPULATION;
        this.card = card;
        this.source = source;
        this.duration = Settings.ACTION_DUR_FAST;
    }

    @Override
    public void update() {
        if (this.duration == Settings.ACTION_DUR_FAST) {
            if (this.source != null && this.source.contains(this.card)
                    && AbstractDungeon.player.hand.size() < BaseMod.MAX_HAND_SIZE) {
                AbstractDungeon.player.hand.addToHand(this.card);
                this.card.stopGlowing();
                this.card.unhover();
                if (this.source.type == CardGroup.CardGroupType.EXHAUST_PILE) {
                    this.card.unfadeOut();
                }
                this.card.setAngle(0.0F, true);
                this.card.lighten(false);
                this.card.drawScale = 0.12F;
                this.card.targetDrawScale = 0.75F;
                this.card.applyPowers();
                this.source.removeCard(this.card);
            }
            AbstractDungeon.player.hand.refreshHandLayout();
            AbstractDungeon.player.hand.glowCheck();
        }
        tickDuration();
        this.isDone = true;
    }
}
