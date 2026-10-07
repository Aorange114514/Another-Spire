package anotherspirerework.cards.green;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.GlassKnifeMarkPower;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class GlassKnife extends AbstractCard {
    public static final String ID = "Glass Knife";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public GlassKnife() {
        super(ID, cardStrings.NAME, "green/attack/glass_knife", 1, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.GREEN, CardRarity.RARE, CardTarget.ENEMY);
        this.baseDamage = 8;
        this.baseMagicNumber = 3;
        this.magicNumber = this.baseMagicNumber;
        this.exhaust = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_DIAGONAL));
        addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_HEAVY));
        addToBot(new ApplyPowerAction(m, p, new GlassKnifeMarkPower(m, this.magicNumber), this.magicNumber));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(2);
        }
    }

    public AbstractCard makeCopy() {
        return new GlassKnife();
    }
}
