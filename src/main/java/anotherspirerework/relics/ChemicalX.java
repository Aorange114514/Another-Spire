package anotherspirerework.relics;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;

/**
 * Chemical X: keeps its vanilla X-cost boost and text. Only the acquisition changes: it may spawn
 * only when the master deck - or the shop currently being opened - contains an X-cost card, so the
 * relic is never offered without something to boost.
 *
 * <p>Replaces the vanilla prototype through {@code patches.RelicSwapPatch}.
 */
public class ChemicalX extends com.megacrit.cardcrawl.relics.ChemicalX {
    public ChemicalX() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new ChemicalX();
    }

    @Override
    public boolean canSpawn() {
        if (AbstractDungeon.player == null) {
            return false;
        }
        for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
            if (c.cost == -1) {
                return true;
            }
        }
        if (AbstractDungeon.shopScreen != null) {
            for (AbstractCard c : AbstractDungeon.shopScreen.coloredCards) {
                if (c.cost == -1) {
                    return true;
                }
            }
            for (AbstractCard c : AbstractDungeon.shopScreen.colorlessCards) {
                if (c.cost == -1) {
                    return true;
                }
            }
        }
        return false;
    }
}
