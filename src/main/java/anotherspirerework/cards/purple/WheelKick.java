package anotherspirerework.cards.purple;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.FreeSkillPower;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** Wheel Kick: Wheel Kick plus a free Skill, the Skill counterpart of Swivel. */
public class WheelKick extends AbstractCard {
    public static final String ID = "WheelKick";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    private static final int DRAW_AMOUNT = 2;

    public WheelKick() {
        super(ID, cardStrings.NAME, "purple/attack/wheel_kick", 2, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.PURPLE, CardRarity.UNCOMMON, CardTarget.ENEMY);
        this.baseDamage = 15;
        this.baseMagicNumber = DRAW_AMOUNT;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn), AbstractGameAction.AttackEffect.BLUNT_HEAVY));
        addToBot(new DrawCardAction(p, DRAW_AMOUNT));
        addToBot(new ApplyPowerAction(p, p, new FreeSkillPower(p, 1), 1));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeDamage(5);
        }
    }

    public AbstractCard makeCopy() {
        return new WheelKick();
    }
}
