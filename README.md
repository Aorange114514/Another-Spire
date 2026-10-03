# AnotherSpire Rework

## 回合遗物与添水修复（2026-10-03）

- 后续反馈：长蛇戒指的闪光与额外抽牌共用前三回合条件，第四回合起不再执行原版无条件闪光；不改变已正常的基础抽牌量。
- 黑星增加原版遗物角标计数器，显示本局累计精英击杀数（不封顶于5），拾取、胜利、读档和显示时同步；获得前的击杀也计入。

- 壶铃：原版实例在原有力量之外，按举重计数提供等量敏捷；包装实例不重复结算。
- 棱镜碎片：复用原版Boss遗物及长蛇戒指的拾取字段逻辑，增加 `energyMaster` 和 `masterHandSize` 各1；不再每回合追加加费/抽牌动作。
- 长蛇戒指：取消原版永久增加抽牌量，独立计数每场战斗前三回合，各额外抽2张；不依赖遗物钩子后才自增的原版回合计数。
- 添水：原版与包装实例共用锁定状态，战斗开始重置。仅药水菜单的主动丢弃分支解锁；饮用、投掷和仙灵药水消耗不解锁。战斗外不限制。
- 黑星：替换原版额外精英遗物奖励为本局击败至少5个精英后，每回合获得1能量。全局记录不依赖持有黑星，使用BaseMod保存；旧存档以原版前三幕击杀数兜底。
- 旧存档一次性迁移：已持有棱镜碎片补上基础能量/抽牌量；迁移标记保存后不会重复应用。原版长蛇戒指旧存档可能仍保存着永久+1抽牌，存档不保存实例类型，无法可靠区分包装版是否加过该值，因此不猜测扣除，请在新的一局验收。升级前请备份存档；不支持从更新版倒退到旧版再升级的混用存档。
- 验证新增30项回合/药水/精英阈值用例、丢弃分支字节码及棱镜原版字段检查。仍需游戏内验收，不宣称已完成图形实测。

## 遗物反馈修复（2026-10-03）

- 痛楚印记、大锅、天鹅绒颈圈添加原版类直接补丁，原版实例与重写实例共用同一行为，避免只换描述而保留原效果。
- 痛楚印记战斗开始仅安排两张伤口进入弃牌堆的动作。
- 天鹅绒颈圈允许第七张及后续卡牌，超过六张的下一回合扣一能量；计数在回合开始重置。
- 大锅先增加两个药水栏位（同时扩展药水列表），再走原版药水奖励界面；领取界面剔除临时卡牌奖励，与破碎金冠同时持有时不额外产生遗物。
- 神圣树皮按最新需求改为基础额外一能量，回合开始实际药水少于两瓶时扣一能量，空药水栏不计数。中英描述同步更新。
- 破碎金冠不再在卡牌奖励构造期间清空 `cards`；奖励列表定位前使用原版遗物奖励构造函数替换，避免 `setupItemReward` 的 `cards.size()` 空指针，并初始化完整遗物命中框。
- 自动验证包含52个阈值用例及动作/构造路径的字节码结构检查；这不等于图形界面的游戏内实测。

升级前备份存档，优先在新的一局验收：已持有神圣树皮的旧存档不会再次触发拾取，保存的基础能量可能仍是旧值；本次不自动改写旧存档能量。大锅增加栏位只发生在拾取时，不会因更新补发。

## 2026-10-03 更新

- 精巧刺击：非空手牌始终打开可选零张的弃牌界面，不再在手牌不超过三张时自动全弃。
- 死吧死吧死吧：小刀复制逐张结算后再安排下一张，时间扭曲仍正常计数、结束回合和加力量，但不取消后续复制；目标死亡后停止。
- 新增陨石打击重做：5费，30伤害，生成3/4个等离子球。
- 实现 `c:\Users\riced\VS Code\STS modding\requirements.txt` 中19项遗物改动，保留原版ID和图片。
- 融合之锤随机降级最多四次，每次一级，允许重复选中灼热攻击；不再禁止休息处升级。
- 化学物X、李家华夫饼不满足条件时留在可存档的商店遗物池中，后续生成时重新检查。
- 大锅制作药水使用角色可获得药水列表，果汁双权重仅作用于这五瓶药水；通过原版奖励界面领取。
- 灵体外质仅在金币至少为5时支付并提供能量，可在第一、二幕Boss宝箱生成；恢复正常获取金币。
- 破碎金冠每份卡牌奖励改为一件随机普通/罕见/稀有遗物，不再提供原版额外能量。
- 手钻仅让玩家的普通攻击伤害绕过敌人格挡和坚不可摧，不影响中毒/荆棘，也不消除敌人格挡。
- 添水每场战斗重新锁定药水，丢弃一瓶后解锁；包括鲜血药水、果汁和自动复活的仙灵药水。战斗外不限制。
- 黑暗之血使用本回合实际费用至少为2的第一张攻击牌，按各段实际生命伤害回血；费用为X的攻击牌不计入。
- 邪教徒头套、送货员保持原描述。
- 遗物本地化按需求原文改写主句，并改用原版遗物标注规范（`#r卡名`、`#g遗物名`、`#y遗物`，区别于卡牌的 `*卡名`）；需求中以 `#` 标注的补充说明（生成条件、果汁双权重、坚不可摧、战斗外药水、一举一力一敏、稀有度构成）不进描述文本。陨石打击文案与原版 `Meteor Strike` 逐字一致，未改动。

本机依赖不在默认Steam目录，可使用：

```powershell
mvn -f 'c:\Users\riced\VS Code\STS modding\AnotherSpireRework\pom.xml' '-Dlocal.dependencies=C:/Users/riced/Games/OtherGames/sts mod/dependencies' clean package
& 'c:\Users\riced\VS Code\STS modding\AnotherSpireRework\scripts\verify.ps1' -CheckJar
```

新增无图形环境的补丁契约检查：使用实际依赖JAR验证目标方法、插入定位器、字段及Instrument产生的字节码。
这不能替代ModTheSpire启动及游戏内验收。需重点实测时间吞噬者跨12/24张牌、心脏限伤、药水丢弃、商店补货、Boss遗物替换、休息处举重和降级。

## 重写版说明

原工程保留在相邻的 AnotherSpire 目录。此工程使用独立包名、资源目录和
mod ID `anotherspirerework`，但原版卡牌 ID 与效果保持原实现的基线。
**不要同时启用 AnotherSpire 和此版本**：两者修改相同卡牌和方法，独立 ID 不代表机制可叠加。
切换版本前请结束当前战斗并备份存档；新 Power 的命名空间不同，不承诺迁移进行中的战斗。

实际重构：集中卡牌替换表、独立饱和算术工具、临时伤害/力量的 finally 恢复、
语言大小写转换独立于系统区域设置、追踪状态重置，以及移除未使用的 StSLib 构建依赖。
复杂补丁及各卡牌机制保留原实现，尚不宣称所有游戏场景已经实测。

中文空格按关键词和符号词元边界保留，不作机械删除。JSON 可解析并不足以证明卡面渲染正确。

验证（PowerShell，在此工程目录执行）：

```powershell
mvn clean package
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/verify.ps1 -CheckJar
```

Maven 的 test 阶段执行无额外测试库的饱和算术边界/随机回归测试。
默认打包不会安装到游戏；需要安装时，将 target/anotherspirerework.jar 手动复制到游戏 mods 目录，
在 ModTheSpire 中禁用原版、仅启用重写版与 BaseMod。

A Slay the Spire mod that reworks 82 vanilla cards (Ironclad / Silent / Defect / Watcher / Colorless), plus 19 relic changes.
The full list of changes lives in `../requirements.txt`.

## How it works

* Every reworked card is an `AbstractCard` subclass of our own (`anotherspirerework.cards.<color>.<Name>`) that keeps the
  **original card ID** and the **original art path**, so card pools, starting decks, shops, rewards, transforms,
  the compendium and existing save files all keep working without any extra code.
* `patches.CardSwapPatch` rewrites `CardLibrary.cards` right after `CardLibrary.initialize()`.
  The vanilla game builds every card from that map (`CardLibrary.getCardList`, `AbstractDungeon.getCardFromPool`,
  `AbstractPlayer.initializeStarterDeck`, ...), so replacing the map entries replaces the cards everywhere.
  The vanilla `isSeen` flag is carried over and the library counters are left untouched.
* Card text lives under mod prefixed keys (`anotherspirerework:Clash`, `anotherspirerework:Steam`, ...) in
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
| Power | `anotherspirerework:NextTurnFocus` | Leap, Blizzard (negative amount = lose it again) and Hyperbeam (positive amount = gives the Focus back) |
| Power | `anotherspirerework:GlassKnifeMark` | Glass Knife (the enemy debuff, vanilla "Talk to the Hand" icon) |
| Power | `anotherspirerework:Mark` | Path to Victory (the mod's own Mark debuff, no vanilla `MarkPower` trigger) |
| Power | `anotherspirerework:HelloWorldAnyCard` | Hello World (vanilla HelloPower without the Common rarity restriction) |
| Power | `anotherspirerework:ShivMastery` | Unload (Shivs Retain + first Shiv of the turn hits harder) |
| Power | `anotherspirerework:FreeSkill` | Wheel Kick (next Skill costs 0, mirrors vanilla `FreeAttackPower`) |
| Power | `anotherspirerework:CreativeAIUpgraded` | Creative AI+ (hands out upgraded Power cards) |
| Power | `anotherspirerework:BattleHymnSmite` | Battle Hymn |
| Power | `anotherspirerework:LikeWaterCalm` | Like Water |
| Power | `anotherspirerework:StudyInsight` | Study |
| Power | `anotherspirerework:DevotionDraw` | Devotion |
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

Java 8 bytecode is produced (`maven.compiler.source/target = 1.8`).
Copy `target/anotherspirerework.jar` into the game's `mods/` folder manually; deployment is skipped by default.
Start ModTheSpire with Java 8, mod id `anotherspirerework`, dependencies: BaseMod only.
