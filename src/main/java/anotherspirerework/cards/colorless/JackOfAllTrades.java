package anotherspirerework.cards.colorless;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.JackOfAllTradesPower;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * Jack Of All Trades: a Power now, paying energy back for energy spent.
 *
 * <p>Repeated copies each get their own power instance rather than stacking into one: the base game
 * only merges powers with the same ID, so the instance is added to the list directly. That is what
 * lets every copy show its own counter on its own icon.
 */
public class JackOfAllTrades extends AbstractCard {
    public static final String ID = "Jack Of All Trades";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public JackOfAllTrades() {
        super(ID, cardStrings.NAME, "colorless/skill/jack_of_all_trades", 2, cardStrings.DESCRIPTION, CardType.POWER, CardColor.COLORLESS, CardRarity.UNCOMMON, CardTarget.SELF);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        JackOfAllTradesPower power = new JackOfAllTradesPower(p);
        power.owner = p;
        p.powers.add(power);
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(1);
        }
    }

    public AbstractCard makeCopy() {
        return new JackOfAllTrades();
    }
}
