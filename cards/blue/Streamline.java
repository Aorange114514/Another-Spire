package anotherspire.cards.blue;

import anotherspire.AnotherSpire;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.ReduceCostAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Streamline extends AbstractCard {
    public static final String ID = "Streamline";

    private static final CardStrings cardStrings = AnotherSpire.getCardStrings(ID);

    public Streamline() {
        super(ID, cardStrings.NAME, "blue/attack/streamline", 3, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.BLUE, CardRarity.COMMON, CardTarget.ENEMY);
        this.baseDamage = 18;
        this.baseMagicNumber = 1;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_DIAGONAL));
    }

    public void triggerWhenDrawn() {
        addToBot(new ReduceCostAction(this.uuid, this.magicNumber));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeDamage(4);
        }
    }

    public AbstractCard makeCopy() {
        return new Streamline();
    }
}
