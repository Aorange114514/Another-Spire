package anotherspirerework.cards.purple;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.utility.ScryAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.combat.FlickCoinEffect;

/** Just Lucky: Scry, Block, damage - and one extra card drawn once upgraded. */
public class JustLucky extends AbstractCard {
    public static final String ID = "JustLucky";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    private static final int DRAW_ON_UPGRADE = 1;

    public JustLucky() {
        super(ID, cardStrings.NAME, "purple/attack/just_lucky", 0, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.PURPLE, CardRarity.COMMON, CardTarget.ENEMY);
        this.baseDamage = 3;
        this.baseBlock = 2;
        this.baseMagicNumber = 1;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ScryAction(this.magicNumber));
        if (p != null && m != null) {
            addToBot(new VFXAction(new FlickCoinEffect(p.hb.cX, p.hb.cY, m.hb.cX, m.hb.cY), 0.3F));
        }
        addToBot(new GainBlockAction(p, p, this.block));
        addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn), AbstractGameAction.AttackEffect.NONE));
        if (this.upgraded) {
            addToBot(new DrawCardAction(p, DRAW_ON_UPGRADE));
        }
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            this.rawDescription = cardStrings.UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }

    public AbstractCard makeCopy() {
        return new JustLucky();
    }
}
