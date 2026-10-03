package anotherspirerework.actions;

import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.BorderFlashEffect;
import com.megacrit.cardcrawl.vfx.combat.SanctityEffect;

import java.util.ArrayList;

/**
 * Sanctity: gains energy instead of drawing cards when the previous card played this combat was
 * a Skill. Same timing check as the vanilla SanctityAction (which reads the card before this one).
 */
public class SanctityEnergyAction extends AbstractGameAction {
    private final int energyAmount;

    public SanctityEnergyAction(int energyAmount) {
        this.energyAmount = energyAmount;
    }

    @Override
    public void update() {
        ArrayList<AbstractCard> played = AbstractDungeon.actionManager.cardsPlayedThisCombat;
        if (played.size() >= 2 && played.get(played.size() - 2).type == AbstractCard.CardType.SKILL) {
            addToTop(new GainEnergyAction(this.energyAmount));
            addToTop(new VFXAction(new SanctityEffect(AbstractDungeon.player.hb.cX, AbstractDungeon.player.hb.cY)));
            addToTop(new SFXAction("HEAL_1"));
            addToTop(new VFXAction(new BorderFlashEffect(Color.GOLD, true), 0.1F));
        }
        this.isDone = true;
    }
}
