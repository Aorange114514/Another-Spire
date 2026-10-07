package anotherspirerework.cards.purple;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.actions.watcher.ChangeStanceAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.tempCards.Insight;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Vengeance extends AbstractCard {
    public static final String ID = "Vengeance";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Vengeance() {
        super(ID, cardStrings.NAME, "purple/skill/simmering_fury", 1, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.PURPLE, CardRarity.UNCOMMON, CardTarget.NONE);
        this.baseMagicNumber = 1;
        this.magicNumber = this.baseMagicNumber;
        this.cardsToPreview = new Insight();
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ChangeStanceAction("Wrath"));
        addToBot(new MakeTempCardInDrawPileAction(this.cardsToPreview.makeStatEquivalentCopy(), this.magicNumber, true, true));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(1);
        }
    }

    public AbstractCard makeCopy() {
        return new Vengeance();
    }
}
