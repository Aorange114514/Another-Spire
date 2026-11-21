package anotherspirerework.cards.blue;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.orbs.Dark;

/** Doom and Gloom: a Skill now; Block plus one Dark orb instead of the vanilla attack. */
public class DoomAndGloom extends AbstractCard {
    public static final String ID = "Doom and Gloom";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public DoomAndGloom() {
        super(ID, cardStrings.NAME, "blue/attack/doom_and_gloom", 2, cardStrings.DESCRIPTION,
                CardType.SKILL, CardColor.BLUE, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseBlock = 12;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new GainBlockAction(p, p, this.block));
        addToBot(new ChannelAction(new Dark()));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBlock(3);
        }
    }

    public AbstractCard makeCopy() {
        return new DoomAndGloom();
    }
}
