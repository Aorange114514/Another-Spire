package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.DexterityPower;
import com.megacrit.cardcrawl.powers.LoseDexterityPower;
import com.megacrit.cardcrawl.powers.LoseStrengthPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;

/**
 * Duality (the vanilla relic ID is still {@code "Yang"}): playing an Attack grants 1 temporary
 * Dexterity and playing a Skill grants 1 temporary Strength. The vanilla relic only ever paid out
 * Dexterity, and only for Attacks.
 *
 * <p>Like every reworked relic it replaces the vanilla prototype through
 * {@code patches.RelicSwapPatch}, keeping the vanilla ID, art and acquisition path. Every hook this
 * class does not override is inherited from the vanilla relic unchanged.
 */
public class Duality extends com.megacrit.cardcrawl.relics.Duality {
    public Duality() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new Duality();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** Both halves replace the vanilla Attack-only effect; other card types are ignored. */
    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (card == null) {
            return;
        }
        boolean attack = card.type == AbstractCard.CardType.ATTACK;
        boolean skill = card.type == AbstractCard.CardType.SKILL;
        if (!attack && !skill) {
            return;
        }
        flash();
        addToBot(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        if (attack) {
            addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player,
                    new DexterityPower(AbstractDungeon.player, 1), 1));
            addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player,
                    new LoseDexterityPower(AbstractDungeon.player, 1), 1));
        } else {
            addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player,
                    new StrengthPower(AbstractDungeon.player, 1), 1));
            addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player,
                    new LoseStrengthPower(AbstractDungeon.player, 1), 1));
        }
    }
}
