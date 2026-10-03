package anotherspirerework.cards.purple;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.MarkPower;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.combat.PressurePointEffect;

public class PathToVictory extends AbstractCard {
    public static final String ID = "PathToVictory";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public PathToVictory() {
        super(ID, cardStrings.NAME, "purple/skill/pressure_points", 1, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.PURPLE, CardRarity.COMMON, CardTarget.ENEMY);
        this.baseMagicNumber = 8;
        this.magicNumber = this.baseMagicNumber;
        this.exhaust = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        if (m != null) {
            addToBot(new VFXAction(new PressurePointEffect(m.hb.cX, m.hb.cY)));
        }
        addToBot(new ApplyPowerAction(m, p, new MarkPower(m, this.magicNumber), this.magicNumber));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeMagicNumber(3);
        }
    }

    public AbstractCard makeCopy() {
        return new PathToVictory();
    }
}
