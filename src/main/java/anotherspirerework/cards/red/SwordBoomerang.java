package anotherspirerework.cards.red;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.AttackDamageRandomEnemyAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class SwordBoomerang extends AbstractCard {
    public static final String ID = "Sword Boomerang";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public SwordBoomerang() {
        super(ID, cardStrings.NAME, "red/attack/sword_boomerang", 1, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.RED, CardRarity.COMMON, CardTarget.ALL_ENEMY);
        this.baseDamage = 3;
        this.baseMagicNumber = 3;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        for (int i = 0; i < this.magicNumber; i++) {
            addToBot(new AttackDamageRandomEnemyAction(this, AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
        }
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeDamage(1);
            upgradeMagicNumber(1);
        }
    }

    public AbstractCard makeCopy() {
        return new SwordBoomerang();
    }
}
