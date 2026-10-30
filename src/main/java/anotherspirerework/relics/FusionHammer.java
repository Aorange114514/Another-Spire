package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.ui.campfire.AbstractCampfireOption;
import java.util.ArrayList;

/**
 * Fusion Hammer: on pickup it randomly downgrades four upgraded cards of the master deck (one
 * upgrade level each, so the same card can be picked more than once) and still grants [E] every
 * turn. The vanilla Smith lock is gone, so Rest Sites can upgrade cards again.
 *
 * <p>Replaces the vanilla prototype through {@code patches.RelicSwapPatch}, keeping the vanilla
 * energy hook and onUnequip. The vanilla class rewrites its own description on a class change, so
 * both string hooks are overridden to keep the mod text.
 */
public class FusionHammer extends com.megacrit.cardcrawl.relics.FusionHammer {
    public FusionHammer() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new FusionHammer();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    @Override
    public void updateDescription(AbstractPlayer.PlayerClass c) {
        this.description = getUpdatedDescription();
        this.tips.clear();
        this.tips.add(new PowerTip(this.name, this.description));
        initializeTips();
    }

    @Override
    public void onEquip() {
        super.onEquip();
        downgradeCards();
    }

    /** The vanilla class locked the Smith; the rework hands it back. */
    @Override
    public boolean canUseCampfireOption(AbstractCampfireOption option) {
        return true;
    }

    /** Rebuild one upgrade level without losing UUID, bottles or persistent misc. */
    private static void downgradeCards() {
        for (int i = 0; i < 4; i++) {
            ArrayList<AbstractCard> candidates = new ArrayList<>();
            for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
                if (c.upgraded && c.timesUpgraded > 0) {
                    candidates.add(c);
                }
            }
            if (candidates.isEmpty()) {
                break;
            }
            AbstractCard old = candidates.get(AbstractDungeon.miscRng.random(candidates.size() - 1));
            AbstractCard card = com.megacrit.cardcrawl.helpers.CardLibrary.getCard(old.cardID).makeCopy();
            // SearingBlow.makeCopy carries its upgrade count even though stats start at base.
            card.timesUpgraded = 0;
            card.upgraded = false;
            for (int n = 1; n < old.timesUpgraded; n++) {
                card.upgrade();
            }
            card.uuid = old.uuid;
            card.misc = old.misc;
            card.inBottleFlame = old.inBottleFlame;
            card.inBottleLightning = old.inBottleLightning;
            card.inBottleTornado = old.inBottleTornado;
            AbstractDungeon.player.masterDeck.group.set(
                    AbstractDungeon.player.masterDeck.group.indexOf(old), card);
        }
    }
}
