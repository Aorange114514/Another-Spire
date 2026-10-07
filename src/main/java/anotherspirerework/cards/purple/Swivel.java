package anotherspirerework.cards.purple;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.watcher.FreeAttackPower;

public class Swivel extends AbstractCard {
    public static final String ID = "Swivel";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Swivel() {
        super(ID, cardStrings.NAME, "purple/skill/swivel", 2, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.PURPLE, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseBlock = 10;
        this.baseMagicNumber = 2;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new GainBlockAction(p, p, this.block));
        addToBot(new DrawCardAction(p, this.magicNumber));
        addToBot(new ApplyPowerAction(p, p, new FreeAttackPower(p, 1), 1));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(1);
        }
    }

    public AbstractCard makeCopy() {
        return new Swivel();
    }
}
