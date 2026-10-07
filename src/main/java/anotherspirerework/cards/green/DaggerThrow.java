package anotherspirerework.cards.green;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.combat.ThrowDaggerEffect;

public class DaggerThrow extends AbstractCard {
    public static final String ID = "Dagger Throw";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public DaggerThrow() {
        super(ID, cardStrings.NAME, "green/attack/dagger_throw", 1, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.GREEN, CardRarity.COMMON, CardTarget.ENEMY);
        this.baseDamage = 9;
        this.baseMagicNumber = 1;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        if (m != null) {
            addToBot(new VFXAction(new ThrowDaggerEffect(m.hb.cX, m.hb.cY)));
        }
        addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn)));
        addToBot(new DrawCardAction(p, this.magicNumber));
        addToBot(new DiscardAction(p, p, this.magicNumber, false));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeDamage(3);
            upgradeMagicNumber(1);
        }
    }

    public AbstractCard makeCopy() {
        return new DaggerThrow();
    }
}
