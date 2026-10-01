package anotherspire.powers;

import anotherspire.AnotherSpire;
import com.megacrit.cardcrawl.actions.common.ExhaustAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.tempCards.Smite;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

/** Battle Hymn: at the start of each turn exhaust a random card in hand, then add a Smite. */
public class BattleHymnSmitePower extends AbstractPower {
    public static final String POWER_ID = AnotherSpire.makeID("BattleHymnSmite");
    private static final PowerStrings powerStrings = AnotherSpire.getPowerStrings("BattleHymnSmite");

    public BattleHymnSmitePower(AbstractCreature owner, int amount) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;
        updateDescription();
        loadRegion("hymn");
    }

    @Override
    public void updateDescription() {
        if (this.amount > 1) {
            this.description = powerStrings.DESCRIPTIONS[0] + this.amount + powerStrings.DESCRIPTIONS[1];
        } else {
            this.description = powerStrings.DESCRIPTIONS[0] + this.amount + powerStrings.DESCRIPTIONS[2];
        }
    }

    @Override
    public void stackPower(int stackAmount) {
        this.fontScale = 8.0F;
        this.amount += stackAmount;
        updateDescription();
    }

    @Override
    public void atStartOfTurnPostDraw() {
        if (AbstractDungeon.getMonsters().areMonstersBasicallyDead()) {
            return;
        }
        flash();
        // Same UX as Tools of the Trade: the player picks the card themselves (false = not random).
        addToBot(new ExhaustAction(this.amount, false));
        addToBot(new MakeTempCardInHandAction(new Smite(), this.amount, false));
    }
}
