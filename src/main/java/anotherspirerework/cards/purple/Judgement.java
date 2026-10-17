package anotherspirerework.cards.purple;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.actions.PercentJudgementAction;
import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.combat.GiantTextEffect;
import com.megacrit.cardcrawl.vfx.combat.WeightyImpactEffect;

public class Judgement extends AbstractCard {
    public static final String ID = "Judgement";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Judgement() {
        super(ID, cardStrings.NAME, "purple/skill/judgment", 3, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.PURPLE, CardRarity.RARE, CardTarget.ENEMY);
        this.baseMagicNumber = 20;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        if (m != null) {
            addToBot(new VFXAction(new WeightyImpactEffect(m.hb.cX, m.hb.cY, Color.GOLD.cpy())));
            addToBot(new WaitAction(0.8F));
            addToBot(new VFXAction(new GiantTextEffect(m.hb.cX, m.hb.cY)));
        }
        addToBot(new PercentJudgementAction(m, this.magicNumber));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(5);
        }
    }

    public AbstractCard makeCopy() {
        return new Judgement();
    }
}
