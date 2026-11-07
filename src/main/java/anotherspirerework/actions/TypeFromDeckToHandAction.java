package anotherspirerework.actions;

import anotherspirerework.AnotherSpireRework;
import basemod.BaseMod;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.UIStrings;

/**
 * Secret Weapon / Secret Technique: put up to {@code amount} cards of one type from the draw pile
 * into the hand. The base game only skips its picker when a single card is left and always asks
 * for "one card", so the upgrade that picks two would still show the wrong prompt. Here a picker
 * opens only when there is a real choice, and its header carries the actual count.
 */
public class TypeFromDeckToHandAction extends AbstractGameAction {
    private static final UIStrings uiStrings =
            CardCrawlGame.languagePack.getUIString(AnotherSpireRework.makeID("DeckToHand"));

    private final AbstractPlayer p = AbstractDungeon.player;
    private final AbstractCard.CardType type;

    public TypeFromDeckToHandAction(AbstractCard.CardType type, int amount) {
        this.type = type;
        setValues(this.p, this.p, amount);
        this.actionType = ActionType.CARD_MANIPULATION;
        this.duration = Settings.ACTION_DUR_MED;
    }

    @Override
    public void update() {
        if (this.duration == Settings.ACTION_DUR_MED) {
            CardGroup candidates = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
            for (AbstractCard card : this.p.drawPile.group) {
                if (card.type == this.type) {
                    candidates.addToRandomSpot(card);
                }
            }
            if (candidates.isEmpty()) {
                this.isDone = true;
                return;
            }
            if (candidates.size() <= this.amount) {
                for (AbstractCard card : candidates.group) {
                    take(card);
                }
                this.isDone = true;
                return;
            }
            AbstractDungeon.gridSelectScreen.open(candidates, this.amount, prompt(), false);
            tickDuration();
            return;
        }
        if (!AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {
            for (AbstractCard card : AbstractDungeon.gridSelectScreen.selectedCards) {
                card.unhover();
                if (this.p.hand.size() >= BaseMod.MAX_HAND_SIZE) {
                    this.p.drawPile.moveToDiscardPile(card);
                    this.p.createHandIsFullDialog();
                    continue;
                }
                moveToHand(card);
            }
            AbstractDungeon.gridSelectScreen.selectedCards.clear();
            this.p.hand.refreshHandLayout();
        }
        tickDuration();
    }

    /** The picker header, e.g. "选择2张牌。" — the count is the point of it. */
    private String prompt() {
        String[] text = uiStrings == null ? null : uiStrings.TEXT;
        if (text == null || text.length < 2) {
            return "";
        }
        return text[0] + this.amount + (this.amount == 1 && text.length > 2 ? text[2] : text[1]);
    }

    /** The base game's one-card branch, reused for every card when no picker is shown. */
    private void take(AbstractCard card) {
        if (this.p.hand.size() >= BaseMod.MAX_HAND_SIZE) {
            this.p.drawPile.moveToDiscardPile(card);
            this.p.createHandIsFullDialog();
            return;
        }
        card.unhover();
        card.lighten(true);
        card.setAngle(0.0F);
        card.drawScale = 0.12F;
        card.targetDrawScale = 0.75F;
        card.current_x = CardGroup.DRAW_PILE_X;
        card.current_y = CardGroup.DRAW_PILE_Y;
        moveToHand(card);
    }

    private void moveToHand(AbstractCard card) {
        this.p.drawPile.removeCard(card);
        this.p.hand.addToTop(card);
        this.p.hand.refreshHandLayout();
        this.p.hand.applyPowers();
    }
}
