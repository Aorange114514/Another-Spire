package anotherspirerework.patches;

import anotherspirerework.effects.ChooseDeckCardsEffect;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.EmptyDeckShuffleAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.LoseHPAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.actions.unique.PoisonLoseHpAction;
import com.megacrit.cardcrawl.actions.utility.ScryAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.PlatedArmorPower;
import com.megacrit.cardcrawl.powers.PoisonPower;
import com.megacrit.cardcrawl.relics.*;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomBoss;
import com.megacrit.cardcrawl.rooms.MonsterRoomElite;
import com.megacrit.cardcrawl.screens.CombatRewardScreen;
import com.megacrit.cardcrawl.stances.AbstractStance;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

/**
 * Behavior of the relics added in this round, plus the two whose tier changed.
 *
 * <p>Every one of them is a vanilla relic patched in place, so an existing save, the compendium and
 * the relic pools keep working; nothing new is registered.
 */
public class RareRelicFixes {

    /** Cards offered by Astrolabe. */
    public static final int ASTROLABE_CHOICES = 40;

    /** Twisted Funnel runs the poison tick this many extra times. */
    public static final int EXTRA_POISON_TICKS = 2;

    /** Card reward size used for the extra rare reward of Nloth's Gift. */
    private static final int RARE_REWARD_CARDS = 3;

    public static boolean playerHas(String relicId) {
        return AbstractDungeon.player != null && AbstractDungeon.player.hasRelic(relicId);
    }

    /*----------1. Prismatic Shard and 33. Tiny House changed tier.----------*/

    /** The pool a relic belongs in once the tier change is applied; null for every other relic. */
    public static AbstractRelic.RelicTier poolFor(String relicId) {
        if ("PrismaticShard".equals(relicId)) {
            return AbstractRelic.RelicTier.BOSS;
        }
        if ("Tiny House".equals(relicId)) {
            return AbstractRelic.RelicTier.SHOP;
        }
        return null;
    }

    /**
     * The tier is rewritten in the relic constructor, which is what the base game reads when it fills
     * the relic pools and when it sorts the compendium.
     */
    public static AbstractRelic.RelicTier tierFor(String relicId, AbstractRelic.RelicTier fallback) {
        AbstractRelic.RelicTier tier = poolFor(relicId);
        return tier == null ? fallback : tier;
    }

    /**
     * Whether a shop slot may offer this id. The two relics whose tier changed are compared against
     * their new tier, so a shop pool saved before the change cannot put a boss relic on the rug at
     * the boss price of #b999 gold.
     */
    public static boolean allowedInShop(String relicId, AbstractRelic.RelicTier tier) {
        return tierFor(relicId, tier) == AbstractRelic.RelicTier.SHOP;
    }

    /*----------1b. Pools of a run that was saved before the tier change.----------*/

    /**
     * A run in progress carries its relic pools around in the save file and {@code loadSave} reads
     * them back verbatim, so a run started before the tier change still holds Prismatic Shard in
     * {@code shopRelicPool} -- where the rewritten BOSS tier makes it cost #b999 gold -- and Tiny
     * House in {@code bossRelicPool}, out of reach of every shop. Both are moved to the pool their
     * tier says they belong in.
     *
     * <p>The new position is drawn from the run seed instead of {@code Math.random}, so saving and
     * reloading in front of a shop or a boss chest cannot be used to reroll the pool. An entry the
     * player already owns is dropped rather than offered a second time, and nothing is added twice.
     */
    public static void normalizePools(ArrayList<String> shop, ArrayList<String> boss,
            Collection<String> owned, boolean diverse, Random rng) {
        for (int i = shop.size() - 1; i >= 0; i--) {
            String id = shop.get(i);
            if (poolFor(id) == AbstractRelic.RelicTier.BOSS) {
                shop.remove(i);
                if (!diverse && !owned.contains(id) && !boss.contains(id)) {
                    boss.add(rng.nextInt(boss.size() + 1), id);
                }
            }
        }
        for (int i = boss.size() - 1; i >= 0; i--) {
            String id = boss.get(i);
            if (poolFor(id) == AbstractRelic.RelicTier.SHOP) {
                boss.remove(i);
                if (!owned.contains(id) && !shop.contains(id)) {
                    shop.add(rng.nextInt(shop.size() + 1), id);
                }
            }
        }
    }

    /** Repairs the two pools of the run being loaded, with the player's relics already known. */
    public static void normalizeRelicPools() {
        if (AbstractDungeon.player == null || AbstractDungeon.shopRelicPool == null
                || AbstractDungeon.bossRelicPool == null) {
            return;
        }
        ArrayList<String> owned = new ArrayList<>();
        for (AbstractRelic relic : AbstractDungeon.player.relics) {
            owned.add(relic.relicId);
        }
        long seed = com.megacrit.cardcrawl.core.Settings.seed == null
                ? 0L : com.megacrit.cardcrawl.core.Settings.seed;
        // The Diverse daily modifier hands out Prismatic Shard itself, and the base game keeps it
        // out of the pools there; read the flag defensively so a run without mod data still loads.
        boolean diverse = com.megacrit.cardcrawl.helpers.ModHelper.enabledMods != null
                && com.megacrit.cardcrawl.helpers.ModHelper.isModEnabled("Diverse");
        normalizePools(AbstractDungeon.shopRelicPool, AbstractDungeon.bossRelicPool, owned, diverse,
                new Random(seed));
    }

    /**
     * {@code loadSave} installs the pools straight from the save file. It runs inside the dungeon
     * constructor, after the player and its relics were rebuilt, which is what the ownership check
     * needs. A new run builds its pools from the relic tiers instead, so it needs no repair.
     */
    @SpirePatch(clz = AbstractDungeon.class, method = "loadSave")
    public static class LoadedPools {
        @SpirePostfixPatch
        public static void after() {
            normalizeRelicPools();
        }
    }

    /*----------32. Astrolabe: any 40 cards of any colour join the deck.----------*/

    @SpirePatch(clz = Astrolabe.class, method = "onEquip")
    public static class AstrolabeEquip {
        @SpirePrefixPatch
        public static SpireReturn<Void> before(Astrolabe __instance) {
            __instance.flash();
            ChooseDeckCardsEffect.start(ChooseDeckCardsEffect.Choice.ADD_ANY_COLOR,
                    cardPickMessage("Astrolabe"), ASTROLABE_CHOICES);
            return SpireReturn.Return(null);
        }
    }

    /*----------33. Tiny House: fighting is over, the campfire stays open (see TinyHouseCampfirePatch).----------*/

    @SpirePatch(clz = TinyHouse.class, method = "onEquip")
    public static class TinyHouseEquip {
        @SpirePrefixPatch
        public static SpireReturn<Void> before(TinyHouse __instance) {
            // The free campfire comes from TinyHouseCampfirePatch and the Rest Site pulse from its
            // RestRoomPulse hook, so the vanilla reward handout is skipped. The relic keeps the one
            // flash the base game gives every relic on pickup, and nothing flashes twice.
            return SpireReturn.Return(null);
        }
    }

    /*----------34. Violet Lotus: leaving Calm also draws a card.----------*/

    @SpirePatch(clz = VioletLotus.class, method = "onChangeStance")
    public static class LotusStance {
        @SpirePostfixPatch
        public static void after(VioletLotus __instance, AbstractStance prevStance, AbstractStance newStance) {
            if (prevStance != null && "Calm".equals(prevStance.ID) && newStance != null && !"Calm".equals(newStance.ID)) {
                AbstractDungeon.actionManager.addToBottom(new DrawCardAction(1));
            }
        }
    }

    /*----------35. Face of Cleric: two more max HP instead of one.----------*/

    @SpirePatch(clz = FaceOfCleric.class, method = "onVictory")
    public static class ClericVictory {
        @SpirePrefixPatch
        public static SpireReturn<Void> before() {
            AbstractDungeon.player.increaseMaxHp(2, true);
            return SpireReturn.Return(null);
        }
    }

    /*----------36. Snecko Eye: three extra cards per turn.----------*/

    public static final int SNECKO_HAND_SIZE = 3;

    @SpirePatch(clz = SneckoEye.class, method = "onEquip")
    public static class SneckoEquip {
        @SpirePrefixPatch
        public static SpireReturn<Void> before() {
            AbstractDungeon.player.masterHandSize += SNECKO_HAND_SIZE;
            return SpireReturn.Return(null);
        }
    }

    @SpirePatch(clz = SneckoEye.class, method = "onUnequip")
    public static class SneckoUnequip {
        @SpirePrefixPatch
        public static SpireReturn<Void> before() {
            AbstractDungeon.player.masterHandSize -= SNECKO_HAND_SIZE;
            return SpireReturn.Return(null);
        }
    }

    /** Saves made before this change only added two hand slots; the missing slot is granted once. */
    public static class SneckoMigration implements basemod.abstracts.CustomSavable<Boolean> {
        public Boolean onSave() {
            return true;
        }

        public void onLoad(Boolean migrated) {
            if (Boolean.TRUE.equals(migrated) || AbstractDungeon.player == null) {
                return;
            }
            if (AbstractDungeon.player.hasRelic("Snecko Eye")) {
                AbstractDungeon.player.masterHandSize += 1;
            }
        }
    }

    /*----------37. Twisted Funnel: two extra poison ticks.----------*/

    /** Vanilla poisoned every enemy at the start of the fight; only the extra ticks are left. */
    @SpirePatch(clz = TwistedFunnel.class, method = "atBattleStart")
    public static class FunnelBattleStart {
        @SpirePrefixPatch
        public static SpireReturn<Void> before() {
            return SpireReturn.Return(null);
        }
    }

    /** The extra ticks are appended to the one the base game already queued. */
    @SpirePatch(clz = PoisonPower.class, method = "atStartOfTurn")
    public static class FunnelTicks {
        @SpirePostfixPatch
        public static void after(PoisonPower __instance) {
            if (!playerHas("TwistedFunnel") || __instance.owner == null || __instance.owner.isPlayer) {
                return;
            }
            AbstractRoom room = AbstractDungeon.getCurrRoom();
            if (room == null || room.phase != AbstractRoom.RoomPhase.COMBAT
                    || AbstractDungeon.getMonsters().areMonstersBasicallyDead()) {
                return;
            }
            for (int i = 0; i < EXTRA_POISON_TICKS; i++) {
                AbstractDungeon.actionManager.addToBottom(new PoisonExtraTickAction(__instance.owner));
            }
        }
    }

    /** One more poison tick at the layer's current size, on top of the base game's own tick. */
    private static class PoisonExtraTickAction extends AbstractGameAction {
        PoisonExtraTickAction(AbstractCreature target) {
            this.target = target;
            this.actionType = ActionType.DAMAGE;
        }

        @Override
        public void update() {
            if (this.target != null && this.target.currentHealth > 0) {
                AbstractPower poison = this.target.getPower(PoisonPower.POWER_ID);
                if (poison != null && poison.amount > 0) {
                    addToTop(new PoisonLoseHpAction(this.target, this.target, poison.amount, AttackEffect.POISON));
                }
            }
            this.isDone = true;
        }
    }

    /** The health the poisoned bar shows: the vanilla tick plus the ticks this relic adds. */
    public static int previewPoison(int amount) {
        return anotherspirerework.util.SaturatingMath.poisonPreview(amount, EXTRA_POISON_TICKS);
    }

    /** Only the amount is ever read from the power the health bar asks for. */
    private static class PreviewPower extends AbstractPower {
    }

    private static final PreviewPower BAR_POWER = new PreviewPower();

    /** Swaps the power the health bar reads for one that carries the previewed amount. */
    public static AbstractPower poisonForBar(AbstractPower original) {
        if (original == null || !(original.owner instanceof AbstractMonster) || !playerHas("TwistedFunnel")) {
            return original;
        }
        int preview = previewPoison(original.amount);
        if (preview == original.amount) {
            return original;
        }
        BAR_POWER.amount = preview;
        return BAR_POWER;
    }

    @SpirePatch(clz = AbstractCreature.class, method = "renderRedHealthBar")
    public static class FunnelHealthBar {
        @SpireInstrumentPatch
        public static ExprEditor edit() {
            return new ExprEditor() {
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("getPower")
                            && m.getClassName().equals("com.megacrit.cardcrawl.core.AbstractCreature")) {
                        m.replace("{ $_ = anotherspirerework.patches.RareRelicFixes.poisonForBar((com.megacrit.cardcrawl.powers.AbstractPower)$proceed($$)); }");
                    }
                }
            };
        }
    }

    /*----------38. Melange: colourless cards give Block.----------*/

    public static final int MELANGE_BLOCK = 3;

    /** The vanilla scry on shuffle is gone; the relic is a Block source now. */
    @SpirePatch(clz = Melange.class, method = "onShuffle")
    public static class MelangeShuffle {
        @SpirePrefixPatch
        public static SpireReturn<Void> before() {
            return SpireReturn.Return(null);
        }
    }

    /** Insight and Miracle are colourless too; Status and Curses are not "colourless cards" here. */
    public static boolean isColourlessCard(AbstractCard card) {
        return card != null && card.color == AbstractCard.CardColor.COLORLESS
                && card.type != AbstractCard.CardType.STATUS && card.type != AbstractCard.CardType.CURSE;
    }

    @SpirePatch(clz = AbstractRelic.class, method = "onUseCard")
    public static class MelangeUse {
        @SpirePostfixPatch
        public static void after(AbstractRelic __instance, AbstractCard card, UseCardAction action) {
            if (!(__instance instanceof Melange) || !isColourlessCard(card)) {
                return;
            }
            __instance.flash();
            AbstractDungeon.actionManager.addToBottom(new RelicAboveCreatureAction(AbstractDungeon.player, __instance));
            AbstractDungeon.actionManager.addToBottom(new GainBlockAction(AbstractDungeon.player, AbstractDungeon.player, MELANGE_BLOCK));
        }
    }

    /*----------39. The Specimen: poison damage spreads to the other enemies.----------*/

    /** The vanilla poison transfer on death is replaced by the damage spread below. */
    @SpirePatch(clz = TheSpecimen.class, method = "onMonsterDeath")
    public static class SpecimenDeath {
        @SpirePrefixPatch
        public static SpireReturn<Void> before() {
            return SpireReturn.Return(null);
        }
    }

    @SpirePatch(clz = PoisonLoseHpAction.class, method = "update")
    public static class SpecimenSpread {
        @SpireInstrumentPatch
        public static ExprEditor edit() {
            return new ExprEditor() {
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("damage")
                            && m.getClassName().equals("com.megacrit.cardcrawl.core.AbstractCreature")) {
                        m.replace("{ $proceed($$); anotherspirerework.patches.RareRelicFixes.spreadPoison(this.target, (com.megacrit.cardcrawl.cards.DamageInfo)$1); }");
                    }
                }
            };
        }
    }

    /**
     * Makes every other enemy lose the health the poisoned one just lost.
     *
     * <p>The loss is plain HP loss, the same kind the base game uses for poison, so it cannot be
     * blocked and cannot start a chain of spreads of its own.
     */
    public static void spreadPoison(AbstractCreature victim, DamageInfo info) {
        if (victim == null || info == null || info.type != DamageInfo.DamageType.HP_LOSS
                || !playerHas("The Specimen") || AbstractDungeon.getCurrRoom() == null
                || AbstractDungeon.getMonsters() == null) {
            return;
        }
        int lost = info.output;
        if (lost <= 0) {
            return;
        }
        for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
            if (m == victim || m.isDead || m.isDying || m.currentHealth <= 0) {
                continue;
            }
            AbstractDungeon.actionManager.addToTop(new LoseHPAction(m, victim, lost, AbstractGameAction.AttackEffect.POISON));
        }
    }

    /*----------Second line of our own RelicStrings, used as a pick screen header.----------*/

    public static String cardPickMessage(String relicId) {
        String[] descriptions = CardCrawlGame.languagePack
                .getRelicStrings(anotherspirerework.AnotherSpireRework.makeID(relicId)).DESCRIPTIONS;
        String message = descriptions.length > 1 ? descriptions[1] : descriptions[0];
        return message == null ? "" : message;
    }

    /*----------40. Prayer Wheel and 43. Nloth's Gift: extra card rewards.----------*/

    /** Three distinct Rare cards out of the player's own pools. */
    public static RewardItem rareCardReward() {
        RewardItem item = new RewardItem();
        item.cards.clear();
        ArrayList<String> picked = new ArrayList<>();
        for (int guard = 0; item.cards.size() < RARE_REWARD_CARDS && guard < RARE_REWARD_CARDS * 20; guard++) {
            AbstractCard card = AbstractDungeon.getCard(AbstractCard.CardRarity.RARE);
            if (card != null && !picked.contains(card.cardID)) {
                picked.add(card.cardID);
                item.cards.add(card.makeCopy());
            }
        }
        for (AbstractCard c : item.cards) {
            for (AbstractRelic r : AbstractDungeon.player.relics) {
                r.onPreviewObtainCard(c);
            }
        }
        // The reward screen draws the gold card icon for a reward that calls itself a boss reward,
        // which is how the base game shows a Rare group; the elite drop has to look the same even
        // though it was not dropped by a boss.
        basemod.ReflectionHacks.setPrivate(item, RewardItem.class, "isBoss", true);
        return item;
    }

    /** Vanilla rolled a Rare three times as often; the requirement drops that bonus. */
    @SpirePatch(clz = NlothsGift.class, method = "changeRareCardRewardChance")
    public static class NlothsChance {
        @SpirePrefixPatch
        public static SpireReturn<Integer> before(NlothsGift __instance, int rareCardChance) {
            return SpireReturn.Return(rareCardChance);
        }
    }

    /**
     * Adds one more card reward after elite and boss fights (Prayer Wheel) and a Rare group after
     * elite fights (Nloth's Gift).
     *
     * <p>Both run as a postfix on the reward screen's own setup, so the extra entry is positioned
     * exactly like the rewards it joins. A boss room already rolls nothing but Rares, which is what
     * makes the Prayer Wheel reward there a second Rare group.
     */
    @SpirePatch(clz = CombatRewardScreen.class, method = "setupItemReward")
    public static class ExtraCardRewards {
        @SpirePostfixPatch
        public static void after(CombatRewardScreen __instance) {
            AbstractRoom room = AbstractDungeon.getCurrRoom();
            if (AbstractDungeon.player == null || room == null) {
                return;
            }
            if (room instanceof MonsterRoomBoss) {
                if (playerHas("Prayer Wheel")) {
                    addCardReward(__instance, new RewardItem());
                }
            } else if (room instanceof MonsterRoomElite) {
                if (playerHas("Prayer Wheel")) {
                    addCardReward(__instance, new RewardItem());
                }
                if (playerHas("Nloth's Gift")) {
                    addCardReward(__instance, rareCardReward());
                }
            }
        }

        private static void addCardReward(CombatRewardScreen screen, RewardItem item) {
            if (item.cards == null || item.cards.isEmpty()) {
                return;
            }
            screen.rewards.add(item);
            screen.positionRewards();
        }
    }

    /*----------41. Golden Eye: scry 3 every turn, and no extra scry from its own effect.----------*/

    public static final int GOLDEN_EYE_SCRY = 3;

    /** Vanilla added two cards to every scry; the relic is a fixed scry of its own now. */
    @SpirePatch(clz = ScryAction.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {int.class})
    public static class GoldenEyeNoBonus {
        @SpireInstrumentPatch
        public static ExprEditor edit() {
            return new ExprEditor() {
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("hasRelic")) {
                        m.replace("{ $_ = false; }");
                    }
                }
            };
        }
    }

    /**
     * The scry itself, shared by the opening hand and the start of every later turn.
     *
     * <p>Same timing as the base game's own scry power: an empty draw pile is shuffled first, so the
     * pick has something to work with. Package private on purpose: the nested patches below call it
     * directly, while a private method would compile to a synthetic accessor instead (the same
     * reason {@code ShuffleTriggerPatch.flush} is not private).
     */
    static void goldenEyeScry() {
        if (AbstractDungeon.player == null) {
            return;
        }
        AbstractRelic relic = AbstractDungeon.player.getRelic("GoldenEye");
        if (relic != null) {
            relic.flash();
        }
        if (AbstractDungeon.player.drawPile.size() <= 0) {
            AbstractDungeon.actionManager.addToTop(new EmptyDeckShuffleAction());
        }
        AbstractDungeon.actionManager.addToBottom(new ScryAction(GOLDEN_EYE_SCRY));
    }

    /**
     * True once the opening hand of this combat has been scried for, so the turn start that follows
     * it does not scry a second time. Package private for the same accessor reason as the method.
     */
    static boolean goldenEyeScriedBeforeDraw;

    /**
     * The first turn of a combat is special: the base game queues the opening draw before it runs the
     * start of turn relics, so scrying from atTurnStart would let the player draw first. Scrying here
     * instead puts the pick before the opening hand, which is the order every later turn already has.
     */
    @SpirePatch(clz = com.megacrit.cardcrawl.characters.AbstractPlayer.class, method = "applyStartOfCombatPreDrawLogic")
    public static class GoldenEyeOpeningScry {
        @SpirePrefixPatch
        public static void before() {
            goldenEyeScriedBeforeDraw = playerHas("GoldenEye");
            if (goldenEyeScriedBeforeDraw) {
                goldenEyeScry();
            }
        }
    }

    @SpirePatch(clz = AbstractRelic.class, method = "atTurnStart")
    public static class GoldenEyeTurn {
        @SpirePostfixPatch
        public static void after(AbstractRelic __instance) {
            if (!(__instance instanceof GoldenEye) || AbstractDungeon.player == null) {
                return;
            }
            if (goldenEyeScriedBeforeDraw) {
                // The opening hand has already been scried for this combat.
                goldenEyeScriedBeforeDraw = false;
                return;
            }
            goldenEyeScry();
        }
    }

    /*----------42. Thread and Needle: two Plated Armor every turn.----------*/

    public static final int THREAD_AND_NEEDLE_ARMOR = 2;

    /** Vanilla granted the armor once per combat; it is a per-turn grant now. */
    @SpirePatch(clz = ThreadAndNeedle.class, method = "atBattleStart")
    public static class NeedleBattleStart {
        @SpirePrefixPatch
        public static SpireReturn<Void> before() {
            return SpireReturn.Return(null);
        }
    }

    @SpirePatch(clz = AbstractRelic.class, method = "atTurnStart")
    public static class NeedleTurn {
        @SpirePostfixPatch
        public static void after(AbstractRelic __instance) {
            if (!(__instance instanceof ThreadAndNeedle)) {
                return;
            }
            __instance.flash();
            AbstractDungeon.actionManager.addToBottom(new RelicAboveCreatureAction(AbstractDungeon.player, __instance));
            AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player,
                    new PlatedArmorPower(AbstractDungeon.player, THREAD_AND_NEEDLE_ARMOR), THREAD_AND_NEEDLE_ARMOR));
        }
    }

    /*----------Text of the relics whose description this file owns.----------*/

    @SpirePatch(clz = Astrolabe.class, method = "getUpdatedDescription")
    @SpirePatch(clz = TinyHouse.class, method = "getUpdatedDescription")
    @SpirePatch(clz = VioletLotus.class, method = "getUpdatedDescription")
    @SpirePatch(clz = FaceOfCleric.class, method = "getUpdatedDescription")
    @SpirePatch(clz = SneckoEye.class, method = "getUpdatedDescription")
    @SpirePatch(clz = TwistedFunnel.class, method = "getUpdatedDescription")
    @SpirePatch(clz = Melange.class, method = "getUpdatedDescription")
    @SpirePatch(clz = TheSpecimen.class, method = "getUpdatedDescription")
    @SpirePatch(clz = PrayerWheel.class, method = "getUpdatedDescription")
    @SpirePatch(clz = GoldenEye.class, method = "getUpdatedDescription")
    @SpirePatch(clz = ThreadAndNeedle.class, method = "getUpdatedDescription")
    @SpirePatch(clz = NlothsGift.class, method = "getUpdatedDescription")
    public static class Description {
        @SpirePostfixPatch
        public static String after(String __result, AbstractRelic __instance) {
            return CardCrawlGame.languagePack.getRelicStrings(
                    anotherspirerework.AnotherSpireRework.makeID(__instance.relicId)).DESCRIPTIONS[0];
        }
    }

    /** The relic ids whose text this file owns; kept for the contract test. */
    public static final ArrayList<String> REWRITTEN_TEXTS = new ArrayList<>(Arrays.asList(
            "Astrolabe", "Tiny House", "VioletLotus", "FaceOfCleric", "Snecko Eye", "TwistedFunnel",
            "Melange", "The Specimen", "Prayer Wheel", "GoldenEye", "Thread and Needle", "Nloth's Gift"));
}
