package anotherspirerework.cards.blue;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.orbs.Plasma;
import com.megacrit.cardcrawl.vfx.combat.WeightyImpactEffect;

public class MeteorStrike extends AbstractCard {
    public static final String ID = "Meteor Strike";
    private static final CardStrings strings = AnotherSpireRework.getCardStrings(ID);

    public MeteorStrike() {
        super(ID, strings.NAME, "blue/attack/meteor_strike", 5, strings.DESCRIPTION,
                CardType.ATTACK, CardColor.BLUE, CardRarity.RARE, CardTarget.ENEMY);
        baseDamage = 30;
        magicNumber = baseMagicNumber = 3;
        tags.add(CardTags.STRIKE);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        if (m != null) {
            addToBot(new VFXAction(new WeightyImpactEffect(m.hb.cX, m.hb.cY)));
        }
        addToBot(new WaitAction(0.8F));
        addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn), AbstractGameAction.AttackEffect.NONE));
        for (int i = 0; i < magicNumber; i++) {
            addToBot(new ChannelAction(new Plasma()));
        }
    }

    public void upgrade() {
        if (!upgraded) {
            upgradeName();
            upgradeMagicNumber(1);
        }
    }

    public AbstractCard makeCopy() {
        return new MeteorStrike();
    }
}