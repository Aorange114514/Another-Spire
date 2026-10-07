package anotherspirerework.relics;

import anotherspirerework.AnotherSpireRework;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.curses.Pain;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.ui.campfire.AbstractCampfireOption;
import com.megacrit.cardcrawl.ui.campfire.LiftOption;
import java.util.ArrayList;

/** Vanilla IDs/art/acquisition are retained; only the requested behavior is replaced. */
public class ReworkedRelic extends AbstractRelic {
    private final AbstractRelic original;
    private int turns;
    public boolean bloodUsed;

    public ReworkedRelic(AbstractRelic original) {
        super(original.relicId, original.imgUrl, original.tier, LandingSound.CLINK);
        this.original = original;
        counter = original.counter;
    }

    public static boolean supports(String id) {
        return id.equals("PrismaticShard") || id.equals("Sling") || id.equals("HandDrill")
                || id.equals("CultistMask") || id.equals("Cauldron") || id.equals("Fusion Hammer")
                || id.equals("Mark of Pain") || id.equals("Velvet Choker") || id.equals("Ectoplasm")
                || id.equals("Black Blood") || id.equals("Busted Crown") || id.equals("SacredBark")
                || id.equals("Sozu") || id.equals("Ring of the Serpent") || id.equals("Girya") || id.equals("Black Star");
    }

    @Override
    public String getUpdatedDescription() {
        if ("CultistMask".equals(relicId)) {
            return DESCRIPTIONS[0];
        }
        return com.megacrit.cardcrawl.core.CardCrawlGame.languagePack
                .getRelicStrings(AnotherSpireRework.makeID(relicId)).DESCRIPTIONS[0];
    }

    @Override
    public AbstractRelic makeCopy() {
        return new ReworkedRelic(original.makeCopy());
    }

    @Override
    public boolean canSpawn() {
        return relicId.equals("Ectoplasm") ? AbstractDungeon.actNum <= 2 : original.canSpawn();
    }

    @Override
    public void onEquip() {
        anotherspirerework.patches.TurnRelicFixes.syncStar(this);
        if (relicId.equals("PrismaticShard")) {
            original.onEquip();
        } else if (relicId.equals("Fusion Hammer")) {
            AbstractDungeon.player.energy.energyMaster++;
            downgradeCards();
        } else if (relicId.equals("Mark of Pain") || relicId.equals("Velvet Choker") || relicId.equals("Sozu") || relicId.equals("SacredBark")) {
            AbstractDungeon.player.energy.energyMaster++;
        } else if (relicId.equals("Cauldron")) {
            anotherspirerework.patches.ReportedRelicFixes.cauldronEquip(this);
        }
    }

    @Override
    public void onUnequip() {
        if (relicId.equals("PrismaticShard")) {
            anotherspirerework.patches.TurnRelicFixes.prismEquip(-1);
        }
        if (relicId.equals("Fusion Hammer") || relicId.equals("Mark of Pain")
                || relicId.equals("Velvet Choker") || relicId.equals("Sozu") || relicId.equals("SacredBark")) {
            AbstractDungeon.player.energy.energyMaster--;
        }
    }

    @Override
    public void atBattleStart() {
        turns = 0;
        if (relicId.equals("Sozu")) anotherspirerework.patches.TurnRelicFixes.resetBattle();
        bloodUsed = false;
        if (relicId.equals("Velvet Choker")) counter = 0;
        if (relicId.equals("Sling")) {
            apply(new StrengthPower(AbstractDungeon.player, 2));
            apply(new DexterityPower(AbstractDungeon.player, 2));
        } else if (relicId.equals("CultistMask")) {
            original.atBattleStart();
        } else if (relicId.equals("Mark of Pain")) {
            anotherspirerework.patches.ReportedRelicFixes.markStart();
        } else if (relicId.equals("Black Blood")) {
            addToBot(new MakeTempCardInHandAction(new Pain()));
        } else if (relicId.equals("Girya") && counter > 0) {
            apply(new StrengthPower(AbstractDungeon.player, counter));
            apply(new DexterityPower(AbstractDungeon.player, counter));
        }
    }

    private void apply(AbstractPower power) {
        addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, power, power.amount));
    }

    @Override
    public void atTurnStart() {
        turns++;
        if (relicId.equals("Velvet Choker")) {
            anotherspirerework.patches.ReportedRelicFixes.chokerStart(this);
        } else if (relicId.equals("Ectoplasm") && AbstractDungeon.player.gold >= 5) {
            AbstractDungeon.player.loseGold(5);
            addToBot(new GainEnergyAction(1));
        } else if (relicId.equals("SacredBark")) {
            anotherspirerework.patches.ReportedRelicFixes.barkStart();
        } else if (relicId.equals("Ring of the Serpent")) {
            anotherspirerework.patches.TurnRelicFixes.serpentTurn(this, turns);
        } else if (relicId.equals("Black Star")) {
            anotherspirerework.patches.TurnRelicFixes.starTurn();
        }
    }

    /**
     * Wrapped relics replace the vanilla instance, so a vanilla hook this class does not forward
     * simply no longer happens. The Velvet Choker counter is one of those: the base game hides it
     * with {@code -1} at the end of every fight, and without that the number of cards played on the
     * last turn stays on the relic bar until the next battle starts.
     */
    @Override
    public void onVictory() {
        if (relicId.equals("Velvet Choker")) {
            counter = -1;
        }
    }

    @Override
    public void onPlayCard(AbstractCard card, com.megacrit.cardcrawl.monsters.AbstractMonster m) {
        if (relicId.equals("Velvet Choker")) {
            if (counter < Integer.MAX_VALUE) counter++;
        }
    }

    @Override
    public void addCampfireOption(ArrayList<AbstractCampfireOption> options) {
        if (relicId.equals("Girya")) {
            options.add(new LiftOption(counter < anotherspirerework.patches.NewRelicFixes.GIRYA_MAX_LIFTS));
        }
    }

    /** Rebuild one upgrade level without losing UUID, bottles or persistent misc. */
    private static void downgradeCards() {
        for (int i = 0; i < 4; i++) {
            ArrayList<AbstractCard> candidates = new ArrayList<>();
            for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
                if (c.upgraded && c.timesUpgraded > 0) {
                    candidates.add(c);
                }
            }
            if (candidates.isEmpty()) {
                break;
            }
            AbstractCard old = candidates.get(AbstractDungeon.miscRng.random(candidates.size() - 1));
            AbstractCard card = com.megacrit.cardcrawl.helpers.CardLibrary.getCard(old.cardID).makeCopy();
            // SearingBlow.makeCopy carries its upgrade count even though stats start at base.
            card.timesUpgraded = 0;
            card.upgraded = false;
            for (int n = 1; n < old.timesUpgraded; n++) {
                card.upgrade();
            }
            card.uuid = old.uuid;
            card.misc = old.misc;
            card.inBottleFlame = old.inBottleFlame;
            card.inBottleLightning = old.inBottleLightning;
            card.inBottleTornado = old.inBottleTornado;
            AbstractDungeon.player.masterDeck.group.set(AbstractDungeon.player.masterDeck.group.indexOf(old), card);
        }
    }
}