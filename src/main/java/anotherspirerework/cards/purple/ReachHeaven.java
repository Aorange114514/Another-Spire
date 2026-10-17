package anotherspirerework.cards.purple;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.actions.ReachHeavenAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class ReachHeaven extends AbstractCard {
    public static final String ID = "ReachHeaven";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public ReachHeaven() {
        super(ID, cardStrings.NAME, "purple/attack/reach_heaven", 3, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.PURPLE, CardRarity.UNCOMMON, CardTarget.ENEMY);
        this.baseDamage = 15;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ReachHeavenAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn)));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeDamage(5);
        }
    }

    public AbstractCard makeCopy() {
        return new ReachHeaven();
    }
}
