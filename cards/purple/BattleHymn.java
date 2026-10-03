package anotherspirerework.cards.purple;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.BattleHymnSmitePower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.tempCards.Smite;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class BattleHymn extends AbstractCard {
    public static final String ID = "BattleHymn";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public BattleHymn() {
        super(ID, cardStrings.NAME, "purple/power/battle_hymn", 1, cardStrings.DESCRIPTION, CardType.POWER, CardColor.PURPLE, CardRarity.UNCOMMON, CardTarget.SELF);
        this.cardsToPreview = new Smite();
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new BattleHymnSmitePower(p, 1), 1));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(0);
        }
    }

    public AbstractCard makeCopy() {
        return new BattleHymn();
    }
}
