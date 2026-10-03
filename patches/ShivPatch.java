package anotherspirerework.patches;

import anotherspirerework.powers.GlassKnifeMarkPower;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;

/** Glass Knife: gain Block whenever the enemy marked by it takes Shiv damage. */
@SpirePatch(clz = Shiv.class, method = "use", paramtypez = {AbstractPlayer.class, AbstractMonster.class})
public class ShivPatch {
    // ModTheSpire passes the arguments in the order: instance (if any), then the patched method's parameters.
    @SpirePostfixPatch
    public static void Postfix(Shiv __instance, AbstractPlayer p, AbstractMonster m) {
        if (m == null) {
            return;
        }
        AbstractPower mark = m.getPower(GlassKnifeMarkPower.POWER_ID);
        if (mark != null && mark.amount > 0) {
            AbstractDungeon.actionManager.addToBottom(new GainBlockAction(p, p, mark.amount));
        }
    }
}
