package anotherspirerework.cards.red;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.actions.WhirlwindXAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Whirlwind extends AbstractCard {
    public static final String ID = "Whirlwind";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Whirlwind() {
        super(ID, cardStrings.NAME, "red/attack/whirlwind", -1, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.RED, CardRarity.UNCOMMON, CardTarget.ALL_ENEMY);
        this.baseDamage = 6;
        this.isMultiDamage = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new WhirlwindXAction(p, this.multiDamage, this.damageTypeForTurn, this.freeToPlayOnce, this.energyOnUse, this.upgraded));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeDamage(4);
            this.rawDescription = cardStrings.UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }

    public AbstractCard makeCopy() {
        return new Whirlwind();
    }
}
