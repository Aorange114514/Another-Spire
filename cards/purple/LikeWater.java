package anotherspirerework.cards.purple;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.LikeWaterCalmPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class LikeWater extends AbstractCard {
    public static final String ID = "LikeWater";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public LikeWater() {
        super(ID, cardStrings.NAME, "purple/power/like_water", 1, cardStrings.DESCRIPTION, CardType.POWER, CardColor.PURPLE, CardRarity.UNCOMMON, CardTarget.NONE);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new LikeWaterCalmPower(p, 1), 1));
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
        return new LikeWater();
    }
}
