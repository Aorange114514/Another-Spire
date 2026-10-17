package anotherspirerework.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.watcher.ChangeStanceAction;
import com.megacrit.cardcrawl.actions.watcher.IndignationAction;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

/**
 * Base-game bug: IndignationAction queues its ChangeStanceAction at the bottom, behind the card's own
 * UseCardAction, so on-change-stance effects (Mental Fortress Block) resolve after the Heart's Beat of
 * Death. Queuing the stance change at the top gives the same order as Eruption: Block first.
 */
@SpirePatch(clz = IndignationAction.class, method = "update")
public class IndignationStancePatch {
    @SpireInstrumentPatch
    public static ExprEditor stanceFirst() {
        return new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                if (m.getMethodName().equals("addToBot")) {
                    m.replace("if ($1 instanceof " + ChangeStanceAction.class.getName() + ") { addToTop($1); }"
                            + " else { $_ = $proceed($$); }");
                }
            }
        };
    }
}
