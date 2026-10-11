package anotherspirerework.effects;

import anotherspirerework.AnotherSpireRework;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.UpgradeShineEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;

import java.util.ArrayList;

/**
 * Tiny House on pickup: upgrade one card, then three colourless card rewards.
 *
 * <p>It runs as an effect rather than an action because relics are picked up in shops, chests and
 * reward screens, where the base game's action queue is not running. The upgrade uses the base
 * game's own "pick a card to upgrade" grid, and the rewards are the room's own reward list opened
 * again, exactly the way the base game opens it after a fight.
 */
public class TinyHouseEffect extends AbstractGameEffect {

    private static final float OPEN_DELAY = 0.4F;
    private static final int COLOURLESS_REWARDS = 3;

    private final AbstractRelic relic;
    private float delay = OPEN_DELAY;
    private boolean opened;

    public TinyHouseEffect(AbstractRelic relic) {
        this.relic = relic;
        this.duration = 0.0F;
    }

    public static void start(AbstractRelic relic) {
        AbstractDungeon.effectList.add(new TinyHouseEffect(relic));
    }

    @Override
    public void update() {
        if (this.isDone) {
            return;
        }
        if (AbstractDungeon.player == null) {
            this.isDone = true;
            return;
        }
        if (ChooseDeckCardsEffect.browsingCards()) {
            return;
        }
        if (!this.opened) {
            this.delay -= Gdx.graphics.getDeltaTime();
            if (this.delay > 0.0F) {
                return;
            }
            this.opened = true;
            CardGroup group = upgradableCards();
            if (group.size() == 0) {
                openRewards();
                this.isDone = true;
                return;
            }
            AbstractDungeon.gridSelectScreen.open(group, 1, ours(2), true);
            return;
        }
        // The pick is over only once the screen is really gone, the same way the deck picker waits:
        // the map and the deck view can be opened on top of the grid, and the base game restores it
        // afterwards.
        if (AbstractDungeon.isScreenUp) {
            return;
        }
        for (AbstractCard card : new ArrayList<>(AbstractDungeon.gridSelectScreen.selectedCards)) {
            upgrade(card);
        }
        AbstractDungeon.gridSelectScreen.selectedCards.clear();
        openRewards();
        this.isDone = true;
    }

    /** The deck's own upgradable cards, so the grid can only offer legal picks. */
    private static CardGroup upgradableCards() {
        CardGroup group = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
        for (AbstractCard card : AbstractDungeon.player.masterDeck.group) {
            if (card.canUpgrade()) {
                group.addToTop(card);
            }
        }
        return group;
    }

    /** The card is upgraded in the deck itself, like the base game's campfire does. */
    private static void upgrade(AbstractCard card) {
        card.upgrade();
        card.unhover();
        card.untip();
        card.stopGlowing();
        AbstractDungeon.topLevelEffects.add(new UpgradeShineEffect(Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
        AbstractDungeon.topLevelEffects.add(new ShowCardBrieflyEffect(card.makeStatEquivalentCopy()));
    }

    private void openRewards() {
        for (int i = 0; i < COLOURLESS_REWARDS; i++) {
            RewardItem item = new RewardItem(AbstractCard.CardColor.COLORLESS);
            if (item.cards != null && !item.cards.isEmpty()) {
                AbstractDungeon.getCurrRoom().rewards.add(item);
            }
        }
        AbstractDungeon.combatRewardScreen.open(ours(1));
        AbstractDungeon.getCurrRoom().rewardPopOutTimer = 0.0F;
    }

    /** One of our own RelicStrings lines for this relic; missing lines fall back to empty. */
    private String ours(int index) {
        String[] descriptions = CardCrawlGame.languagePack
                .getRelicStrings(AnotherSpireRework.makeID(this.relic.relicId)).DESCRIPTIONS;
        if (descriptions == null || descriptions.length <= index || descriptions[index] == null) {
            return "";
        }
        return descriptions[index];
    }

    @Override
    public void render(SpriteBatch sb) {
    }

    @Override
    public void dispose() {
    }
}
