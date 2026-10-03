package anotherspirerework.cards.green;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.actions.PlayShivsFromExhaustAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * Die Die Die: an Attack turned into a Skill that replays every Shiv in the exhaust pile, which
 * doubles the number of Shivs sitting there. The upgrade makes it cheaper.
 */
public class DieDieDie extends AbstractCard {
    public static final String ID = "Die Die Die";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    /** Whether the description currently carries the live counter line. */
    private boolean showingCounter = false;

    public DieDieDie() {
        super(ID, cardStrings.NAME, "green/attack/die_die_die", 2, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.GREEN, CardRarity.RARE, CardTarget.ENEMY);
    }

    /** The Shivs waiting in the exhaust pile: exactly what the card is about to replay. */
    public static int countShivsInExhaust(AbstractPlayer p) {
        if (p == null || p.exhaustPile == null) {
            return 0;
        }
        int count = 0;
        for (AbstractCard c : p.exhaustPile.group) {
            if (c instanceof Shiv) {
                count++;
            }
        }
        return count;
    }

    /**
     * Outside a fight the counter line is not part of the description at all, so the reward screen,
     * the deck view and the compendium show the plain text. During a fight the vanilla "!M!" dynamic
     * number keeps the shown amount in sync with the exhaust pile.
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
        int shivs = combat ? countShivsInExhaust(AbstractDungeon.player) : 0;
        if (this.baseMagicNumber != shivs) {
            this.baseMagicNumber = shivs;
            this.magicNumber = shivs;
        }
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new PlayShivsFromExhaustAction(m));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(1);
        }
    }

    public AbstractCard makeCopy() {
        return new DieDieDie();
    }
}
