package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.curses.Pain;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.AbstractRelic;

/**
 * Black Blood: the vanilla heal at the end of combat is gone. Instead, at the start of every combat
 * a Pain is shuffled into the hand, and the first Attack costing 2 or more played each combat heals
 * the unblocked damage it deals.
 *
 * <p>Replaces the vanilla prototype through {@code patches.RelicSwapPatch}. The heal has to read
 * the damage after it landed, which lives in the damage pipeline, so the three nested patches below
 * carry it; the per-combat state stays on the relic itself. {@link #reset()} clears it from the
 * main class between runs.
 */
public class BlackBlood extends com.megacrit.cardcrawl.relics.BlackBlood {
    /** The first qualifying Attack of the combat has already been marked. */
    private boolean used;
    /** True between the marked Attack being played and that card finishing its action. */
    private static boolean bloodActive;

    public BlackBlood() {
        super();
    }

    /** Called from the main class after a combat and at the start of a run. */
    public static void reset() {
        bloodActive = false;
    }

    @Override
    public AbstractRelic makeCopy() {
        return new BlackBlood();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** Vanilla healed 12 HP after every combat; the rework heals through the Pain/Attack loop. */
    @Override
    public void onVictory() {
    }

    @Override
    public void atBattleStart() {
        used = false;
        addToBot(new MakeTempCardInHandAction(new Pain()));
    }

    /** The card just played arms or disarms the heal; only the first qualifying Attack counts. */
    @SpirePatch(clz = AbstractPlayer.class, method = "useCard")
    public static class BloodCard {
        @SpirePrefixPatch
        public static void before(AbstractPlayer __instance, AbstractCard c) {
            bloodActive = false;
            AbstractRelic relic = AbstractDungeon.player.getRelic(ID);
            if (relic instanceof BlackBlood) {
                BlackBlood blood = (BlackBlood) relic;
                if (c.type == AbstractCard.CardType.ATTACK && c.costForTurn >= 2 && !blood.used) {
                    blood.used = true;
                    bloodActive = true;
                }
            }
        }
    }

    /** The heal reads the damage that actually got through the monster's Block. */
    @SpirePatch(clz = AbstractMonster.class, method = "damage")
    public static class BloodHeal {
        @SpirePostfixPatch
        public static void after(AbstractMonster __instance, DamageInfo info) {
            if (bloodActive && info.owner == AbstractDungeon.player
                    && info.type == DamageInfo.DamageType.NORMAL
                    && __instance.lastDamageTaken > 0 && AbstractDungeon.player.currentHealth > 0) {
                AbstractDungeon.player.heal(__instance.lastDamageTaken);
            }
        }
    }

    /** The marked card's action is complete, so no later damage of the same play can heal. */
    @SpirePatch(clz = UseCardAction.class, method = "update")
    public static class BloodFinish {
        @SpirePostfixPatch
        public static void after(UseCardAction __instance) {
            if (__instance.isDone) {
                bloodActive = false;
            }
        }
    }
}
