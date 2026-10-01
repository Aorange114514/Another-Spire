package anotherspire.cards.blue;

import anotherspire.AnotherSpire;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.combat.SweepingBeamEffect;

public class SweepingBeam extends AbstractCard {
    public static final String ID = "Sweeping Beam";

    private static final CardStrings cardStrings = AnotherSpire.getCardStrings(ID);

    public SweepingBeam() {
        super(ID, cardStrings.NAME, "blue/attack/sweeping_beam", 1, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.BLUE, CardRarity.COMMON, CardTarget.ALL_ENEMY);
        this.baseDamage = 6;
        this.baseMagicNumber = 2;
        this.magicNumber = this.baseMagicNumber;
        this.isMultiDamage = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new SFXAction("ATTACK_DEFECT_BEAM"));
        addToBot(new VFXAction(p, new SweepingBeamEffect(AbstractDungeon.player.hb.cX, AbstractDungeon.player.hb.cY, AbstractDungeon.player.flipHorizontal), 0.4F));
        addToBot(new DamageAllEnemiesAction(p, this.multiDamage, this.damageTypeForTurn, AbstractGameAction.AttackEffect.FIRE));
        addToBot(new DrawCardAction(p, this.magicNumber));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeDamage(1);
            upgradeMagicNumber(1);
        }
    }

    public AbstractCard makeCopy() {
        return new SweepingBeam();
    }
}
