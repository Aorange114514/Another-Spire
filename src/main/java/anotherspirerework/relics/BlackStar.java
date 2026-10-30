package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.util.RelicChecks;
import basemod.abstracts.CustomSavable;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomElite;
import javassist.expr.ExprEditor;

/**
 * Black Star: once at least five Elites have been defeated this run, gain [E] every turn. The relic
 * counter shows the run's total Elite kills (saved and restored with the run). The vanilla extra
 * Elite relic drop is gone.
 *
 * <p>Replaces the vanilla prototype through {@code patches.RelicSwapPatch}.
 */
public class BlackStar extends com.megacrit.cardcrawl.relics.BlackStar {
    /** Kills needed before the relic pays out energy each turn. */
    public static final int ELITES_REQUIRED = 5;

    /** Elites killed this run, counted by the main class on every victory. */
    private static int elites;

    public BlackStar() {
        super();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new BlackStar();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /** Elite kills of the three acts, for runs saved before this mod kept its own counter. */
    public static int vanillaElites() {
        return CardCrawlGame.elites1Slain + CardCrawlGame.elites2Slain + CardCrawlGame.elites3Slain;
    }

    /** The counter's value: the mod's own count, or the vanilla one when it is higher. */
    public static int currentElites() {
        return Math.max(elites, vanillaElites());
    }

    /** True once the energy payout is active. */
    public static boolean active(int kills) {
        return anotherspirerework.util.RelicChecks.blackStarActive(kills);
    }

    /** Called from the main class after a combat room was won. */
    public static void onVictory(AbstractRoom room) {
        if (room instanceof MonsterRoomElite) {
            elites++;
        }
        syncOwnedStar();
    }

    /** A new run starts at zero; the vanilla counters are reset by the base game as well. */
    public static void resetRun() {
        elites = 0;
    }

    /** Shows the global Elite total on the given relic, using the vanilla counter. */
    public static void sync(AbstractRelic relic) {
        if (relic != null && ID.equals(relic.relicId)) {
            relic.counter = currentElites();
        }
    }

    static void syncOwnedStar() {
        if (AbstractDungeon.player == null) {
            return;
        }
        sync(AbstractDungeon.player.getRelic(ID));
    }

    @Override
    public void onEquip() {
        sync(this);
    }

    @Override
    public void atTurnStart() {
        syncOwnedStar();
        if (active(currentElites())) {
            addToBot(new GainEnergyAction(1));
        }
    }

    /** Reading the counter straight before drawing keeps the number right after a load. */
    @Override
    public void renderCounter(com.badlogic.gdx.graphics.g2d.SpriteBatch sb, boolean inTopPanel) {
        sync(this);
        super.renderCounter(sb, inTopPanel);
    }

    /** Vanilla dropped an extra relic on Elite kills; the new effect pays energy instead. */
    @SpirePatch(clz = MonsterRoomElite.class, method = "dropReward")
    public static class NoExtraRelic {
        @SpireInstrumentPatch
        public static ExprEditor edit() {
            return RelicChecks.ignoreHasRelic(ID);
        }
    }

    /** Save field registered by the main class; keeps the Elite count across save/load. */
    public static class SaveData implements CustomSavable<Integer> {
        @Override
        public Integer onSave() {
            return currentElites();
        }

        @Override
        public void onLoad(Integer value) {
            elites = value == null ? vanillaElites() : value;
            syncOwnedStar();
        }
    }
}
