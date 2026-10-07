package anotherspirerework.patches;

import anotherspirerework.powers.MarkPower;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.LoseHPAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;

/** Path to Victory: marked enemies lose HP equal to their Mark whenever they are attacked. */
@SpirePatch(clz = AbstractMonster.class, method = "damage", paramtypez = {DamageInfo.class})
public class MarkAttackPatch {
    @SpirePostfixPatch
    public static void Postfix(AbstractMonster __instance, DamageInfo info) {
        if (info == null || info.type != DamageInfo.DamageType.NORMAL || __instance.isDeadOrEscaped()) {
            return;
        }
        AbstractPower mark = __instance.getPower(MarkPower.POWER_ID);
        if (mark != null && mark.amount > 0) {
            AbstractDungeon.actionManager.addToBottom(new LoseHPAction(__instance, null, mark.amount, AbstractGameAction.AttackEffect.FIRE));
        }
    }
}
