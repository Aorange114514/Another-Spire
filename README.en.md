<a id="top" name="top"></a>

# AnotherSpire Rework

[简体中文](README.md) | **English**

Reworks the cards and relics of *Slay the Spire* under their vanilla IDs. Mod id `anotherspirerework`, depends on
[BaseMod](https://github.com/daviscook477/BaseMod), runs on [Java 8](https://adoptium.net/temurin/releases/?version=8) and
[ModTheSpire](https://github.com/kiooeht/ModTheSpire). Repository:
[github.com/Aorange114514/Another-Spire](https://github.com/Aorange114514/Another-Spire).

This file lists only the changes; for the replacement mechanism, card art / power icon rules, saves, build
verification and maintenance rules, see
[CHANGELOG.md — implementation & maintenance notes (Chinese)](CHANGELOG.md#maintenance).

> Do not enable the old `AnotherSpire` at the same time. Both modify the same cards and methods; different package names/modIDs do not mean the mechanics can be stacked.

## Contents

- [1. Card changes](#cards)
  - [Ironclad (24)](#ironclad)
  - [Silent (31)](#silent)
  - [Defect (28)](#defect)
  - [Watcher (30)](#watcher)
  - [Colorless (17 replacements + Bite)](#colorless)
- [2. Relic changes](#relics)
  - [The 27 relics that do not go to Act 4 (the Heart floor)](#no-act4)
- [3. Other mechanics changes](#mechanics)
- [CHANGELOG.md — implementation & maintenance notes (Chinese)](CHANGELOG.md#maintenance) (build, verification and maintenance rules)

<a id="cards" name="cards"></a>

## 1. Card changes

- [CardSwapPatch.replacements()](src/main/java/anotherspirerework/patches/CardSwapPatch.java) replaces **130 cards** in total, all keeping their vanilla cardIDs and vanilla image paths.
- Bite (`Bite`) does not go through the card pool; it is patched separately by [BitePatch](src/main/java/anotherspirerework/patches/BitePatch.java) and is not counted among the 130.
- Costs are written as "base (upgraded)"; numbers in parentheses in the effect column are the upgraded values. ⚠ marks a change of card type or rarity.

<a id="ironclad" name="ironclad"></a>

### Ironclad (24)

| Card | Vanilla ID | Cost | Effect (upgraded) |
|---|---|---|---|
| Clash | `Clash` | 0 | Deal 10 (14) damage; damage is lowered by 2 for each non-Attack card in your hand |
| Wild Strike | `Wild Strike` | 1 | Deal 12 (17) damage; put a Wound into your discard pile |
| Heavy Blade ⚠Common→Uncommon | `Heavy Blade` | 2 | Deal 15 (20) damage; Strength counts 5 (7) times on Heavy Blade |
| Clothesline | `Clothesline` | 2 | Deal 14 (18) damage; apply 3 Weak |
| Iron Wave ⚠Common→Uncommon | `Iron Wave` | 0 | Deal 3 (5) damage; gain Block equal to the damage dealt |
| Flex | `Flex` | 1 (0) | Gain 5 temporary Strength (lost at the end of the turn) |
| Thunderclap | `Thunderclap` | 1 | Deal 4 (6) damage to ALL enemies; apply 1 (2) Vulnerable |
| Cleave | `Cleave` | 1 | Deal 11 (14) damage to ALL enemies; Exhaust |
| Sword Boomerang | `Sword Boomerang` | 1 | Deal 3 (4) damage to a random enemy 3 (4) times |
| Uppercut | `Uppercut` | 1 | Deal 7 (8) damage; apply 1 (2) Weak and 1 (2) Vulnerable |
| Sentinel | `Sentinel` | 1 | Gain 6 (9) Block; draw 1 card; if this card is Exhausted, gain [E][E] |
| Entrench | `Entrench` | 1 (0) | At the start of your next turn, gain Block equal to your current Block |
| Bloodletting ⚠Uncommon→Common | `Bloodletting` | 0 | Gain [E][E] ([E][E][E]); lose 3 (2) HP |
| Sever Soul | `Sever Soul` | 2 (1) | Exhaust all non-Attack cards in your hand; deal 10 damage |
| Whirlwind | `Whirlwind` | X | Deal 6 (7) damage to ALL enemies X (X+1) times |
| Rampage | `Rampage` | 1 | Deal 4 damage 2 times; each time you play this card, its damage increases by 5 (8 when upgraded) this combat |
| Searing Blow | `Searing Blow` | 2 | Deal 7 (10) damage; gain 7 (10) Block; can be upgraded multiple times |
| Berserk | `Berserk` | 2 | At the start of each turn, gain [E] ([E][E]); no longer applies Vulnerable to yourself |
| Metallicize | `Metallicize` | X | At the end of your turn, gain X×3 (4) Block |
| Expect a Fight ⚠Attack→Skill (replaces Pummel) | `Pummel` | 2 (1) | Gain [E] for each Attack card in your hand |
| Burning Pact ⚠Uncommon→Common | `Burning Pact` | 1 | Exhaust 1 card; draw 2 (3) cards (effect otherwise unchanged) |
| Seeing Red | `Seeing Red` | 1 (0) | If you have Exhausted a card this turn, gain [E][E][E] |
| Infernal Blade | `Infernal Blade` | 1 (0) | Choose 1 of 3 random Attack cards to add to your hand; it costs 0 this turn; Exhaust |
| Demon Form | `Demon Form` | 3 | Ethereal; deal 1 additional damage to enemies for each card in your exhaust pile (Ethereal removed when upgraded) |

<a id="silent" name="silent"></a>

### Silent (31)

| Card | Vanilla ID | Cost | Effect (upgraded) |
|---|---|---|---|
| Dagger Spray | `Dagger Spray` | 1 | Deal 2 damage to ALL enemies 5 (8) times; Exhaust |
| Backflip | `Backflip` | 1 | Gain 5 (6) Block; draw 2 (3) cards |
| Poisoned Stab | `Poisoned Stab` | 1 | Deal 4 (6) damage; apply 5 (6) Poison |
| Quick Slash | `Quick Slash` | — (Unplayable) | If this card is discarded from your hand, deal 10 (14) damage to ALL enemies |
| Dagger Throw | `Dagger Throw` | 1 | Deal 9 (12) damage; draw 1 (2) cards; discard 1 (2) cards |
| Outmaneuver | `Outmaneuver` | 0 | Next turn, gain [E] ([E][E]) |
| Choke | `Choke` | 1 | Deal 6 damage; whenever you play a card this turn, the enemy loses 3 (5) HP |
| Riddle With Holes | `Riddle With Holes` | 2 | Deal 3 damage 6 (8) times |
| Glass Knife | `Glass Knife` | 1 | Deal 4 damage 2 times; whenever this enemy takes damage from a Shiv, gain 3 (5) Block; Exhaust |
| A Thousand Cuts | `A Thousand Cuts` | 1 | Whenever you play a card, deal 1 (2) damage to ALL enemies |
| Unload ⚠Rare→Uncommon, Attack→Power | `Unload` | 1 | Shivs gain Retain; the first Shiv you play each turn deals +9 (12) damage |
| Masterful Stab ⚠Attack→Skill | `Masterful Stab` | 0 | Discard up to 3 cards; for each card discarded, add a Shiv+ to your hand |
| Crippling Cloud ⚠Uncommon→Rare | `Crippling Poison` | 3 (2) | Apply 12 Poison and 3 Weak to ALL enemies; Exhaust |
| Expertise | `Expertise` | 1 | Draw cards until you have 7 (8) cards in your hand; discard 1 card |
| Storm of Steel | `Storm of Steel` | 2 (1) | Play a copy of every Shiv in your exhaust pile against an enemy |
| After Image | `After Image` | 1 | Whenever you play a card, gain 1 (2) Block |
| Dodge and Roll | `Dodge and Roll` | — (Unplayable) | If this card is discarded from your hand, gain 8 (11) Block |
| Die Die Die ⚠Attack→Power | `Die Die Die` | 2 (1) | Shivs now attack ALL enemies; add 3 Shivs to your hand |
| Envenom | `Envenom` | 1 | Your Shivs apply 1 (2) Poison; add 1 Shiv to your hand |
| Distraction | `Distraction` | 1 (0) | Choose 1 of 3 random Skill cards to add to your hand; it costs 0 this turn; Exhaust |
| Cloak and Dagger | `Cloak And Dagger` | 1 | Gain 6 Block; choose 1 (2) cards in your draw pile and transform them into Shivs |
| Concentrate | `Concentrate` | 0 | Discard 3 cards; gain [E][E] ([E][E][E]) |
| Deadly Poison | `Deadly Poison` | 1 | Apply 7 (9) Poison; Exhaust |
| Bane ⚠Common→Uncommon | `Bane` | 1 | Deal 8 (10) damage; if the enemy is Poisoned, convert the unblocked damage into Poison |
| Flying Knee ⚠Common→Uncommon | `Flying Knee` | 1 | Deal 8 (11) damage; if the enemy is Weakened, damage is doubled |
| Catalyst ⚠Uncommon→Common | `Catalyst` | 1 | Double an enemy's Poison; Exhaust; upgraded gains Retain |
| Predator ⚠Uncommon→Common | `Predator` | 2 | Deal 15 (18) damage; draw 2 (3) additional cards next turn |
| Sucker Punch | `Sucker Punch` | 1 | Deal 7 (10) damage; apply 2 Weak |
| Blur | `Blur` | 1 (0) | After you play this card, gain 1 Block whenever you draw 1 card this turn |
| Bouncing Flask | `Bouncing Flask` | X | Apply 6 Poison to random enemies X (X+1) times |
| Skewer | `Skewer` | X | Deal 9 (12) damage X times |

<a id="defect" name="defect"></a>

### Defect (28)

| Card | Vanilla ID | Cost | Effect (upgraded) |
|---|---|---|---|
| Quadcast (reworked from Dualcast) | `Dualcast` | 1 (0) | Evoke your rightmost Orb 4 times |
| Rebound | `Rebound` | 1 | Deal 3 (5) damage; Channel a copy of the last Orb you Channeled this combat |
| Barrage | `Barrage` | 1 | Deal 5 (8) damage for each Orb you have |
| Sweeping Beam | `Sweeping Beam` | 1 | Deal 10 (14) damage to ALL enemies; draw 1 (2) cards; put a Slimed into your discard pile |
| Claw | `Gash` | 0 | Deal 1 damage 2 (3) times; increase the damage of ALL Claw cards by 2 (3) this combat |
| Streamline | `Streamline` | 3 | Deal 22 (28) damage; each time you draw this card, its cost is reduced by 1 this combat |
| Steam Barrier | `Steam` | 0 | Channel 1 (2) Frost; put a Dazed into your draw pile; does not Exhaust |
| Leap ⚠Common→Uncommon | `Leap` | 1 | Gain 9 (10) Block; gain 2 (4) temporary Focus this turn |
| Hello World | `Hello World` | 1 | At the start of your turn, add a random card to your hand (Innate when upgraded) |
| Chill | `Chill` | 0 | Channel 1 Frost for each enemy; Exhaust (Exhaust removed when upgraded) |
| Force Field | `Force Field` | 2 | Gain 12 Block; gain +4 (6) Block for each Power card you have |
| Blizzard ⚠Attack→Skill | `Blizzard` | 1 (0) | Gain 2 (3) temporary Focus this turn for each Frost Orb you have |
| Rip and Tear | `Rip and Tear` | 2 (1) | Exhaust all Status cards in your hand and piles; for each card Exhausted, deal 7 damage to a random enemy |
| Melter ⚠Attack→Skill | `Melter` | 1 (0) | Remove all Artifact and Block from an enemy; apply 2 Weak and 2 Vulnerable; Exhaust |
| Fusion | `Fusion` | 1 | Channel 1 Plasma; Exhaust (Exhaust removed when upgraded) |
| Reprogram ⚠Skill→Power | `Reprogram` | 2 | Lose an Orb slot; gain 2 (3) Strength and 2 (3) Dexterity; does not Exhaust |
| Multi-Cast | `Multi-Cast` | X | Evoke ALL your Orbs X (X×2) times |
| Hyperbeam | `Hyperbeam` | 2 | Deal 24 (32) damage to ALL enemies; lose 3 (4) Focus this turn |
| Rainbow | `Rainbow` | 2 | Channel 1 Lightning, 1 Frost, 1 Dark and 1 Plasma; Exhaust (Exhaust removed when upgraded) |
| Heatsinks | `Heatsinks` | 1 | Whenever you play a Power card, draw 2 cards (Innate when upgraded) |
| Core Surge ⚠Attack→Skill | `Core Surge` | 0 | Gain an Orb slot; draw 1 (2) cards; increase this card's cost by 1 this combat |
| Reboot | `Reboot` | 0 | Remove all your debuffs; Exhaust all your Status cards; shuffle all your non-Exhausted cards into your draw pile; draw 4 (5) cards; Exhaust |
| Creative AI | `Creative AI` | 3 | At the start of each turn, add an (upgraded) random Power card to your hand |
| Meteor Strike | `Meteor Strike` | 5 | Deal 30 damage; Channel 3 (4) Plasma |
| Stack | `Stack` | 1 (0) | Gain Block equal to the number of cards in your draw pile (the card shows the live value); whenever you shuffle, put this card on top of your draw pile |
| Thunder Strike | `Thunder Strike` | 3 | For each Lightning Orb you Channeled this combat, Channel 1 Lightning and deal 3 damage to a random enemy; Exhaust (Exhaust removed when upgraded) |
| Tempest | `Tempest` | X | Channel X (X+1) Lightning |
| Overclock ⚠Uncommon→Common | `Steam Power` | 0 | Draw 2 (3) cards; put a Burn into your discard pile (effect otherwise unchanged) |

<a id="watcher" name="watcher"></a>

### Watcher (30)

| Card | Vanilla ID | Cost | Effect (upgraded) |
|---|---|---|---|
| Path to Victory | `PathToVictory` | 1 | Apply 4 (6) Mark; all enemies with Mark lose additional HP equal to their Mark when attacked; Exhaust |
| Consecrate | `Consecrate` | 1 (0) | Deal 6 damage to ALL enemies; draw 1 card for each enemy hit |
| Follow-Up | `FollowUp` | 1 | Deal 9 (12) damage; Wrath: cost is reduced to 0 |
| Perseverance | `Perseverance` | 1 | Retain; enter Calm; whenever this card is Retained, gain 5 (7) Block |
| Third Eye | `ThirdEye` | 1 | Add 2 (3) Insights to your draw pile |
| Battle Hymn | `BattleHymn` | 1 (0) | At the start of each turn, Exhaust a card in your hand and add a Smite to your hand |
| Like Water | `LikeWater` | 1 | At the end of your turn, enter Calm (Innate when upgraded); the buff icon does not show a counter |
| Simmering Fury | `Vengeance` | 1 | Enter Wrath; add 1 (2) Insights to your draw pile |
| Swivel | `Swivel` | 2 | Gain 10 Block; draw 2 (3) cards; the next Attack you play costs 0 |
| Windmill Strike | `WindmillStrike` | 2 | Retain; deal 5 (6) damage 3 times; enter Wrath; whenever Retained, its number of hits increases by 1 this combat |
| Deceive Reality | `DeceiveReality` | 1 (0) | Add an Insight and a Miracle to your draw pile |
| Study | `Study` | 2 (1) | At the start of your turn, add an Insight to your hand |
| Weave | `Weave` | 0 | Deal 4 damage; Wrath: enter Calm; Calm: enter Wrath |
| Devotion | `Devotion` | 1 | Whenever you gain Mantra, draw 1 card (Innate when upgraded) |
| Sanctity | `Sanctity` | 1 | Gain 6 (8) Block; if the last card played this combat was a Skill, gain 1 (2) Energy |
| Carve Reality ⚠Attack→Skill | `CarveReality` | 1 (0) | Add a Smite and a Safety to your hand |
| Wheel Kick | `WheelKick` | 2 | Deal 15 (20) damage; draw 2 cards; the next Skill you play costs 0 |
| Just Lucky | `JustLucky` | 0 | Scry 1; gain 2 Block; deal 3 damage (draw 1 card) |
| Flying Sleeves | `FlyingSleeves` | 1 | Retain; deal 2 damage 4 (5) times |
| Sash Whip | `SashWhip` | 0 | Deal 4 (5) damage; Calm: apply 1 (2) Weak |
| Evaluate | `Evaluate` | 1 | Gain 8 (11) Block; add an Insight to your draw pile |
| Nirvana | `Nirvana` | 1 | When the number of cards played this turn reaches 5, draw 2 cards per stack at the start of the next turn (Innate when upgraded). |
| Spirit Shield ⚠Skill→Power | `SpiritShield` | 2 (1) | While in Calm, attack damage you take from monsters is halved (multiple stacks combine as 1/2ⁿ) |
| Empty Body ⚠Common→Uncommon, Skill→Power | `EmptyBody` | 1 | At the end of your turn, leave your current Stance and gain 7 (9) Block |
| Empty Mind ⚠Uncommon→Common | `EmptyMind` | 1 | Draw 2 (3) cards; exit your current Stance. (effect otherwise unchanged) |
| Flurry of Blows | `FlurryOfBlows` | 0 | Deal 4 (6) damage; whenever you change Stance, put this card from your draw pile, discard pile or exhaust pile into your hand |
| Ragnarok | `Ragnarok` | X | Deal X×2 damage to random enemies X×2 times, with a 0.1 second pause between each; Exhaust (Exhaust removed when upgraded) |
| Judgment | `Judgement` | 3 | If the target enemy has no Artifact and its HP is below 20% (25%) of its Max HP, set its HP to 0 |
| Reach Heaven | `ReachHeaven` | 3 | Deal 15 (20) damage; if this card kills the enemy, enter Divinity |
| Wave of the Hand | `WaveOfTheHand` | 1 (0) | This turn, whenever you change Stance, apply 2 Weak to ALL enemies; Exhaust |

<a id="colorless" name="colorless"></a>

### Colorless (17 replacements + Bite)

| Card | Vanilla ID | Cost | Effect (upgraded) |
|---|---|---|---|
| Purity | `Purity` | 0 | Retain; Exhaust up to 3 (5) cards from your hand; this card still Exhausts when played |
| Mayhem | `Mayhem` | 1 (0) | Every time you draw 10 cards, gain [E] |
| Thinking Ahead | `Thinking Ahead` | 0 | Draw 2 (3) cards, then put a card from your hand on top of your draw pile; Exhaust |
| Secret Technique | `Secret Technique` | 0 | Choose 1 (2) Skill cards from your draw pile and put them into your hand; Exhaust |
| Secret Weapon | `Secret Weapon` | 0 | Choose 1 (2) Attack cards from your draw pile and put them into your hand; Exhaust |
| Chrysalis ⚠Skill→Power | `Chrysalis` | 2 (1) | Whenever you shuffle your draw pile, gain [E] |
| Metamorphosis ⚠Skill→Power | `Metamorphosis` | 2 (1) | Whenever you shuffle your draw pile, choose up to 1 card from your draw pile and put it into your hand; multiple stacks become up to n cards, and the buff description and the card-choice prompt show n; if the cards you draw right after the shuffle would empty the draw pile, no card-choice screen is shown |
| Jack Of All Trades ⚠Skill→Power | `Jack Of All Trades` | 2 (1) | Every time you spend 4 Energy, gain [E] |
| Flash of Steel | `Flash of Steel` | 0 | Deal 5 (8) damage; draw 1 card |
| Finesse | `Finesse` | 0 | Gain 3 (5) Block; draw 1 card |
| Swift Strike | `Swift Strike` | 0 | Deal 7 (10) damage; add a random Attack card to your hand; it gains Ethereal and Exhaust |
| Good Instincts | `Good Instincts` | 0 | Gain 6 (9) Block; add a random Skill card to your hand; it gains Ethereal and Exhaust |
| Mind Blast | `Mind Blast` | 1 (0) | Innate; deal damage equal to the number of cards in your deck |
| Dramatic Entrance | `Dramatic Entrance` | 0 | Innate; deal 10 (12) damage to ALL enemies; Exhaust |
| Sadistic Nature | `Sadistic Nature` | 2 (1) | Weak and Vulnerable on enemies are twice as strong |
| Violence | `Violence` | 3 | Play 3 (4) random Attack cards from your draw pile |
| Transmutation | `Transmutation` | 1 (0) | Exhaust all cards in your hand; for each card Exhausted, add a random card to your hand |
| Bite | `Bite` | 1 | Deal 7 (9) damage; unblocked damage heals you. |

<a id="relics" name="relics"></a>

## 2. Relic changes

All relics keep their vanilla IDs and vanilla images. The wrapper class ([ReworkedRelic](src/main/java/anotherspirerework/relics/ReworkedRelic.java)) and the direct-patch paths coexist; for details see
[CHANGELOG.md — implementation & maintenance notes (Chinese)](CHANGELOG.md#maintenance).

| Relic | Change |
|---|---|
| Prismatic Shard | Changed to a BOSS relic (can appear in Act 1 and Act 2 boss chests); combat rewards can drop Colorless and off-color cards; at the start of each turn gain +[E] and draw 1 card |
| Sling of Courage | At the start of each combat, +1 Strength and +1 Dexterity |
| Hand Drill | The player's normal attack damage ignores enemy Block (including the Heart's "Invincible"); does not affect Poison/Thorns, and does not remove enemy Block |
| Cultist Headpiece | Keeps its original description, plus a hidden effect: gain 1 Ritual at the start of combat |
| Chemical X | Only spawns when your deck or the current shop has a card costing X; otherwise skipped and retried at the next shop |
| Cauldron | On pickup, +1 potion slot and brew 5 potions (Fruit Juice generation chance doubled); the original 2 slots become 1 |
| Lee's Waffle | Only spawns when your current HP is below 50%; otherwise skipped and retried at the next shop |
| The Courier | Keeps its original description, plus a hidden effect: after you buy a shop relic, another shop relic spawns in its place; once the pool is empty, generation is normal |
| Fusion Hammer | On pickup, randomly downgrade 4 cards (1 level each time, Searing Blow can be picked repeatedly); +[E] each turn; no longer blocks campfire smithing |
| Mark of Pain | Each turn +[E]; at the start of combat put 2 Wounds into your discard pile |
| Velvet Choker | Each turn +[E]; if you play more than 6 cards in a turn, lose [E] at the start of the next turn |
| Ectoplasm | At the start of your turn lose 5 Gold for [E] (if you have less than 5 Gold you do not pay); can spawn in Act 1 and Act 2 boss chests; Gold gain restored to normal |
| Black Blood | Replaces Burning Blood: the first Attack card costing ≥2 each combat heals HP equal to its unblocked damage; at the start of combat add 1 Pain to your hand |
| Busted Crown | Each card reward is replaced by a random Common/Uncommon/Rare relic; no longer provides extra Energy |
| Sacred Bark | Each turn +[E]; lose [E] when you have fewer than 2 potions (empty slots do not count) |
| Sozu | Each turn +[E]; potions are locked each combat until you voluntarily discard one (drinking/throwing/Fairy in a Bottle does not unlock; no restriction outside combat) |
| Ring of the Serpent | Replaces Ring of the Snake: draw 2 extra cards in each of the first 3 turns of each combat; the vanilla permanent +1 draw is removed |
| Girya | Campfire lifting cap 5 times (vanilla 3 times), each time also +1 Strength and +1 Dexterity; the rest-site "Lift" button tooltip is synced to (max +5); wrapper instances do not apply twice |
| Calling Bell | Unique Curse + 5 random relics, with at least 1 Common/Uncommon/Rare each |
| Black Star | After defeating ≥5 elites this run, +[E] each turn; the relic counter shows the cumulative elite kills this run (uncapped, synced when loading a save) |
| Ninja Scroll | At the start of each turn, add 1 Shiv to your hand (replaces the vanilla 3 at combat start) |
| Duality | Whenever you play an Attack card, +1 temporary Dexterity; whenever you play a Skill card, +1 temporary Strength |
| Enchiridion | At the start of combat, choose 1 of 3 random Power cards to add to your hand; it costs 0 this turn; can be skipped |
| Medical Kit | When you draw a Status card, Exhaust it and draw 1 card; Status cards can be played and Exhaust |
| Blue Candle | When you draw a Curse card, Exhaust it; Curse cards can be played and Exhaust, and playing one costs 1 HP |
| Strange Spoon | On pickup, choose up to 2 cards in your deck to remove Exhaust from (replaces the vanilla 50% no-Exhaust chance); the record persists across saves |
| Orrery | On pickup, choose any number of cards from 10 Common/10 Uncommon/10 Rare to add to your deck (replaces the vanilla 4 card rewards) |
| Frozen Eye | Starter Strikes and Defends gain Ethereal (written on token boundaries, synced with the deck on pickup/loss) |
| Inserter | Every 2 turns, at the start of the turn gain 1 Orb slot and Channel 1 random Orb |
| Matryoshka | Draw 1 card when you shuffle (replaces the vanilla extra relic from chests) |
| Astrolabe | On pickup, choose any number of cards from 40 cards of any color to add to your deck (base, Special, Status, Curse and HEALING excluded) |
| Tiny House | Changed to a shop relic; at rest sites you can choose any number of options (each option once per campfire; "Recall" is unavailable after using other options, and if you choose "Recall" first the campfire ends as usual); flashes only once on pickup (the vanilla pickup flash), flashes and pulses continuously while at a rest site and stops after leaving |
| Violet Lotus | Whenever you leave Calm, draw 1 extra card in addition to the vanilla Energy |
| Face of Cleric | After each combat, +2 Max HP (vanilla +1) |
| Snecko Eye | Draw 3 extra cards each turn; gain Confusion at the start of each combat |
| Twisted Funnel | Poison triggers 2 extra times (e.g. 7 stacks → lose 7+6+5, leaving 4 stacks); the green part of the HP bar previews it |
| Melange | Gain 3 Block whenever you play a Colorless card (including generated cards such as Insight and Miracle; Status/Curse do not count) |
| The Specimen | Whenever an enemy loses HP from Poison, all other enemies lose the same amount of HP |
| Prayer Wheel | Elites and BOSSes give one extra card reward group (so a BOSS gives 2 Rare groups) |
| Golden Eye | At the start of each turn, Scry 3 (on the first turn, before the opening draw); no longer gives other Scry +2 |
| Thread and Needle | At the start of each turn, gain 2 Plated Armor |
| Nloth's Gift | Elites drop one extra Rare card reward; no longer increases Rare card odds |
| Bottled Tornado, Bottled Lightning, Bottled Flame | Original effects unchanged; when rolled in Act 1 or Act 2 they do not consume a pool slot, and can only spawn from Act 3 onward |

<a id="no-act4" name="no-act4"></a>

### The 27 relics that do not go to Act 4 (the Heart floor)

White Beast Statue, Darkstone Periapt, Preserved Insect, Omamori, Smiling Mask, Juzu Bracelet, Ancient Tea Set, Tiny Chest, Dream Catcher, Regal Pillow,
Ceramic Fish, Meal Ticket, Gremlin Horn, Meat on the Bone, Ninja Scroll, Eternal Feather, The Courier, Question Card, Singing Bowl, Stone Calendar, Old Coin, Girya,
Peace Pipe, The Specimen, Wing Boots, Prayer Wheel, Shovel.

<a id="mechanics" name="mechanics"></a>

## 3. Other mechanics changes

- **Colorless card choices gain a "Skip"**: Discovery, Colorless Potion, Toolbox, Nilry's Codex, Foreign Influence; normal card rewards and the Watcher's "choose one" screen are unaffected.
- **Numeric caps removed**: the ±999 caps on Block, Strength/Dexterity/Focus/Shackled, Poison, Energized, Energy and others are relaxed to the int limit.
- **Overflow saturation**: out-of-range arithmetic saturates to the int limit instead of wrapping sign; Heavy Blade uses saturating multiplication.
- **Shuffle events**: triggered after `EmptyDeckShuffleAction` / `ShuffleAllAction` has finished resolving and the shuffle animation has finished playing; Stack, Chrysalis, Metamorphosis and Matryoshka rely on it.
- **UI protection**: while browsing piles / the map / the single-card view, shuffle triggers, the draw handling of Medical Kit and Blue Candle, and the Strange Spoon / Orrery card selection are deferred until the UI closes.
- **Shop relic price cuts**: Uncommon relics −50, Rare relics −100 (vanilla 250 / 300 → 200 / 200); only affects shop shelves.
- **Vanilla bug fix (Indignation + Mental Fortress)**: Indignation's Stance change into Wrath was originally ordered after card resolution, so the Corrupt Heart's Beat of Death resolved before Mental Fortress's Block; now the Stance change resolves immediately like Tantrum, gaining Block first and then resolving Beat of Death.

---

[↑ Back to top](#top) · [Chinese version](README.md) · [CHANGELOG.md — implementation & maintenance notes (Chinese)](CHANGELOG.md#maintenance)
