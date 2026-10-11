package anotherspirerework.cards.purple;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.EmptyBodyPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * Empty Body: a Power now. The Block is granted at the end of every turn, so the value lives on
 * the power's stack level and the upgrade raises it.
 */
public class EmptyBody extends AbstractCard {
    public static final String ID = "EmptyBody";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public EmptyBody() {
        super(ID, cardStrings.NAME, "purple/skill/empty_body", 1, cardStrings.DESCRIPTION, CardType.POWER, CardColor.PURPLE, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseMagicNumber = 7;
        this.magicNumber = this.baseMagicNumber;
        this.tags.add(CardTags.EMPTY);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new EmptyBodyPower(p, this.magicNumber), this.magicNumber));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(2);
        }
    }

    public AbstractCard makeCopy() {
        return new EmptyBody();
    }
}
