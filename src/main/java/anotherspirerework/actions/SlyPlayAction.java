package anotherspirerework.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Sly: a card that was discarded from the hand resolves its own effect.
 *
 * <p>The effect runs through {@code use()} instead of a real play, so the card costs nothing and
 * Beat of Death / Time Warp never see it; a single-target card picks a random enemy, an ALL_ENEMY
 * card recalculates against every enemy, X reads the current energy (Chemical X still adds its +2),
 * a Power leaves combat and an Exhaust card is exhausted. The mark comes from the reworked Sly
 * cards, Setup and Tools of the Trade.
 *
 * <p>The end-turn key cuts the chain short: {@link #interrupt()} drops the plays that have not
 * started yet, and the next player turn re-arms the queue with {@link #resume()}.
 */
public class SlyPlayAction extends AbstractGameAction {
    private static final float DURATION = Settings.ACTION_DUR_XFAST;

    /** Set by the end-turn key, cleared when the next player turn starts. */
    private static boolean interrupted;

    private final AbstractCard card;
    private boolean resolved;

    public SlyPlayAction(AbstractCard card) {
        this.card = card;
        this.actionType = ActionType.USE;
        this.duration = DURATION;
    }

    @Override
    public void update() {
        if (!this.resolved) {
            this.resolved = true;
            // The end-turn key can also land between the queue handing this action out as the
            // current action and this update, so the flag has to be read here as well.
            if (!interrupted) {
                play();
            }
        }
        this.isDone = true;
    }

    /**
     * Resolves a discarded card immediately. A Power cannot wait for the action queue: the discard
     * already started its flight to the discard pile, and the Power has to leave combat instead. The
     * caller cancels that flight and calls this from the discard hook, so the empower animation starts
     * from the hand position.
     */
    public static void playNow(AbstractCard card) {
        new SlyPlayAction(card).update();
    }

    /**
     * The end-turn key ends the resolution chain. The plays that have not started leave the action
     * queue, and a discard that is still queued (the queue can hold a DiscardAction that was
     * ordered before the end turn) does not start a new one. Each play is atomic: {@code use()}
     * queues a card's whole effect at once, so dropping a play never cuts half an effect in two,
     * and a card whose play already started finishes normally.
     */
    public static void interrupt() {
        interrupted = true;
        for (Iterator<AbstractGameAction> i = AbstractDungeon.actionManager.actions.iterator(); i.hasNext(); ) {
            if (i.next() instanceof SlyPlayAction) {
                i.remove();
            }
        }
    }

    /** The player has control again, so a discarded Sly card resolves normally. */
    public static void resume() {
        interrupted = false;
    }

    /** True between the end-turn key and the next player turn. */
    public static boolean interrupted() {
        return interrupted;
    }

    private void play() {
        AbstractPlayer player = AbstractDungeon.player;
        if (player == null || this.card == null
                || AbstractDungeon.getMonsters().areMonstersBasicallyDead()) {
            return;
        }
        // A card that cannot be played has no effect to trigger; its discard hook already ran.
        if (this.card.cost == -2) {
            return;
        }
        AbstractMonster monster = null;
        if (this.card.target == AbstractCard.CardTarget.ENEMY) {
            monster = AbstractDungeon.getRandomMonster();
            if (monster == null) {
                return;
            }
        }
        // Auto-play semantics (Distilled Chaos, Havoc): X reads the current energy and the card
        // spends none of it. Chemical X still adds its +2 inside the card's own X-cost effect.
        this.card.energyOnUse = EnergyPanel.totalCount;
        this.card.freeToPlayOnce = true;
        this.card.isInAutoplay = true;
        this.card.applyPowers();
        if (monster != null) {
            this.card.calculateCardDamage(monster);
        } else if (this.card.target == AbstractCard.CardTarget.ALL_ENEMY) {
            // AbstractPlayer.useCard() recalculates against the play target right before use(); for
            // an ALL_ENEMY card that target is null, and the null branch writes each monster's
            // Vulnerable/Slow into multiDamage.
            this.card.calculateCardDamage(null);
        }
        ArrayList<AbstractGameAction> queuedBefore = new ArrayList<>(AbstractDungeon.actionManager.actions);
        this.card.use(player, monster);
        SlyHistoryAction.wrapNewActions(this.card, queuedBefore);
        this.card.freeToPlayOnce = false;
        this.card.isInAutoplay = false;
        if (this.card.type == AbstractCard.CardType.POWER) {
            // Mirrors UseCardAction's Power branch: the card never lands in a pile, so it cannot be
            // drawn and played a second time.
            player.discardPile.group.remove(this.card);
            player.hand.empower(this.card);
            player.hand.applyPowers();
            player.hand.glowCheck();
        } else if (this.card.exhaust || this.card.exhaustOnUseOnce) {
            player.discardPile.moveToExhaustPile(this.card);
            this.card.exhaustOnUseOnce = false;
        }
    }
}
