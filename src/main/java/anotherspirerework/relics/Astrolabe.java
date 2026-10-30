package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.effects.ChooseDeckCardsEffect;
import com.megacrit.cardcrawl.relics.AbstractRelic;

/**
 * Astrolabe: upon pickup the player chooses any number of cards out of a forty-card pool of any
 * colour to add to the deck. The vanilla effect transformed exactly three deck cards, so neither the
 * old pick screen nor its {@code giveCards} handout is used any more.
 *
 * <p>Like every reworked relic it replaces the vanilla prototype through
 * {@code patches.RelicSwapPatch}, keeping the vanilla ID, art and acquisition path. Every hook this
 * class does not override is inherited from the vanilla relic unchanged.
 */
public class Astrolabe extends com.megacrit.cardcrawl.relics.Astrolabe {
    /** Distinct cards offered by the picker. */
    public static final int CHOICES = 40;

    public Astrolabe() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new Astrolabe();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /**
     * The vanilla three-card transform is replaced by the mod's own picker: the player picks any
     * number of cards of any colour, and the second description line is the pick screen's header.
     * The grid select is opened by an effect rather than by an action, because this runs in shops,
     * chests and reward screens where the action queue accepts nothing.
     */
    @Override
    public void onEquip() {
        flash();
        ChooseDeckCardsEffect.start(ChooseDeckCardsEffect.Choice.ADD_ANY_COLOR,
                AnotherSpireRework.getRelicDescription(relicId, 1), CHOICES);
    }
}
