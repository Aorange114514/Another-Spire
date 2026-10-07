package anotherspirerework.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.UIStrings;

/**
 * Masterful Stab: discard up to {@code amount} cards, then add a Shiv (Shiv+ once upgraded) for
 * each card that was actually discarded. Mirrors the vanilla DiscardAction, which is why the
 * card-selection branch looks the same.
 */
public class DiscardForShivsAction extends AbstractGameAction {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString("DiscardAction");
    public static final String[] TEXT = uiStrings.TEXT;

    private static final float DURATION = Settings.ACTION_DUR_XFAST;

    private final boolean upgradedShivs;

    public DiscardForShivsAction(int amount, boolean upgradedShivs) {
        this.amount = amount;
        this.upgradedShivs = upgradedShivs;
        this.actionType = ActionType.DISCARD;
        this.duration = DURATION;
    }

    @Override
    public void update() {
        if (this.duration == DURATION) {
            if (AbstractDungeon.getMonsters().areMonstersBasicallyDead() || AbstractDungeon.player.hand.isEmpty()) {
                this.isDone = true;
                return;
            }
            AbstractDungeon.handCardSelectScreen.open(TEXT[0], this.amount, true, true);
            AbstractDungeon.player.hand.applyPowers();
            tickDuration();
            return;
        }
        if (!AbstractDungeon.handCardSelectScreen.wereCardsRetrieved) {
            int count = 0;
            for (AbstractCard c : AbstractDungeon.handCardSelectScreen.selectedCards.group) {
                discard(c);
                count++;
            }
            AbstractDungeon.handCardSelectScreen.wereCardsRetrieved = true;
            AbstractDungeon.handCardSelectScreen.selectedCards.clear();
            addShivs(count);
        }
        tickDuration();
    }

    private static void discard(AbstractCard card) {
        AbstractDungeon.player.hand.moveToDiscardPile(card);
        card.triggerOnManualDiscard();
        GameActionManager.incrementDiscard(false);
    }

    private void addShivs(int count) {
        if (count <= 0) {
            return;
        }
        Shiv shiv = new Shiv();
        if (this.upgradedShivs) {
            shiv.upgrade();
        }
        addToTop(new MakeTempCardInHandAction(shiv, count));
    }
}
