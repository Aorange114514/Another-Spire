package anotherspirerework.cards.green;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Concentrate extends AbstractCard {
    public static final String ID = "Concentrate";
    private static final CardStrings strings = AnotherSpireRework.getCardStrings(ID);

    public Concentrate() {
        super(ID, strings.NAME, "green/skill/concentrate", 0, strings.DESCRIPTION,
                CardType.SKILL, CardColor.GREEN, CardRarity.UNCOMMON, CardTarget.SELF);
        baseMagicNumber = magicNumber = 2;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DiscardAction(p, p, 3, false));
        addToBot(new GainEnergyAction(magicNumber));
    }

    public void upgrade() {
        if (!upgraded) {
            upgradeName(); upgradeMagicNumber(1);
            rawDescription = strings.UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }
    public AbstractCard makeCopy() { return new Concentrate(); }
}