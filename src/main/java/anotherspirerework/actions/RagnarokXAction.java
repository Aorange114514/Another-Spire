package anotherspirerework.actions;

import anotherspirerework.cards.purple.Ragnarok;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.AttackDamageRandomEnemyAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;

/**
 * Ragnarok: X energy buys X * 2 damage, dealt X * 2 times to random enemies.
 *
 * <p>The per-hit damage is written into the card before the base game's random hit is queued, and
 * that action runs calculateCardDamage itself, so Strength, Weak and Vulnerable apply exactly as
 * they would on a printed number.
 *
 * <p>The strikes are spaced by {@link #HIT_PAUSE}, the same pause the base game puts in front of
 * every random lightning hit ({@code actions.defect.NewThunderStrikeAction}), so a long Ragnarok
 * reads as a sequence of strikes instead of one flash.
 */
public class RagnarokXAction extends AbstractGameAction {
    /** Pause before each strike; the base game skips it in fast mode. */
    public static final float HIT_PAUSE = 0.1F;

    private final AbstractCard card;
    private final AbstractPlayer p;
    private final boolean freeToPlayOnce;
    private final int energyOnUse;

    public RagnarokXAction(AbstractCard card, AbstractPlayer p, boolean freeToPlayOnce, int energyOnUse) {
        this.card = card;
        this.p = p;
        this.freeToPlayOnce = freeToPlayOnce;
        this.energyOnUse = energyOnUse;
        this.duration = Settings.ACTION_DUR_XFAST;
        this.actionType = ActionType.SPECIAL;
    }

    /**
     * The X this card would spend right now. The card face uses it too, so the number shown and the
     * number dealt are worked out the same way; it does not flash the relic.
     */
    public static int energyFor(int energyOnUse) {
        int effect = EnergyPanel.totalCount;
        if (energyOnUse != -1) {
            effect = energyOnUse;
        }
        if (AbstractDungeon.player != null && AbstractDungeon.player.hasRelic("Chemical X")) {
            effect += 2;
        }
        return effect;
    }

    @Override
    public void update() {
        int effect = energyFor(this.energyOnUse);
        if (this.p.hasRelic("Chemical X")) {
            this.p.getRelic("Chemical X").flash();
        }
        if (effect > 0) {
            int hits = effect * Ragnarok.MULTIPLIER;
            this.card.baseDamage = effect * Ragnarok.MULTIPLIER;
            for (int i = 0; i < hits; i++) {
                // Queued before the strike, so each hit gets its own beat like the base game's own
                // random lightning (NewThunderStrikeAction).
                if (!Settings.FAST_MODE) {
                    addToBot(new WaitAction(HIT_PAUSE));
                }
                addToBot(new AttackDamageRandomEnemyAction(this.card, AttackEffect.LIGHTNING));
            }
            if (!this.freeToPlayOnce) {
                this.p.energy.use(EnergyPanel.totalCount);
            }
        }
        this.isDone = true;
    }
}
