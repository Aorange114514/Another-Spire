package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

/**
 * Demon Form: every card in the exhaust pile makes the player's attacks hit harder.
 *
 * <p>The pile is counted live instead of being cached, so a card exhausted by the very attack that
 * is being resolved still counts for it, and the pile is never walked while nothing changed.
 */
public class DemonFormExhaustPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpireRework.makeID("DemonFormExhaust");
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("DemonFormExhaust");

    public DemonFormExhaustPower(AbstractCreature owner, int amount) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;
        updateDescription();
        loadRegion("demonForm");
    }

    /** Only real attacks grow; Thorns, Poison and HP loss keep their own numbers. */
    @Override
    public float atDamageGive(float damage, DamageInfo.DamageType type) {
        if (type != DamageInfo.DamageType.NORMAL) {
            return damage;
        }
        return damage + exhaustPileSize() * this.amount;
    }

    private static int exhaustPileSize() {
        return AbstractDungeon.player == null ? 0 : AbstractDungeon.player.exhaustPile.size();
    }

    @Override
    public void updateDescription() {
        this.description = powerStrings.DESCRIPTIONS[0] + this.amount + powerStrings.DESCRIPTIONS[1];
    }
}
