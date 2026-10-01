package anotherspire.cards.red;

import anotherspire.AnotherSpire;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.LoseStrengthPower;
import com.megacrit.cardcrawl.powers.StrengthPower;

public class Flex extends AbstractCard {
    public static final String ID = "Flex";

    private static final CardStrings cardStrings = AnotherSpire.getCardStrings(ID);

    public Flex() {
        super(ID, cardStrings.NAME, "red/skill/flex", 0, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.RED, CardRarity.COMMON, CardTarget.SELF);
        this.baseMagicNumber = 4;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new StrengthPower(p, this.magicNumber), this.magicNumber));
        addToBot(new ApplyPowerAction(p, p, new LoseStrengthPower(p, this.magicNumber), this.magicNumber));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(4);
        }
    }

    public AbstractCard makeCopy() {
        return new Flex();
    }
}
