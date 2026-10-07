package anotherspirerework.cards.red;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.patches.ExhaustTracker;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** Seeing Red: the vanilla energy burst, but it only pays out if a card was already exhausted this turn. */
public class SeeingRed extends AbstractCard {
    public static final String ID = "Seeing Red";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public SeeingRed() {
        super(ID, cardStrings.NAME, "red/skill/seeing_red", 1, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.RED, CardRarity.UNCOMMON, CardTarget.NONE);
        this.baseMagicNumber = 3;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        if (ExhaustTracker.exhaustedThisTurn()) {
            addToBot(new GainEnergyAction(this.magicNumber));
        }
    }

    public void triggerOnGlowCheck() {
        this.glowColor = ExhaustTracker.exhaustedThisTurn() ? AbstractCard.GOLD_BORDER_GLOW_COLOR.cpy() : AbstractCard.BLUE_BORDER_GLOW_COLOR.cpy();
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(0);
            this.rawDescription = cardStrings.UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }

    public AbstractCard makeCopy() {
        return new SeeingRed();
    }
}
