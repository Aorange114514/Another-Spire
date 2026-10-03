package anotherspirerework.cards.green;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class RiddleWithHoles extends AbstractCard {
    public static final String ID = "Riddle With Holes";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public RiddleWithHoles() {
        super(ID, cardStrings.NAME, "green/attack/riddle_with_holes", 2, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.GREEN, CardRarity.UNCOMMON, CardTarget.ENEMY);
        this.baseDamage = 3;
        this.baseMagicNumber = 6;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        for (int i = 0; i < this.magicNumber; i++) {
            addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
        }
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(2);
        }
    }

    public AbstractCard makeCopy() {
        return new RiddleWithHoles();
    }
}
