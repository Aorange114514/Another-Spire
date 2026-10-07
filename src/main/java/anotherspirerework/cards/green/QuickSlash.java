package anotherspirerework.cards.green;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class QuickSlash extends AbstractCard {
    public static final String ID = "Quick Slash";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public QuickSlash() {
        super(ID, cardStrings.NAME, "green/attack/quick_slash", -2, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.GREEN, CardRarity.COMMON, CardTarget.ALL_ENEMY);
        this.baseDamage = 8;
        this.isMultiDamage = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
    }

    public boolean canUse(AbstractPlayer p, AbstractMonster m) {
        this.cantUseMessage = cardStrings.EXTENDED_DESCRIPTION[0];
        return false;
    }

    public void triggerOnManualDiscard() {
        applyPowers();
        AbstractDungeon.actionManager.addToBottom(new DamageAllEnemiesAction(AbstractDungeon.player, this.multiDamage, this.damageTypeForTurn, AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeDamage(4);
        }
    }

    public AbstractCard makeCopy() {
        return new QuickSlash();
    }
}
