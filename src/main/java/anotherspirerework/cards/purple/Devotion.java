package anotherspirerework.cards.purple;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.DevotionDrawPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Devotion extends AbstractCard {
    public static final String ID = "Devotion";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Devotion() {
        super(ID, cardStrings.NAME, "purple/power/devotion", 1, cardStrings.DESCRIPTION, CardType.POWER, CardColor.PURPLE, CardRarity.RARE, CardTarget.NONE);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new DevotionDrawPower(p, 1), 1));
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
        return new Devotion();
    }
}
