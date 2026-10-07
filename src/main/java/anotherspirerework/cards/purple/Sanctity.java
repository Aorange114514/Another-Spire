package anotherspirerework.cards.purple;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.actions.SanctityEnergyAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** Sanctity: pays out energy instead of cards when the previous card was a Skill. */
public class Sanctity extends AbstractCard {
    public static final String ID = "Sanctity";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Sanctity() {
        super(ID, cardStrings.NAME, "purple/skill/sanctity", 1, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.PURPLE, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseBlock = 6;
        this.baseMagicNumber = 1;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new GainBlockAction(p, p, this.block));
        addToBot(new SanctityEnergyAction(this.magicNumber));
    }

    public void triggerOnGlowCheck() {
        if (!AbstractDungeon.actionManager.cardsPlayedThisCombat.isEmpty()
                && AbstractDungeon.actionManager.cardsPlayedThisCombat.get(AbstractDungeon.actionManager.cardsPlayedThisCombat.size() - 1).type == CardType.SKILL) {
            this.glowColor = AbstractCard.GOLD_BORDER_GLOW_COLOR.cpy();
        } else {
            this.glowColor = AbstractCard.BLUE_BORDER_GLOW_COLOR.cpy();
        }
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBlock(2);
            upgradeMagicNumber(1);
            this.rawDescription = cardStrings.UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }

    public AbstractCard makeCopy() {
        return new Sanctity();
    }
}
