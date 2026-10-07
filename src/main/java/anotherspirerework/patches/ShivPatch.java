package anotherspirerework.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import anotherspirerework.powers.ShivAllEnemiesPower;
import anotherspirerework.powers.ShivPoisonPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import anotherspirerework.actions.ShivDamageAction;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.powers.PoisonPower;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;

/** Resolve Shiv area attacks, poison and Glass Knife without duplicating vanilla damage. */
@SpirePatch(clz = Shiv.class, method = "use", paramtypez = {AbstractPlayer.class, AbstractMonster.class})
public class ShivPatch {
    // ModTheSpire passes the arguments in the order: instance (if any), then the patched method's parameters.
    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(Shiv __instance, AbstractPlayer p, AbstractMonster m) {
        // Explicitly prepare mastery before building damage, independent of patch ordering.
        ShivMasteryPatch.Prefix(__instance);
        if (p.hasPower(ShivAllEnemiesPower.POWER_ID)) {
            int previous = __instance.damage;
            try {
                __instance.applyPowers();
                __instance.calculateCardDamage(null);
                int index = 0;
                for (AbstractMonster enemy : AbstractDungeon.getMonsters().monsters) {
                    if (!enemy.isDeadOrEscaped()) {
                        __instance.damage = __instance.multiDamage[index];
                        attack(__instance, p, enemy);
                    }
                    index++;
                }
            } finally { __instance.damage = previous; }
        } else if (m != null) {
            __instance.calculateCardDamage(m);
            attack(__instance, p, m);
        }
        return SpireReturn.Return(null);
    }

    private static void attack(Shiv card, AbstractPlayer p, AbstractMonster m) {
        AbstractDungeon.actionManager.addToBottom(new ShivDamageAction(m,
                new DamageInfo(p, card.damage, card.damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
        AbstractPower poison = p.getPower(ShivPoisonPower.POWER_ID);
        if (poison != null) {
            AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m, p,
                    new PoisonPower(m, p, poison.amount), poison.amount));
        }
    }

    @SpirePatch(clz = AbstractCard.class, method = "applyPowers")
    public static class Targeting {
        @SpirePrefixPatch
        public static void before(AbstractCard __instance, @ByRef boolean[] ___isMultiDamage) {
            if (__instance instanceof Shiv) {
                boolean all = AbstractDungeon.player != null
                        && AbstractDungeon.player.hasPower(ShivAllEnemiesPower.POWER_ID);
                __instance.target = all ? AbstractCard.CardTarget.ALL_ENEMY : AbstractCard.CardTarget.ENEMY;
                ___isMultiDamage[0] = all;
            }
        }
    }
}
