package anotherspire.cards.purple;

import anotherspire.AnotherSpire;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.tempCards.Insight;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.combat.ThirdEyeEffect;

public class ThirdEye extends AbstractCard {
    public static final String ID = "ThirdEye";

    private static final CardStrings cardStrings = AnotherSpire.getCardStrings(ID);

    public ThirdEye() {
        super(ID, cardStrings.NAME, "purple/skill/third_eye", 1, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.PURPLE, CardRarity.COMMON, CardTarget.SELF);
        this.baseMagicNumber = 3;
        this.magicNumber = this.baseMagicNumber;
        this.cardsToPreview = new Insight();
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        if (p != null) {
            addToBot(new VFXAction(new ThirdEyeEffect(p.hb.cX, p.hb.cY)));
        }
        addToBot(new MakeTempCardInHandAction(this.cardsToPreview.makeStatEquivalentCopy(), this.magicNumber));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(1);
        }
    }

    public AbstractCard makeCopy() {
        return new ThirdEye();
    }
}
