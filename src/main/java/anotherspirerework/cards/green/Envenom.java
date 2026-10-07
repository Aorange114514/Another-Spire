package anotherspirerework.cards.green;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.ShivPoisonPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Envenom extends AbstractCard {
    public static final String ID = "Envenom";
    private static final CardStrings strings = AnotherSpireRework.getCardStrings(ID);

    public Envenom() {
        super(ID, strings.NAME, "green/power/envenom", 1, strings.DESCRIPTION,
                CardType.POWER, CardColor.GREEN, CardRarity.RARE, CardTarget.SELF);
        baseMagicNumber = magicNumber = 1;
        cardsToPreview = new Shiv();
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new ShivPoisonPower(p, magicNumber), magicNumber));
        addToBot(new MakeTempCardInHandAction(new Shiv(), 1));
    }

    public void upgrade() { if (!upgraded) { upgradeName(); upgradeMagicNumber(1); } }
    public AbstractCard makeCopy() { return new Envenom(); }
}