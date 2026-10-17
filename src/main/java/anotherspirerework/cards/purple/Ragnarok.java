package anotherspirerework.cards.purple;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.actions.RagnarokXAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * Ragnarok: X energy buys X * 2 damage, dealt X * 2 times to random enemies.
 *
 * <p>{@code baseDamage} is the damage per energy spent, so the card face can show what the current
 * energy would buy; {@link RagnarokXAction} repeats the base game's random hit and sets the real
 * damage for the play itself. The base game's hit count is kept in one place: {@link #MULTIPLIER}.
 */
public class Ragnarok extends AbstractCard {
    public static final String ID = "Ragnarok";

    /** Damage and hit count are both the energy spent multiplied by this. */
    public static final int MULTIPLIER = 2;

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public Ragnarok() {
        super(ID, cardStrings.NAME, "purple/attack/ragnarok", -1, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.PURPLE, CardRarity.RARE, CardTarget.ALL_ENEMY);
        this.baseDamage = MULTIPLIER;
        this.exhaust = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new RagnarokXAction(this, p, this.freeToPlayOnce, this.energyOnUse));
    }

    /** The base game runs this (and calculateCardDamage) to keep the card face honest. */
    @Override
    public void applyPowers() {
        int realBaseDamage = this.baseDamage;
        this.baseDamage = MULTIPLIER * RagnarokXAction.energyFor(this.energyOnUse);
        super.applyPowers();
        this.baseDamage = realBaseDamage;
        this.isDamageModified = this.damage != this.baseDamage;
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            this.exhaust = false;
            this.rawDescription = cardStrings.UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }

    public AbstractCard makeCopy() {
        return new Ragnarok();
    }
}
