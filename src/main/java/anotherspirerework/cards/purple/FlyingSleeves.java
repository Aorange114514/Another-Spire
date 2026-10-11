package anotherspirerework.cards.purple;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.combat.AnimatedSlashEffect;
import com.badlogic.gdx.graphics.Color;

/**
 * Flying Sleeves: Retain, then two quick slashes. The hits are counted in the magic number, so the
 * upgrade adds a swing instead of damage; the presentation is the base game's pair of slashes.
 */
public class FlyingSleeves extends AbstractCard {
    public static final String ID = "FlyingSleeves";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public FlyingSleeves() {
        super(ID, cardStrings.NAME, "purple/attack/flying_sleeves", 1, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.PURPLE, CardRarity.COMMON, CardTarget.ENEMY);
        this.baseDamage = 2;
        this.baseMagicNumber = 4;
        this.magicNumber = this.baseMagicNumber;
        this.selfRetain = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        for (int i = 0; i < this.magicNumber; i++) {
            addToBot(new SFXAction(i % 2 == 0 ? "ATTACK_WHIFF_2" : "ATTACK_WHIFF_1", 0.2F));
            addToBot(new SFXAction("ATTACK_FAST", 0.2F));
            if (m != null) {
                float direction = i % 2 == 0 ? 200.0F : -200.0F;
                addToBot(new VFXAction(new AnimatedSlashEffect(m.hb.cX, m.hb.cY - 30.0F * Settings.scale,
                        500.0F, direction, 290.0F, 3.0F, Color.VIOLET, Color.PINK)));
            }
            addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn), AbstractGameAction.AttackEffect.NONE));
        }
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(1);
        }
    }

    public AbstractCard makeCopy() {
        return new FlyingSleeves();
    }
}
