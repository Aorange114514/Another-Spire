package anotherspirerework.cards.green;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.actions.TransformDrawPileToShivsAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class CloakAndDagger extends AbstractCard {
    public static final String ID = "Cloak And Dagger";
    private static final CardStrings strings = AnotherSpireRework.getCardStrings(ID);

    public CloakAndDagger() {
        super(ID, strings.NAME, "green/skill/cloak_and_dagger", 1, strings.DESCRIPTION,
                CardType.SKILL, CardColor.GREEN, CardRarity.COMMON, CardTarget.SELF);
        baseBlock = 6;
        baseMagicNumber = magicNumber = 1;
        cardsToPreview = new Shiv();
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new GainBlockAction(p, p, block));
        addToBot(new TransformDrawPileToShivsAction(magicNumber));
    }
    public void upgrade() { if (!upgraded) { upgradeName(); upgradeMagicNumber(1); } }
    public AbstractCard makeCopy() { return new CloakAndDagger(); }
}