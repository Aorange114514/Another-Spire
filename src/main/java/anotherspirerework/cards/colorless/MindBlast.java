package anotherspirerework.cards.colorless;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.combat.MindblastEffect;

/**
 * Mind Blast: damage equal to the size of the deck, shown on the card as it changes.
 *
 * <p>Innate and cheaper once upgraded. The number comes from the whole deck rather than the draw
 * pile, and the extra description line is the base game's convention for showing a computed value.
 */
public class MindBlast extends AbstractCard {
    public static final String ID = "Mind Blast";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public MindBlast() {
        super(ID, cardStrings.NAME, "colorless/attack/mind_blast", 1, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.COLORLESS, CardRarity.UNCOMMON, CardTarget.ENEMY);
        this.baseDamage = 1;
        this.isInnate = true;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new VFXAction(new MindblastEffect(p.dialogX, p.dialogY, p.flipHorizontal)));
        addToBot(new DamageAction(m, new DamageInfo(p, this.damage, DamageInfo.DamageType.NORMAL), AbstractGameAction.AttackEffect.NONE));
        this.rawDescription = cardStrings.DESCRIPTION;
        initializeDescription();
    }

    @Override
    public void applyPowers() {
        this.baseDamage = AbstractDungeon.player == null ? 0 : AbstractDungeon.player.masterDeck.size();
        super.applyPowers();
        this.rawDescription = cardStrings.DESCRIPTION + AnotherSpireRework.extendedDescription(cardStrings, 0);
        initializeDescription();
    }

    @Override
    public void calculateCardDamage(AbstractMonster mo) {
        super.calculateCardDamage(mo);
        this.rawDescription = cardStrings.DESCRIPTION + AnotherSpireRework.extendedDescription(cardStrings, 0);
        initializeDescription();
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(0);
            initializeDescription();
        }
    }

    public AbstractCard makeCopy() {
        return new MindBlast();
    }
}
