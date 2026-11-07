package anotherspirerework.cards.green;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.ExhaustAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
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
                CardType.SKILL, CardColor.GREEN, CardRarity.UNCOMMON, CardTarget.SELF);
        baseBlock = 2;
        cardsToPreview = new Shiv();
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new GainBlockAction(p, p, block));
        addToBot(new ExhaustAction(1, false, false, false));
        addToBot(new MakeTempCardInHandAction(new Shiv(), 1));
    }
    public void upgrade() { if (!upgraded) { upgradeName(); upgradeBaseCost(0); } }
    public AbstractCard makeCopy() { return new CloakAndDagger(); }
}
