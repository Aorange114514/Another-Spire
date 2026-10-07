package anotherspirerework.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import com.megacrit.cardcrawl.ui.buttons.SkipCardButton;

/**
 * Every in-combat card pick that deals in Colorless cards (the Discovery card, the Colorless
 * Potion, Toolbox, Nilry's Codex, Foreign Influence) goes through
 * {@code CardRewardScreen.customCombatOpen}, and the base game only shows the skip button when the
 * caller asks for it. Asking for it on every one of those screens adds the missing skip without
 * touching reward screens or the Watcher's choose-one screen, which are not part of this menu.
 */
public class ColorlessSkipPatch {

    @SpirePatch(clz = CardRewardScreen.class, method = "customCombatOpen")
    public static class AlwaysSkippable {
        @SpirePostfixPatch
        public static void after(CardRewardScreen __instance) {
            ReflectionHacks.setPrivate(__instance, CardRewardScreen.class, "skippable", true);
            SkipCardButton skipButton = ReflectionHacks.getPrivate(__instance, CardRewardScreen.class, "skipButton");
            if (skipButton != null) {
                skipButton.show();
            }
        }
    }
}
