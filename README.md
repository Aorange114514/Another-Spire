# AnotherSpire

A Slay the Spire mod that reworks 61 vanilla cards (Ironclad / Silent / Defect / Watcher / Colorless).
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
  `AbstractPlayer.channelOrb` (Rebound), `MarkPower` + an attack hook (Path to Victory),
  `VampireDamageAction` (Bite), X-cost handling from Whirlwind/Multi-Cast (Metallicize, Whirlwind, Multi-Cast).
* `Bite` is the only card that is not obtained through `CardLibrary` (the Vampires event does `new Bite()`),
  so it is patched in place (`patches.BitePatch`) and its strings are swapped with `ReflectionHacks`.

## New helper content

| Type | Name | Used by |
|---|---|---|
| Power | `anotherspire:NextTurnFocus` | Leap, Blizzard (negative amount = lose it again) and Hyperbeam (positive amount = gives the Focus back) |
| Power | `anotherspire:GlassKnifeMark` | Glass Knife (the enemy debuff, vanilla "Talk to the Hand" icon) |
| Power | `anotherspire:BattleHymnSmite` | Battle Hymn |
| Power | `anotherspire:LikeWaterCalm` | Like Water |
| Power | `anotherspire:StudyInsight` | Study |
| Power | `anotherspire:DevotionDraw` | Devotion |
| Action | `WhirlwindXAction` / `MultiCastXAction` | Whirlwind / Multi-Cast (X and X+1 / X*2) |
| Action | `GashClawAction` | Claw (vanilla `GashAction` only matches the vanilla class) |

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
| Bloodletting | energy `[E][E] [E]` (vanilla upgrade direction) |
| Bite | +2 damage |
| Quick Slash | +4 damage (vanilla direction) |
| Rampage | +3 to the per-play damage bonus (vanilla direction) |
| Rebound | +3 damage (vanilla direction) |
| Weave | +2 damage (vanilla direction) |
| Deceive Reality | one extra Insight |
| Blizzard | cost `1 -> 0` **and** `2 -> 3` temporary Focus per Frost Orb (both values were listed in the requirements) |

## Build

```powershell
mvn -q clean package
```

Java 8 bytecode is produced (`maven.compiler.source/target = 1.8`) and `maven-antrun-plugin` copies
`target/anotherspire.jar` into `<Steam>/steamapps/common/SlayTheSpire/mods/`.
Start ModTheSpire with Java 8, mod id `anotherspire`, dependencies: BaseMod only.
