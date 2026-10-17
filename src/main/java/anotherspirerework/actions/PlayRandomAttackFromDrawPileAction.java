package anotherspirerework.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.actions.utility.UnlimboAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import java.util.ArrayList;

/**
 * Vanilla PlayTopCardAction (Havoc / Mayhem), except that the card is a random Attack from the draw
 * pile and the draw pile is not reshuffled when it has none. Each action picks its own random target.
 */
public class PlayRandomAttackFromDrawPileAction extends AbstractGameAction {

    public PlayRandomAttackFromDrawPileAction() {
        this.duration = Settings.ACTION_DUR_FAST;
        this.actionType = ActionType.WAIT;
        this.source = AbstractDungeon.player;
    }

    public void update() {
        if (this.duration == Settings.ACTION_DUR_FAST) {
            ArrayList<AbstractCard> attacks = new ArrayList<>();
            for (AbstractCard c : AbstractDungeon.player.drawPile.group) {
                if (c.type == AbstractCard.CardType.ATTACK) {
                    attacks.add(c);
                }
            }
            if (!attacks.isEmpty()) {
                AbstractCard card = attacks.get(AbstractDungeon.cardRandomRng.random(attacks.size() - 1));
                AbstractDungeon.player.drawPile.group.remove(card);
                AbstractDungeon.getCurrRoom().souls.remove(card);
                AbstractDungeon.player.limbo.group.add(card);
                card.current_y = -200.0F * Settings.scale;
                card.target_x = Settings.WIDTH / 2.0F + 200.0F * Settings.xScale;
                card.target_y = Settings.HEIGHT / 2.0F;
                card.targetAngle = 0.0F;
                card.lighten(false);
                card.drawScale = 0.12F;
                card.targetDrawScale = 0.75F;
                card.applyPowers();
                this.target = AbstractDungeon.getCurrRoom().monsters.getRandomMonster(null, true, AbstractDungeon.cardRandomRng);
                addToTop(new NewQueueCardAction(card, this.target, false, true));
                addToTop(new UnlimboAction(card));
                addToTop(new WaitAction(Settings.FAST_MODE ? Settings.ACTION_DUR_FASTER : Settings.ACTION_DUR_MED));
            }
            this.isDone = true;
        }
    }
}
