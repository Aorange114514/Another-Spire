package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;

public class ShivAllEnemiesPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpireRework.makeID("ShivAllEnemies");

    /**
     * Die Die Die's buff. The field has to be named {@code powerStrings}: every other power in this
     * mod uses that name, and the power list screens read it by reflection to build the entry
     * title. Without it the title is never resolved and the base game logs
     * "PowerString: anotherspirerework.powers.ShivAllEnemiesPower not found".
     */
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("ShivAllEnemies");

    public ShivAllEnemiesPower(AbstractCreature owner) {
        this.owner = owner;
        ID = POWER_ID;
        name = powerStrings.NAME;
        amount = -1;
        type = PowerType.BUFF;
        loadRegion("accuracy");
        updateDescription();
    }

    public void updateDescription() {
        description = powerStrings.DESCRIPTIONS[0];
    }

    @Override
    public void stackPower(int stackAmount) { /* Additional copies do not multiply area attacks. */ }

    public void onInitialApplication() {
        for (CardGroup group : new CardGroup[]{AbstractDungeon.player.hand, AbstractDungeon.player.drawPile,
                AbstractDungeon.player.discardPile, AbstractDungeon.player.exhaustPile}) {
            for (AbstractCard card : group.group) if (card instanceof Shiv) card.applyPowers();
        }
    }
}