package anotherspirerework.patches;

import anotherspirerework.AnotherSpireRework;
import anotherspirerework.powers.ShivAllEnemiesPower;
import anotherspirerework.powers.ShivMasteryPower;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

/**
 * The two buffs that change what a Shiv does also rewrite its card text: Unload (Retain) adds the
 * Retain line, Die Die Die ("hits ALL enemies") replaces the damage line, and both lines show when
 * both buffs are up. The variants live in this mod's own CardStrings entry, so the vanilla Shiv
 * text is only read as the base and is never overwritten; without a buff the vanilla line comes
 * back. Every Shiv is refreshed when a buff is applied or removed, because the cards in the player's
 * piles outlive the buff and are not re-created afterwards.
 */
@SpirePatch(clz = AbstractCard.class, method = "initializeDescription")
public class ShivDescriptionPatch {
    /** EXTENDED_DESCRIPTION indices of the three rewritten Shiv descriptions. */
    public static final int RETAIN = 0;
    public static final int ALL_ENEMIES = 1;
    public static final int RETAIN_AND_ALL_ENEMIES = 2;

    @SpirePrefixPatch
    public static void Prefix(AbstractCard __instance) {
        if (__instance instanceof Shiv) {
            __instance.rawDescription = description();
        }
    }

    /** Rewrites one Shiv's text, for a card whose pile has to catch up with the current buffs. */
    public static void refresh(AbstractCard card) {
        if (card instanceof Shiv) {
            card.initializeDescription();
        }
    }

    /** Every Shiv the player owns: hand, draw pile, discard pile and exhaust pile. */
    public static void refreshAll() {
        if (AbstractDungeon.player == null) {
            return;
        }
        for (CardGroup group : new CardGroup[]{AbstractDungeon.player.hand, AbstractDungeon.player.drawPile,
                AbstractDungeon.player.discardPile, AbstractDungeon.player.exhaustPile}) {
            for (AbstractCard card : group.group) {
                refresh(card);
            }
        }
    }

    /** Which rewritten text the buffs ask for, or -1 for the vanilla one. */
    public static int variant(boolean retain, boolean allEnemies) {
        if (retain && allEnemies) {
            return RETAIN_AND_ALL_ENEMIES;
        }
        if (retain) {
            return RETAIN;
        }
        if (allEnemies) {
            return ALL_ENEMIES;
        }
        return -1;
    }

    private static String description() {
        if (AbstractDungeon.player == null) {
            return baseDescription();
        }
        boolean retain = isActive(AbstractDungeon.player.getPower(ShivMasteryPower.POWER_ID));
        boolean allEnemies = isActive(AbstractDungeon.player.getPower(ShivAllEnemiesPower.POWER_ID));
        int variant = variant(retain, allEnemies);
        if (variant < 0) {
            return baseDescription();
        }
        String text = AnotherSpireRework.extendedDescription(AnotherSpireRework.getCardStrings(Shiv.ID), variant);
        return text.isEmpty() ? baseDescription() : text;
    }

    /** A buff that is already being removed must no longer write its line. */
    private static boolean isActive(AbstractPower power) {
        if (power instanceof ShivMasteryPower) {
            return ((ShivMasteryPower) power).isActive();
        }
        if (power instanceof ShivAllEnemiesPower) {
            return ((ShivAllEnemiesPower) power).isActive();
        }
        return false;
    }

    /** The base game's Shiv line, so another mod's Shiv text keeps working as the base. */
    private static String baseDescription() {
        CardStrings vanilla = CardCrawlGame.languagePack.getCardStrings(Shiv.ID);
        return vanilla == null || vanilla.DESCRIPTION == null ? "" : vanilla.DESCRIPTION;
    }
}
