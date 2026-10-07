package anotherspirerework.cards.colorless;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.unique.AttackFromDeckToHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * Secret Weapon: pick one (two upgraded) Attack cards out of the draw pile. The base game
 * removes the Exhaust on upgrade, here the upgrade adds a card instead.
 */
public class SecretWeapon extends AbstractCard {
    public static final String ID = "Secret Weapon";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public SecretWeapon() {
        super(ID, cardStrings.NAME, "colorless/skill/secret_weapon", 0, cardStrings.DESCRIPTION,
                CardType.SKILL, CardColor.COLORLESS, CardRarity.RARE, CardTarget.NONE);
        this.baseMagicNumber = 1;
        this.magicNumber = this.baseMagicNumber;
        this.exhaust = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new AttackFromDeckToHandAction(this.magicNumber));
    }

    public boolean canUse(AbstractPlayer p, AbstractMonster m) {
        boolean canUse = super.canUse(p, m);
        if (!canUse) {
            return false;
        }
        for (AbstractCard c : p.drawPile.group) {
            if (c.type == CardType.ATTACK) {
                return true;
            }
        }
        this.cantUseMessage = AnotherSpireRework.extendedDescription(cardStrings, 0);
        return false;
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(1);
            this.rawDescription = cardStrings.UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }

    public AbstractCard makeCopy() {
        return new SecretWeapon();
    }
}
