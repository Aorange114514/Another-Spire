package anotherspirerework.cards.blue;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.NextTurnFocusPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.Frost;
import com.megacrit.cardcrawl.powers.FocusPower;

public class Blizzard extends AbstractCard {
    public static final String ID = "Blizzard";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Blizzard() {
        super(ID, cardStrings.NAME, "blue/attack/blizzard", 1, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.BLUE, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseMagicNumber = 2;
        this.magicNumber = this.baseMagicNumber;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        int frostCount = 0;
        for (AbstractOrb o : p.orbs) {
            if (o instanceof Frost) {
                frostCount++;
            }
        }
        int amount = frostCount * this.magicNumber;
        if (amount > 0) {
            addToBot(new ApplyPowerAction(p, p, new FocusPower(p, amount), amount));
            addToBot(new ApplyPowerAction(p, p, new NextTurnFocusPower(p, -amount), -amount));
        }
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(0);
            upgradeMagicNumber(1);
        }
    }

    public AbstractCard makeCopy() {
        return new Blizzard();
    }
}
