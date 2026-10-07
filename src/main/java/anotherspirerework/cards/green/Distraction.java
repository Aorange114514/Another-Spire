package anotherspirerework.cards.green;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.unique.DiscoveryAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Distraction extends AbstractCard {
    public static final String ID = "Distraction";
    private static final CardStrings strings = AnotherSpireRework.getCardStrings(ID);

    public Distraction() {
        super(ID, strings.NAME, "green/skill/distraction", 1, strings.DESCRIPTION,
                CardType.SKILL, CardColor.GREEN, CardRarity.UNCOMMON, CardTarget.NONE);
        exhaust = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) { addToBot(new DiscoveryAction(CardType.SKILL, 1)); }
    public void upgrade() { if (!upgraded) { upgradeName(); upgradeBaseCost(0); } }
    public AbstractCard makeCopy() { return new Distraction(); }
}