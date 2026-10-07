package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

/**
 * Creative AI, upgraded: same as the vanilla CreativeAIPower but the Power card it hands out is
 * already upgraded.
 */
public class CreativeAIUpgradedPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpireRework.makeID("CreativeAIUpgraded");
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("CreativeAIUpgraded");

    private final boolean upgradeCards;

    public CreativeAIUpgradedPower(AbstractCreature owner, int amount, boolean upgradeCards) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.upgradeCards = upgradeCards;
        this.type = PowerType.BUFF;
        updateDescription();
        loadRegion("ai");
    }

    @Override
    public void updateDescription() {
        if (this.amount > 1) {
            this.description = powerStrings.DESCRIPTIONS[0] + this.amount + powerStrings.DESCRIPTIONS[2];
        } else {
            this.description = powerStrings.DESCRIPTIONS[0] + this.amount + powerStrings.DESCRIPTIONS[1];
        }
    }

    @Override
    public void atStartOfTurn() {
        for (int i = 0; i < this.amount; i++) {
            AbstractCard card = randomPowerCard();
            if (card != null) {
                if (this.upgradeCards && card.canUpgrade()) {
                    card.upgrade();
                }
                addToBot(new MakeTempCardInHandAction(card));
            }
        }
    }

    /** Vanilla returnTrulyRandomCardInCombat crashes on an empty pool, so the pool is probed first. */
    private static AbstractCard randomPowerCard() {
        if (!hasPowerCard()) {
            AnotherSpireRework.logger.error("Creative AI could not find a Power card to add to the hand.");
            return null;
        }
        return AbstractDungeon.returnTrulyRandomCardInCombat(AbstractCard.CardType.POWER).makeCopy();
    }

    private static boolean hasPowerCard() {
        for (AbstractCard c : AbstractDungeon.srcCommonCardPool.group) {
            if (c.type == AbstractCard.CardType.POWER) {
                return true;
            }
        }
        for (AbstractCard c : AbstractDungeon.srcUncommonCardPool.group) {
            if (c.type == AbstractCard.CardType.POWER) {
                return true;
            }
        }
        for (AbstractCard c : AbstractDungeon.srcRareCardPool.group) {
            if (c.type == AbstractCard.CardType.POWER) {
                return true;
            }
        }
        return false;
    }
}
