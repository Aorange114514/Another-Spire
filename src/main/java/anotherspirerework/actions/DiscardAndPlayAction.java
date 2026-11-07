package anotherspirerework.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;

import java.util.ArrayList;

/**
 * Survivor: discard the picked cards, then let each one act as if it had been played. The effect
 * runs off the discard hook instead of a real play, so Beat of Death and Time Warp stay untouched
 * and a card that needs a target picks a random enemy rather than asking. The discarded cards cost
 * no energy, X resolves from the current energy the way an auto-played card does (Chemical X still
 * adds its +2), and a card with the Exhaust keyword is exhausted the same way a real play would
 * exhaust it. A Power plays the vanilla power animation and then leaves the combat for good, so it
 * never lands in the discard pile to be drawn and played again.
 */
public class DiscardAndPlayAction extends AbstractGameAction {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString("DiscardAction");
    public static final String[] TEXT = uiStrings.TEXT;

    private static final float DURATION = Settings.ACTION_DUR_XFAST;

    public DiscardAndPlayAction(int amount) {
        this.amount = amount;
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
            if (AbstractDungeon.player.hand.size() <= this.amount) {
                for (AbstractCard card : new ArrayList<>(AbstractDungeon.player.hand.group)) {
                    discardAndPlay(card);
                }
                AbstractDungeon.player.hand.refreshHandLayout();
                AbstractDungeon.player.hand.applyPowers();
                this.isDone = true;
                return;
            }
            AbstractDungeon.handCardSelectScreen.open(TEXT[0], this.amount, false, false);
            AbstractDungeon.player.hand.refreshHandLayout();
            tickDuration();
            return;
        }
        if (!AbstractDungeon.handCardSelectScreen.wereCardsRetrieved) {
            for (AbstractCard card : AbstractDungeon.handCardSelectScreen.selectedCards.group) {
                discardAndPlay(card);
            }
            AbstractDungeon.handCardSelectScreen.wereCardsRetrieved = true;
            AbstractDungeon.handCardSelectScreen.selectedCards.clear();
            AbstractDungeon.player.hand.refreshHandLayout();
        }
        this.isDone = true;
    }

    private static void discardAndPlay(AbstractCard card) {
        AbstractPlayer player = AbstractDungeon.player;
        if (card.type == AbstractCard.CardType.POWER) {
            card.triggerOnManualDiscard();
            GameActionManager.incrementDiscard(false);
            play(card);
            // Mirrors UseCardAction's Power branch: the card whooshes to the player and is never
            // added to a pile, so it cannot be drawn and played a second time.
            player.hand.empower(card);
            return;
        }
        player.hand.moveToDiscardPile(card);
        card.triggerOnManualDiscard();
        GameActionManager.incrementDiscard(false);
        play(card);
        if (card.exhaust || card.exhaustOnUseOnce) {
            player.discardPile.moveToExhaustPile(card);
            card.exhaustOnUseOnce = false;
        }
    }

    private static void play(AbstractCard card) {
        AbstractMonster monster = null;
        if (card.target == AbstractCard.CardTarget.ENEMY) {
            monster = AbstractDungeon.getRandomMonster();
            if (monster == null) {
                return;
            }
        }
        // Auto-play semantics (Distilled Chaos, Havoc): X reads the current energy and the card
        // spends none of it. Chemical X still adds its +2 inside the card's own X-cost effect.
        card.energyOnUse = EnergyPanel.totalCount;
        card.freeToPlayOnce = true;
        card.isInAutoplay = true;
        card.applyPowers();
        if (monster != null) {
            card.calculateCardDamage(monster);
        }
        card.use(AbstractDungeon.player, monster);
        card.freeToPlayOnce = false;
        card.isInAutoplay = false;
    }
}
