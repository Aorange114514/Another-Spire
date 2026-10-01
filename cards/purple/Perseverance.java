package anotherspire.cards.purple;

import anotherspire.AnotherSpire;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.watcher.ChangeStanceAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Perseverance extends AbstractCard {
    public static final String ID = "Perseverance";

    private static final CardStrings cardStrings = AnotherSpire.getCardStrings(ID);

    public Perseverance() {
        super(ID, cardStrings.NAME, "purple/skill/perseverance", 1, cardStrings.DESCRIPTION, CardType.SKILL, CardColor.PURPLE, CardRarity.UNCOMMON, CardTarget.SELF);
        this.baseBlock = 5;
        this.selfRetain = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ChangeStanceAction("Calm"));
    }

    public void onRetained() {
        addToBot(new GainBlockAction(AbstractDungeon.player, AbstractDungeon.player, this.block));
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBlock(2);
        }
    }

    public AbstractCard makeCopy() {
        return new Perseverance();
    }
}
