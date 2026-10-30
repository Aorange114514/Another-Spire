package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.util.RelicChecks;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.AbstractRelic;

/**
 * Velvet Choker: gain [E] every turn (as vanilla), but playing cards is no longer capped at six.
 * Instead, playing more than six cards in one turn costs 1 energy at the start of the next turn.
 *
 * <p>Replaces the vanilla prototype through {@code patches.RelicSwapPatch}; the vanilla counter
 * bookkeeping ({@code atBattleStart}, {@code onVictory}) is inherited.
 */
public class VelvetChoker extends com.megacrit.cardcrawl.relics.VelvetChoker {
    /** Playing more than this many cards in a turn triggers the energy penalty. */
    public static final int PLAY_LIMIT = 6;

    public VelvetChoker() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new VelvetChoker();
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

    @Override
    public void atTurnStart() {
        if (this.counter > PLAY_LIMIT) {
            RelicChecks.loseEnergy();
        }
        this.counter = 0;
    }

    @Override
    public void onPlayCard(AbstractCard card, AbstractMonster m) {
        if (this.counter < Integer.MAX_VALUE) {
            this.counter++;
        }
    }

    @Override
    public boolean canPlay(AbstractCard card) {
        return true;
    }
}
