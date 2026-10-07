package anotherspirerework.cards.purple;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

/** Follow-Up: costs 0 while in Wrath. */
public class FollowUp extends AbstractCard {
    public static final String ID = "FollowUp";

    private static final CardStrings cardStrings = AnotherSpireRework.getCardStrings(ID);

    public FollowUp() {
        super(ID, cardStrings.NAME, "purple/attack/follow_up", 1, cardStrings.DESCRIPTION, CardType.ATTACK, CardColor.PURPLE, CardRarity.COMMON, CardTarget.ENEMY);
        this.baseDamage = 7;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn), AbstractGameAction.AttackEffect.BLUNT_HEAVY));
    }

    /**
     * Wrath: this card costs 0. Overseeing freeToPlay() is how the base game makes the next
     * card free (see FreeAttackPower / Swivel), so it works for both the cost preview and the payment.
     */
    @Override
    public boolean freeToPlay() {
        return super.freeToPlay() || inWrath();
    }

    private static boolean inWrath() {
        if (AbstractDungeon.player == null || AbstractDungeon.currMapNode == null) {
            return false;
        }
        AbstractRoom room = AbstractDungeon.getCurrRoom();
        if (room == null || room.phase != AbstractRoom.RoomPhase.COMBAT) {
            return false;
        }
        return AbstractDungeon.player.stance != null && "Wrath".equals(AbstractDungeon.player.stance.ID);
    }

    public void triggerOnGlowCheck() {
        this.glowColor = inWrath() ? AbstractCard.GOLD_BORDER_GLOW_COLOR.cpy() : AbstractCard.BLUE_BORDER_GLOW_COLOR.cpy();
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeDamage(3);
        }
    }

    public AbstractCard makeCopy() {
        return new FollowUp();
    }
}
