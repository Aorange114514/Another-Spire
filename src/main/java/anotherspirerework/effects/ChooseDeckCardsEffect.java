package anotherspirerework.effects;

import anotherspirerework.relics.Astrolabe;
import anotherspirerework.relics.StrangeSpoon;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
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
 * selected just closes it. The choice is applied the moment the screen goes, not a frame later:
 * {@code GridCardSelectScreen.selectedCards} is shared with everything else that reads the grid,
 * and the shop's own card removal reads it at the start of the next frame. The main game closes a
 * grid from two places - the grid's confirm button (inside the GRID update) and the cancel/leave
 * button on the overlay (after the effect list has already run) - and only the first one happens
 * before the picker would get to see the closed screen.
 */
public class ChooseDeckCardsEffect extends AbstractGameEffect {

    public enum Choice { REMOVE_EXHAUST, ADD_TO_DECK, ADD_ANY_COLOR }

    /** Ten distinct cards of each rarity for Orrery. */
    private static final int PER_RARITY = 10;

    /** Rarities Astrolabe may offer: Basic, Special and the special-only pools are left out. */
    private static final AbstractCard.CardRarity[] ANY_COLOR_RARITIES = {
            AbstractCard.CardRarity.COMMON, AbstractCard.CardRarity.UNCOMMON, AbstractCard.CardRarity.RARE
    };

    private static final float OPEN_DELAY = 0.4F;

    /** The picker whose grid is on screen, so the close hook can apply that one. */
    private static ChooseDeckCardsEffect showing;

    private final Choice choice;
    private final String message;
    private final int maxCards;
    private float delay = OPEN_DELAY;
    private boolean opened;

    /** The group this picker put on the grid; the close hook uses it to tell its own grid apart. */
    private CardGroup shownGroup;

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

    /** Forgets a picker whose run is gone. Called by {@code AbandonedPickPatch}. */
    public static void forget() {
        showing = null;
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
            // Never take over a grid that is already open (the shop's card removal, a campfire
            // upgrade, another picker): that grid's own reader is still waiting for it. Its
            // previousScreen would also be inherited, and closing this grid would reopen that
            // screen while the pick is still in the shared list.
            if (busyScreen()) {
                return;
            }
            this.delay -= Gdx.graphics.getDeltaTime();
            if (this.delay > 0.0F) {
                return;
            }
            CardGroup group = this.group();
            if (group == null || group.size() == 0) {
                this.isDone = true;
                return;
            }
            this.opened = true;
            this.shownGroup = group;
            showing = this;
            AbstractDungeon.gridSelectScreen.open(group, this.maxCards, true, this.message);
            // The overlay keeps whatever cancel button the previous screen had: in a shop that is
            // its "leave" button, which would close the grid (and the shop's removal service would
            // then read this pick as its own). "Up to N" includes N = 0, so there is nothing to
            // cancel here.
            if (AbstractDungeon.overlayMenu != null) {
                AbstractDungeon.overlayMenu.cancelButton.hide();
            }
            return;
        }
        if (AbstractDungeon.isScreenUp) {
            return;
        }
        settle();
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

    /**
     * Screens this picker must not open on top of. The reward, shop and boss relic screens are not
     * in here on purpose: the relic is picked up there, so that is exactly where it has to show.
     */
    private static boolean busyScreen() {
        switch (AbstractDungeon.screen) {
            case GRID:
            case SETTINGS:
            case INPUT_SETTINGS:
            case HAND_SELECT:
                return true;
            default:
                return false;
        }
    }

    /** The cards this instance offers: the deck itself, Orrery's 10/10/10, or any colour at all. */
    private CardGroup group() {
        switch (this.choice) {
            case REMOVE_EXHAUST:
                return exhaustChoices();
            case ADD_ANY_COLOR:
                return anyColorChoices();
            default:
                return orreryChoices();
        }
    }

    /**
     * Only the cards that really carry Exhaust: the Spoon removes the keyword, so offering a card
     * without it would spend one of the two picks on nothing. The flag, not the description, decides -
     * cards such as Rip and Tear mention the keyword in their text without Exhausting themselves.
     * The same card objects are listed, not copies, so the pick changes the master deck card.
     */
    private static CardGroup exhaustChoices() {
        CardGroup group = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
        for (AbstractCard card : AbstractDungeon.player.masterDeck.group) {
            if (StrangeSpoon.hasExhaust(card)) {
                group.addToTop(card);
            }
        }
        return group;
    }

    /**
     * Applies the pick and clears the shared selection. Runs the moment the grid closes - the close
     * hook calls it for every way the grid can go away, and {@link #update()} calls it as well for
     * a close that never passed through {@code AbstractDungeon.closeCurrentScreen}.
     */
    void settle() {
        if (this.isDone) {
            return;
        }
        // Another screen took the grid over; its selection is not this pick.
        if (AbstractDungeon.gridSelectScreen.targetGroup != this.shownGroup) {
            finish();
            return;
        }
        for (AbstractCard card : new ArrayList<>(AbstractDungeon.gridSelectScreen.selectedCards)) {
            apply(card);
        }
        AbstractDungeon.gridSelectScreen.selectedCards.clear();
        finish();
    }

    private void finish() {
        this.isDone = true;
        if (showing == this) {
            showing = null;
        }
    }

    private void apply(AbstractCard card) {
        // Selected cards are left glowing by the grid select - one of the base game's own branches
        // stops it, the "any number" one this picker uses does not - and a card that keeps that
        // glow looks like a playable combat card once it sits in the deck.
        card.unhover();
        card.untip();
        card.stopGlowing();
        if (this.choice != Choice.REMOVE_EXHAUST) {
            UnlockTracker.markCardAsSeen(card.cardID);
            AbstractDungeon.effectsQueue.add(new ShowCardAndObtainEffect(
                    card, Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
        } else {
            StrangeSpoon.mark(card);
        }
    }

    /**
     * The grid can also be closed from outside itself: the overlay's cancel/leave button, the
     * settings exit, a room restart mod. Those close it after the effect list has already run, so
     * waiting for the next frame leaves the pick in the shared selection for the room's own update -
     * a shop reads it there as its card removal and deletes the picked cards. Applying the pick
     * before the original close runs means no reader can ever see it.
     */
    @SpirePatch(clz = AbstractDungeon.class, method = "closeCurrentScreen")
    public static class SettleOnClose {
        @SpirePrefixPatch
        public static void before() {
            ChooseDeckCardsEffect pick = showing;
            if (pick == null || AbstractDungeon.screen != AbstractDungeon.CurrentScreen.GRID) {
                return;
            }
            pick.settle();
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

    /**
     * Distinct cards of any colour for Astrolabe: the rarity is rolled first so every rarity is as
     * likely to show up, and Basic, Special, Status and Curse cards never are. Healing cards are
     * offered on purpose: this pick adds the card to the deck outside combat, while the HEALING tag
     * only keeps the base game's in-combat random generation away from them.
     */
    private static CardGroup anyColorChoices() {
        CardGroup group = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
        Set<String> seen = new HashSet<>();
        int wanted = Astrolabe.CHOICES;
        for (int guard = 0; seen.size() < wanted && guard < wanted * 40; guard++) {
            AbstractCard.CardRarity rarity = ANY_COLOR_RARITIES[AbstractDungeon.cardRandomRng.random(ANY_COLOR_RARITIES.length - 1)];
            AbstractCard card = com.megacrit.cardcrawl.helpers.CardLibrary.getAnyColorCard(rarity);
            if (card != null && isPickable(card) && seen.add(card.cardID)) {
                group.addToTop(card.makeCopy());
            }
        }
        return group;
    }

    private static boolean isPickable(AbstractCard card) {
        if (card.rarity == AbstractCard.CardRarity.BASIC || card.rarity == AbstractCard.CardRarity.SPECIAL) {
            return false;
        }
        return card.type != AbstractCard.CardType.STATUS && card.type != AbstractCard.CardType.CURSE;
    }

    @Override
    public void render(SpriteBatch sb) {
    }

    @Override
    public void dispose() {
    }
}
