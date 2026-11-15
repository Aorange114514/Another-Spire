package anotherspirerework.cards.green;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.actions.SetupSlyAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * Setup: put a card from hand on top of the draw pile, and that card gains Sly for the rest of the
 * fight. The vanilla card marks it free-to-play instead; this rework trades the cost cut for Sly.
 */
public class Setup extends AbstractCard {
    public static final String ID = "Setup";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Setup() {
        super(ID, cardStrings.NAME, "green/skill/setup", 1, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.GREEN, CardRarity.UNCOMMON, CardTarget.NONE);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new SetupSlyAction());
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(0);
        }
    }

    public AbstractCard makeCopy() {
        return new Setup();
    }
}
