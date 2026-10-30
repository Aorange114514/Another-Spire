package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.ExhaustSpecificCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import java.util.ArrayList;

/**
 * Medical Kit: keeps the inherited vanilla effect - Status cards can be played and are Exhausted
 * when played - and adds that a Status card is Exhausted the moment it is drawn, drawing a card in
 * its place.
 *
 * <p>While a screen is open the draw effect is remembered instead of done: it moves cards (exhaust
 * one, draw another) and a deck view on screen is animating the very same objects. The queue is
 * drained by {@link #processPendingDraws()} once that screen is gone.
 *
 * <p>Like every reworked relic it replaces the vanilla prototype through
 * {@code patches.RelicSwapPatch}, keeping the vanilla ID, art and acquisition path. Every hook this
 * class does not override is inherited from the vanilla relic unchanged.
 */
public class MedicalKit extends com.megacrit.cardcrawl.relics.MedicalKit {
    /** Status cards drawn while the player was looking at a pile, handled once that screen is gone. */
    private static final ArrayList<AbstractCard> PENDING = new ArrayList<>();

    public MedicalKit() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new MedicalKit();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** Called for every card the player draws. */
    public static void onCardDrawn(AbstractCard card) {
        if (card == null || AbstractDungeon.player == null || card.type != AbstractCard.CardType.STATUS) {
            return;
        }
        if (AbstractDungeon.player.getRelic("Medical Kit") == null) {
            return;
        }
        if (AbstractDungeon.isScreenUp || CardCrawlGame.isPopupOpen) {
            PENDING.add(card);
            return;
        }
        applyDrawn(card);
    }

    /** Called every frame; finishes what was drawn while a screen was open. */
    public static void processPendingDraws() {
        if (PENDING.isEmpty() || AbstractDungeon.player == null) {
            return;
        }
        if (AbstractDungeon.isScreenUp || CardCrawlGame.isPopupOpen) {
            return;
        }
        for (AbstractCard card : new ArrayList<>(PENDING)) {
            if (AbstractDungeon.player.hand.group.contains(card)) {
                applyDrawn(card);
            }
        }
        PENDING.clear();
    }

    /** Exhausts the drawn Status card and draws one card in its place; one flash per card. */
    private static void applyDrawn(AbstractCard card) {
        AbstractRelic kit = AbstractDungeon.player.getRelic("Medical Kit");
        if (kit == null) {
            return;
        }
        kit.flash();
        AbstractDungeon.actionManager.addToBottom(new ExhaustSpecificCardAction(card, AbstractDungeon.player.hand));
        AbstractDungeon.actionManager.addToBottom(new DrawCardAction(AbstractDungeon.player, 1));
    }
}
