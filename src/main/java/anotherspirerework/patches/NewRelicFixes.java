package anotherspirerework.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.ExhaustSpecificCardAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.actions.defect.IncreaseMaxOrbAction;
import com.megacrit.cardcrawl.actions.unique.DiscoveryAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import com.megacrit.cardcrawl.powers.DexterityPower;
import com.megacrit.cardcrawl.powers.LoseDexterityPower;
import com.megacrit.cardcrawl.powers.LoseStrengthPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.relics.*;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Behavior of the relics that were reworked on top of their vanilla classes. The vanilla instances
 * are patched directly so a save that already holds one keeps working; the wrapper in
 * {@code anotherspirerework.relics.ReworkedRelic} calls the same helpers, so a relic never runs
 * both the old and the new effect.
 */
public class NewRelicFixes {

    /**
     * Relics that keep their vanilla effect but must never be offered inside the Ending, where the
     * Heart sits. The ones that already restrict themselves are listed too; being blocked here as
     * well is harmless.
     */
    private static final String[] ACT_FOUR_BLOCKED = {
            "White Beast Statue", "Darkstone Periapt", "PreservedInsect", "Omamori", "Smiling Mask",
            "Juzu Bracelet", "Ancient Tea Set", "Tiny Chest", "Dream Catcher", "Regal Pillow",
            "CeramicFish", "MealTicket", "Gremlin Horn", "Meat on the Bone", "Ninja Scroll",
            "Eternal Feather", "The Courier", "Question Card", "Singing Bowl", "StoneCalendar",
            "Old Coin", "Girya", "Peace Pipe", "The Specimen", "WingedGreaves", "Prayer Wheel", "Shovel"
    };

    /** Strike and Defend cards of every character, the ones Frozen Eye hands Ethereal to. */
    private static final String[] BASIC_ATTACK_AND_DEFEND = {
            "Strike_R", "Strike_G", "Strike_B", "Strike_P",
            "Defend_R", "Defend_G", "Defend_B", "Defend_P"
    };

    public static boolean inActFour() {
        return !Settings.isEndless && AbstractDungeon.actNum >= 4;
    }

    public static boolean actFourBlocked(String relicId) {
        return inActFour() && Arrays.asList(ACT_FOUR_BLOCKED).contains(relicId);
    }
    /*----------21. Ninja Scroll: one Shiv at the start of every turn.----------*/

    public static void ninjaScrollTurn(AbstractRelic relic) {
        relic.flash();
        toBot(new RelicAboveCreatureAction(AbstractDungeon.player, relic));
        toBot(new MakeTempCardInHandAction(new Shiv()));
    }

    /** The vanilla three-Shiv opening is replaced by the per-turn Shiv. */
    @SpirePatch(clz = NinjaScroll.class, method = "atBattleStartPreDraw")
    public static class NinjaScrollBattleStart {
        @SpirePrefixPatch public static SpireReturn<Void> before() { return SpireReturn.Return(null); }
    }

    /** Ninja Scroll does not declare atTurnStart itself, so the shared relic hook is used. */
    @SpirePatch(clz = AbstractRelic.class, method = "atTurnStart")
    public static class NinjaScrollTurn {
        @SpirePostfixPatch
        public static void after(AbstractRelic __instance) {
            if (__instance instanceof NinjaScroll) {
                ninjaScrollTurn(__instance);
            }
        }
    }

    /*----------22. Yang: Attacks give temporary Dexterity, Skills temporary Strength.----------*/

    public static void yangUse(AbstractRelic relic, AbstractCard card) {
        if (card == null) {
            return;
        }
        boolean attack = card.type == AbstractCard.CardType.ATTACK;
        boolean skill = card.type == AbstractCard.CardType.SKILL;
        if (!attack && !skill) {
            return;
        }
        relic.flash();
        toBot(new RelicAboveCreatureAction(AbstractDungeon.player, relic));
        if (attack) {
            toBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player,
                    new DexterityPower(AbstractDungeon.player, 1), 1));
            toBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player,
                    new LoseDexterityPower(AbstractDungeon.player, 1), 1));
        } else {
            toBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player,
                    new StrengthPower(AbstractDungeon.player, 1), 1));
            toBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player,
                    new LoseStrengthPower(AbstractDungeon.player, 1), 1));
        }
    }

    /** The vanilla Attack-only effect is replaced, the Skill half is added on top. */
    @SpirePatch(clz = Duality.class, method = "onUseCard")
    public static class YangUse {
        @SpirePrefixPatch
        public static SpireReturn<Void> before(Duality __instance, AbstractCard card, UseCardAction action) {
            yangUse(__instance, card);
            return SpireReturn.Return(null);
        }
    }

    /*----------24. Enchiridion: choose one of three Power cards at the start of combat.----------*/

    public static void enchiridionStart(AbstractRelic relic) {
        relic.flash();
        toBot(new RelicAboveCreatureAction(AbstractDungeon.player, relic));
        toBot(new DiscoveryAction(AbstractCard.CardType.POWER, 1));
    }

    /** Vanilla handed out one random Power card; the requirement wants the three-card choice. */
    @SpirePatch(clz = Enchiridion.class, method = "atPreBattle")
    public static class EnchiridionBattle {
        @SpirePrefixPatch
        public static SpireReturn<Void> before(Enchiridion __instance) {
            enchiridionStart(__instance);
            return SpireReturn.Return(null);
        }
    }

    /*----------25. / 26. Medical Kit and Blue Candle burning what is drawn.----------*/

    /** Cards drawn while the player was looking at a pile, handled once that screen is gone. */
    private static final ArrayList<AbstractCard> PENDING_DRAWS = new ArrayList<>();

    /**
     * Called for every card the player draws.
     *
     * <p>While a screen is open the work is remembered instead of done: both effects move cards
     * (exhaust one, draw another) and a deck view on screen is animating the very same objects.
     */
    public static void onCardDrawn(AbstractCard card) {
        if (card == null || AbstractDungeon.player == null) {
            return;
        }
        if (!isTracked(AbstractDungeon.player, card)) {
            return;
        }
        if (AbstractDungeon.isScreenUp || com.megacrit.cardcrawl.core.CardCrawlGame.isPopupOpen) {
            PENDING_DRAWS.add(card);
            return;
        }
        applyDrawnCard(card);
    }

    /** Called every frame; finishes what was drawn while a screen was open. */
    public static void processPendingDraws() {
        if (PENDING_DRAWS.isEmpty() || AbstractDungeon.player == null) {
            return;
        }
        if (AbstractDungeon.isScreenUp || com.megacrit.cardcrawl.core.CardCrawlGame.isPopupOpen) {
            return;
        }
        for (AbstractCard card : new ArrayList<>(PENDING_DRAWS)) {
            if (AbstractDungeon.player.hand.group.contains(card)) {
                applyDrawnCard(card);
            }
        }
        PENDING_DRAWS.clear();
    }

    /** True when the relic that handles this card type is held. */
    private static boolean isTracked(AbstractPlayer player, AbstractCard card) {
        if (card.type == AbstractCard.CardType.STATUS) {
            return player.getRelic("Medical Kit") != null;
        }
        return card.type == AbstractCard.CardType.CURSE && player.getRelic("Blue Candle") != null;
    }

    private static void applyDrawnCard(AbstractCard card) {
        if (card.type == AbstractCard.CardType.STATUS) {
            AbstractRelic kit = AbstractDungeon.player.getRelic("Medical Kit");
            if (kit == null) {
                return;
            }
            kit.flash();
            toBot(new ExhaustSpecificCardAction(card, AbstractDungeon.player.hand));
            toBot(new DrawCardAction(AbstractDungeon.player, 1));
        } else {
            AbstractRelic candle = AbstractDungeon.player.getRelic("Blue Candle");
            if (candle == null) {
                return;
            }
            candle.flash();
            toBot(new ExhaustSpecificCardAction(card, AbstractDungeon.player.hand));
        }
    }


    private static void toBot(com.megacrit.cardcrawl.actions.AbstractGameAction action) {
        AbstractDungeon.actionManager.addToBottom(action);
    }

    /*----------29. Frozen Eye: Strike and Defend become Ethereal.----------*/

    public static boolean isStrikeOrDefend(AbstractCard card) {
        return card != null && Arrays.asList(BASIC_ATTACK_AND_DEFEND).contains(card.cardID);
    }

    public static boolean frozenEyeOwned() {
        return AbstractDungeon.player != null && AbstractDungeon.player.hasRelic("Frozen Eye");
    }

    /** Marks one card Ethereal, text included, so the keyword is visible on the card. */
    public static void applyFrozenEye(AbstractCard card) {
        if (!isStrikeOrDefend(card) || card.isEthereal) {
            return;
        }
        card.isEthereal = true;
        card.rawDescription += " NL " + GameDictionary.ETHEREAL.NAMES[0] + keywordSuffix();
        card.initializeDescription();
    }

    public static void removeFrozenEye(AbstractCard card) {
        if (!isStrikeOrDefend(card) || !card.isEthereal) {
            return;
        }
        card.isEthereal = false;
        card.rawDescription = card.rawDescription
                .replace(" NL " + GameDictionary.ETHEREAL.NAMES[0] + keywordSuffix(), "");
        card.initializeDescription();
    }

    /**
     * The keyword has to be its own word: the description parser splits on spaces, so a Chinese
     * "虚无。" would be treated as one unknown word and never highlighted. Latin text is fine with
     * the period attached, the parser strips punctuation from the end of a word there.
     */
    public static String keywordSuffix(String languageName) {
        return languageName.equals("ZHS") || languageName.equals("ZHT") || languageName.equals("JPN")
                ? " 。" : ".";
    }

    private static String keywordSuffix() {
        return keywordSuffix(Settings.language.name());
    }

    /** Covers every basic Strike / Defend the player builds or picks up while the relic is held. */
    @SpirePatch(clz = AbstractCard.class, method = SpirePatch.CONSTRUCTOR,
            paramtypez = {String.class, String.class, String.class, int.class, String.class,
                    AbstractCard.CardType.class, AbstractCard.CardColor.class,
                    AbstractCard.CardRarity.class, AbstractCard.CardTarget.class})
    public static class FrozenEyeNewCards {
        @SpirePostfixPatch
        public static void after(AbstractCard __instance) {
            if (frozenEyeOwned()) {
                applyFrozenEye(__instance);
            }
        }
    }

    /** Ethereal cards are exhausted at the end of the player's turn; mirror that for our cards. */
    @SpirePatch(clz = AbstractCard.class, method = "triggerOnEndOfPlayerTurn")
    public static class FrozenEyeEndOfTurn {
        @SpirePrefixPatch
        public static void before(AbstractCard __instance) {
            if (!__instance.isEthereal && frozenEyeOwned() && isStrikeOrDefend(__instance)) {
                AbstractDungeon.actionManager.addToTop(
                        new ExhaustSpecificCardAction(__instance, AbstractDungeon.player.hand));
            }
        }
    }

    /** Cards already in the deck when the relic is picked up (or lost) are updated in place. */
    @SpirePatch(clz = AbstractRelic.class, method = "onEquip")
    public static class FrozenEyeEquip {
        @SpirePostfixPatch
        public static void after(AbstractRelic __instance) {
            if (__instance instanceof FrozenEye) {
                markEverywhere(true);
            }
        }
    }

    @SpirePatch(clz = AbstractRelic.class, method = "onUnequip")
    public static class FrozenEyeUnequip {
        @SpirePostfixPatch
        public static void after(AbstractRelic __instance) {
            if (__instance instanceof FrozenEye) {
                markEverywhere(false);
            }
        }
    }

    private static void markEverywhere(boolean ethereal) {
        if (AbstractDungeon.player == null) {
            return;
        }
        java.util.List<com.megacrit.cardcrawl.cards.CardGroup> groups = new ArrayList<>();
        groups.add(AbstractDungeon.player.masterDeck);
        groups.add(AbstractDungeon.player.hand);
        groups.add(AbstractDungeon.player.drawPile);
        groups.add(AbstractDungeon.player.discardPile);
        groups.add(AbstractDungeon.player.exhaustPile);
        for (com.megacrit.cardcrawl.cards.CardGroup group : groups) {
            if (group == null) {
                continue;
            }
            for (AbstractCard card : new ArrayList<>(group.group)) {
                if (ethereal) {
                    applyFrozenEye(card);
                } else {
                    removeFrozenEye(card);
                }
            }
        }
    }

    /*----------30. Inserter: one orb slot every turn.----------*/

    public static void inserterTurn(AbstractRelic relic) {
        relic.flash();
        toBot(new RelicAboveCreatureAction(AbstractDungeon.player, relic));
        toBot(new IncreaseMaxOrbAction(1));
    }

    /** Vanilla gained a slot every second turn; the requirement wants one every turn. */
    @SpirePatch(clz = Inserter.class, method = "atTurnStart")
    public static class InserterTurn {
        @SpirePrefixPatch
        public static SpireReturn<Void> before(Inserter __instance) {
            inserterTurn(__instance);
            return SpireReturn.Return(null);
        }
    }

    /*----------31. Matryoshka: draw a card whenever the draw pile is shuffled.----------*/

    /** Called by {@link ShuffleTriggerPatch} once the shuffle really finished. */
    public static void matryoshkaShuffle() {
        if (AbstractDungeon.player == null) {
            return;
        }
        AbstractRelic relic = AbstractDungeon.player.getRelic("Matryoshka");
        if (relic == null) {
            return;
        }
        relic.flash();
        toBot(new RelicAboveCreatureAction(AbstractDungeon.player, relic));
        toBot(new DrawCardAction(AbstractDungeon.player, 1));
    }

    /** The vanilla extra relic from non-boss chests is gone. */
    @SpirePatch(clz = Matryoshka.class, method = "onChestOpen")
    public static class MatryoshkaChest {
        @SpirePrefixPatch public static SpireReturn<Void> before() { return SpireReturn.Return(null); }
    }

    /** Matryoshka no longer counts chests, so its vanilla counter is hidden on pickup. */
    @SpirePatch(clz = AbstractRelic.class, method = "onEquip")
    public static class MatryoshkaEquip {
        @SpirePostfixPatch
        public static void after(AbstractRelic __instance) {
            if (__instance instanceof Matryoshka) {
                __instance.counter = -1;
            }
        }
    }

    /*----------23. Act four: hide a set of relics inside the Ending.----------*/

    /** Relics that do not override canSpawn themselves. */
    @SpirePatch(clz = AbstractRelic.class, method = "canSpawn")
    public static class ActFourBase {
        @SpirePostfixPatch
        public static boolean after(boolean __result, AbstractRelic __instance) {
            return __result && !actFourBlocked(__instance.relicId);
        }
    }

    /** Relics that do override canSpawn, so the base class patch never runs for them. */
    @SpirePatch(clz = DarkstonePeriapt.class, method = "canSpawn")
    @SpirePatch(clz = PreservedInsect.class, method = "canSpawn")
    @SpirePatch(clz = Omamori.class, method = "canSpawn")
    @SpirePatch(clz = SmilingMask.class, method = "canSpawn")
    @SpirePatch(clz = JuzuBracelet.class, method = "canSpawn")
    @SpirePatch(clz = AncientTeaSet.class, method = "canSpawn")
    @SpirePatch(clz = TinyChest.class, method = "canSpawn")
    @SpirePatch(clz = DreamCatcher.class, method = "canSpawn")
    @SpirePatch(clz = RegalPillow.class, method = "canSpawn")
    @SpirePatch(clz = CeramicFish.class, method = "canSpawn")
    @SpirePatch(clz = MealTicket.class, method = "canSpawn")
    @SpirePatch(clz = MeatOnTheBone.class, method = "canSpawn")
    @SpirePatch(clz = Courier.class, method = "canSpawn")
    @SpirePatch(clz = QuestionCard.class, method = "canSpawn")
    @SpirePatch(clz = SingingBowl.class, method = "canSpawn")
    @SpirePatch(clz = OldCoin.class, method = "canSpawn")
    @SpirePatch(clz = Girya.class, method = "canSpawn")
    @SpirePatch(clz = PeacePipe.class, method = "canSpawn")
    @SpirePatch(clz = PrayerWheel.class, method = "canSpawn")
    @SpirePatch(clz = Shovel.class, method = "canSpawn")
    @SpirePatch(clz = WingBoots.class, method = "canSpawn")
    public static class ActFourOverride {
        @SpirePrefixPatch
        public static SpireReturn<Boolean> before(AbstractRelic __instance) {
            return actFourBlocked(__instance.relicId) ? SpireReturn.Return(false) : SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = Inserter.class, method = "onEquip")
    public static class InserterNoCounter {
        @SpirePrefixPatch
        public static SpireReturn<Void> before(Inserter __instance) {
            __instance.counter = -1;
            return SpireReturn.Return(null);
        }
    }


    /*----------Text of the relics whose description we rewrote.----------*/

    @SpirePatch(clz = Duality.class, method = "getUpdatedDescription")
    @SpirePatch(clz = Enchiridion.class, method = "getUpdatedDescription")
    @SpirePatch(clz = MedicalKit.class, method = "getUpdatedDescription")
    @SpirePatch(clz = BlueCandle.class, method = "getUpdatedDescription")
    @SpirePatch(clz = StrangeSpoon.class, method = "getUpdatedDescription")
    @SpirePatch(clz = Orrery.class, method = "getUpdatedDescription")
    @SpirePatch(clz = FrozenEye.class, method = "getUpdatedDescription")
    @SpirePatch(clz = Inserter.class, method = "getUpdatedDescription")
    @SpirePatch(clz = Matryoshka.class, method = "getUpdatedDescription")
    @SpirePatch(clz = NinjaScroll.class, method = "getUpdatedDescription")
    public static class Description {
        @SpirePostfixPatch
        public static String after(String __result, AbstractRelic __instance) {
            return com.megacrit.cardcrawl.core.CardCrawlGame.languagePack.getRelicStrings(
                    anotherspirerework.AnotherSpireRework.makeID(__instance.relicId)).DESCRIPTIONS[0];
        }
    }

    /*----------27. Strange Spoon: on pickup, up to two cards lose their Exhaust.----------*/

    public static void spoonEquip(AbstractRelic relic) {
        relic.flash();
        anotherspirerework.effects.ChooseDeckCardsEffect.start(
                anotherspirerework.effects.ChooseDeckCardsEffect.Choice.REMOVE_EXHAUST,
                pickMessage(relic.relicId), 2);
    }

    /*----------28. Orrery: on pickup, any number of cards out of 10/10/10 join the deck.----------*/

    public static void orreryEquip(AbstractRelic relic) {
        relic.flash();
        anotherspirerework.effects.ChooseDeckCardsEffect.start(
                anotherspirerework.effects.ChooseDeckCardsEffect.Choice.ADD_TO_DECK,
                pickMessage(relic.relicId), 30);
    }

    /** The second line of our own RelicStrings is the header of the pick screen. */
    private static String pickMessage(String relicId) {
        com.megacrit.cardcrawl.localization.RelicStrings strings =
                com.megacrit.cardcrawl.core.CardCrawlGame.languagePack.getRelicStrings(
                        anotherspirerework.AnotherSpireRework.makeID(relicId));
        return strings != null && strings.DESCRIPTIONS != null && strings.DESCRIPTIONS.length > 1
                ? strings.DESCRIPTIONS[1] : "";
    }

    /** The vanilla four card rewards are replaced by the 30-card pick. */
    @SpirePatch(clz = Orrery.class, method = "onEquip")
    public static class OrreryEquip {
        @SpirePrefixPatch
        public static SpireReturn<Void> before(Orrery __instance) {
            orreryEquip(__instance);
            return SpireReturn.Return(null);
        }
    }

    /** Strange Spoon does not declare onEquip, so it is picked up from the shared hook. */
    @SpirePatch(clz = AbstractRelic.class, method = "onEquip")
    public static class SpoonEquip {
        @SpirePostfixPatch
        public static void after(AbstractRelic __instance) {
            if (__instance instanceof StrangeSpoon) {
                spoonEquip(__instance);
            }
        }
    }

    /*----------18. Girya: up to five lifts instead of three.----------*/

    public static final int GIRYA_MAX_LIFTS = 5;

    public static void giryaCampfireOptions(Girya relic,
            ArrayList<com.megacrit.cardcrawl.ui.campfire.AbstractCampfireOption> options) {
        options.add(new com.megacrit.cardcrawl.ui.campfire.LiftOption(relic.counter < GIRYA_MAX_LIFTS));
    }

    @SpirePatch(clz = Girya.class, method = "addCampfireOption")
    public static class GiryaLiftLimit {
        @SpirePrefixPatch
        public static SpireReturn<Void> before(Girya __instance,
                ArrayList<com.megacrit.cardcrawl.ui.campfire.AbstractCampfireOption> options) {
            giryaCampfireOptions(__instance, options);
            return SpireReturn.Return(null);
        }
    }

}
