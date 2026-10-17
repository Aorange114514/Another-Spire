package anotherspirerework.cards.green;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.BlurDrawPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * Blur: for the rest of this turn every card drawn also grants Block.
 *
 * <p>The counting lives in {@link BlurDrawPower}, which the mod's PostDraw subscriber feeds.
 * The printed payout is a flat 1 Block; the upgrade only takes the card from 1 energy to 0.
 */
public class Blur extends AbstractCard {
    public static final String ID = "Blur";

    /** Block granted per card drawn, at both levels. */
    public static final int BLOCK_PER_DRAW = 1;

    /** 1 energy, 0 when upgraded. */
    public static final int BASE_COST = 1;
    public static final int UPGRADED_COST = 0;

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Blur() {
        super(ID, cardStrings.NAME, "green/skill/blur", BASE_COST, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.GREEN, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseMagicNumber = BLOCK_PER_DRAW;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new BlurDrawPower(p, this.magicNumber), this.magicNumber));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(UPGRADED_COST);
        }
    }

    public AbstractCard makeCopy() {
        return new Blur();
    }
}
