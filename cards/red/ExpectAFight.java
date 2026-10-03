package anotherspirerework.cards.red;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * Expect a Fight (the reworked vanilla "Pummel"): an Attack card turned into an energy source,
 * one [E] for each Attack card in hand.
 *
 * The card ID stays "Pummel" so the card swap keeps replacing the vanilla card and existing saves,
 * pools and the compendium carry over untouched.
 */
public class ExpectAFight extends AbstractCard {
    public static final String ID = "Pummel";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    /** Whether the description currently carries the live counter line. */
    private boolean showingCounter = false;

    public ExpectAFight() {
        super(ID, cardStrings.NAME, "red/attack/pummel", 2, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.RED, CardRarity.UNCOMMON, CardTarget.NONE);
    }

    /** The Attacks in hand: what the card hands out, and what its description counts. */
    public static int countAttacks(AbstractPlayer p) {
        if (p == null || p.hand == null) {
            return 0;
        }
        int count = 0;
        for (AbstractCard c : p.hand.group) {
            if (c.type == CardType.ATTACK) {
                count++;
            }
        }
        return count;
    }

    /**
     * Outside a fight the counter line is not part of the description at all, so the reward screen,
     * the deck view and the compendium show the plain text. During a fight the vanilla "!M!" dynamic
     * number keeps the shown amount in sync with the hand, which card groups update every frame.
     */
    @Override
    public void update() {
        super.update();
        boolean combat = AnotherSpireRework.inCombat();
        if (combat != this.showingCounter) {
            this.showingCounter = combat;
            this.rawDescription = combat
                    ? cardStrings.DESCRIPTION + AnotherSpireRework.extendedDescription(cardStrings, 0)
                    : cardStrings.DESCRIPTION;
            initializeDescription();
        }
        int attacks = combat ? countAttacks(AbstractDungeon.player) : 0;
        if (this.baseMagicNumber != attacks) {
            this.baseMagicNumber = attacks;
            this.magicNumber = attacks;
        }
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        int attacks = countAttacks(p);
        if (attacks > 0) {
            addToBot(new GainEnergyAction(attacks));
        }
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(1);
        }
    }

    public AbstractCard makeCopy() {
        return new ExpectAFight();
    }
}
