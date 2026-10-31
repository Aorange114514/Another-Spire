package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.badlogic.gdx.Gdx;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.CampfireUI;
import com.megacrit.cardcrawl.rooms.RestRoom;
import com.megacrit.cardcrawl.ui.campfire.AbstractCampfireOption;
import com.megacrit.cardcrawl.ui.campfire.RecallOption;

/**
 * Tiny House: at a Rest Site you may use any number of options. The vanilla pickup rewards (a card
 * upgrade, 5 Max HP, 50 Gold and a potion) are dropped, and the relic is a shop relic now instead of
 * a boss relic.
 *
 * <p>Like every reworked relic it replaces the vanilla prototype through
 * {@code patches.RelicSwapPatch}, keeping the vanilla ID, art and acquisition path; the class also
 * carries the Rest Site patches below, because the free campfire is a room behavior rather than a
 * relic hook.
 *
 * <p>Nothing about the options themselves is reimplemented. The base game already resolves each of
 * them (rest heals 30% + 15 and triggers {@code onRest}, the smith opens its grid, Toke, Lift, Dig
 * and Recall play their own effects) and each of those effects marks the room complete when it
 * finishes; the only thing that ends a visit there is the campfire being hidden. So this class marks
 * the option that was just used as spent and, once the screens and the effect are gone, calls
 * {@code CampfireUI.reopen()} - the same call the base game makes when a card choice is cancelled -
 * and marks the room complete so the map is still the way out.
 *
 * <p>The reopen is deferred: every vanilla option plays an effect (black screen, grid, reward
 * screen), and putting the campfire back on top of those would fight them. The work runs from
 * {@code receivePostDungeonUpdate}, like the mod's other deferred work.
 */
public class TinyHouse extends com.megacrit.cardcrawl.relics.TinyHouse {
    public static final String RELIC_ID = "Tiny House";

    /** Grace period after an option settles, so its fade-in is over before the campfire returns. */
    public static final float REOPEN_DELAY = 0.3F;

    /** Never wait longer than this, even if an option's effect did not settle the room. */
    public static final float FORCE_AFTER = 3.0F;

    private static boolean pendingReopen = false;
    private static float delay = 0.0F;
    private static float stuck = 0.0F;

    /** The option armed by the last click, so a cancelled choice can be handed back. */
    private static AbstractCampfireOption lastUsed = null;

    /** Set by the two hooks below; only used to notice the frame a click was consumed. */
    private static boolean selectedBefore = false;
    private static AbstractCampfireOption touchedBefore = null;

    /** The Rest Site's Recall button, as soon as one has updated; any other option locks it. */
    private static AbstractCampfireOption recallOption = null;

    /**
     * True when the pending arm is what locked Recall. Cancelling the pick then hands Recall back;
     * a Recall locked by an option that was actually used earlier must stay locked.
     */
    private static boolean recallLockedByArm = false;

    public TinyHouse() {
        super();
        this.tier = AbstractRelic.RelicTier.SHOP;
    }

    @Override
    public AbstractRelic makeCopy() {
        return new TinyHouse();
    }

    @Override
    public String getUpdatedDescription() {
        return AnotherSpireRework.getRelicDescription(relicId);
    }

    /**
     * The vanilla reward handout is dropped: the free campfire comes from the Rest Site patches
     * below and the Rest Site pulse from {@code RestRoomPulse}, so there is nothing to give here.
     * The relic keeps the one flash the base game gives every relic on pickup, and nothing flashes
     * twice.
     */
    @Override
    public void onEquip() {
    }

    /** Package private so the nested patches reach it directly rather than through a javac bridge. */
    static boolean owned() {
        return AbstractDungeon.player != null && AbstractDungeon.player.hasRelic(RELIC_ID);
    }

    static CampfireUI campfire() {
        if (!(AbstractDungeon.getCurrRoom() instanceof RestRoom)) {
            return null;
        }
        return ((RestRoom) AbstractDungeon.getCurrRoom()).campfireUI;
    }

    static void arm(AbstractCampfireOption option) {
        // One use per option per campfire: the campfire comes back, the spent option does not.
        option.usable = false;
        if (option instanceof RecallOption) {
            // Recall ends the visit the vanilla way: no reopen, the options stay gone.
            pendingReopen = false;
            lastUsed = null;
            recallLockedByArm = false;
            return;
        }
        // Recall and the other options are mutually exclusive for Tiny House. Remember whether this
        // very click is what locks Recall: cancelling the pick must reopen Recall, while a Recall
        // locked by an option used earlier has to stay locked.
        recallLockedByArm = recallOption != null && recallOption.usable;
        if (recallLockedByArm) {
            recallOption.usable = false;
        }
        lastUsed = option;
        pendingReopen = true;
        delay = REOPEN_DELAY;
        stuck = 0.0F;
    }

    /** Anything left over from a previous Rest Site must not follow the player. */
    public static void reset() {
        pendingReopen = false;
        delay = 0.0F;
        stuck = 0.0F;
        lastUsed = null;
        touchedBefore = null;
        recallOption = null;
        recallLockedByArm = false;
    }

    /**
     * Same presentation as The Courier and the Membership Card in a shop: one flash on arrival, then
     * the relic keeps pulsing for as long as the room lasts, and stops the moment another room is
     * entered. {@code onEnterRoom} runs on every map transition for every relic, so it is the one
     * hook that also sees the player leave.
     */
    @SpirePatch(clz = AbstractRelic.class, method = "onEnterRoom")
    public static class RestRoomPulse {
        @SpirePostfixPatch
        public static void after(AbstractRelic __instance, AbstractRoom room) {
            if (!RELIC_ID.equals(__instance.relicId)) {
                return;
            }
            if (room instanceof RestRoom) {
                __instance.beginLongPulse();
                __instance.flash();
            } else {
                __instance.stopPulse();
            }
        }
    }

    /**
     * Mouse and controller use: {@code useOption()} is followed by
     * {@code campfireUI.somethingSelected = true}, which is the only reliable signal that this
     * option was the one used this frame.
     */
    @SpirePatch(clz = AbstractCampfireOption.class, method = "update")
    public static class MarkUsed {
        @SpirePrefixPatch
        public static void before() {
            CampfireUI ui = campfire();
            selectedBefore = ui != null && ui.somethingSelected;
        }

        @SpirePostfixPatch
        public static void after(AbstractCampfireOption __instance) {
            if (__instance instanceof RecallOption) {
                recallOption = __instance;
            }
            CampfireUI ui = campfire();
            if (ui == null || !owned() || selectedBefore == ui.somethingSelected) {
                return;
            }
            arm(__instance);
        }
    }

    /** Touch screens confirm through {@code CampfireUI.updateTouchscreen}, not through the option. */
    @SpirePatch(clz = CampfireUI.class, method = "updateTouchscreen")
    public static class MarkUsedByTouch {
        @SpirePrefixPatch
        public static void before(CampfireUI __instance) {
            selectedBefore = __instance.somethingSelected;
            touchedBefore = __instance.touchOption;
        }

        @SpirePostfixPatch
        public static void after(CampfireUI __instance) {
            AbstractCampfireOption option = touchedBefore;
            touchedBefore = null;
            if (option == null || selectedBefore == __instance.somethingSelected || !owned()) {
                return;
            }
            arm(option);
        }
    }

    /**
     * Cancelling a choice (the Campfire grid's cancel button calls {@code reopen()} itself) must hand
     * the option back: nothing was forged or removed, so it was not spent, and Recall - which the
     * click grayed out - becomes clickable again. The reopen this class performs itself has already
     * cleared {@code pendingReopen}, so it never lands here.
     */
    @SpirePatch(clz = CampfireUI.class, method = "reopen")
    public static class CancelledChoice {
        @SpirePostfixPatch
        public static void after() {
            if (!pendingReopen) {
                return;
            }
            pendingReopen = false;
            if (lastUsed != null) {
                lastUsed.usable = true;
            }
            // Clicking the option also grayed Recall out; cancelling means nothing was used, so the
            // Recall button is clickable again. Recall that was already locked by an option used
            // before this click stays locked.
            if (recallLockedByArm && recallOption != null) {
                recallOption.usable = true;
            }
            recallLockedByArm = false;
            lastUsed = null;
        }
    }

    /** Puts the campfire back once the option's effect and any screen it opened are finished. */
    public static void tick() {
        if (!pendingReopen) {
            return;
        }
        RestRoom room = AbstractDungeon.getCurrRoom() instanceof RestRoom
                ? (RestRoom) AbstractDungeon.getCurrRoom() : null;
        if (room == null || room.campfireUI == null) {
            reset();
            return;
        }
        float delta = Gdx.graphics.getDeltaTime();
        stuck += delta;
        if (AbstractDungeon.isScreenUp || AbstractDungeon.isFadingOut) {
            return;
        }
        // The vanilla effects mark the room complete when they are done; Smith and Toke only do so
        // once the campfire is hidden, so either signal counts as "settled".
        boolean settled = room.phase == AbstractRoom.RoomPhase.COMPLETE || CampfireUI.hidden;
        if (settled) {
            delay -= delta;
            if (delay > 0.0F) {
                return;
            }
        } else if (stuck < FORCE_AFTER) {
            return;
        }
        pendingReopen = false;
        room.campfireUI.reopen();
        lastUsed = null;
        recallLockedByArm = false;
        room.phase = AbstractRoom.RoomPhase.COMPLETE;
        AbstractRoom.waitTimer = 0.0F;
    }
}
