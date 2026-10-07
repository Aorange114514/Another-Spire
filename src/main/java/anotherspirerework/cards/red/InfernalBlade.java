package anotherspirerework.cards.red;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.unique.DiscoveryAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class InfernalBlade extends AbstractCard {
    public static final String ID = "Infernal Blade";
    private static final CardStrings strings = AnotherSpireRework.getCardStrings(ID);

    public InfernalBlade() {
        super(ID, strings.NAME, "red/skill/infernal_blade", 1, strings.DESCRIPTION,
                CardType.SKILL, CardColor.RED, CardRarity.UNCOMMON, CardTarget.NONE);
        exhaust = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) { addToBot(new DiscoveryAction(CardType.ATTACK, 1)); }
    public void upgrade() { if (!upgraded) { upgradeName(); upgradeBaseCost(0); } }
    public AbstractCard makeCopy() { return new InfernalBlade(); }
}