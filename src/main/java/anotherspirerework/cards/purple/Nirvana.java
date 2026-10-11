package anotherspirerework.cards.purple;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.NirvanaCounterPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * Nirvana: a counter that pays out cards when a turn gets busy. Innate once upgraded, and its
 * cost/timing text is the same at both levels, so only the innate keyword changes.
 */
public class Nirvana extends AbstractCard {
    public static final String ID = "Nirvana";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Nirvana() {
        super(ID, cardStrings.NAME, "purple/power/nirvana", 1, cardStrings.DESCRIPTION, CardType.POWER, CardColor.PURPLE, CardRarity.UNCOMMON, CardTarget.SELF);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new NirvanaCounterPower(p, 1), 1));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            this.isInnate = true;
            this.rawDescription = cardStrings.UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }

    public AbstractCard makeCopy() {
        return new Nirvana();
    }
}
