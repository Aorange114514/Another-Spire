package anotherspirerework.cards.red;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.combat.CleaveEffect;

public class Cleave extends AbstractCard {
    public static final String ID = "Cleave";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Cleave() {
        super(ID, cardStrings.NAME, "red/attack/cleave", 1, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.RED, CardRarity.COMMON, CardTarget.ALL_ENEMY);
        this.baseDamage = 11;
        this.isMultiDamage = true;
        this.exhaust = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new SFXAction("ATTACK_HEAVY"));
        addToBot(new VFXAction(p, new CleaveEffect(), 0.1F));
        addToBot(new DamageAllEnemiesAction(p, this.multiDamage, this.damageTypeForTurn, AbstractGameAction.AttackEffect.NONE));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeDamage(3);
        }
    }

    public AbstractCard makeCopy() {
        return new Cleave();
    }
}
