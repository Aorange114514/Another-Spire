# AnotherSpire

A Slay the Spire mod that reworks 81 vanilla cards (Ironclad / Silent / Defect / Watcher / Colorless).
The full list of changes lives in `../requirements.txt`.

## How it works

* Every reworked card is an `AbstractCard` subclass of our own (`anotherspire.cards.<color>.<Name>`) that keeps the
  **original card ID** and the **original art path**, so card pools, starting decks, shops, rewards, transforms,
  the compendium and existing save files all keep working without any extra code.
* `patches.CardSwapPatch` rewrites `CardLibrary.cards` right after `CardLibrary.initialize()`.
  The vanilla game builds every card from that map (`CardLibrary.getCardList`, `AbstractDungeon.getCardFromPool`,
  `AbstractPlayer.initializeStarterDeck`, ...), so replacing the map entries replaces the cards everywhere.
  The vanilla `isSeen` flag is carried over and the library counters are left untouched.
* Card text lives under mod prefixed keys (`anotherspire:Clash`, `anotherspire:Steam`, ...) in
  `localization/eng|zhs/CardStrings.json`, so vanilla strings are never overwritten and no load order matters.
* Vanilla hooks are used wherever possible: `triggerOnManualDiscard` (Quick Slash), `onRetained`
  (Perseverance, Windmill Strike), `triggerWhenDrawn` (Streamline), `triggerOnExhaust` (Sentinel),
  `AbstractPlayer.channelOrb` (Rebound), `PostExhaustSubscriber` + `GameActionManager.turn`
  (Seeing Red), `MarkPower` + an attack hook (Path to Victory, pressure points),
  `Shiv.<init>` / `Shiv.use` hooks (Unload), `AbstractCard.freeToPlay()` (Follow-Up, Wheel Kick),
  `VampireDamageAction` (Bite), X-cost handling from Whirlwind/Multi-Cast (Metallicize, Whirlwind,
  Multi-Cast).
* Cards that hand out another card use the vanilla presentation: a `cardsToPreview` preview and a
  `*CardName` marker in the description (Shiv, Smite, Safety, Insight, Miracle), so no extra
  keyword had to be registered for them.
* `Purity` keeps its vanilla effect (exhaust up to 3 (5) cards in your hand) and gains Retain
  (`selfRetain`); the card itself is still exhausted when played.
* `Bite` is the only card that is not obtained through `CardLibrary` (the Vampires event does `new Bite()`),
  so it is patched in place (`patches.BitePatch`) and its strings are swapped with `ReflectionHacks`.
* Cards whose value depends on the rest of the board show a live counter on the card face, written with the
  vanilla `!M!` dynamic number (the game substitutes it while rendering, so the number follows the board
  with no extra work): `Expect a Fight` (the reworked `Pummel`) counts the Attacks in hand, `Die Die Die`
  counts the Shivs waiting in the exhaust pile. Only the display name of `Pummel` changed; its ID did not.
  The counter line lives in `EXTENDED_DESCRIPTION` and is only appended to the description while a fight is
  going on, so the card reward screen, the deck view and the compendium show the plain text.

## New helper content

| Type | Name | Used by |
|---|---|---|
| Power | `anotherspire:NextTurnFocus` | Leap, Blizzard (negative amount = lose it again) and Hyperbeam (positive amount = gives the Focus back) |
| Power | `anotherspire:GlassKnifeMark` | Glass Knife (the enemy debuff, vanilla "Talk to the Hand" icon) |
| Power | `anotherspire:Mark` | Path to Victory (the mod's own Mark debuff, no vanilla `MarkPower` trigger) |
| Power | `anotherspire:HelloWorldAnyCard` | Hello World (vanilla HelloPower without the Common rarity restriction) |
| Power | `anotherspire:ShivMastery` | Unload (Shivs Retain + first Shiv of the turn hits harder) |
| Power | `anotherspire:FreeSkill` | Wheel Kick (next Skill costs 0, mirrors vanilla `FreeAttackPower`) |
| Power | `anotherspire:CreativeAIUpgraded` | Creative AI+ (hands out upgraded Power cards) |
| Power | `anotherspire:BattleHymnSmite` | Battle Hymn |
| Power | `anotherspire:LikeWaterCalm` | Like Water |
| Power | `anotherspire:StudyInsight` | Study |
| Power | `anotherspire:DevotionDraw` | Devotion |
| Action | `WhirlwindXAction` / `MultiCastXAction` | Whirlwind / Multi-Cast (X and X+1 / X*2) |
| Action | `GashClawAction` | Claw (vanilla `GashAction` only matches the vanilla class) |
| Action | `DiscardForShivsAction` | Masterful Stab (discard up to 3, then a Shiv per card) |
| Action | `PlayShivsFromExhaustAction` | Die Die Die (replays a copy of every Shiv in the exhaust pile) |
| Action | `ExhaustAllStatusAction` | Reboot (burns every Status card) |
| Action | `IncreaseCostAction` | Core Surge (raise this card's cost by 1 this combat) |
| Action | `SanctityEnergyAction` | Sanctity (energy instead of cards when the previous card was a Skill) |

Patches that go with them: `ShivCreationPatch` (new Shivs are born Retaining and already show the
Shiv mastery bonus), `ShivMasteryPatch` (first Shiv of the turn bonus), `FreeSkillCardPatch`
(teaches `AbstractCard.freeToPlay()` about the Free Skill power), `MarkAttackPatch` (marked enemies
lose HP when attacked), `BitePatch.StringPatch` (writes our strings onto every Bite instance as well
as the class), `ExhaustTracker` (Seeing Red's "did you exhaust a card this turn" bookkeeping),
`RetainedCardFadeInPatch` (fades cards back in if the base game ever hands the player one that was
left in limbo as it was fading out).

`PlayShivsFromExhaustAction` parks its replays in `AbstractDungeon.player.limbo` while they are being
played, exactly like the base game's own `PlayTopCardAction` does, and takes each copy back out again
with an `UnlimboAction` once the replay is over (the copies also drop their Retain flags). Leaving
them there would be a bug: at the end of a turn `GameActionManager.cleanCardQueue` marks everything
still in limbo as fading out, and `DiscardAtEndOfTurnAction` hands every retained limbo card back to
the player, which with Unload's Retain on Shivs produced invisible but playable Shivs in hand.

Powers reuse vanilla icon regions (`focus`, `talk_to_hand`, `hymn`, `like_water`, `draw`, `devotion`), so no artwork is needed.
Entrench intentionally uses the **vanilla** `NextTurnBlockPower` (the power Dodge and Roll uses).
Rebound's "last orb" tracker is cleared when a battle ends (`PostBattleSubscriber`) and when a run starts,
so orbs channeled by relics during `atPreBattle` (Cracked Core, Nuclear Battery, ...) are recorded.

## Localization

`eng` and `zhs` files are provided for card text, keyword entries and power strings.
The `Mark` keyword (`印记`) is the only new glossary entry; every other keyword is written exactly as the base game
writes it, so the in-game highlighting and tooltips work as usual.

## Defaults chosen for values the requirements did not specify

| Card | Upgrade |
|---|---|
| Bloodletting | energy `[E][E] -> [E][E][E]`, HP loss `3 -> 2` |
| Bite | +2 damage |
| Quick Slash | +4 damage (vanilla direction) |
| Rampage | +3 to the per-play damage bonus (vanilla direction) |
| Rebound | +2 damage |
| Weave | +2 damage (vanilla direction) |
| Blizzard | cost `1 -> 0` **and** `2 -> 3` temporary Focus per Frost Orb (both values were listed in the requirements) |
| Dodge and Roll | +3 Block |
| Just Lucky | adds "draw 1 card"; Block and damage stay at 2 / 3 |
| Rainbow | keeps the vanilla structure: Exhaust on the base card, Exhaust removed by the upgrade |
| Die Die Die | cost `2 -> 1` |
| Steam Barrier | keeps Exhaust at both levels (the requirements list it once, with no "remove it when upgraded" note) |
| Perseverance | the requirements list no Block for playing it, so it only pays out when Retained |

## Uncapped values

The base game clamps a set of numbers to +-999 (Poison to 9999). Every one of those clamps is lifted to
the int limit (`Integer.MAX_VALUE`). `long` is not reachable: these values live in `int` fields
(`AbstractCreature.currentBlock`, `AbstractPower.amount`, `EnergyPanel.totalCount`), so widening them
would mean replacing the fields, every formula that reads them and every save file.

| Value | Where the base game clamped it | Patch |
|---|---|---|
| Block | `AbstractCreature.addBlock` | `NoBlockCapPatch` |
| Strength, Dexterity, Focus, Shackled | constructor, `stackPower`, `reducePower` | `NoPowerCapPatch` |
| Plated Armor, Like Water | `stackPower` | `NoPowerCapPatch` |
| Poison | constructor (cap 9999) | `NoPowerCapPatch` |
| Energized (green / blue), Collect | constructor, `stackPower` | `NoPowerCapPatch` |
| Energy | `EnergyPanel.addEnergy` | `NoEnergyCapPatch` |

They all work the same way: a Prefix works out the amount the base game wanted, a Postfix writes that
amount back when (and only when) the clamp actually fired, and the power tooltip is refreshed. No part of
the original code is copied or replaced, so patches from other mods still run.

Block is the odd one out because the clamp sits in the middle of `addBlock`, after the relics and powers
have modified the amount. `NoBlockCapPatch.BlockCapWriteMatcher` searches for the one write of
`currentBlock` that pushes 999 right before storing it (the clamp statement itself), so the Insert lands
inside the clamp branch where `currentBlock` still holds the amount that is about to be discarded.

## Overflow: over the limit means at the limit

Once the 999 clamps are gone, arithmetic that used to be safe can wrap around, so anything that could
go past the int limit saturates instead of flipping sign (that is what "over the limit means at the
limit" means here):

* `NoPowerCapPatch` also wraps `AbstractPower.stackPower`, which is where every power does its plain
  `amount += ...`. The sign check it uses (`delta > 0 && result < before`, or `delta < 0 && result >
  before`) is an exact overflow test for int addition.
* `NoBlockCapPatch` does the same for block: if `currentBlock` came out lower than it went in, the
  addition wrapped, so it is set to `Integer.MAX_VALUE`.
* `NoEnergyCapPatch` does it for energy, because doubling effects (`Double Energy`) add the current
  energy to itself.
* `TriplePoisonSaturatePatch` handles the one multiplication that happens outside of any power
  method: Catalyst+ computes `poison.amount * 2` in int math before applying it, so the current
  Poison is saturated to `Integer.MAX_VALUE / 3` first and the tripling stays in range.
* The reworked **Heavy Blade** used to copy the base game's trick of multiplying the player's Strength
  power by its multiplier and dividing it back afterwards. With uncapped Strength that wraps as soon as
  Strength passes ~429 million, and dividing cannot undo the wrap, which left the player with *negative*
  Strength. It now multiplies with saturating math and restores the exact original value instead.

## Build

```powershell
mvn -q clean package
```

Java 8 bytecode is produced (`maven.compiler.source/target = 1.8`) and `maven-antrun-plugin` copies
`target/anotherspire.jar` into `<Steam>/steamapps/common/SlayTheSpire/mods/`.
Start ModTheSpire with Java 8, mod id `anotherspire`, dependencies: BaseMod only.
