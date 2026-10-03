package anotherspirerework.patches;

import anotherspirerework.powers.FreeSkillPower;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

/**
 * Wheel Kick: makes freeToPlay() recognise the mod's FreeSkill power, the same way the base game
 * hard codes FreeAttackPower there for Swivel. This covers both the cost shown on the card and
 * the energy actually spent.
 */
@SpirePatch(clz = AbstractCard.class, method = "freeToPlay")
public class FreeSkillCardPatch {
    @SpirePostfixPatch
    public static boolean Postfix(boolean __result, AbstractCard __instance) {
        if (__result || __instance.type != AbstractCard.CardType.SKILL) {
            return __result;
        }
        if (AbstractDungeon.player == null || AbstractDungeon.currMapNode == null) {
            return false;
        }
        AbstractRoom room = AbstractDungeon.getCurrRoom();
        if (room == null || room.phase != AbstractRoom.RoomPhase.COMBAT) {
            return false;
        }
        return AbstractDungeon.player.hasPower(FreeSkillPower.POWER_ID);
    }
}
