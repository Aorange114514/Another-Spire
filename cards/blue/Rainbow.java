package anotherspire.cards.blue;

import anotherspire.AnotherSpire;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.orbs.Dark;
import com.megacrit.cardcrawl.orbs.Frost;
import com.megacrit.cardcrawl.orbs.Lightning;
import com.megacrit.cardcrawl.orbs.Plasma;
import com.megacrit.cardcrawl.vfx.RainbowCardEffect;

/**
 * Rainbow: channels all four orb types. Like the vanilla card it Exhausts, and upgrading it
 * takes the Exhaust off.
 */
public class Rainbow extends AbstractCard {
    public static final String ID = "Rainbow";

    private static final CardStrings cardStrings = AnotherSpire.getCardStrings(ID);

    public Rainbow() {
        super(ID, cardStrings.NAME, "blue/skill/rainbow", 2, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.BLUE, CardRarity.RARE, CardTarget.SELF);
        this.exhaust = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new VFXAction(new RainbowCardEffect()));
        addToBot(new ChannelAction(new Lightning()));
        addToBot(new ChannelAction(new Frost()));
        addToBot(new ChannelAction(new Dark()));
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
        return new Rainbow();
    }
}
