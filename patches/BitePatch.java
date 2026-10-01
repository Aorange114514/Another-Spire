package anotherspire.patches;

import anotherspire.AnotherSpire;
import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.unique.VampireDamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.colorless.Bite;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.combat.BiteEffect;

/**
 * Bite is only ever created by the Vampires event (never through CardLibrary), so it is patched
 * in place instead of being swapped: "Deal 7 damage. Heal HP equal to the unblocked damage dealt."
 */
public class BitePatch {

    /** Keeps the patched card's static text in sync with our own strings. */
    public static void applyStrings() {
        try {
            ReflectionHacks.setPrivateStaticFinal(Bite.class, "cardStrings", AnotherSpire.getCardStrings("Bite"));
        } catch (Exception e) {
            AnotherSpire.logger.error("Failed to override Bite card strings.", e);
        }
    }

    /**
     * The reflection above only fixes what future Bite instances read from the class; this also
     * writes the text onto the instance, so the card is right no matter who created it.
     */
    @SpirePatch(clz = Bite.class, method = SpirePatch.CONSTRUCTOR)
    public static class StringPatch {
        @SpirePostfixPatch
        public static void Postfix(Bite __instance) {
            CardStrings strings = AnotherSpire.getCardStrings("Bite");
            if (strings == null) {
                return;
            }
            __instance.name = strings.NAME;
            __instance.originalName = strings.NAME;
            __instance.rawDescription = strings.DESCRIPTION;
            __instance.initializeDescription();
        }
    }

    @SpirePatch(clz = Bite.class, method = "use", paramtypez = {AbstractPlayer.class, AbstractMonster.class})
    public static class UsePatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(Bite __instance, AbstractPlayer p, AbstractMonster m) {
            if (m != null) {
                float dur = Settings.FAST_MODE ? 0.1F : 0.3F;
                AbstractDungeon.actionManager.addToBottom(new VFXAction(new BiteEffect(m.hb.cX, m.hb.cY - 40.0F * Settings.scale, Settings.GOLD_COLOR.cpy()), dur));
            }
            AbstractDungeon.actionManager.addToBottom(new VampireDamageAction(m, new DamageInfo(p, __instance.damage, __instance.damageTypeForTurn), AbstractGameAction.AttackEffect.NONE));
            return SpireReturn.Return();
        }
    }

    /** Vanilla upgrades give +1 damage / +1 (unused) heal; ours gives +2 damage instead. */
    @SpirePatch(clz = Bite.class, method = "upgrade")
    public static class UpgradePatch {
        @SpirePostfixPatch
        public static void Postfix(Bite __instance) {
            if (__instance.upgraded && __instance.timesUpgraded == 1) {
                __instance.baseDamage += 1;
            }
        }
    }
}

