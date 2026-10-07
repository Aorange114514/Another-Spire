package anotherspirerework.cards.red;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.combat.SearingBlowEffect;

public class SearingBlow extends AbstractCard {
    public static final String ID = "Searing Blow";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public SearingBlow() {
        this(0);
    }

    public SearingBlow(int upgrades) {
        super(ID, cardStrings.NAME, "red/attack/searing_blow", 2, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.RED, CardRarity.UNCOMMON, CardTarget.ENEMY);
        this.baseDamage = 10;
        this.baseBlock = 10;
        this.timesUpgraded = upgrades;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        if (m != null) {
            addToBot(new VFXAction(new SearingBlowEffect(m.hb.cX, m.hb.cY, this.timesUpgraded), 0.2F));
        }
        addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn), AbstractGameAction.AttackEffect.BLUNT_HEAVY));
        addToBot(new GainBlockAction(p, p, this.block));
    }

    public void upgrade() {
        upgradeDamage(3 + this.timesUpgraded);
        upgradeBlock(3 + this.timesUpgraded);
        this.timesUpgraded++;
        this.upgraded = true;
        this.name = cardStrings.NAME + "+" + this.timesUpgraded;
        initializeTitle();
    }

    public boolean canUpgrade() {
        return true;
    }

    public AbstractCard makeCopy() {
        return new SearingBlow(this.timesUpgraded);
    }
}
