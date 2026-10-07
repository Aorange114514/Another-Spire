package anotherspirerework.cards.blue;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.actions.GashClawAction;
import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.combat.ClawEffect;

public class Claw extends AbstractCard {
    public static final String ID = "Gash";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Claw() {
        super(ID, cardStrings.NAME, "blue/attack/claw", 0, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.BLUE, CardRarity.COMMON, CardTarget.ENEMY);
        this.baseDamage = 1;
        this.baseMagicNumber = 2;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        if (m != null) {
            addToBot(new VFXAction(new ClawEffect(m.hb.cX, m.hb.cY, Color.CYAN, Color.WHITE), 0.1F));
        }
        for (int i = 0; i < this.magicNumber; i++) {
            addToBot(new DamageAction(m, new DamageInfo(p, this.damage, DamageInfo.DamageType.NORMAL), AbstractGameAction.AttackEffect.NONE));
        }
        addToBot(new GashClawAction(this, this.magicNumber));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(1);
        }
    }

    public AbstractCard makeCopy() {
        return new Claw();
    }
}
