package anotherspire.cards.green;

import anotherspire.AnotherSpire;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.PoisonPower;
import com.megacrit.cardcrawl.powers.WeakPower;

/** Crippling Cloud (vanilla id: Crippling Poison): a big Poison + Weak burst, now Rare. */
public class CripplingPoison extends AbstractCard {
    public static final String ID = "Crippling Poison";

    private static final CardStrings cardStrings = AnotherSpire.getCardStrings(ID);

    private static final int WEAK_AMOUNT = 3;

    public CripplingPoison() {
        super(ID, cardStrings.NAME, "green/skill/crippling_poison", 3, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.GREEN, CardRarity.RARE, CardTarget.ALL_ENEMY);
        this.baseMagicNumber = 12;
        this.magicNumber = this.baseMagicNumber;
        this.exhaust = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        if (AbstractDungeon.getMonsters().areMonstersBasicallyDead()) {
            return;
        }
        flash();
        for (AbstractMonster monster : AbstractDungeon.getMonsters().monsters) {
            if (!monster.isDead && !monster.isDying) {
                addToBot(new ApplyPowerAction(monster, p, new PoisonPower(monster, p, this.magicNumber), this.magicNumber));
                addToBot(new ApplyPowerAction(monster, p, new WeakPower(monster, WEAK_AMOUNT, false), WEAK_AMOUNT));
            }
        }
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(2);
        }
    }

    public AbstractCard makeCopy() {
        return new CripplingPoison();
    }
}
