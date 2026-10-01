package anotherspire.cards.purple;

import anotherspire.AnotherSpire;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.watcher.ChangeStanceAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Weave extends AbstractCard {
    public static final String ID = "Weave";

    private static final CardStrings cardStrings = AnotherSpire.getCardStrings(ID);

    public Weave() {
        super(ID, cardStrings.NAME, "purple/attack/weave", 0, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.PURPLE, CardRarity.UNCOMMON, CardTarget.ENEMY);
        this.baseDamage = 4;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn), AbstractGameAction.AttackEffect.BLUNT_LIGHT));
        if (p.stance.ID.equals("Wrath")) {
            addToBot(new ChangeStanceAction("Calm"));
        } else if (p.stance.ID.equals("Calm")) {
            addToBot(new ChangeStanceAction("Wrath"));
        }
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeDamage(2);
        }
    }

    public AbstractCard makeCopy() {
        return new Weave();
    }
}
