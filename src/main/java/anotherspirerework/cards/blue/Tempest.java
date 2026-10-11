package anotherspirerework.cards.blue;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.unique.TempestAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * Tempest: the vanilla X-cost Lightning flood without the Exhaust.
 *
 * <p>The base game's own action already handles Chemical X and the upgraded extra orb; dropping
 * Exhaust is only a matter of not setting the flag on the card.
 */
public class Tempest extends AbstractCard {
    public static final String ID = "Tempest";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Tempest() {
        super(ID, cardStrings.NAME, "blue/skill/tempest", -1, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.BLUE, CardRarity.UNCOMMON, CardTarget.SELF);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new TempestAction(p, this.energyOnUse, this.upgraded, this.freeToPlayOnce));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            this.rawDescription = cardStrings.UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }

    public AbstractCard makeCopy() {
        return new Tempest();
    }
}
