package anotherspirerework.effects;

import anotherspirerework.patches.StrangeSpoonExhaust;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * The card picker behind the reworked Strange Spoon and Orrery.
 *
 * <p>It runs as an effect rather than as a game action on purpose: the base game's action queue
 * only accepts actions while a fight is going on, and both relics are picked up in shops, chests
 * and reward screens, where an action would simply be dropped. Effects in
 * {@code AbstractDungeon.effectList} update in every room.
 *
 * <p>The grid select is opened in the base game's "any number" mode, so confirming with nothing
 * selected just closes it. The choice is applied only once the screen is really gone.
 */
public class ChooseDeckCardsEffect extends AbstractGameEffect {

    public enum Choice { REMOVE_EXHAUST, ADD_TO_DECK }

    /** Ten distinct cards of each rarity for Orrery. */
    private static final int PER_RARITY = 10;

    private static final float OPEN_DELAY = 0.4F;

    private final Choice choice;
    private final String message;
    private final int maxCards;
    private float delay = OPEN_DELAY;
    private boolean opened;

    public ChooseDeckCardsEffect(Choice choice, String message, int maxCards) {
        this.choice = choice;
        this.message = message;
        this.maxCards = maxCards;
        this.duration = 0.0F;
    }

    /** Queues the picker; it opens once the relic's own feedback has played. */
    public static void start(Choice choice, String message, int maxCards) {
        AbstractDungeon.effectList.add(new ChooseDeckCardsEffect(choice, message, maxCards));
    }

    @Override
    public void update() {
        if (this.isDone) {
            return;
        }
        if (AbstractDungeon.player == null) {
            this.isDone = true;
            return;
        }
        if (browsingCards()) {
            return;
        }
        if (!this.opened) {
            this.delay -= Gdx.graphics.getDeltaTime();
            if (this.delay > 0.0F) {
                return;
            }
            CardGroup group = this.choice == Choice.REMOVE_EXHAUST
                    ? AbstractDungeon.player.masterDeck : orreryChoices();
            if (group == null || group.size() == 0) {
                this.isDone = true;
                return;
            }
            this.opened = true;
            AbstractDungeon.gridSelectScreen.open(group, this.maxCards, true, this.message);
            return;
        }
        // The pick is over only once the screen is really gone. The map and the deck view can be
        // opened on top of the grid select - the base game sets previousScreen and restores the
        // grid afterwards - so a check that only looks for GRID ends the pick during that detour:
        // the cards picked so far are added to the deck right away and the still open screen is
        // left without anything to apply when the player finally confirms.
        if (AbstractDungeon.isScreenUp) {
            return;
        }
        for (AbstractCard card : new ArrayList<>(AbstractDungeon.gridSelectScreen.selectedCards)) {
            apply(card);
        }
        AbstractDungeon.gridSelectScreen.selectedCards.clear();
        this.isDone = true;
    }

    /**
     * True while the player is looking at cards in a deck, discard, exhaust or draw pile screen, at
     * the map, or at a single card. The picker waits for those instead of taking the screen over:
     * the master deck cards it has to show are the very ones such a screen is laying out, and
     * opening the grid on top of the map would leave the map screen half open behind it.
     */
    public static boolean browsingCards() {
        if (com.megacrit.cardcrawl.core.CardCrawlGame.isPopupOpen) {
            return true;
        }
        if (!AbstractDungeon.isScreenUp) {
            return false;
        }
        switch (AbstractDungeon.screen) {
            case MASTER_DECK_VIEW:
            case GAME_DECK_VIEW:
            case DISCARD_VIEW:
            case EXHAUST_VIEW:
            case MAP:
                return true;
            default:
                return false;
        }
    }

    private void apply(AbstractCard card) {
        // Selected cards are left glowing by the grid select - one of the base game's own branches
        // stops it, the "any number" one this picker uses does not - and a card that keeps that
        // glow looks like a playable combat card once it sits in the deck.
        card.unhover();
        card.untip();
        card.stopGlowing();
        if (this.choice == Choice.ADD_TO_DECK) {
            UnlockTracker.markCardAsSeen(card.cardID);
            AbstractDungeon.effectsQueue.add(new ShowCardAndObtainEffect(
                    card, Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
        } else {
            StrangeSpoonExhaust.mark(card);
        }
    }

    /** Ten distinct cards of each rarity out of the player's own card pool, generated once. */
    private static CardGroup orreryChoices() {
        CardGroup group = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
        for (AbstractCard.CardRarity rarity : new AbstractCard.CardRarity[]{
                AbstractCard.CardRarity.COMMON, AbstractCard.CardRarity.UNCOMMON, AbstractCard.CardRarity.RARE}) {
            Set<String> seen = new HashSet<>();
            for (int guard = 0; seen.size() < PER_RARITY && guard < PER_RARITY * 20; guard++) {
                AbstractCard card = AbstractDungeon.getCard(rarity);
                if (card != null && seen.add(card.cardID)) {
                    group.addToTop(card.makeCopy());
                }
            }
        }
        return group;
    }

    @Override
    public void render(SpriteBatch sb) {
    }

    @Override
    public void dispose() {
    }
}
