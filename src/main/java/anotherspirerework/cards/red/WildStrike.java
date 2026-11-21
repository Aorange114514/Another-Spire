package anotherspirerework.cards.red;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.actions.ExhaustRandomHandCardAction;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** Wild Strike: a heavy hit whose cost is a random card from the hand. */
public class WildStrike extends AbstractCard {
    public static final String ID = "Wild Strike";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public WildStrike() {
        super(ID, cardStrings.NAME, "red/attack/wild_strike", 2, cardStrings.DESCRIPTION,
                CardType.ATTACK, CardColor.RED, CardRarity.COMMON, CardTarget.ENEMY);
        this.baseDamage = 18;
        this.tags.add(CardTags.STRIKE);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn),
                AbstractGameAction.AttackEffect.SLASH_HEAVY));
        addToBot(new ExhaustRandomHandCardAction());
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeDamage(4);
        }
    }

    public AbstractCard makeCopy() {
        return new WildStrike();
    }
}
