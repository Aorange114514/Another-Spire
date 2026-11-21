package anotherspirerework.cards.purple;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** Halt: Block, plus a card drawn while the player is in Wrath. */
public class Halt extends AbstractCard {
    public static final String ID = "Halt";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Halt() {
        super(ID, cardStrings.NAME, "purple/skill/halt", 0, cardStrings.DESCRIPTION,
                CardType.SKILL, CardColor.PURPLE, CardRarity.COMMON, CardTarget.SELF);
        this.baseBlock = 5;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new GainBlockAction(p, p, this.block));
        if (inWrath()) {
            addToBot(new DrawCardAction(p, 1));
        }
    }

    private static boolean inWrath() {
        return AbstractDungeon.player != null && AbstractDungeon.player.stance != null
                && "Wrath".equals(AbstractDungeon.player.stance.ID);
    }

    public void triggerOnGlowCheck() {
        this.glowColor = inWrath()
                ? AbstractCard.GOLD_BORDER_GLOW_COLOR.cpy()
                : AbstractCard.BLUE_BORDER_GLOW_COLOR.cpy();
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBlock(3);
        }
    }

    public AbstractCard makeCopy() {
        return new Halt();
    }
}
