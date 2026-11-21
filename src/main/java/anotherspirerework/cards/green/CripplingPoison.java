package anotherspirerework.cards.green;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.util.SlyMark;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.PoisonPower;

/** Crippling Cloud: back to Uncommon, and now a Sly poison burst. */
public class CripplingPoison extends AbstractCard {
    public static final String ID = "Crippling Poison";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public CripplingPoison() {
        super(ID, cardStrings.NAME, "green/skill/crippling_poison", 3, cardStrings.DESCRIPTION,
                CardType.SKILL, CardColor.GREEN, CardRarity.UNCOMMON, CardTarget.ALL_ENEMY);
        this.baseMagicNumber = 4;
        this.magicNumber = this.baseMagicNumber;
        SlyMark.mark(this);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        if (AbstractDungeon.getMonsters().areMonstersBasicallyDead()) {
            return;
        }
        flash();
        for (AbstractMonster monster : AbstractDungeon.getMonsters().monsters) {
            if (!monster.isDead && !monster.isDying) {
                addToBot(new ApplyPowerAction(monster, p, new PoisonPower(monster, p, this.magicNumber), this.magicNumber));
            }
        }
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(2);
        }
    }

    public AbstractCard makeCopy() {
        return new CripplingPoison();
    }
}
