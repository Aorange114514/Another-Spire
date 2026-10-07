package anotherspirerework.cards.blue;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveAllBlockAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.powers.WeakPower;

public class Melter extends AbstractCard {
    public static final String ID = "Melter";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Melter() {
        super(ID, cardStrings.NAME, "blue/attack/melter", 1, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.BLUE, CardRarity.UNCOMMON, CardTarget.ENEMY);
        this.baseMagicNumber = 2;
        this.magicNumber = this.baseMagicNumber;
        this.exhaust = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new RemoveSpecificPowerAction(m, p, "Artifact"));
        addToBot(new RemoveAllBlockAction(m, p));
        addToBot(new ApplyPowerAction(m, p, new WeakPower(m, this.magicNumber, false), this.magicNumber));
        addToBot(new ApplyPowerAction(m, p, new VulnerablePower(m, this.magicNumber, false), this.magicNumber));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(0);
        }
    }

    public AbstractCard makeCopy() {
        return new Melter();
    }
}
