package anotherspirerework.cards.green;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.util.SlyMark;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * Sneaky Strike: hit the enemy with the least HP, the way the Dark orb picks its target. The card
 * chooses that enemy itself, so it is ALL_ENEMY and the game never asks the player for a target.
 */
public class SneakyStrike extends AbstractCard {
    public static final String ID = "Underhanded Strike";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public SneakyStrike() {
        super(ID, cardStrings.NAME, "green/attack/sneaky_strike", 3, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.GREEN, CardRarity.COMMON, CardTarget.ALL_ENEMY);
        this.baseDamage = 16;
        this.tags.add(CardTags.STRIKE);
        SlyMark.mark(this);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        AbstractMonster weakest = weakestMonster();
        if (weakest == null) {
            return;
        }
        calculateCardDamage(weakest);
        addToBot(new DamageAction(weakest, new DamageInfo(p, this.damage, this.damageTypeForTurn), AbstractGameAction.AttackEffect.BLUNT_LIGHT));
    }

    /** The same target choice the Dark orb makes: the first living monster with the least HP. */
    private static AbstractMonster weakestMonster() {
        AbstractMonster weakest = null;
        for (AbstractMonster monster : AbstractDungeon.getMonsters().monsters) {
            if (monster.isDeadOrEscaped()) {
                continue;
            }
            if (weakest == null || monster.currentHealth < weakest.currentHealth) {
                weakest = monster;
            }
        }
        return weakest;
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeDamage(4);
        }
    }

    public AbstractCard makeCopy() {
        return new SneakyStrike();
    }
}
