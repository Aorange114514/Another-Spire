package anotherspirerework.cards.red;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.actions.common.LoseHPAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Bloodletting extends AbstractCard {
    public static final String ID = "Bloodletting";

    private static final int BASE_HP_LOSS = 3;
    private static final int UPGRADED_HP_LOSS = 2;

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Bloodletting() {
        super(ID, cardStrings.NAME, "red/skill/bloodletting", 0, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.RED, CardRarity.COMMON, CardTarget.SELF);
        this.baseMagicNumber = 2;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new LoseHPAction(p, p, this.currentHpLoss()));
        addToBot(new GainEnergyAction(this.magicNumber));
    }

    /** The HP cost is reduced by the upgrade, so it cannot live in baseMagicNumber. */
    private int currentHpLoss() {
        return this.upgraded ? UPGRADED_HP_LOSS : BASE_HP_LOSS;
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(1);
            this.rawDescription = cardStrings.UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }

    public AbstractCard makeCopy() {
        return new Bloodletting();
    }
}
