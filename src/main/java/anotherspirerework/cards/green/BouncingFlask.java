package anotherspirerework.cards.green;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.actions.BouncingFlaskXAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * Bouncing Flask: X energy buys X (X + 1 once upgraded) applications of Poison on random enemies.
 *
 * <p>The X is spent inside {@link BouncingFlaskXAction}, which the base game's own bouncing action
 * then repeats, so the poison lands on a random enemy every time.
 */
public class BouncingFlask extends AbstractCard {
    public static final String ID = "Bouncing Flask";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public BouncingFlask() {
        super(ID, cardStrings.NAME, "green/skill/bouncing_flask", -1, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.GREEN, CardRarity.UNCOMMON, CardTarget.ALL_ENEMY);
        this.baseMagicNumber = 6;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new BouncingFlaskXAction(p, this.magicNumber, this.upgraded, this.freeToPlayOnce, this.energyOnUse));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            this.rawDescription = cardStrings.UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }

    public AbstractCard makeCopy() {
        return new BouncingFlask();
    }
}
