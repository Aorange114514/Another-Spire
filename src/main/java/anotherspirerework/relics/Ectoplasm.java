package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.util.RelicChecks;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import javassist.expr.ExprEditor;

/**
 * Ectoplasm: no passive energy any more. At the start of every turn, pay 5 Gold to gain [E] (with
 * fewer than 5 Gold nothing happens). Vanilla's "you can no longer gain Gold" restriction is gone.
 *
 * <p>Paying and gaining are one action. The Gold used to be taken on the spot while the energy was
 * only queued, so anything that dropped that queued gain (a combat ending inside the turn-start
 * window, the queue being cleared) kept the Gold and never paid out.
 *
 * <p>Replaces the vanilla prototype through {@code patches.RelicSwapPatch}.
 */
public class Ectoplasm extends com.megacrit.cardcrawl.relics.Ectoplasm {
    /** Gold paid each turn; the player only pays when they can cover it. */
    public static final int GOLD_COST = 5;

    public Ectoplasm() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new Ectoplasm();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** Vanilla replaces the text with its own class description, so it has to be replaced here. */
    @Override
    public void updateDescription(AbstractPlayer.PlayerClass c) {
        this.description = getUpdatedDescription();
        this.tips.clear();
        this.tips.add(new PowerTip(this.name, this.description));
        initializeTips();
    }

    /** The passive energy of the vanilla relic is replaced by the per-turn purchase. */
    @Override
    public void onEquip() {
    }

    @Override
    public void onUnequip() {
    }

    /** Still only offered by the first boss chests, where the vanilla relic was restricted too. */
    @Override
    public boolean canSpawn() {
        return AbstractDungeon.actNum <= 2;
    }

    @Override
    public void atTurnStart() {
        addToBot(new PayForEnergy(this));
    }

    /**
     * The purchase, so the Gold and the energy can never come apart: the check, the payment and the
     * payout all happen in one update.
     */
    public static class PayForEnergy extends AbstractGameAction {
        private final Ectoplasm relic;

        PayForEnergy(Ectoplasm relic) {
            this.relic = relic;
        }

        @Override
        public void update() {
            if (AbstractDungeon.player.gold >= GOLD_COST) {
                AbstractDungeon.player.loseGold(GOLD_COST);
                gainEnergy();
                this.relic.flash();
            }
            this.isDone = true;
        }

        /** The three steps of {@code GainEnergyAction}, applied here so nothing can drop the gain. */
        private static void gainEnergy() {
            AbstractDungeon.player.gainEnergy(1);
            AbstractDungeon.actionManager.updateEnergyGain(1);
            for (AbstractCard card : AbstractDungeon.player.hand.group) {
                card.triggerOnGainEnergy(1, true);
            }
        }
    }

    /** Vanilla blocks every gold gain behind a hasRelic("Ectoplasm") check; that restriction is gone. */
    @SpirePatch(clz = AbstractPlayer.class, method = "gainGold")
    public static class GainGold {
        @SpireInstrumentPatch
        public static ExprEditor edit() {
            return RelicChecks.ignoreHasRelic(ID);
        }
    }
}
