package anotherspirerework.cards.colorless;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.SadisticNaturePower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * Sadistic Nature: the Weak and Vulnerable on enemies hit twice as hard.
 *
 * <p>The doubling is applied where the base game reads those powers, in
 * {@code SadisticNaturePatch}, and is switched on by {@link SadisticNaturePower}.
 */
public class SadisticNature extends AbstractCard {
    public static final String ID = "Sadistic Nature";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public SadisticNature() {
        super(ID, cardStrings.NAME, "colorless/power/sadistic_nature", 2, cardStrings.DESCRIPTION, CardType.POWER, CardColor.COLORLESS, CardRarity.RARE, CardTarget.SELF);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new SadisticNaturePower(p, 1), 1));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(1);
        }
    }

    public AbstractCard makeCopy() {
        return new SadisticNature();
    }
}
