package anotherspirerework.cards.green;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.ShivAllEnemiesPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class DieDieDie extends AbstractCard {
    public static final String ID = "Die Die Die";
    private static final CardStrings strings = AnotherSpireRework.getCardStrings(ID);

    public DieDieDie() {
        super(ID, strings.NAME, "green/attack/die_die_die", 2, strings.DESCRIPTION,
                CardType.POWER, CardColor.GREEN, CardRarity.RARE, CardTarget.SELF);
        cardsToPreview = new Shiv();
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new ShivAllEnemiesPower(p)));
        addToBot(new MakeTempCardInHandAction(new Shiv(), 3));
    }

    public void upgrade() { if (!upgraded) { upgradeName(); upgradeBaseCost(1); } }
    public AbstractCard makeCopy() { return new DieDieDie(); }
}