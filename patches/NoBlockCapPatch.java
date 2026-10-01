package anotherspire.patches;

import anotherspire.AnotherSpire;
import com.evacipated.cardcrawl.modthespire.lib.LineFinder;
import com.evacipated.cardcrawl.modthespire.lib.Matcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertLocator;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.core.AbstractCreature;
import javassist.CtBehavior;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Opcode;
import javassist.expr.Expr;
import javassist.expr.FieldAccess;

/**
 * Block is capped at 999 in the middle of AbstractCreature.addBlock: the method adds the block
 * (together with every relic and power modifier) and then throws away everything above the cap.
 *
 * The gained amount only exists inside that method, so an Insert patch grabs currentBlock from
 * inside the clamp branch, right before the clamp overwrites it, and the Postfix puts the real
 * amount back. Everything else in addBlock (the achievement checks, the gain animation) still
 * runs exactly as the base game wrote it. currentBlock is an int, so int is as far as block goes.
 */
@SpirePatch(clz = AbstractCreature.class, method = "addBlock", paramtypez = {int.class})
public class NoBlockCapPatch {
    /** The cap the base game applies. */
    private static final int CAP = 999;

    private static int uncapped;

    /** currentBlock as it was before the block was added, used to spot int overflow. */
    private static int before;

    @SpirePrefixPatch
    public static void Prefix(AbstractCreature __instance) {
        uncapped = 0;
        before = __instance.currentBlock;
    }

    /** Runs inside the clamp branch, where currentBlock still holds the amount about to be lost. */
    @SpireInsertPatch(locator = BlockCapLocator.class)
    public static void Insert(AbstractCreature __instance) {
        uncapped = __instance.currentBlock;
    }

    @SpirePostfixPatch
    public static void Postfix(AbstractCreature __instance) {
        if (__instance.currentBlock == CAP && uncapped > CAP) {
            __instance.currentBlock = uncapped;
        }
        uncapped = 0;
        // Adding block to an already huge block can wrap around into a negative value; saturate
        // instead, exactly like the power patches do.
        if (before > 0 && __instance.currentBlock < before) {
            __instance.currentBlock = Integer.MAX_VALUE;
        }
        before = 0;
    }

    public static class BlockCapLocator extends SpireInsertLocator {
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            return LineFinder.findInOrder(ctMethodToPatch, new BlockCapWriteMatcher());
        }
    }

    /**
     * Matches the one write of currentBlock that pushes 999 right before storing it, which is the
     * clamp itself. Matching the statement instead of a line number keeps the patch working even
     * if the surrounding code ever moves around.
     */
    public static class BlockCapWriteMatcher extends Matcher.FieldAccessMatcher {
        public BlockCapWriteMatcher() {
            super(AbstractCreature.class, "currentBlock");
        }

        public boolean match(Expr toMatch) {
            if (!(toMatch instanceof FieldAccess) || !super.match(toMatch)) {
                return false;
            }
            FieldAccess write = (FieldAccess) toMatch;
            if (!write.isWriter()) {
                return false;
            }
            try {
                CodeIterator code = write.where().getMethodInfo().getCodeAttribute().iterator();
                int previous = -1;
                while (code.hasNext()) {
                    int index = code.next();
                    if (index >= write.indexOfBytecode()) {
                        break;
                    }
                    previous = index;
                }
                return previous >= 0
                        && code.byteAt(previous) == Opcode.SIPUSH
                        && code.byteAt(previous + 1) == (CAP >>> 8)
                        && code.byteAt(previous + 2) == (CAP & 0xFF);
            } catch (Exception e) {
                AnotherSpire.logger.error("Could not read AbstractCreature.addBlock; block stays capped at " + CAP + ".", e);
                return false;
            }
        }
    }
}
