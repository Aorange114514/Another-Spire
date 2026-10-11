package anotherspirerework.powers;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.watcher.ChangeStanceAction;
import com.megacrit.cardcrawl.actions.watcher.NotStanceCheckAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.vfx.combat.EmptyStanceEffect;

/**
 * Empty Body: leaves the stance and gains Block at the end of every turn.
 *
 * <p>Block taken at the end of the turn survives the monster turn, which is what makes the effect
 * worth anything - the same timing Metallicize uses. The base game's own EmptyBody presentation
 * (the stance leaving effect, plus the check that stops it from playing while already Neutral) is
 * reused so the card still looks and sounds like itself.
 */
public class EmptyBodyPower extends AbstractPower {
    public static final String POWER_ID = AnotherSpireRework.makeID("EmptyBody");
    private static final PowerStrings powerStrings = AnotherSpireRework.getPowerStrings("EmptyBody");

    public EmptyBodyPower(AbstractCreature owner, int amount) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;
        updateDescription();
        loadRegion("no_stance");
    }

    @Override
    public void atEndOfTurn(boolean isPlayer) {
        if (!isPlayer) {
            return;
        }
        flash();
        addToBot(new NotStanceCheckAction("Neutral",
                new VFXAction(new EmptyStanceEffect(this.owner.hb.cX, this.owner.hb.cY), 0.1F)));
        addToBot(new ChangeStanceAction("Neutral"));
        addToBot(new GainBlockAction(this.owner, this.owner, this.amount));
    }

    @Override
    public void updateDescription() {
        this.description = powerStrings.DESCRIPTIONS[0] + this.amount + powerStrings.DESCRIPTIONS[1];
    }
}
