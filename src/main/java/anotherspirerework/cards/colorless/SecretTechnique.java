package anotherspirerework.cards.colorless;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.unique.SkillFromDeckToHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * Secret Technique: pick one (two upgraded) Skill cards out of the draw pile. The base game
 * removes the Exhaust on upgrade, here the upgrade adds a card instead.
 */
public class SecretTechnique extends AbstractCard {
    public static final String ID = "Secret Technique";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public SecretTechnique() {
        super(ID, cardStrings.NAME, "colorless/skill/secret_technique", 0, cardStrings.DESCRIPTION,
                CardType.SKILL, CardColor.COLORLESS, CardRarity.RARE, CardTarget.NONE);
        this.baseMagicNumber = 1;
        this.magicNumber = this.baseMagicNumber;
        this.exhaust = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new SkillFromDeckToHandAction(this.magicNumber));
    }

    public boolean canUse(AbstractPlayer p, AbstractMonster m) {
        boolean canUse = super.canUse(p, m);
        if (!canUse) {
            return false;
        }
        for (AbstractCard c : p.drawPile.group) {
            if (c.type == CardType.SKILL) {
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
        return new SecretTechnique();
    }
}
