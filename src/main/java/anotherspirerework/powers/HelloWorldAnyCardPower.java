package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

/**
 * Hello World's power. This is the vanilla HelloPower with the Common rarity restriction removed:
 * the card still comes out of AbstractDungeon's own card pools, so it stays inside the current
 * character's card pool and the dailies that add other colours (Diverse, Colorless Cards) keep
 * working exactly like they do for the vanilla power.
 */
public class HelloWorldAnyCardPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpireRework.makeID("HelloWorldAnyCard");
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("HelloWorldAnyCard");

    private static final AbstractCard.CardRarity[] RARITIES = {
            AbstractCard.CardRarity.COMMON,
            AbstractCard.CardRarity.UNCOMMON,
            AbstractCard.CardRarity.RARE
    };

    public HelloWorldAnyCardPower(AbstractCreature owner, int amount) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;
        updateDescription();
        loadRegion("hello");
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
    public void atStartOfTurn() {
        if (AbstractDungeon.getMonsters().areMonstersBasicallyDead()) {
            return;
        }
        flash();
        for (int i = 0; i < this.amount; i++) {
            AbstractCard card = randomCard();
            if (card != null) {
                addToBot(new MakeTempCardInHandAction(card, 1, false));
            }
        }
    }

    /** Picks one of the three rarities, then asks the base game for a card of it (vanilla pool rules). */
    private static AbstractCard randomCard() {
        int start = AbstractDungeon.cardRandomRng.random(RARITIES.length - 1);
        for (int i = 0; i < RARITIES.length; i++) {
            AbstractCard card = AbstractDungeon.getCard(RARITIES[(start + i) % RARITIES.length], AbstractDungeon.cardRandomRng);
            if (card != null) {
                return card.makeCopy();
            }
        }
        AnotherSpireRework.logger.error("Hello World could not find a card to add to the hand.");
        return null;
    }
}
