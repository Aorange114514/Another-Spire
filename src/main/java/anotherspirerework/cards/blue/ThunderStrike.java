package anotherspirerework.cards.blue;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.AttackDamageRandomEnemyAction;
import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.Lightning;

/**
 * Thunder Strike: for every Lightning orb channeled earlier in this combat, channel a new
 * Lightning orb and hit a random enemy. The count is frozen before the first channel, so the
 * orbs this card creates do not feed the loop.
 */
public class ThunderStrike extends AbstractCard {
    public static final String ID = "Thunder Strike";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public ThunderStrike() {
        super(ID, cardStrings.NAME, "blue/attack/thunder_strike", 3, cardStrings.DESCRIPTION,
                CardType.ATTACK, CardColor.BLUE, CardRarity.RARE, CardTarget.ALL_ENEMY);
        this.baseDamage = 3;
        this.baseMagicNumber = 0;
        this.magicNumber = 0;
        this.tags.add(CardTags.STRIKE);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        int orbs = lightningChanneledThisCombat();
        for (int i = 0; i < orbs; i++) {
            addToBot(new ChannelAction(new Lightning()));
            addToBot(new AttackDamageRandomEnemyAction(this, AbstractGameAction.AttackEffect.LIGHTNING));
        }
    }

    /** The base game tracks every orb channeled this combat, which is exactly what the card counts. */
    private static int lightningChanneledThisCombat() {
        int count = 0;
        for (AbstractOrb orb : AbstractDungeon.actionManager.orbsChanneledThisCombat) {
            if (orb instanceof Lightning) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void applyPowers() {
        super.applyPowers();
        showChanneledCount();
    }

    @Override
    public void calculateCardDamage(AbstractMonster mo) {
        super.calculateCardDamage(mo);
        showChanneledCount();
    }

    /** Shows the live Lightning count on the card, the way the base game does for its own version. */
    private void showChanneledCount() {
        int count = lightningChanneledThisCombat();
        this.baseMagicNumber = count;
        this.magicNumber = count;
        if (count > 0) {
            this.rawDescription = cardStrings.DESCRIPTION + AnotherSpireRework.extendedDescription(cardStrings, 0);
            initializeDescription();
        }
    }

    @Override
    public void onMoveToDiscard() {
        this.rawDescription = cardStrings.DESCRIPTION;
        initializeDescription();
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(2);
        }
    }

    public AbstractCard makeCopy() {
        return new ThunderStrike();
    }
}
