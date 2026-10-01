package anotherspire.cards.green;

import anotherspire.AnotherSpire;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** Dodge and Roll: unplayable, but a big pile of Block whenever it is discarded. */
public class DodgeAndRoll extends AbstractCard {
    public static final String ID = "Dodge and Roll";

    private static final CardStrings cardStrings = AnotherSpire.getCardStrings(ID);

    public DodgeAndRoll() {
        super(ID, cardStrings.NAME, "green/skill/dodge_and_roll", -2, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.GREEN, CardRarity.COMMON, CardTarget.SELF);
        this.baseBlock = 8;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
    }

    public boolean canUse(AbstractPlayer p, AbstractMonster m) {
        this.cantUseMessage = cardStrings.EXTENDED_DESCRIPTION[0];
        return false;
    }

    public void triggerOnManualDiscard() {
        applyPowers();
        AbstractDungeon.actionManager.addToBottom(new GainBlockAction(AbstractDungeon.player, AbstractDungeon.player, this.block));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBlock(3);
        }
    }

    public AbstractCard makeCopy() {
        return new DodgeAndRoll();
    }
}
