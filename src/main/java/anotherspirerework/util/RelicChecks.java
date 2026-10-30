package anotherspirerework.util;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

/**
 * The little checks the per-relic classes share.
 *
 * <p>Every reworked relic is its own class in {@code anotherspirerework.relics}, replacing the
 * vanilla prototype through {@code patches.RelicSwapPatch}. Where the base game hard-codes a relic's
 * old behavior as a {@code hasRelic("...")} test somewhere else (gold gain, potion potency, ...),
 * the relic class carries a small patch of its own that calls {@link #ignoreHasRelic(String)}.
 */
public final class RelicChecks {
    private RelicChecks() {
    }

    /** True when the player holds a relic with this vanilla ID; safe outside a run. */
    public static boolean owned(String relicId) {
        return AbstractDungeon.player != null && AbstractDungeon.player.hasRelic(relicId);
    }

    /** True inside the Ending (act four), where a set of relics may not spawn. */
    public static boolean inActFour() {
        return !Settings.isEndless && AbstractDungeon.actNum >= 4;
    }

    /**
     * The Ethereal keyword has to be its own word: the description parser splits on spaces, so a
     * Chinese "虚无。" would be treated as one unknown word and never highlighted. Latin text is fine
     * with the period attached, the parser strips punctuation from the end of a word there.
     */
    public static String keywordSuffix(String languageName) {
        return languageName.equals("ZHS") || languageName.equals("ZHT") || languageName.equals("JPN")
                ? " 。" : ".";
    }

    /**
     * Removes the Exhaust keyword from a description. The keyword is written as its own word in
     * Chinese (" NL 消耗 。") and with the full stop attached in English (" NL Exhaust."), so both
     * spellings have to be handled or the card would keep advertising an effect it no longer has.
     */
    public static String withoutExhaustText(String description, String keyword) {
        String result = description;
        for (String suffix : new String[]{
                " NL " + keyword + "。", " NL " + keyword + " 。",
                " NL " + keyword + ".", " NL " + keyword + " .",
                keyword + "。", keyword + " 。", keyword + ".", keyword + " ."}) {
            result = result.replace(suffix, "");
        }
        return result.replace(" NL " + keyword, "");
    }

    /** True only on the first three turns of a combat (Ring of the Serpent). */
    public static boolean serpentTurnActive(int turn) {
        return turn >= 1 && turn <= 3;
    }

    /** True once at least five Elites were defeated this run (Black Star). */
    public static boolean blackStarActive(int kills) {
        return kills >= 5;
    }

    /** True while potions are locked: a fight is on, Sozu is held, nothing was discarded yet. */
    public static boolean sozuLocked(boolean combat, boolean sozu, boolean discarded) {
        return combat && sozu && !discarded;
    }

    /**
     * Loses 1 energy straight on the energy panel.
     *
     * <p>Going through the panel instead of a card-play path keeps the Velvet Choker and Sacred Bark
     * penalties from being mistaken for energy spent on a card by Jack Of All Trades.
     */
    public static void loseEnergy() {
        AbstractDungeon.actionManager.addToBottom(new AbstractGameAction() {
            @Override
            public void update() {
                AbstractDungeon.player.loseEnergy(1);
                this.isDone = true;
            }
        });
    }

    /**
     * An instrument editor that turns every {@code hasRelic("id")} check into {@code false}, so the
     * vanilla restriction the rewritten relic is supposed to drop no longer applies.
     */
    public static ExprEditor ignoreHasRelic(final String relicId) {
        return new ExprEditor() {
            @Override
            public void edit(MethodCall call) throws CannotCompileException {
                if (call.getMethodName().equals("hasRelic")) {
                    call.replace("{ $_ = $1.equals(\"" + relicId + "\") ? false : $proceed($$); }");
                }
            }
        };
    }
}
