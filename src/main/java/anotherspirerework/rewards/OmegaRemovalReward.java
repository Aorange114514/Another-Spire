package anotherspirerework.rewards;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.patches.OmegaRewardTypePatch;
import basemod.abstracts.CustomReward;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.vfx.cardManip.PurgeCardEffect;

/**
 * Omega: a reward-screen item that opens the shop's card removal grid when picked up. The reward
 * follows the vanilla reward logic: it stays on the reward screen while the removal grid is open
 * and only disappears once a card was really removed; cancelling the grid leaves it clickable.
 *
 * <p>The icon is a 64x64 file at {@code anotherspirerework/images/omega_reward.png} when supplied,
 * falling back to the Omega card portrait otherwise.
 */
public class OmegaRemovalReward extends CustomReward {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack
            .getUIString(AnotherSpireRework.makeID("OmegaRemoval"));

    private static final String ICON_PATH = "omega_reward";

    /** The reward whose removal grid is currently open, or null. */
    private static OmegaRemovalReward activeReward;

    /** True once the player really removed a card; the reward screen then drops the item. */
    private boolean consumed;

    public OmegaRemovalReward() {
        super(icon(), uiStrings.TEXT[0], OmegaRewardTypePatch.OMEGA_REMOVAL);
    }

    private static TextureRegion icon() {
        FileHandle file = Gdx.files.internal(AnotherSpireRework.imagePath(ICON_PATH + ".png"));
        if (file.exists()) {
            return new TextureRegion(new Texture(file));
        }
        AbstractCard omega = CardLibrary.getCard("Omega");
        return omega == null ? null : omega.portrait;
    }

    @Override
    public boolean claimReward() {
        if (this.consumed) {
            return true;
        }
        CardGroup purgeable = CardGroup.getGroupWithoutBottledCards(
                AbstractDungeon.player.masterDeck.getPurgeableCards());
        if (purgeable.isEmpty()) {
            return true;
        }
        AbstractDungeon.previousScreen = AbstractDungeon.CurrentScreen.COMBAT_REWARD;
        AbstractDungeon.gridSelectScreen.open(purgeable, 1, uiStrings.TEXT[1], false, false, true, true);
        // The removal grid covers the reward screen, so its banner ("Loot!" and the other flavour
        // labels) would float over the grid; vanilla re-appears it when the reward screen reopens.
        AbstractDungeon.dynamicBanner.hide();
        activeReward = this;
        return false;
    }

    /**
     * Applies the removal once the player confirmed a card in the grid. While the grid is still
     * open this waits; when it closes without a selection the reward simply stays clickable, and a
     * later unrelated grid pick can never be mistaken for the Omega removal.
     */
    public static void tick() {
        if (activeReward == null || AbstractDungeon.screen == AbstractDungeon.CurrentScreen.GRID) {
            return;
        }
        OmegaRemovalReward reward = activeReward;
        activeReward = null;
        if (AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {
            return;
        }
        for (AbstractCard card : AbstractDungeon.gridSelectScreen.selectedCards) {
            CardCrawlGame.metricData.addPurgedItem(card.getMetricID());
            AbstractDungeon.topLevelEffects.add(new PurgeCardEffect(
                    card, (float) Settings.WIDTH / 2.0F, (float) Settings.HEIGHT / 2.0F));
            AbstractDungeon.player.masterDeck.removeCard(card);
        }
        AbstractDungeon.gridSelectScreen.selectedCards.clear();
        reward.consumed = true;
        reward.isDone = true;
    }

    /** A safety reset shared with the other combat-state cleanups. */
    public static void reset() {
        activeReward = null;
    }
}
