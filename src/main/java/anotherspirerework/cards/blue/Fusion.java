package anotherspirerework.cards.blue;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.orbs.Plasma;

public class Fusion extends AbstractCard {
    public static final String ID = "Fusion";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Fusion() {
        super(ID, cardStrings.NAME, "blue/skill/fusion", 1, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.BLUE, CardRarity.UNCOMMON, CardTarget.SELF);
        this.exhaust = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ChannelAction(new Plasma()));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            this.exhaust = false;
            this.rawDescription = cardStrings.UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }

    public AbstractCard makeCopy() {
        return new Fusion();
    }
}
