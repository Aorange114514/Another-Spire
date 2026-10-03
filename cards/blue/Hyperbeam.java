package anotherspirerework.cards.blue;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.NextTurnFocusPower;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.FocusPower;
import com.megacrit.cardcrawl.vfx.combat.MindblastEffect;

public class Hyperbeam extends AbstractCard {
    public static final String ID = "Hyperbeam";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Hyperbeam() {
        super(ID, cardStrings.NAME, "blue/attack/hyper_beam", 2, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.BLUE, CardRarity.RARE, CardTarget.ALL_ENEMY);
        this.baseDamage = 24;
        this.baseMagicNumber = 3;
        this.magicNumber = this.baseMagicNumber;
        this.isMultiDamage = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new SFXAction("ATTACK_HEAVY"));
        addToBot(new VFXAction(p, new MindblastEffect(p.dialogX, p.dialogY, p.flipHorizontal), 0.1F));
        addToBot(new DamageAllEnemiesAction(p, this.multiDamage, this.damageTypeForTurn, AbstractGameAction.AttackEffect.NONE));
        addToBot(new ApplyPowerAction(p, p, new FocusPower(p, -this.magicNumber), -this.magicNumber));
        addToBot(new ApplyPowerAction(p, p, new NextTurnFocusPower(p, this.magicNumber), this.magicNumber));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeDamage(8);
        }
    }

    public AbstractCard makeCopy() {
        return new Hyperbeam();
    }
}
