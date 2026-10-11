package anotherspirerework.cards.purple;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.SpiritShieldPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * Spirit Shield: a Power now, halving the damage the player takes while in Calm. Upgrading only
 * makes it cheaper; the strength lives in how many copies are running.
 */
public class SpiritShield extends AbstractCard {
    public static final String ID = "SpiritShield";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public SpiritShield() {
        super(ID, cardStrings.NAME, "purple/skill/spirit_shield", 2, cardStrings.DESCRIPTION, CardType.POWER, CardColor.PURPLE, CardRarity.RARE, CardTarget.SELF);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new SpiritShieldPower(p, 1), 1));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(1);
        }
    }

    public AbstractCard makeCopy() {
        return new SpiritShield();
    }
}
