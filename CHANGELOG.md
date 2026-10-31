> 本文件是 AnotherSpire Rework 的历史修复记录（按时间倒序），末尾附「实现与维护参考」。
> 改动清单见 [README.md](README.md)。


# AnotherSpire Rework

## 第十批：小刀卡面文案与「死吧死吧死吧」同时结算（2026-10-31）

### 小刀卡面文案跟随两个能力

- **现象**：乾坤一掷（`Unload`）让小刀获得保留、死吧死吧死吧（`Die Die Die`）让小刀攻击所有敌人，但小刀卡面始终是原版「造成 n 点伤害。 NL 消耗 。」，玩家看不出这两个能力已经生效。
- **实现**：新增 `patches.ShivDescriptionPatch`，给 `AbstractCard.initializeDescription` 加前缀：小刀实例的文案改从本模组 `anotherspirerework:Shiv` 条目取——
  - 仅乾坤一掷：`保留 。 NL 造成 !D! 点伤害。 NL 消耗 。`
  - 仅死吧死吧死吧：`对所有敌人造成 !D! 点伤害。 NL 消耗 。`
  - 两者兼有：`保留 。 NL 对所有敌人造成 !D! 点伤害。 NL 消耗 。`
  伤害数字仍由 `!D!` 在渲染时取值；没有能力时读取原版 `Shiv` 字符串并按原版文案恢复，不覆盖原版本地化。两个能力的 `onInitialApplication` / `onRemove` 都会刷新手牌、抽牌堆、弃牌堆与消耗堆里的所有小刀（新生成的小刀在构造时就会带正确文案）。`onRemove` 在移除动作把能力移出列表之前触发，因此两个能力各带一个 `isActive()` 标记，移除时不再写自己的那一行。
- **验证**：`PatchContractTest` 增加文案变体映射断言（无/保留/全体/两者 → `-1/0/1/2`）。

### 死吧死吧死吧：所有敌人同时结算

- **之前**：`ShivPatch` 对每个敌人各排一个 `ShivDamageAction`，小刀依次命中，动画与结算都是一个敌人接一个敌人。
- **现在**：新增 `actions.ShivDamageAllAction`（继承原版 `DamageAllEnemiesAction`），一次 action 对全部敌人同时结算，类似顺劈斩；攻击特效仍传 `AttackEffect.SLASH_HORIZONTAL`（小刀自己的攻击特效），不换成顺劈斩的 `CleaveEffect`。涂毒仍按敌人逐个排 `ApplyPowerAction`（原版 `ApplyPowerAction` 会跳过已死亡的敌人）；玻璃刀刃的格挡奖励改为在该 action 结算完成后，按「实际掉血」的敌人逐个补发，语义与单体 `ShivDamageAction` 一致。
- **验证**：`PatchContractTest` 断言 `ShivDamageAllAction` 继承 `DamageAllEnemiesAction`、`update` 调用 `rewardGlassKnife`、`rewardGlassKnife` 调用 `getPower` 与 `GainBlockAction`，以及 `ShivPatch.allEnemies` 使用新 action 与 `PoisonPower`。

验证：`mvn -B clean package`（JDK 8）通过（255 个源文件，127 个补丁目标，`PatchContractTest`、`SaturatingMathTest` 全部通过）；`verify.ps1 -CheckJar` 通过。
仍需游戏内验收：持有乾坤一掷/死吧死吧死吧/两者时，手牌、抽牌堆、弃牌堆、消耗堆里的小刀文案；打出死吧死吧死吧后新生成的小刀；多敌人战斗里小刀同时命中全部敌人、只播一次小刀特效；玻璃刀刃与涂毒在这套同时结算下的表现；能力被移除后文案恢复。

## 第九批：重启 + 羽化：选中的牌被随后洗回抽牌堆（2026-10-31）

### 重启 + 羽化：先弹选牌、后洗手牌

- **现象**：持有「羽化」时打出「重启」，羽化的选牌界面先弹出；选中的牌加入手牌后，「重启」才把手牌洗入抽牌堆，于是选牌没有任何实际效果。弃牌堆为空时必现（例如第一回合手牌里就有重启）。
- **原因**：`ShuffleAllAction`（重启）只负责弃牌堆：它在第一次 `update` 里把「手牌 → 抽牌堆」的 `PutOnDeckAction` 用 `addToTop` 排进队列，重启随后再排一个 `ShuffleAction`。旧实现只要 `ShuffleAllAction.isDone` 就置 `pendingShuffle`，而它结束的那一帧 `PutOnDeckAction` 还没轮到；弃牌堆为空时没有洗牌动画（`SoulGroup`）可等，`receivePostDungeonUpdate` 里的 `tick()` 当帧就 `fire()`，选牌动作于是插到了 `PutOnDeckAction` 前面。结果「选牌 → 加入手牌」先结算，紧接着 `PutOnDeckAction` 把整只手牌（含刚选中的牌）搬回抽牌堆。弃牌堆非空时只是被洗牌动画挡住，所以此前没暴露。
- **修复**：`ShuffleTriggerPatch` 把重启的洗牌拆成三步：`ShuffleAllAction` 结束只置 `waitingForHandOnDeck`；`PutOnDeckAction` 结算后再置 `waitingForFinalShuffle`；重启的 `ShuffleAction` 结算后才置 `pendingShuffle`。`flush()` 在这两个标记清空前一律不触发。若某条流程没有补 `ShuffleAction`，紧随其后的 `DrawCardAction` 会把它当作已完成（此时排在它前面的动作都已结算）。
- **验证**：`PatchContractTest` 新增 `rebootShuffleOrderRegression`——`All.after` 只能写 `waitingForHandOnDeck`、不能直接写 `pendingShuffle`；`HandOnDeck` / `FinalShuffle` 分别挂在 `PutOnDeckAction.update` / `ShuffleAction.update` 上并写各自标记；`flush` 必须同时读这两个标记。

验证：`mvn -B clean package`（JDK 8）通过（253 个源文件，126 个补丁目标，`PatchContractTest`、`SaturatingMathTest` 全部通过）；`verify.ps1 -CheckJar` 通过。
仍需游戏内验收：第一回合（弃牌堆为空）持有羽化打重启，选牌应出现在手牌已洗入抽牌堆之后、且选中的牌留在手里；弃牌堆非空时行为不变；羽化叠加到 2 层时同样只弹一次界面。

## 第八批：小屋子取消选牌恢复「回忆」、星盘放开 HEALING、小屋子文案补注（2026-10-31）

### 小屋子：取消选牌后「回忆」仍置灰

- **现象**：在休息处点「锻造」（抽烟/净化的选牌界面同理）后点取消，营火回来了，但「回忆」按钮仍是灰的、点不了。
- **原因**：`TinyHouse.arm` 在点任意其它选项时把「回忆」置灰；取消时 `CampfireUI.reopen` 的后缀只退还了刚点的选项（`lastUsed`），没有把「回忆」还回去，于是它一直保持 `usable = false`。
- **修复**：新增 `recallLockedByArm`，记录「这一次点击是否由本选项锁掉了回忆」。`CancelledChoice` 退还选项时只在本次锁掉的情况下恢复「回忆」；此前已经真正用掉其它选项而锁住的「回忆」不会被取消解锁。`arm`/`tick`/`reset` 同步清理该标记。
- **验证**：`PatchContractTest` 新增「取消后退还选项与回忆」的断言（`CancelledChoice.after` 至少两次 `usable` 写入）。

### 星盘：允许出现 HEALING 卡

- **之前**：`ChooseDeckCardsEffect.isPickable` 排除带 `AbstractCard.CardTags.HEALING` 的牌，40 张候选里永远没有狂宴、死亡收割、自我修复、许愿、包扎、噬咬等 HEALING 牌。
- **现在**：不再按该标签过滤。星盘是把选中的牌**加入牌组**、不在战斗内生成；原版 `CardLibrary.getAnyColorCard` 本身不过滤 HEALING，过滤只来自本模组。基础/特殊稀有度、状态与诅咒仍排除。
- **验证**：`PatchContractTest` 反向断言 `isPickable` 不再调用 `contains`；`verify.ps1` 断言该源码不再出现 `CardTags.HEALING`。

### 本地化

- 小屋子：`你可以在休息处选择任意数量的选项。（不包括回忆）` / `You may choose any number of options at Rest Sites. (Recall excluded)`；中英 key/字段不变，`DESCRIPTIONS[1]` 仍为 flavor。
- README 中英两张遗物表同步（小屋子补「不包括回忆」，星盘去掉 HEALING 排除）；`verify.ps1` 增加「小屋子描述必须提到回忆」的双语断言。

验证：`mvn -B clean package`（JDK 8）通过（253 个源文件，124 个补丁目标，`PatchContractTest`、`SaturatingMathTest` 全部通过）；`verify.ps1 -CheckJar` 通过。
仍需游戏内验收：真实营火里「锻造→取消」后回忆可点、此前已用过选项的营火里回忆仍灰；星盘 40 张候选里确实能刷出 HEALING 牌并正常加入牌组。

## 第七批：遗物改为「一件遗物一个类」（2026-10-30）

遗物实现从「包装类 + 分散在原版类上的补丁」改为与卡牌一致的范式：每件被重写的遗物在
`src/main/java/anotherspirerework/relics/<名称>.java` 里一个类，继承原版遗物类，只覆写改动的钩子；
`patches/RelicSwapPatch` 在 `RelicLibrary.initialize()` 之后把原版原型替换为这些类（保留原版 ID、图片、
品级与全部获取路径）。共 **45** 个遗物类：40 件行为重写（含送货员的隐藏补货）、化学物 X 与李家华夫饼的
生成条件、瓶装三遗物的幕数门槛。

- 删除旧实现：`patches/RelicReworkPatch`、`NewRelicFixes`、`RareRelicFixes`、`ReportedRelicFixes`、
  `TurnRelicFixes`、`StrangeSpoonExhaust`、`TinyHouseCampfirePatch` 与 `relics/ReworkedRelic`。
- 跨遗物的规则保留为独立补丁：`RelicSwapPatch`（替换表）、`ActFourRelicBlockPatch`（27 件不进第四幕）、
  `RelicTierPatch`（旧存档遗物池修复）、`ShopRelicPatch`（遗物池抽取与条件遗物保留）、`ShopPricePatch`。
- 原版写在别处（`hasRelic` 判断、伤害管线、奖励界面、洗牌事件）的行为放进对应遗物类的嵌套补丁；
  存档字段类改为 `relics.BlackStar.SaveData`、`relics.PrismaticShard.StatMigration`、
  `relics.StrangeSpoon.SaveData`、`relics.SneckoEye.Migration`（字段键不变，旧存档可继续读）。
- 顺手修正：`scripts/verify.ps1 -CheckJar` 与文档里的产物名改为实际的 `target/anotherspire.jar`。

验证：`mvn -B clean package`（JDK 8）通过（SaturatingMathTest、PatchContractTest 124 个补丁目标），
`verify.ps1 -CheckJar` 通过。仍需游戏内验收：读档后的遗物文案/品级/池、N'loth 事件的直接构造、
小屋子营火流程、勺子的跨存档恢复。

## 第六批：新增串刺/转化（2026-10-24）

| 卡牌 | 原版 ID | 改动 | 实现 |
|---|---|---|---|
| 串刺 | `Skewer` | X 费，造成 9（12）点伤害 X 次 | 复用原版 `SkewerAction`；升级只 `upgradeDamage(3)` |
| 转化 | `Transmutation` | 1（0）费，消耗所有手牌；每消耗一张牌，将一张随机牌加入手牌 | 新增 `ExhaustAllHandAction` 报告实际消耗张数；随机牌沿用枯木树枝的 `AbstractDungeon.returnTrulyRandomCardInCombat()` + `MakeTempCardInHandAction`，手牌满时按原版规则进入弃牌堆 |

两者都是原版卡「串刺」「转化」的替换（需求中的「新增」指新增条目），沿用原版 ID 与图片路径。替换表 128 → **130**。

验证：`mvn -B clean package`（JDK 8）编译 210 个源文件，202 个补丁目标与全部契约测试通过；`verify.ps1 -CheckJar` 通过。
仍需游戏内验收：串刺的 X 次数与化学物 X 加成、转化在满手牌时的替换与弃牌堆溢出、以及枯木树枝同时持有时的双倍印牌。

## 第五批反馈修复（2026-10-17）

| 项 | 之前 | 现在 | 实现 |
|---|---|---|---|
| 立地升天 | 击杀判定照搬狂宴（爪牙、半死不算），文本「斩杀」 | 照搬原版**分离**：目标 `isDying` 或生命 ≤ 0 即进入神格；文本同分离「如果这张牌杀死了敌人」 | `ReachHeavenAction` 改为 `SunderAction` 的写法（`ACTION_DUR_FASTER`、`addToBot`） |
| 羽化 | 叠加后仍按原版 `SeekAction` 必须选满 n 张，描述不显示数量 | 最多选 n 张；buff 描述与选牌界面提示都显示 n | 新增 `ShuffleSeekAction`：`gridSelectScreen.open(group, n, true, msg)`（原版"任意张数"模式），提示语放在羽化的 `EXTENDED_DESCRIPTION`；手牌上限改用 `BaseMod.MAX_HAND_SIZE` |
| 涅槃 | 计数器只从能力生效后开始数，打出第二张时数值不统一 | 计数器 = 原版 `cardsPlayedThisTurn`（含涅槃本身与之前打出的牌），多张共用；在 `onAfterUseCard` 判定，第 5 张打出的是涅槃时按叠加后层数立即结算；已结算后再叠加的涅槃在 `stackPower` 里补上自己的 2 张 | `NirvanaCounterPower` |
| 壶铃 | 举重按钮仍显示原版「（最多+3）」 | 「在战斗开始时获得+1力量和+1敏捷。（最多+5）」 | `NewRelicFixes$GiryaLiftText`（`LiftOption` 构造器后缀）；新增 `localization/{eng,zhs}/UIStrings.json` 并由主类加载，`verify.ps1` 的双语检查纳入 UIStrings |
| 如水 | buff 显示叠加层数 | 不显示计数器（`amount = -1`，`stackPower` 不改变它） | `LikeWaterCalmPower` |
| 结茧 | buff 描述固定为一个 [E] | 描述显示叠加后的能量数（同原版能量提升的写法） | `ShuffleEnergyPower.updateDescription` |

羽化体验优化：洗牌事件现在把被打断的抽牌还要抽的张数（按手牌上限截断）传给监听者（`ShuffleTriggerPatch.drawsAfterShuffle()`）；
抽牌堆张数不超过这个数时，接下来的抽牌本来就会把整个抽牌堆抽进手牌，`ShuffleSeekPower` 不再弹出选牌界面。

### 小屋子闪光

- **保留正常拾取闪光**：原版 `instantObtain()` 给每件遗物的那一次闪光原样保留。
- **删除额外闪光**：`TinyHouseEquip` 不再自己 `flash()`，拾取时只闪一次。
- **闪光时机改到休息处**：新增 `TinyHouseCampfirePatch$RestRoomPulse`（`AbstractRelic.onEnterRoom` 后缀），进入 `RestRoom` 时 `beginLongPulse()` + `flash()`，与送货员/会员卡在商店里一样持续脉冲；进入其它房间（含离开休息处）时 `stopPulse()`。存档并在休息处读档时同样会重新点亮。
- **回忆与其它选项互斥**：`TinyHouseCampfirePatch.arm` 认出 `RecallOption`——选「回忆」时不调用 `reopen`，营火与选项按原版消失；先用了其它选项，则把「回忆」置灰（`usable = false`）。
- **删除旧奖励界面代码**：原 `effects/TinyHouseEffect`（拾取时升级 1 张牌并打开 3 组无色卡奖励的 `combatRewardScreen.open`）已删除；`TinyHouseEquip` 前缀继续拦截原版 `TinyHouse.onEquip` 的金币/药水/无色牌奖励，只保留本模组的自由营火。

验证：`mvn -B clean package`（JDK 8）202 个补丁目标与全部契约测试通过；`verify.ps1 -CheckJar` 通过。

## 第五批：供奉/天眼/欺瞒现实/堆栈调整，替换审判/立地升天/摆手/暴力，修复义愤填膺结算顺序（2026-10-17）

> 此前同日的两轮实现（含小屋子拾取不闪光、星系仪弹窗等待两项附带修复）未符合需求，已整体撤回后按需求重做。

### 卡牌

| 卡牌 | 原版 ID | 改动 | 实现 |
|---|---|---|---|
| 供奉 | `Consecrate` | 1（0）费，每命中一名敌人抽 1 张 | 升级只 `upgradeBaseCost(0)` |
| 天眼 | `ThirdEye` | 2（3）张洞见放入抽牌堆 | 同怒火中烧，`MakeTempCardInDrawPileAction(…, true, true)` |
| 欺瞒现实 | `DeceiveReality` | 洞见与奇迹放入抽牌堆 | 同上 |
| 堆栈 | `Stack` | 1（0）费，格挡 = 抽牌堆张数（取消升级 +3）；洗牌时把这张牌放到抽牌堆顶部 | `Stack.moveToTopOfDrawPile()`，由 `ShuffleTriggerPatch.fire()` 在洗牌动画结束、剩余抽牌之前调用 |
| 审判 | `Judgement` | 3 费；目标无人工制品且生命值低于最大生命值的 20%（25%）时生命值变为 0 | `PercentJudgementAction`：原版 `JudgementAction` 的百分比版本，仍用 `InstantKillAction` 与原版特效 |
| 立地升天 | `ReachHeaven` | 3 费，15（20）伤害，斩杀时进入神格；不再加入以暴易暴 | `ReachHeavenAction`：原版 `FeedAction` 的斩杀判定（不含爪牙与半死状态） |
| 摆手 | `WaveOfTheHand` | 1（0）费，本回合每次切换姿态给予所有敌人 2 层虚弱；消耗 | `WaveOfTheHandStancePower` 继承原版 `WaveOfTheHandPower`（沿用其 ID、图标与回合结束移除），只把触发从获得格挡改为切换姿态 |
| 暴力 | `Violence` | 3 费，随机打出抽牌堆中 3（4）张攻击牌，不再消耗 | `PlayRandomAttackFromDrawPileAction`：原版 `PlayTopCardAction`（破灭/乱战）的随机攻击牌版本；抽牌堆没有攻击牌时不洗牌 |

摆手是原版卡「摆手」的替换（需求中的「新增」指新增条目），因此沿用原版 ID，不使用命名空间 ID。替换表 124 → **128**。

### 机制：义愤填膺 + 心灵堡垒 + 死亡律动

原版 `IndignationAction` 用 `addToBot` 排队姿态变化，排在该牌自己的 `UseCardAction` 之后；`UseCardAction` 触发死亡律动的伤害，
于是心灵堡垒的格挡比伤害晚。暴怒直接在 `use()` 里排队姿态变化，没有这个问题。`patches/IndignationStancePatch`（Instrument）
只把 `IndignationAction.update` 里的 `ChangeStanceAction` 改为 `addToTop`，结算顺序与暴怒一致；不改心灵堡垒本身。

### 验证

- `mvn -B clean package`（JDK 8）：**200 个补丁目标**、`PatchContractTest` 全部通过（新增 `fifthRoundRegression`）。
- `scripts/verify.ps1 -CheckJar`：双语键/词元、**128 张唯一替换**、卡图与 JAR 条目通过。
- 仍需游戏内验收：义愤填膺 + 心灵堡垒对腐化之心；堆栈在洗牌后（含羽化/套娃同时触发）位于抽牌堆顶；暴力连续打出 3（4）张及目标随机；
  立地升天击杀爪牙/觉醒者第一形态时不进神格；审判对有人工制品的敌人无效。

## 第四批：残影/诸神之黄昏/残虐天性调整、小屋子重做、皇家枕头回调（2026-10-17）

### 卡牌

| 卡牌 | 之前 | 现在 | 依据 |
|---|---|---|---|
| 残影（`Blur`） | 0 费，格挡 1（2） | **1（0）费**，格挡固定 **1** | 需求第 29 条（猎手）`（修改）` |
| 诸神之黄昏（`Ragnarok`） | 所有命中一次性入队，无逐击间隔 | 每击前 `WaitAction(0.1F)`（`FAST_MODE` 跳过） | 需求第 27 条（观者）“减缓动画速度，和原版一致” |
| 残虐天性（`Sadistic Nature`） | power 的 `amount = 1`，buff 图标显示“1” | `amount = -1`，图标不显示计数器 | 需求第 16 条（无色）“相关 buff 不显示计数器” |

实现要点：

- **残影**：`cards/green/Blur` 新增 `BASE_COST = 1`、`UPGRADED_COST = 0`、`BLOCK_PER_DRAW = 1`；
  升级只调 `upgradeBaseCost(0)`，不再 `upgradeMagicNumber`，文本仍是 `!M!`（恒为 1），无需 `UPGRADE_DESCRIPTION`。
- **诸神之黄昏**：`actions/RagnarokXAction` 新增 `HIT_PAUSE = 0.1F`，在每次
  `AttackDamageRandomEnemyAction(LIGHTNING)` 之前排队 `WaitAction`——与原版 `actions.defect.NewThunderStrikeAction`
  对随机闪电的节奏写法一致；伤害与命中数（X×2 / X×2 次）不变。
- **残虐天性**：`powers/SadisticNaturePower` 新增 `ICON_AMOUNT = -1`，构造器与 `stackPower` 都用它；
  原版 `AbstractPower.renderAmount` 只在 `amount > 0`（或允许负数）时画数字，因此图标保留、计数器消失。
  翻倍仍由 `patches/SadisticNaturePatch` 在 `WeakPower`/`VulnerablePower` 上完成，叠加纸鹤/纸蛙不受影响。

### 遗物：小屋子重做、皇家枕头回调

- **小屋子**（商店遗物）**重做**：删除“拾起时升级 1 张牌 + 3 组无色牌奖励”的 `effects/TinyHouseEffect`；
  新效果为**在休息处可以选择任意数量的选项**。
  实现见新增的 `patches/TinyHouseCampfirePatch`：不复制任何营火结算，只用
  `AbstractCampfireOption.update` / `CampfireUI.updateTouchscreen` 捕捉“这一帧用掉了哪个选项”，把它置灰，
  等该选项的特效与界面结束后（主类 `receivePostDungeonUpdate` 里的 `tick()`）用 `CampfireUI.reopen()`
  把营火放回去，并把房间标记为完成，地图/继续按钮照常是离开营火的路。
  `RareRelicFixes$TinyHouseEquip` 只保留拾取闪光；品级（商店）与旧存档搬池逻辑不变。
  在选牌界面点取消会退还该选项（取消时原版自己调 `reopen()`，据此判断）。
- **皇家枕头** **回调**：删除 `patches/RegalPillowPatch`（自由休息、接管回血、捕梦网、描述覆盖）与
  中英 `RelicStrings` 的 `anotherspirerework:Regal Pillow` 条目；回到原版“休息额外回复 15 点生命”，
  仍按需求第 23 条不进第四幕。

### 本地化

- `Tiny House`：`你可以在休息处选择任意数量的选项。` / `You may choose any number of options at Rest Sites.`
  （`DESCRIPTIONS[1]` 保留“小小的屋子！”作为 flavor）。
- 删除 `Regal Pillow` 条目（双语同步），`verify.ps1` 新增“该键必须不存在、小屋子必须提到休息处”的断言。
- 残影、诸神之黄昏、残虐天性的文本无需改动（数值/节奏/图标表现变化，词元不变）。

### 验证

- `scripts/verify.ps1`：双语键/词元、124 张唯一替换、小屋子与皇家枕头的新断言。
- `mvn -B clean package`（JDK 8）：**199 个补丁目标**、全部契约测试通过（新增 `fourthRoundRegression`）。
- 仍需游戏内验收：残影 1（0）费与回合内抽牌结算、诸神之黄昏在 X=1/2/3 与化学物 X 下的逐击节奏、
  残虐天性图标无数字且与纸鹤/纸蛙叠加、小屋子的营火（休息/锻造/抽烟/举重/挖掘各一次、界面关闭后才放回营火、
  最后能正常离开）、皇家枕头回到原版行为。

## 第三批：7 张新卡、3 项遗物改动与商店降价（2026-10-16）

### 卡牌

- **新增 7 张**（沿用原版 ID 与原版图片路径，进入 `CardSwapPatch` 替换表，总数 117 → **124**）：
  - 静默猎手：猎杀者（`Predator`，稀有度改普通，2 费，15（18）点伤害 + 下回合多抽 2（3）张）、
    突然一拳（`Sucker Punch`，1 费，7（10）点伤害 + 2 层虚弱，升级只加伤害）、
    残影（`Blur`，0 费，本回合内每抽 1 张牌获得 1（2）点格挡）、
    弹跳药瓶（`Bouncing Flask`，X 费，随机给予敌人 6 层中毒 X（X+1）次）
  - 观者：疾风连击（`FlurryOfBlows`，0 费，4（6）点伤害；转换姿态时从弃牌堆/抽牌堆/消耗堆回到手牌）、
    诸神之黄昏（`Ragnarok`，X 费，随机对敌人造成 X×2 点伤害 X×2 次，消耗；升级取消消耗）
  - 无色：残虐天性（`Sadistic Nature`，2（1）费能力牌，敌人身上的虚弱与易伤效果翻倍，可与纸鹤/纸蛙叠加）
- **修改 3 张**：灾祸（`Bane`，稀有度普通 → 罕见，伤害 7（10）→ **8（10）**）、
  飞膝（`Flying Knee`，伤害 10（12）→ **8（11）**）、
  催化剂（`Catalyst`，升级奖励由“中毒三倍”改为**保留**，仍是 1 费翻倍 + 消耗）

### 校对：本轮核对代码与需求/文档时发现并修正的偏差

| 项 | 之前 | 现在 | 依据 |
|---|---|---|---|
| 致命毒药 | 中毒 6（9） | 中毒 **7（9）** | 需求第 23 条与第二批记录都是 7（9），代码少 1 |
| 飞膝 | 伤害 10（12） | 伤害 **8（11）** | 需求第 25 条与第二批记录 |
| 灾祸 | 伤害 7（10）、普通 | 伤害 **8（10）**、**罕见** | 需求第 24 条 |
| 交锋（README 行） | 14（18） | 10（14） | 代码与需求一致，README 过期 |
| 活动肌肉（README 行） | 0 费、4（8） | 1（0）费、5 | 同上 |
| 断魂斩（README 行） | 16 | 10 | 同上 |
| 旋风斩（README 行） | 6（10） | 6（7） | 同上 |
| 灼热攻击（README 行） | 10（13） | 7（10） | 同上 |
| 玻璃刀刃（README 行） | 8 点伤害 | 4（6）点伤害 | 同上 |
| 扫荡射线（README 行） | 6（7）伤、抽 2（3） | 10（14）伤、抽 1（2）、加黏液 | 同上 |
| 精简改良（README 行） | 18（22） | 22（28） | 同上 |
| 快斩（README 行） | 8 点伤害 | 10（14）点伤害 | 与代码一致；需求写 8，按“仅数值差异以模组为准”保留实现 |

其余 124 张的数值已逐张对着 `baseDamage/baseBlock/baseMagicNumber` 与升级增量核对过，与需求一致。

### 遗物与机制

- **皇家枕头**：休息成为自由行动。休息照常结算（最大生命 30% + 15、`onRest`、捕梦网奖励），
  但营火不结束，休息后仍可选升级/抽烟/举重/挖掘；房间标记为完成，地图按钮照常是离开营火的路。
- **瓶装旋风 / 瓶装闪电 / 瓶装火焰**：原效果不变，仅第三幕起才可能从遗物池抽出；
  第一、二幕抽到它们时不消耗池位（留在可存档的池里），其余 `canSpawn` 条件仍按原版处理。
- **商店遗物降价**：罕见遗物基准价 −50、稀有遗物 −100（250 / 300 → 200 / 200）。

### 反馈修复（本轮）

1. **皇家枕头休息不回血**：自由休息原来把回血排进了 Action 队列（`HealAction`），但
   `GameActionManager.addToBottom` 只在房间处于 `RoomPhase.COMBAT` 时才会入队，营火房间下动作被**静默丢弃**，
   于是休息只闪了一下遗物、生命没变。现在改为直接 `AbstractPlayer.heal(30% + 15, false)`，
   与 `CampfireSleepEffect` 的写法一致；契约测试新增断言：`before` 必须调用 `heal`、且不得再构造 `HealAction`。

### 落点（新增文件）

| 文件 | 作用 |
|---|---|
| `cards/green/Predator`、`SuckerPunch`、`Blur`、`BouncingFlask` | 第三批的猎手牌 |
| `cards/purple/FlurryOfBlows`、`Ragnarok` | 第三批的观者牌 |
| `cards/colorless/SadisticNature` | 第三批的无色牌 |
| `powers/BlurDrawPower` | 残影：本回合抽牌给格挡，回合结束自我移除 |
| `powers/SadisticNaturePower` | 残虐天性的开关与文本 |
| `actions/BouncingFlaskXAction` | 弹跳药瓶的 X 次结算（交给原版 `BouncingFlaskAction`） |
| `actions/RagnarokXAction` | 诸神之黄昏的 X×2 次随机打击（交给原版 `AttackDamageRandomEnemyAction`） |
| `actions/ReturnCardToHandAction` | 从抽牌堆/消耗堆回到手牌（原版只有弃牌堆版本） |
| `patches/FlurryOfBlowsPatch` | 姿态变化时把疾风连击从抽牌堆/消耗堆取回 |
| `patches/SadisticNaturePatch` | 虚弱/易伤的伤害钩子翻倍 |
| `patches/RegalPillowPatch` | 皇家枕头的自由休息、描述与捕梦网（**第四批已删除**，机制改由小屋子承担） |
| `patches/ShopPricePatch` | 商店遗物折价 |

### 需要你提供的卡图

这 7 张都沿用原版类型（攻击/技能/能力不变），原画不会露框，**不需要新卡图**。
想换的话按本文件末尾的「卡图」放图即可。

### 验证

- `mvn -B clean package`（JDK 8）：199 个补丁目标、全部契约测试通过（新增 `thirdRoundRegression`）。
- `scripts/verify.ps1 -CheckJar`：双语键/词元、124 张唯一替换、卡图与 JAR 条目全部通过。
- 仍需游戏内验收：残影的抽牌结算时机、弹跳药瓶与诸神之黄昏的 X 费（含化学物 X）、
  疾风连击从消耗堆回手、残虐天性与纸鹤/纸蛙/其它模组伤害修正的叠加、皇家枕头的营火流程、
  瓶装三遗物的幕数门槛。

## 第二批：新增 21 张卡与 12 件遗物（2026-10-11）

### 卡牌

- **新增 21 张**（沿用原版 ID 与原版图片路径，进入 `CardSwapPatch` 替换表，总数 96 → **117**）：
  - 铁甲战士：恶魔形态（`DemonForm`，能力牌，3 费，虚无；消耗堆每有一张牌，对敌伤害 +1，升级去掉虚无）
  - 静默猎手：致命毒药（`Deadly Poison`，中毒 7（9），消耗）、灾祸（`Bane`，伤害 8（10）；目标中毒时把未被格挡的伤害转为等量中毒）、飞膝（`Flying Knee`，伤害 8（11）；目标虚弱时伤害翻倍）、催化剂（`Catalyst`，原效果不变，稀有度改普通）
  - 故障机器人：暴风雨（`Tempest`，X 费，生成 X（X+1）个闪电，**不消耗**）、超频（`Steam Power`，原效果不变，稀有度改普通）
  - 观者：流云飞袖（`FlyingSleeves`，保留，2 点伤害 4（5）次）、腰带抽打（`SashWhip`，0 费，4（5）点伤害，平静时给予 1（2）层虚弱）、评估（`Evaluate`，8（11）点格挡，加一张洞见进抽牌堆）、涅槃（`Nirvana`，能力牌，回合内打出第 5 张牌时下回合抽 2 张，升级获得固有）、精神护盾（`SpiritShield`，技能→能力牌，2（1）费，平静时受攻击伤害减半，按层数 1/2ⁿ 叠加）、化体为空（`EmptyBody`，技能→能力牌，罕见，回合结束时退出姿态并获得 7（9）点格挡）、化智为空（`EmptyMind`，原效果不变，稀有度改普通）
  - 无色：花样百出（`Jack Of All Trades`，技能→能力牌，2（1）费，每花费 4 点能量获得 1 点）、亮剑（`Flash of Steel`，5（8）点伤害并抽 1 张）、妙计（`Finesse`，3（5）点格挡并抽 1 张）、迅捷打击（`Swift Strike`，7（10）点伤害，随机攻击牌入手并带虚无+消耗）、优秀直觉（`Good Instincts`，6（9）点格挡，随机技能牌入手并带虚无+消耗）、心灵震慑（`Mind Blast`，1（0）费，固有，伤害等于牌组张数）、闪亮登场（`Dramatic Entrance`，固有，10（12）点全体伤害，消耗）
- **修改 9 张**：活动肌肉（1（0）费，5 点临时力量）、弹回（复制保留黑暗充能球数值）、扫荡射线（升级 +4 伤害，弃牌堆加黏液）、蒸汽护壁（抽牌堆加晕眩）、飞跃（改罕见）、狂乱撕扯（2（1）费，消耗全部状态牌，每张随机 7 点伤害）、多重释放（激发**所有**充能球 X（X*2）次）、雷霆打击（消耗 1 费 3 点 → 3 费消耗、升级去消耗）、精简改良（18（22）点伤害）。

### 遗物

- **品级变化**：棱镜碎片 → BOSS 遗物；小屋子 → 商店遗物（不额外限制生成层数）。品级在遗物构造器里改写，遗物池、宝箱与图鉴都跟着走；**旧存档的遗物池**需要单独修复，见下面的「修复：旧存档的遗物池没有跟着品级变化走」一节。
- **勇气投石索**：1 点力量 + 1 点敏捷（中英文本地化同步，含 `ReworkedRelic` 与原生实例两条路径）。
- **星系仪**：修掉 `#b`（`、#b10` 改为 `、 #b10`）并同步英文描述。
- **机械臂**：恢复每 2 回合计数，并且额外生成一个**随机充能球**（`Inserter` 的 `onEquip` 把计数归零）。
- **新增 12 件**（全部通过补丁改造原版遗物，不注册新遗物）：
  - 星盘（40 张任意颜色牌任选，排除基础/特殊稀有度、状态、诅咒与 HEALING）
  - 小屋子（先升级 1 张牌，再给 3 组无色牌奖励，奖励界面沿用原版“小屋子”标题）
  - 紫色莲花（离开平静额外抽 1 张）
  - 牧师的脸（每场战斗后 +2 最大生命）
  - 异蛇之眼（每回合多抽 **3** 张；存档迁移 `SneckoHandV1` 为旧存档补上第 3 个手牌位）
  - 扭曲漏斗（中毒多触发 2 次；血条绿色长度按 7+6+5 预览）
  - 美琅脂（打出无色牌获得 3 点格挡；原版洗牌预见被替换）
  - 生物样本（中毒伤害扩散给其他敌人；原版死亡转移被替换）
  - 转经轮（精英与 BOSS 额外多一组卡牌奖励，BOSS 因此是 2 组稀有）
  - 黄金眼（每回合开始预见 3；不再给其他预见 +2）
  - 针线（每回合开始获得 2 点多层护甲）
  - 恩洛斯的礼物（精英额外掉落一组稀有牌；不再提高稀有牌出现率）

### 需求澄清（本轮确认）

| 项 | 采用 |
|---|---|
| 扭曲漏斗 | 额外触发 **2** 次，例：7 层 → 失去 7+6+5，剩 4 层；血条同步预览 |
| 涅槃 | 回合内打出**第 5 张**牌时触发 |
| 致命毒药 | 中毒 7（**9**） |
| 化体为空 | 格挡 7（**9**） |
| 星盘 | **不出现**基础牌（也不出现特殊/状态/诅咒/HEALING） |
| 美琅脂 | 状态牌与诅咒牌不算无色牌 |

### 反馈修复（本轮）

1. **黄金眼**：第一回合原来是“先抽牌、后预见”，因为基础游戏把起手抽牌排在回合开始遗物之前。现在改为在**抽牌之前**先预见（`applyStartOfCombatPreDrawLogic` 的钩子），紧接着的回合开始跳过一次，第一回合不会预见两次；第二回合起维持原样。抽牌堆为空时仍先洗牌再预见。
2. **恩洛斯的礼物**：精英掉落的额外稀有牌奖励现在把奖励的 boss 标记置为 true，图标与 BOSS 的稀有牌奖励一致（金色）。该标记只影响图标，不影响抽卡逻辑；转经轮额外给的那组是普通牌，保持普通图标。
3. **蒸汽护壁**：去掉“消耗”（`requirements.txt` 第 68 行已同步删除）。中英文描述都删掉“消耗 / Exhaust”，`UPGRADE_DESCRIPTION` 一并移除；`verify.ps1` 的断言由“升级版必须写消耗”改为“两种语言都不得出现消耗”。
4. **能力图标**：新增 `patches/PowerArtLoaderPatch`，见下一节。

### 需要你提供的卡图（改了类型，原画会露框）

只有把 500×380（或 250×190）图片放进 `src/main/resources/anotherspirerework/images/` 才会替换原版卡图，
文件名用卡牌 ID 去空格或原版图片名都可以：

| 卡牌 | 新类型 | 文件名 |
|---|---|---|
| 精巧刺击 | 技能 | `masterful_stab.png` / `MasterfulStab.png` |
| 改造现实 | 技能 | `carve_reality.png` / `CarveReality.png` |
| 精神护盾 | 能力 | `spirit_shield.png` / `SpiritShield.png` |
| 化体为空 | 能力 | `empty_body.png` / `EmptyBody.png` |
| 花样百出 | 能力 | `jack_of_all_trades.png` / `JackOfAllTrades.png` |

已提供的 9 张（跃跃欲试、乾坤一掷、死吧死吧死吧、熔化、核心电涌、重编程、结茧、羽化、暴雪）保持不变。

### 能力图标（可选，同样是放图才生效）

本模组自己新增的能力目前借用原版图标。想换的话，把图标放进：

```
src/main/resources/anotherspirerework/images/powers/
    <名字>_48.png    48×48，buff 栏里的小图标
    <名字>_128.png   128×128，获得 / 触发时的放大闪光
```

- 两种尺寸**互不影响**：只给 `_48` 时放大闪光仍是原版图标，只给 `_128` 时 buff 栏仍是原版图标；两个都不给就用现在的图标。
- 游戏是用固定缩放绘制这两个图标的（1.17 倍与 3 倍），所以文件尺寸必须与后缀一致：`_48` 必须 48×48，`_128` 必须 128×128，PNG 需正方形、透明背景。
- `<名字>` 可以写能力的 ID 去掉 `anotherspirerework:` 前缀（下表第一列），也可以写类名（如 `NirvanaCounterPower`）。
- 只处理本模组自己的能力；原版能力（力量、多层护甲……）即使撞名也不会被替换。
- `scripts/verify.ps1` 会检查尺寸与命名，对不上或名字不认识会给出警告。

| 文件名前缀 | 来源卡牌 | 现在借用的图标 |
|---|---|---|
| `ShivAllEnemies` | 死吧死吧死吧 | accuracy |
| `ShivPoison` | 涂毒 | envenom |
| `ShivMastery` | 乾坤一掷 | accuracy |
| `GlassKnifeMark` | 玻璃刀刃（挂在敌人身上） | talk_to_hand |
| `BattleHymnSmite` | 战歌 | hymn |
| `LikeWaterCalm` | 如水 | like_water |
| `StudyInsight` | 研习 | draw |
| `DevotionDraw` | 虔信 | devotion |
| `Mark` | 点穴的印记（敌人身上） | pressure_points |
| `FreeSkill` | 回环踢（下一张技能牌 0 费） | swivel |
| `NextTurnFocus` | 飞跃 / 暴雪 / 超能光束 | focus |
| `HelloWorldAnyCard` | 你好，世界 | hello |
| `CreativeAIUpgraded` | 创造性AI+ | ai |
| `Scuffle` | 乱战 | mayhem |
| `Chrysalis` | 结茧 | energized_blue |
| `Metamorphosis` | 羽化 | carddraw |
| `DemonFormExhaust` | 恶魔形态 | demonForm |
| `Nirvana` | 涅槃 | nirvana |
| `SpiritShield` | 精神护盾 | mental_fortress |
| `EmptyBody` | 化体为空 | no_stance |
| `JackOfAllTrades` | 花样百出 | energized_blue |

### 已知限制（无图形环境无法验收）

- **花样百出**：每张牌各有一个独立 buff 实例（同 ID 直接写入 `player.powers` 以绕过叠加），因此“多个计数器”成立；
  读档时同 ID 的多个实例可能被合并成一个图标，计数会合并计算。
- **精神护盾**：只对 `NORMAL` 伤害生效（怪物攻击），中毒/失去生命/荆棘不受影响；叠加是 1/2ⁿ。
- **扭曲漏斗 / 生物样本**：两者都改中毒结算，同时持有时掉血次数会叠加（7+6+5 再各扩散一次），未做额外抑制。
- **转经轮 / 恩洛斯的礼物**：额外奖励用 `new RewardItem()` 生成，会正常消耗卡牌随机数。
- **黄金眼**：预见 3 的时机与“先见之明”一致（抽牌堆为空先洗牌，第一回合在起手抽牌之前）。
- **小屋子 / 星盘**：都在拾起后通过 effect 排队等待界面关闭，若拾取时已打开地图/牌组界面会等它关掉再弹。

### 修复：旧存档的遗物池没有跟着品级变化走

**现象**：继续一局在品级改动之前开的存档后，商店里**还会刷出棱镜碎片，而且标价异常高**；小屋子**永远刷不出来**。

**根因**：遗物池是**随存档保存**的字段（`SaveFile.shop_relics` / `boss_relics`），`AbstractDungeon.loadSave` 只做原样赋值，
不会按品级重建。那局存档把棱镜碎片留在 `shopRelicPool`、把小屋子留在 `bossRelicPool`：前者实例已经是 BOSS 品级，
而 `AbstractRelic.getPrice()` 对 BOSS 返回 **999** 金币，于是商店里偶发一件天价遗物；后者根本不在商店池里，
所以商店永远刷不出小屋子。新开局走 `initializeRelicList` 按品级建池，因此只有旧存档会出现这两条现象。

**修复**：

- 新增 `RareRelicFixes.normalizePools`，并给 `AbstractDungeon.loadSave` 打后缀补丁（`RareRelicFixes$LoadedPools`）：
  读档后（此时玩家与遗物已经重建）把这两件挪回各自品级的池子。插入位置由该局种子（`Settings.seed`）决定，
  读档不会重置遗物池；玩家**已持有的遗物丢弃不放回**；Diverse 日常不放回棱镜碎片（原版在这一模式下也会把它移出池子）。
- `ShopRelicPatch.select` 增加第二道守卫 `sellable`：商店位置只接受**自身品级为商店遗物**的 id，
  旧存档里混进来的 BOSS 遗物不会再上架，也就不会以 999 金币出售。
- 不需要存档迁移标记：新开局本来就按品级建池，`normalizePools` 对健康池子是空操作（幂等）。
- 只**搬家**、不凭空补货：如果这一局的商店池已经把棱镜碎片的条目用掉（旧版把它刷在商店、你没买，条目就被消耗了），
  那它这一局不会再出现——原版的池子本来就是“条目用掉就没了”。

### 修复：ModTheSpire 前缀钩子必须显式声明实例

`EnergyManager.use(int)` 是实例方法，`EnergySpentPatch.Spend.before` 一开始只写了 `before(int e)`。ModTheSpire 是**按位置**绑定钩子参数的：实例方法会先把接收者 `$0` 传进来，因此钩子被塞进了 `EnergyManager` 而不是 `int`，补丁期直接抛
`before(com.megacrit.cardcrawl.core.EnergyManager) not found`，表现为一进游戏就加载失败。

- `EnergySpentPatch.Spend.before(EnergyManager __instance, int e)`
- `RareRelicFixes.NlothsChance.before(NlothsGift __instance, int rareCardChance)`（同一个坑）
- `PatchContractTest` 补上两条守卫：**实例方法的 prefix/postfix 钩子第一个参数必须是目标类**（返回值型 postfix 则是排在 `__result` 之后的那个），并把此前未纳入检查的 16 个补丁类全部加入遍历（补丁目标 122 → **189**）。这条守卫正是能提前抓住上面这个崩溃的检查。
- `find()` 支持“未指定参数的构造器补丁”（`BitePatch` 的构造器钩子），单构造器时不再要求写 `paramtypez`。

### 自动验证

`Java 8` 执行 `mvn clean package`：`SaturatingMath` 30302 项、既有 52+30 阈值回归、
**192 个补丁目标**（补丁方法/构造器存在性、`ExprEditor` 注入、插入定位器与字段注入、
以及实例方法钩子的实例参数绑定全部编译通过）与新增的
“第二批遗物/卡牌/能力”契约检查全部通过；遗物池修复另有 14 项断言（棱镜碎片离开商店池、小屋子进入商店池、
移动不重复、读档稳定、幂等、已持有丢弃、Diverse 不放回、无关遗物不动、3 项商店品级守卫）与对应的字节码检查；
`scripts/verify.ps1 -CheckJar` 通过 117 张唯一替换、双语字段与能量词元、
卡图与能力图标尺寸/命名、JAR 条目检查。以上均为无图形环境检查。

## 蒸汽护壁升级文本修复（2026-10-07）

**现象**：`SteamBarrier`（ID `Steam`）两档都 `exhaust = true`，但中英 `UPGRADE_DESCRIPTION` 都只写
“生成 !M! 个冰霜充能球 / Channel !M! Frost”，没有「消耗 / Exhaust」；而且 `upgrade()` 从未把
`UPGRADE_DESCRIPTION` 赋给 `rawDescription`，这段升级文本一直是**死数据**（升级后显示的其实是基础文本）。

**修复**：升级文本补回「消耗 / Exhaust」；`upgrade()` 按原版写法（同 `Chill`、`Bloodletting`）显式
`this.rawDescription = cardStrings.UPGRADE_DESCRIPTION; initializeDescription();`，顺带刷新 `!M!`。
升级后效果 = 生成 2 个冰霜充能球并消耗。`scripts/verify.ps1` 新增断言（两种语言都必须仍标注消耗），
重新逐张核对了全部 15 张带消耗的牌：标志与文本一致。

## 天鹅绒颈圈计数与洗牌触发两处修复（2026-10-07 深夜）

### 1. 天鹅绒颈圈的计数没有在战斗结束时清零

**根因**：模组用 `RelicReworkPatch.Swap` 把原版遗物包装成 `anotherspirerework.relics.ReworkedRelic`。包装类取代了原版实例，于是**它没有转发的原版钩子就等于不存在**。原版 `VelvetChoker.onVictory()` 会把 `counter` 设为 `-1`（战斗结束隐藏计数），而包装类只在 `atBattleStart()` 里 `counter = 0`：战斗结束到下一场开始之间，上一回合打出的张数会一直挂在遗物栏上，看起来就是"计数没清零"。

**修复**：给 `ReworkedRelic` 补 `onVictory()`，颈圈在战斗结束时把 `counter` 置 `-1`，与原版一致。契约测试新增断言：`ReworkedRelic.onVictory` 必须写 `counter` 字段。

**同类排查**（同一原因——包装类不转发原版钩子——可能出问题的地方，逐项核对需求）：

| 遗物 | 原版钩子 | 现状 | 结论 |
|---|---|---|---|
| 天鹅绒颈圈 | `onVictory` → `counter = -1` | 包装类未转发 | **本次修复** |
| 黑暗之血 | `onVictory` → 回复 12 生命 | 包装类未转发 | 符合需求：需求把回血改为"每场战斗第一张费≥2 攻击牌的未格挡伤害"，原版回血应被替换 |
| 黑星 | `onEnterRoom` / `onVictory`（脉冲、闪光） | 包装类未转发 | 仅视觉；计数由 `TurnRelicFixes.syncStar()` 在回合开始与渲染前同步，功能不受影响 |
| 融合之锤 | `canUseCampfireOption`（禁止锻造） | 包装类未转发 | 符合需求：requirements 只写"随机降级 4 张牌 + 每回合 [E]"，未要求禁止锻造 |
| 灵体外质 | `onEquip`/`onUnequip`（+1 能量） | 包装类未转发 | 符合需求：需求改为"每回合失去 5 金币换 [E]"，无常驻能量 |
| 破碎王冠 | `onEquip`（+1 能量）、`changeNumberOfCardsInReward` | 包装类未转发 | 符合需求：需求改为"卡牌奖励替换为遗物" |
| 蛇之戒指 | `onEquip`（手牌上限 +1） | 包装类未转发 | 符合需求：需求改为"每场战斗前 3 回合额外抽 2 张" |
| 手钻 | `onBlockBroken` | 包装类未转发 | 符合需求：需求改为"攻击伤害无视格挡" |
| 壶铃 | 无战斗结束钩子 | — | 计数是营火举重次数，必须跨战斗保留，不能清 |
| 黑星计数 | 无 | — | 全局精英击杀数，必须跨战斗保留 |

其余战斗内状态都在每场战斗开始时归零（`ReworkedRelic.turns`、`bloodUsed`、`TurnRelicFixes.potionUnlocked`），或由 `receivePostBattle()` / `receiveStartGame()` 清理（`ShuffleTriggerPatch.pendingShuffle`、`ExhaustTracker`、`OrbTrackerPatch.lastOrb`、`RelicReworkPatch.bloodActive`）。除颈圈外没有发现第二处漏清。

### 2. 战斗专注 + 深呼吸：洗牌后不触发结茧／羽化

**现象**：打出战斗专注（`No Draw`）后再打深呼吸，深呼吸只洗牌，结茧与羽化都不触发；之后**任意**其它抽牌效果（哪怕被 `No Draw` 挡掉、一张都没抽）才补触发。

**根因**：`DeepBreath.use()` 的动作顺序是

```
EmptyDeckShuffleAction → ShuffleAction → DrawCardAction
```

洗牌事件在 `EmptyDeckShuffleAction` 结束时置 `pendingShuffle`，然后**靠后面那个 `DrawCardAction` 的前缀每帧轮询**：`SoulGroup.isActive()` 为 true（洗牌动画还在飞）就一直等，动画结束后才 `fire()`。但 `DrawCardAction.update()` 第一句就是 `No Draw` 检查——它在"前缀返回"之后的同一帧立刻 `endActionWithFollowUp()` 结束自己，之后再没有任何动作轮询这个标记，于是它一直留到下次抽牌才被消费，正好对应"其它抽牌效果（即使实际没有抽牌）才触发"。

**修复**：把带保护的触发逻辑抽成 `ShuffleTriggerPatch.flush(AbstractGameAction interrupted)`，两条入口共用：

- `Deferred.before`（`DrawCardAction` 前缀）传入自身，保留"被打断的抽牌排在事件之后"的顺序；
- 新增 `ShuffleTriggerPatch.tick()`，由 `AnotherSpireRework.receivePostDungeonUpdate()` 每帧调用（沿用本模组处理延迟工作的既有位置），传入 `AbstractDungeon.actionManager.currentAction`。这样即使抽牌动作已经结束，甚至根本没有后续抽牌（`ScrapeAction`／`FastDrawCardAction`／`ForesightPower` 等自行排 `EmptyDeckShuffleAction` 的路径），只要动画播完就会触发。

顺带补上了上一轮记下的既有缺口：这些"自己排洗牌、后续不是 `DrawCardAction`"的路径以前要等下一次抽牌才触发，现在同样在洗牌动画结束时触发。

**验证**：`PatchContractTest` 调整并新增断言（`flush` 引用 `isActive`／`addToTop`／`DrawCardAction`，`before` 调用 `flush`，`tick` 调用 `flush`，`receivePostDungeonUpdate` 调用 `tick`），另外给 `ReworkedRelic.onVictory` 加了 `counter` 写入断言。

以下仍需游戏内验收：战斗最后一回合打出牌后结束战斗，颈圈计数应立即消失；战斗专注状态下打深呼吸，结茧／羽化应立即结算。

## 四项反馈修复（2026-10-07 晚）

### 1. 星系仪选牌时打开地图会强制结束选牌

**根因**：`ChooseDeckCardsEffect` 判断"选牌还在进行"的条件是 `AbstractDungeon.screen == CurrentScreen.GRID`。而原版在选牌界面按地图键时（`TopPanel`）会记下 `previousScreen = GRID`、`gridSelectScreen.hide()`，再由 `DungeonMapScreen.open()` 把 `screen` 改成 `MAP`。于是地图一开就被当成"选牌结束"：

- `selectedCards` 里的牌当场被 `apply()` 加进牌组，并且保留选牌时的金色发光——原版只有"非任意张数"那条分支会 `stopGlowing()`，而本选牌用的是"任意张数"分支；
- effect 立即 `isDone`；选区界面是原版自己恢复的，关闭地图后仍能点选，但再没有代码去读 `selectedCards`，所以点确定毫无效果；
- 读档时这些状态都由存档重建，异常自然消失。

**修复**：该分支改为等待 `AbstractDungeon.isScreenUp` 变 false 才算结束（`closeCurrentScreen()` 对 GRID 走 `genericScreenOverlayReset()`，只有 `previousScreen == null` 时才把 `isScreenUp` 置 false），地图／牌组界面在上面的整个往返期间都不动 `selectedCards`。另外加入牌组前补 `stopGlowing()`／`untip()`／`unhover()`，并让 `browsingCards()` 把 `MAP` 也算作"正在浏览"，避免拾取时把选牌界面压在玩家正看着的地图上。

### 2. 羽化选牌时机回到"洗牌后立刻触发"

**根因**：触发点挂在后续抽牌 `DrawCardAction.update()` 的前缀上，但洗牌分支排入的抽牌动作（`DrawCardAction(tmp, followUp, false)`）已经排在队首。补丁只把 `SeekAction` 用 `addToTop` 塞进队列，而 `GameActionManager` 要等 `currentAction.isDone` 才会取下一个动作——正在跑的抽牌不会被中断，于是界面出现在"剩余抽牌量全部结算完"之后，与需求的"抽牌直到洗牌 → 弹出选牌 → 加入手牌 → 结算剩余抽牌量"不符。

**修复**：触发时取出这个抽牌动作的剩余张数（`amount`），清零它并把一份 `new DrawCardAction(remaining, false)` 放回队首，然后才 `fire()`；事件排的动作又插在这份抽牌之上，队列因此成为 `[选牌动作…, 剩余抽牌]`。等待 `SoulGroup.isActive()` 变 false 的逻辑保持不变，洗牌动画仍然完整播完才触发。上一版 README 里"特效在动画播完后触发，仍在剩余抽牌之前"的说法，现在才真正成立。

### 3. 涂毒 / 死吧死吧死吧 的 buff 标题

**根因（有日志）**：打开能力列表时日志出现

```
[ERROR] PowerString: anotherspirerework.powers.ShivAllEnemiesPower not found
[ERROR] PowerString: anotherspirerework.powers.ShivPoisonPower not found
```

紧邻的栈是 `loadout.screens.PowerSelectScreen$PowerButton.<init>` 反射 `powerStrings` 抛 `NoSuchFieldException`——能力列表按这个**字段名**反射取标题。本模组其余 14 个 power 都写作 `private static final PowerStrings powerStrings = ...`，只有这两个例外（`ShivPoisonPower` 字段名叫 `strings`，`ShivAllEnemiesPower` 没有该字段），标题因此取不到本地化文本。

**修复**：两个类统一为 `powerStrings` 字段（`ShivAllEnemiesPower` 补上）。标题就是卡名：`PowerStrings.json` 中 `ShivPoison` = 涂毒 / Envenom，`ShivAllEnemies` = 死吧死吧死吧 / Die Die Die；描述按要求未改动。

### 4. 重编程

`Reprogram` 已是能力牌（`CardType.POWER`），但仍留着 `this.exhaust = true;`，中英描述也各自带着 ` NL 消耗 。` 与 ` NL Exhaust.`。现已移除该标志与两处词条，其余不变（2 费、失去一个充能球栏位、力量/敏捷 2→3）。

### 顺带与验证

- `PatchContractTest` 新增 `powerTitleAndReprogramRegression()`：断言 `Reprogram` 构造函数不再写 `exhaust`、两个 power 暴露 `powerStrings`、选牌 effect 的 `update` 读 `isScreenUp`；洗牌触发的断言要求 `flush` 引用 `addToTop` 与 `DrawCardAction`（时序逻辑已抽到 `flush`，见上一节）。
- `verify.ps1` 新增：两个 buff 标题必须等于对应卡名、`Reprogram` 描述不得含 `Exhaust`/`消耗`、每个 `extends AbstractPower` 的类都必须声明 `powerStrings` 字段。
- 构建验证：`mvn -f pom.xml clean package` 在 **Java 8** 下 `BUILD SUCCESS`（122 个补丁目标与上述全部断言通过），`verify.ps1 -CheckJar` 通过。注意用 JDK 25 跑 `mvn package` 会在 `postfixBindingRegression()` 的 `CtClass.toClass()` 处抛 `NullPointerException`（javassist 的 `DefineClassHelper` 在新 JDK 上取不到 `defineClass`），与本轮改动无关，换 Java 8 即可。
- 产物已复制到 `…\SlayTheSpire\mods\anotherspirerework.jar`（pom 的 `mod.deploy.skip=true` 默认不部署）。以下场景仍需游戏内验收：选牌中开／关地图后继续选牌并确认、加入牌组卡牌的外观、一次抽牌跨洗牌时羽化的"先选牌后剩余抽牌"。

## 根因修复：复制补丁的返回值参数顺序（2026-10-07）

**现象**（启用模组时）：永久牌组滚动时卡牌乱飘、退出后飞回手牌；战斗中消耗一张牌，永久牌组里也出现空位；下一场抽到该牌时只剩发光底盘。

**根因（已用实验证实，非推测）**：勺子的"复制继承去消耗"补丁挂在 `AbstractCard.makeStatEquivalentCopy()` 上，原先写成：

```java
public static AbstractCard after(AbstractCard __instance, AbstractCard __result)   // 错误
```

我用随附的 ModTheSpire 补丁器对测试类应用了同样的 Postfix，再反编译生成的字节码，两种写法的调用序列**完全一致**：

```
aload_2                          // $_  = 方法返回值（新副本）
aload_0                          // $0  = 实例（源卡牌）
invokestatic ...after(Object, Object)
```

即 **ModTheSpire 按"先返回值、后实例"的位置传参，与参数名无关**。因此上述写法里 `__instance` 实际是副本、`__result` 实际是**源卡牌**，`return __result` 就把源卡牌当副本交了回去：

- 战斗牌组由 `makeSameInstanceOf()`（内部调用它）建立 → 与永久牌组**共用对象**；
- 战斗中消耗会移动该对象 → 永久牌组页面出现空位；
- 该对象带着战斗中的淡出等残留状态 → 下一场抽到时不可见，只剩发光底盘；
- 多利之镜同样调用它 → 复制出的牌也异常。

实验输出（`result-first` 正确、`instance-first` 复现 bug）：

```
[instance-first] make() returned the source card itself? true      ← 复现
[result-first]   make() returned the source card itself? false     ← 正确写法
```

**修复**：改为 `after(AbstractCard __result, AbstractCard __instance)`，只修改并返回副本；同时撤销上一轮为绕开该问题而临时引入的"按卡牌身份集合继承"，恢复为**按源卡牌对象**判定，因此只有当时选中的那几张会继承去消耗。

**新增两道自动防护**：

- `PatchContractTest.postfixBindingRegression()`：用 ModTheSpire 真实补丁器给测试类打上 Postfix，断言"返回值在前、实例在后"（结果类型与实例类型故意不同，写反会直接编译失败），并检查模组真实钩子的形状；随 `mvn package` 运行。
- `verify.ps1`：扫描全部源码，**任何有返回值的 `@SpirePostfixPatch` 必须把 `__result` 声明为第一个参数**；这正是能在构建期拦住本次 bug 的检查。

### 这轮之后仍保留的取舍

| 项目 | 取舍 |
|---|---|
| 洗牌触发特效／勺子与星系仪选牌界面 | 上一轮为"动画干扰"假设加入的"浏览牌堆时不触发／不抢屏"仍保留：仅**推迟**到界面关闭后，不取消效果。与本次根因无关，如果你希望完全恢复原时序，可以移除 |
| 医药箱／蓝蜡烛 | 浏览牌堆时抽到状态／诅咒牌会推迟处理；若该牌在界面关闭前已离开手牌，这次自动处理跳过 |
| 复制继承判定 | 已恢复为按源卡牌对象判定，只有被选中的牌会继承去消耗；读档仍按"ID+升级+misc 张数"精确恢复 |

## 复制钩子严格化（2026-10-07，已被上面的根因修复取代）

反馈确认 bug 消失后，又定位到一个**可能会重新引发同类问题**的写法并已修正：

- 勺子用于"复制继承去消耗"的补丁挂在 `AbstractCard.makeStatEquivalentCopy()` 上。ModTheSpire 对**有返回值的 postfix** 的约定是：补丁方法的**第一个参数按位置接收原返回值**（见 `ModTheSpire.wiki/SpirePatch.md`：返回类型非 void 时，第一个同类型参数拿到原返回值）。原先的写法把 `AbstractCard __instance` 放在第一位，一旦按位置绑定，`__result` 就会落到"源卡牌对象"上，钩子返回值就会**把源对象当成复制结果交回去**——战斗牌组、抽牌、双持、发现等全部会与永久牌组共用对象，于是"战斗中消耗一张牌，永久牌组里也消失；下一场抽上来只有发光底盘"完全吻合。
- 现在改为**只取 `__result` 一个参数**：无论按位置还是按名称绑定，拿到的都只能是复制结果，永远不可能返回源对象。判定改为按身份（卡牌 ID + 升级次数 + misc）集合 `REMOVED` 命中，该集合由勺子选牌、读档恢复、存档扫描三处维护。
- 契约测试新增断言：`Copies.after` 只能有 1 个参数且类型为 `AbstractCard`，防止以后有人再把它改回带 `__instance` 的形式。

### 这次修复带来的取舍

| 项目 | 取舍 |
|---|---|
| 复制继承的判定 | 由"按对象"改为"按身份"。因此用**等价复制**产生的同名同升级卡（战斗牌组、多利之镜、双持、发现等）会一并继承"不消耗"；商店／奖励的新卡走 `makeCopy`，不受影响。若只想让当时选中的那几张不消耗，需要另做方案（会重新引入读取源对象的需求，风险更高） |
| 洗牌触发特效 | 打开任何界面（牌组／弃牌堆／消耗堆／抽牌堆／单卡检视）期间不结算，**推迟到界面关闭后**；战斗开始/结束时清空待处理，避免跨战斗错位。效果本身不取消 |
| 医药箱／蓝蜡烛 | 浏览牌堆时抽到状态／诅咒牌，消耗与抽牌同样推迟；若该牌在界面关闭前已离开手牌（例如被打出），这次自动处理会跳过 |
| 勺子／星系仪选牌界面 | 不抢占正在浏览的牌堆界面，关闭后才弹出；商店与奖励界面不受影响，仍是拾取即弹 |

以上都不改变效果最终结果，只改变时机。

## 牌组界面干扰撤回（2026-10-06 深夜）

反馈现象：战斗中或战斗外打开右上角**永久牌组界面**时，刚加入卡组的牌（多利之镜复制、抽到的手牌）像是卡在界面里，退出时又飞回手牌，滚轮滚动时还有卡会乱飘。

核查结论（据实说明）：

- **卡图补丁只做绘制**：`CardArtLoaderPatch` 只在原版画卡面的位置画你的图片，不读写卡牌坐标、不改变缩放、不移动卡牌，也没有补丁挂在任何牌组界面的 `open()`／关闭／滚动逻辑上。因此它不是“卡组显示逻辑”的改动来源，按你的要求**保留**。
- 原版永久牌组界面会直接移动 `masterDeck` 里的真实卡牌；正在播放的“获得卡牌”（多利之镜、星系仪）与“抽牌”动画会同时驱动同一张牌的坐标——两者叠加就会出现“卡住／乱飘／退出时飞回原位”。
- 模组此前确实有几处会在**你打开牌组界面的同一时间**安排抽牌或开启选牌，从而制造这种叠加。

本轮撤回／修正的内容（只动这一处，其余保留）：

- **洗牌触发特效**：只要有一层界面打开（牌组界面、卡牌检视等），就不再触发；改为保持待处理状态，等界面关闭后再结算。能量、抽牌与羽化选牌都会等。
- **医药箱／蓝蜡烛**：抽到状态／诅咒牌时若正在浏览牌堆，不再立刻消耗并抽牌，而是记下来，等界面关闭后再处理。
- **勺子／星系仪选牌界面**：不再强占正在浏览的牌组界面；检测到牌组、弃牌堆、消耗堆、抽牌堆或单卡检视界面时等待，界面关闭后才弹出。

以上三处之外没有改动：自定义卡图、勺子张数记录、乱战显示、虚无关键词等全部保留。`verify.ps1` 与补丁契约检查新增了这三处延迟入口的断言。

仍需你确认（我这边无法图形环境复现）：如果**只**用多利之镜且不涉及上述任何流程时仍然出现，那应属原版“获得卡牌飞行动画与牌组界面同时驱动同一张牌”的表现；请告诉我具体是哪张牌、当时是否刚抽到／刚获得，以及关闭本模组是否仍会出现，我再继续收敛。

## 反馈修复与卡图交接（2026-10-06 晚）

**本模组不再自行处理卡图。** 已删除上一轮的 `CardArtPatch`（不再叠加旧卡框、不裁切、不缩放、不补绘），改由作者提供图片。代码只负责“有图就用、没图保持原版”。

### 需要你提供的卡图

放在这两个位置之一（都会识别）：

```
AnotherSpireRework\src\main\resources\anotherspirerework\images\
AnotherSpireRework\src\main\resources\anotherspirerework\images\cards\
```

- 放大图 **500 × 380**：`<名字>.png`（当前仓库用的就是这种）
- 普通卡图 **250 × 190**：`<名字>.png`，与放大图二选一即可

**只给放大图就够**：卡面会用这张 500×380 的图缩放到 250×190 显示（两者本来就是同一画面、同一相对位置），放大界面也用它；反过来只有 250×190 时放大界面会放大显示。图片按整张采样，不需要额外裁切。1× 与 2× 若同时存在，1× 优先用于卡面、2× 优先用于放大界面。

`<名字>` 可用卡牌 ID 去掉空格（`DieDieDie`）或原版图片文件名（`die_die_die`，当前仓库用的就是这种），两种都识别：

| 卡牌 | 当前类型 | 文件名 |
|---|---|---|
| 跃跃欲试 | 技能 | `pummel.png` / `Pummel.png` |
| 乾坤一掷 | 能力 | `unload.png` / `Unload.png` |
| 死吧死吧死吧 | 能力 | `die_die_die.png` / `DieDieDie.png` |
| 熔化 | 技能 | `melter.png` / `Melter.png` |
| 核心电涌 | 技能 | `core_surge.png` / `CoreSurge.png` |
| 重编程 | 能力 | `reprogram.png` / `Reprogram.png` |
| 结茧 | 能力 | `chrysalis.png` / `Chrysalis.png` |
| 羽化 | 能力 | `metamorphosis.png` / `Metamorphosis.png` |
| 暴雪 | 技能 | `blizzard.png` / `Blizzard.png` |

暴雪已按此命名提供卡图并会被加载；**暴雪的机制与数值仍未改动**（只换卡图）。其余卡牌如需换图，按同样命名放入即可，代码无需改动。改图后重新 `mvn package` 并重启游戏生效；两张尺寸都不存在时自动回退原版图集。

`verify.ps1` 会检查每张卡图必须是 250×190 或 500×380，并逐张打印它被哪张卡使用（例如 `Card art 'blizzard.png' -> Blizzard`）；名字拼错时会给出警告而不是静默忽略。

### 本轮修复

- **洗牌特效不再打断洗牌动画**：`EmptyDeckShuffleAction`／`ShuffleAllAction` 结束只标记“有待处理洗牌”，真正的触发点移到后续抽牌等待 `SoulGroup.isActive()` 变 false 的那一刻（原版抽牌本来就会等待动画）。特效在动画播完后触发，仍在剩余抽牌之前。
- **重启 + 羽化顺序修正**：重启会先把手牌回抽牌堆再洗牌，现在等待整套流程（含 PutOnDeck 的飞行动画）结束才触发，不再出现“先选弃牌堆、后洗手牌”。
- **乱战**：能量数改由 `amount` 承载（原版会存档，重读不丢），描述显示“你每抽 10 张牌，获得 n [E]”；图标右下角改显当前已抽张数（含 0，覆盖 `renderAmount`），卡面与能力文本中的 `#b` 已去除。
- **冻结之眼的虚无**：中文关键词必须独立成词，改为 ` NL 虚无 。`（`虚无。` 会被解析成一个未知词，永远不高亮）。英文保持 ` NL Ethereal.`，原版解析器会去掉词尾标点。拾取与失去遗物时都会同步牌组内已有卡牌。
- **奇怪的勺子 / 星系仪真正生效**：两者原本把选牌做成战斗 Action，而 `GameActionManager.addToBottom` 只在战斗房间接收动作，商店购买时被直接丢弃。现改为 `ChooseDeckCardsEffect`（`AbstractDungeon.effectList`，任何房间都会更新）驱动原版选牌界面。
- **奇怪的勺子**：同时用 `ignoreRelic("Strange Spoon")` 关掉原版 50% 不消耗；移除的消耗记录改为**精确到张数**：存档时扫描牌组，按"卡牌 ID + 升级次数 + misc"（原版存档唯一保留的三项）写下"该身份有几张被去掉了消耗"，读档后只对这若干张重新应用（相同的重复卡不会互相牵连），战斗牌组复制与效果复制通过"这张牌不再消耗、而原版版本消耗"来识别并继承。
- **星系仪**：30 张候选只在开始时生成一次（原先每帧重新随机），改为任意张数选择，选中卡用原版 `ShowCardAndObtainEffect` 加入牌组，保留获得动画。
- **壶铃包装实例**：举重上限同步为 5 次，不再出现原版实例 5 次、包装实例 3 次的不一致。
- 自动验证新增：洗牌触发必须引用 `SoulGroup.isActive`、拾取界面为 effect 而非 action、勺子复制/读档钩子、卡图候选名、中英“消耗”文本清理、中英关键词分隔符。

## 新增卡牌与遗物（2026-10-06）

本轮按最新 `requirements.txt` 补齐标为“新增/修改”的条目，全部沿用原版 ID 与替换方式（乱战=Mayhem、深谋远虑=Thinking Ahead、忍术卷轴=Ninja Scroll、两仪=Yang、英雄宝典=Enchiridion、机械臂=Inserter 等），因此没有注册任何新卡池或遗物池。

- 新增7张卡牌重做：雷霆打击、乱战、深谋远虑、秘密技法、秘密武器、结茧、羽化。雷霆打击在打出时先固定“本场战斗已生成过的闪电充能球数”，再逐个生成闪电球并随机造成伤害，新生成的球不会反过来喂给本次结算；乱战用 `ScuffleDrawPower` 计数每抽10张牌给能量，能力图标显示叠加数、说明显示已抽张数；深谋远虑先抽牌再把手牌放回抽牌堆顶；秘密技法/秘密武器按类型从抽牌堆取1（升级2）张并保留消耗；结茧与羽化改为能力牌，分别通过 `ShuffleEnergyPower`／`ShuffleSeekPower` 响应洗牌。
- 洗牌事件由 `ShuffleTriggerPatch` 在 `EmptyDeckShuffleAction`／`ShuffleAllAction` **结算完成**时触发，而不是像原版遗物钩子那样在动作构造时触发。这样羽化的选择界面正好出现在需求要求的顺序上：抽牌直到洗牌 → 弹出选牌 → 加入手牌 → 继续结算剩余抽牌。为避免同一张牌触发两次，没有接入 `ShuffleAction`（深呼吸与重启都会先排队上面两个动作之一）。
- 新增10项遗物改动：忍术卷轴（每回合一张小刀，替换开战三张）、两仪（攻击给临时敏捷、技能给临时力量）、英雄宝典（三选一能力牌，本回合0费，可跳过）、医药箱与蓝蜡烛（抽到状态牌消耗并抽一张／抽到诅咒牌消耗，保留原版打出效果）、奇怪的勺子（拾起时选最多2张移除消耗，替换原版50%不消耗）、星系仪（10/10/10三档任意张加入牌组，替换原版4次卡牌奖励）、冻结之眼（打击与防御获得虚无）、机械臂（每回合一个充能球栏位）、套娃（洗牌时抽一张牌，替换宝箱额外遗物）。
- 大小调整：大锅由2个药水栏位改为1个；壶铃举重上限由3次改为5次（休息处选项与文案同步）；黑暗之血改为每场战斗第一张费≥2的攻击牌，不再每回合重置；勇气投石索文案补上“每场战斗开始时”。
- 第四幕（心脏层）生成限制：需求的27件遗物在 `actNum >= 4` 且非无尽模式时不再生成。不覆盖 `canSpawn` 的6件走 `AbstractRelic` 基类补丁，声明了 `canSpawn` 的21件逐个打前缀补丁，避免基类补丁被子类覆盖绕过。
- 无色选牌“跳过”：发现、无色药水、工具箱、尼尔里的典籍与外来影响都走 `CardRewardScreen.customCombatOpen`，补丁统一打开跳过键；普通卡牌奖励与观者的“抉择”界面不受影响。
- 改类型卡牌的原画裁切修复：攻击/技能/能力三种卡框的画窗宽度不同（攻击约16px边饰、技能约20px、能力是更小的椭圆窗），换框后原画会露出来一截或被多切掉一条。上一轮用 `CardArtPatch` 叠加旧卡框来还原裁切，**该做法已在下面的“反馈修复与卡图交接”中撤销**，改由作者提供卡图，代码只负责加载与回退。
- 自动验证：Java 8 执行 `mvn clean package`，123个补丁目标（含新遗物、洗牌事件、跳过与卡图加载补丁）通过契约检查，新增卡牌/能力/遗物的动作契约、第四幕27件清单与既有52+30项阈值回归全部通过；`verify.ps1 -CheckJar` 检查双语字段与能量词元、96张唯一替换、7个新卡牌类与全部顶层类已打包。以上均为无图形环境检查。
- 需游戏内验收：羽化选牌界面与剩余抽牌的结算顺序、结茧/套娃在深呼吸与重启下的触发次数、星系仪30张选牌界面与卡组写入、奇怪的勺子读档后是否保持无消耗、冻结之眼在打击/防御上的虚无文本与回合结束消耗、医药箱/蓝蜡烛的抽牌连锁、英雄宝典三选一、忍术卷轴手牌满时的处理，以及改类型卡牌的卡面观感。默认不部署；升级前备份存档。
- 暴雪的机制、费用与集中数值本轮未做修改。

## 卡牌新增与修改（2026-10-05）

- 按最新 requirements.txt 完成13项：铁斩波、狂暴、盛怒、钢铁风暴、暴雪、天眼，以及地狱之刃、死吧死吧死吧、涂毒、声东击西、斗篷与匕首、全神贯注、堆栈。
- 铁斩波3/5伤害，保持原有结算方式：先获得等同于卡面攻击数值的格挡，再造成伤害；格挡不依赖敌人的实际生命损失。
- 狂暴2费、每回合1/2能量，无自身易伤；盛怒1/0费、满足本回合消耗条件时获得3能量；全神贯注固定弃3张，获得2/3能量。
- 钢铁风暴接收原死吧死吧死吧的消耗堆小刀复制效果与动态计数；后者改为2/1费能力，小刀群攻并生成3张小刀。涂毒使小刀附带1/2中毒并生成1张。
- 小刀群攻使用原版逐敌伤害计算，与精准和乾坤一掷兼容；玻璃刀刃在小刀造成实际生命伤害后提供格挡。涂毒在小刀攻击后施加中毒（攻击被格挡也可施加，人工制品正常抵消）。群攻能力重复使用不叠加攻击次数。
- 地狱之刃、声东击西复用原版按卡牌类型的 DiscoveryAction，当前角色战斗牌池三选一、本回合0费、消耗。
- 斗篷与匕首在抽牌堆原位置替换选中牌，不触发消耗，也不改永久牌组；不足指定数量时替换全部，空牌堆不操作。堆栈改按抽牌堆计数并保留原版动态格挡显示。
- 双语文本、衍生牌预览、89张库替换与资源验证同步更新。验证脚本不再依赖不存在的旧工程。
- 需游戏内验收：三选一与牌堆选择界面、手牌满时生成、小刀群攻/附毒/精准/乾坤一掷/玻璃刀刃联动、时间吞噬者、心脏限伤、致命伤害格挡及存档读取。默认不部署；更新前备份存档并结束进行中的战斗。
- 自动验证使用 Java 8 执行 Maven clean package：算术30302项、既有遗物/回合边界检查、新卡牌动作与升级结构契约及实际JAR补丁接口检查；verify.ps1 -CheckJar检查双语字段/能量词元、89张唯一替换和全部顶层类打包。以上均为无图形环境验证，不等于游戏内实测。

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
- 实现 `requirements.txt` 中 19 项遗物改动，保留原版 ID 和图片。
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

依赖不在默认 Steam 目录时，用 `-Dlocal.dependencies` 指向同时包含 `desktop-1.0.jar`、`ModTheSpire.jar` 与 `BaseMod.jar` 的目录：

```powershell
mvn -f pom.xml '-Dlocal.dependencies=<依赖目录>' clean package
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/verify.ps1 -CheckJar
```

新增无图形环境的补丁契约检查：使用实际依赖JAR验证目标方法、插入定位器、字段及Instrument产生的字节码。
这不能替代ModTheSpire启动及游戏内验收。需重点实测时间吞噬者跨12/24张牌、心脏限伤、药水丢弃、商店补货、Boss遗物替换、休息处举重和降级。


<a id="maintenance" name="maintenance"></a>

## 实现与维护参考

> 本节由原 README 的机制说明、验证流程与硬性规则移入，供维护时查阅。改动清单见
> [README.md](README.md)（中文）与 [README.en.md](README.en.md)（English）。

### 重写版说明

原工程保留在相邻的 AnotherSpire 目录。此工程使用独立包名、资源目录和 mod ID `anotherspirerework`，
但原版卡牌 ID 与效果保持原实现的基线。**不要同时启用 AnotherSpire 和此版本**：两者修改相同卡牌和方法，
独立 ID 不代表机制可叠加。切换版本前结束当前战斗并备份存档；新 Power 的命名空间不同，
不承诺迁移进行中的战斗。

实际重构：集中卡牌替换表、独立饱和算术工具、临时伤害/力量的 finally 恢复、语言大小写转换独立于系统区域设置、
追踪状态重置，以及移除未使用的 StSLib 构建依赖。复杂补丁及各卡牌机制保留原实现。

### 替换机制

`patches/CardSwapPatch` 在 `CardLibrary.initialize()` 之后重写 `CardLibrary.cards`：把 130 个条目换成
`anotherspirerework.cards.<color>.<Name>`，**保留原版 cardID 与原版图片路径**，并带过 `isSeen`。
游戏所有取卡路径（`CardLibrary.getCardList`、`AbstractDungeon.getCardFromPool`、`initializeStarterDeck`…）
都读这张表，所以替换一处即全局生效。事件里 `new Bite()` 这类**绕过卡库的直接构造**由 `patches.BitePatch` 单独补丁。
全部沿用原版 cardID，因此没有注册任何新卡池。

### 卡图

`patches.CardArtLoaderPatch` **只做绘制**：有图就画，没图回退原版图集，不裁切、不缩放、不动卡牌坐标。

- 位置：`src/main/resources/anotherspirerework/images/` 或 `.../images/cards/`。
- 尺寸：卡面 **250×190**、放大界面 **500×380**，给其中一个即可（另一个按整图缩放使用）。
- 文件名：卡 ID 去空格（`DieDieDie.png`）或原版图片名（`die_die_die.png`），两种都识别；`_p` 后缀表示放大图。
- 改图后重新 `mvn package` 并重启游戏。

### 能力图标

`patches/PowerArtLoaderPatch` 只覆盖**本模组自己新增的能力**（原版能力保持原版图标），有图就换、没图保持现状：

- 位置：`src/main/resources/anotherspirerework/images/powers/`。
- 尺寸：`<名字>_48.png`（48×48，buff 栏小图标）、`<名字>_128.png`（128×128，获得/触发时的放大闪光）；
  两者互不影响，可只给一个。尺寸必须与后缀一致（游戏按固定倍率绘制）。
- 文件名：能力 ID 去掉 `anotherspirerework:` 前缀（如 `Nirvana_48.png`），或能力类名（`NirvanaCounterPower_48.png`）。

### 本地化

中英 `CardStrings` / `PowerStrings` / `RelicStrings` 都按 `anotherspirerework:<原版ID>` 建键，不覆盖原版字符串。
新增关键词只有 `Mark`（印记）；其余关键词沿用原版写法，保证高亮与提示正常。
中文的 ` NL ` 空格、`#`/`*` 标注与关键词边界不要机械删除；JSON 能解析不等于卡面渲染正确。

### 遗物机制

与卡牌同一范式：**一件遗物一个类**。

1. **替换**：`patches.RelicSwapPatch` 在 `RelicLibrary.initialize()` 之后把原版原型换成
   `anotherspirerework.relics.<名称>`（45 件）。替换发生在原型表（`sharedRelics` / `redRelics` /
   `greenRelics` / `blueRelics` / `purpleRelics`）、品级列表和颜色列表上，并带过 `isSeen`；游戏所有获取
   路径都走 `RelicLibrary.getRelic(id).makeCopy()`（卡池、宝箱、奖励、商店、事件、读档），所以替换一处
   即全局生效。品级改变（棱镜碎片 BOSS、小屋子 SHOP）直接写在各遗物构造函数里，替换时同步移入对应品级列表。
2. **继承**：每件遗物的类 `extends com.megacrit.cardcrawl.relics.<原版类>`，只覆写行为改动的钩子；
   **未覆写的原版钩子自动保留**——这是与旧包装类的关键区别（包装类不转发的钩子等于不存在）。
   `makeCopy()` 返回本类实例。原版写在别处的行为（`hasRelic` 判断、伤害管线、奖励界面、洗牌事件）由该
   遗物类里的嵌套 `@SpirePatch` 负责，例如 `HandDrill`、`Ectoplasm`、`Sozu`、`TwistedFunnel`、`TheSpecimen`。
3. **文本**：`getUpdatedDescription()` 返回 `AnotherSpireRework.getRelicDescription(relicId)`。原版类自己
   覆写 `updateDescription()` 的五件（融合之锤、天鹅绒颈圈、灵体外质、破碎王冠、添水）必须一并覆写，
   否则读档后会被原版文案覆盖；邪教徒头套、化学物 X、李家华夫饼、送货员、瓶装三遗物刻意保留原版文本。
   多行描述（勺子/星系仪/星盘/小屋子）用 `getRelicDescription(relicId, 1)` 作选牌界面标题。
4. **直接构造**：N'loth 事件直接 `new NlothsGift()`，由 `relics.NlothsGift$NlothEvent` 的仪器补丁换成新类；
   其余原版内容都经由 `RelicLibrary.getRelic` 获取。
5. **不进第四幕（心脏层）的 27 件遗物**：仍由 `patches.ActFourRelicBlockPatch.BLOCKED` 清单 + `canSpawn`
   后缀/前缀处理（自己声明 `canSpawn` 的 21 件逐个打前缀）。
6. **条件生成**：化学物 X、李家华夫饼的条件写进各自 `canSpawn()`；瓶装三遗物的幕数门槛也写进
   `canSpawn()`，并通过 `relics.KeepInPool` 告诉 `ShopRelicPatch`「这是暂时不满足，留在池里」，
   不消耗本局机会。其余条件仍按原版处理。
7. **跨遗物规则**留在 `patches/`：`RelicTierPatch`（旧存档池修复）、`ShopRelicPatch`（池抽取）与
   `ShopPricePatch`（商店降价）；它们只读遗物 ID/品级与 `canSpawn()`，不包含单件遗物的行为。

遗物描述放在 `localization/eng|zhs/RelicStrings.json` 的 `anotherspirerework:<原版遗物ID>` 键下，
不覆盖原版字符串。遗物标注规范**与卡牌不同**：以 `#r卡名`、`#g遗物名`、`#y遗物` 标注（卡牌才用 `*卡名`）。
需求中以 `#` 标注的补充说明（生成条件、隐藏效果等）不进描述文本。
不进第四幕的 27 件清单见 [README.md](README.md) §2。

### 其它机制实现

- **无色选牌加「跳过」**：`ColorlessSkipPatch` 给 `CardRewardScreen.customCombatOpen` 打开跳过硬开关，
  覆盖发现、无色药水、工具箱、尼尔里的典籍、外来影响；普通卡牌奖励与观者的「抉择」不受影响。
- **移除数值上限**：原版把格挡/力量·敏捷·集中·枷锁/中毒(9999)/充能/能量钳制在 ±999，现放宽到 int 上限。
  补丁为 `NoBlockCapPatch`、`NoPowerCapPatch`、`NoEnergyCapPatch`：**Prefix 算原值、仅当钳制真的触发时由
  Postfix 写回**，不复制也不替换原版代码，其它模组的补丁照常运行。
- **溢出饱和**：上限解除后，越界运算饱和到 int 上限而不是翻符号（"超过上限就是上限"）。由
  `util.SaturatingMath` 与 `NoPowerCapPatch`/`NoBlockCapPatch`/`NoEnergyCapPatch`/`TriplePoisonSaturatePatch`
  负责；重刃不再沿用原版"乘上去再除回来"的取巧写法。
- **洗牌事件**：`EmptyDeckShuffleAction` 结算完成时标记"待处理"；`ShuffleAllAction`（重启）只搬弃牌堆，还要等它
  在第一次 `update` 里用 `addToTop` 排入的 `PutOnDeckAction` 把手牌搬进抽牌堆、以及重启随后排的 `ShuffleAction`
  真正洗牌，才标记完成（`waitingForHandOnDeck` → `waitingForFinalShuffle` → `pendingShuffle`）。之后等
  `SoulGroup.isActive()` 为 false（洗牌动画播完）且没有界面打开时才触发。堆栈、结茧、羽化、套娃都依赖它。
  普通 `ShuffleAction`（深呼吸）不接，避免同一次洗牌触发两次；`ShuffleAction` 只在上面的尾段流程里被认。
- **界面保护（只改时机，不取消效果）**：浏览牌堆/地图/单卡界面期间，洗牌触发、医药箱·蓝蜡烛的抽牌处理、
  勺子·星系仪选牌都推迟到界面关闭后执行。
- **商店遗物降价**：`ShopPricePatch` 在 `StoreRelic` 构造完成后按遗物自身品级改写价格——罕见 −50、稀有 −100
  （原版 250 / 300 → 200 / 200）。只动商店货架，不影响宝箱与奖励。

### 主类与辅助类

主类 `AnotherSpireRework`（`@SpireInitializer`）：

| 方法 | 用途 |
|---|---|
| `makeID(String)` | 生成 `modID:name`，本地化 lookup key |
| `getCardStrings(String)` / `getPowerStrings(String)` | 读带命名空间的字符串；缺失时记录 error 并回退 mock |
| `extendedDescription(CardStrings, int)` | 安全读 `EXTENDED_DESCRIPTION[i]`，越界或翻译缺失返回空串 |
| `localizationPath(String lang, String file)` / `imagePath(String file)` | 资源路径拼接 |
| `inCombat()` | 当前是否在战斗中 |

订阅的 BaseMod 钩子：`PostInitialize`、`PostBattle`、`PostDraw`、`PostExhaust`、`PostDungeonUpdate`、
`PostPowerApply`、`StartGame`、`PreStartGame`、`EditStrings`、`EditKeywords`。

洗牌事件 `patches.ShuffleTriggerPatch`：

| 方法 | 用途 |
|---|---|
| `fire()` | 先把抽牌堆里的堆栈移到顶部，再触发所有 `DrawPileShuffleListener` 与套娃 |
| `reset()` | 战斗开始/结束时清空待处理标记与「等待洗手牌／等待收尾洗牌」标记 |
| `tick()` | 每帧兜底（由 `receivePostDungeonUpdate` 调用），处理"抽牌先结束、动画还没播完"的路径 |
| `flush(AbstractGameAction)` | 包级；三步洗牌流程结束、动画结束且无界面时触发，并把被中断抽牌的剩余张数放回队首 |

选牌界面 `effects.ChooseDeckCardsEffect`：`start(Choice, String message, int maxCards)`、`browsingCards()`、
`enum Choice { REMOVE_EXHAUST, ADD_TO_DECK, ADD_ANY_COLOR }`。它挂在 `AbstractDungeon.effectList` 而**不是**战斗 Action：
`GameActionManager.addToBottom` 只在战斗房间接收动作，商店/宝箱拾取时会被直接丢弃。

饱和算术 `util.SaturatingMath`：`fromLong(long)`、`add(int,int)`、`subtract(int,int)`、`multiply(int,int)`。

卡图 `patches.CardArtLoaderPatch`：`candidateNames(String cardID, String assetUrl)` 按"卡 ID 去空格 → 图片路径转下划线
→ 图片文件名"返回候选名。

能力图标 `patches.PowerArtLoaderPatch`：`fileId(String powerId)`（去掉 `anotherspirerework:` 前缀）、
`candidateNames(String powerId, String className)`、`owns(String className)`（只认 `anotherspirerework.powers.*`）、
`load(String name, int size)`：按 `<名字>_48` / `_128` 找图。

供其它补丁复用的静态助手：

| 类 | 方法 |
|---|---|
| `ExhaustTracker` | `onCardExhausted()`、`exhaustedThisTurn()`、`reset()`（燃愿判断本回合是否消耗过牌） |
| `util.RelicChecks` | `owned(id)`、`inActFour()`、`ignoreHasRelic(id)`（生成 `ExprEditor` 关掉指定遗物的原版 `hasRelic` 分支）、`loseEnergy()`、`keywordSuffix(language)`、`withoutExhaustText(desc, keyword)`、`serpentTurnActive(turn)`、`blackStarActive(kills)`、`sozuLocked(combat, sozu, discarded)` |
| `util.RelicSave` | `formatCounts(Map)`、`parseCounts(String)`：勺子「去消耗」的存档格式（纯逻辑，便于无图形环境测试） |
| `patches.RelicSwapPatch` | `replacements()`：45 件遗物的替换表；在 `RelicLibrary.initialize()` 后替换原型/品级列表/颜色列表并带过 `isSeen` |
| `patches.RelicTierPatch` | `poolFor(id)`、`normalizePools(...)`、`normalizeRelicPools()`：旧存档里棱镜碎片/小屋子的池位置修复 |
| `patches.ActFourRelicBlockPatch` | `BLOCKED`（27 件清单）、`blocked(id)`：第四幕屏蔽；自己声明 `canSpawn` 的 21 件逐个前缀 |
| `patches.ShopRelicPatch` | `sellable(id)`：商店只卖自身品级为 SHOP 且 `canSpawn()` 的遗物；池抽取把 `KeepInPool` 与条件遗物留在池里 |
| `relics.*` | 每件遗物的行为都在自己的类里；`relics.KeepInPool.keepInPool()` 表示"暂时不满足、留在池里" |
| `ShopPricePatch` | `discounted(RelicTier, int)`：商店折后价；常量 `UNCOMMON_DISCOUNT = 50`、`RARE_DISCOUNT = 100` |
| `RagnarokXAction` | `energyFor(int)`：X 费的当前取值（含化学物 X），卡面显示与结算共用 |
| `SadisticNaturePower` | `active()`：玩家是否持有残虐天性，供两处伤害补丁判定 |
| `BlurDrawPower` | `onCardDrawn(AbstractCard)`：残影的抽牌结算，由主类 `receivePostDraw` 调用 |
| `relics.MedicalKit` / `relics.BlueCandle` | `onCardDrawn(AbstractCard)`、`processPendingDraws()`：抽到状态/诅咒的延迟处理，由主类 `receivePostDraw` / `receivePostDungeonUpdate` 调用 |
| `relics.StrangeSpoon` | `mark`、`strip`、`removed`、`onSave`/`onLoad`、`restorePending()`：去消耗的拾取/复制/存档恢复；`SaveData` 是注册的存档字段 |
| `relics.Matryoshka` | `drawOnShuffle()`：洗牌事件触发，由 `ShuffleTriggerPatch.fire()` 调用 |
| `relics.BlackBlood` | `reset()`：战斗开始/结束时清空"本次已触发"标记 |
| `relics.BlackStar` | `onVictory(room)`、`resetRun()`、`sync(relic)`、`currentElites()`、`vanillaElites()`；`SaveData` 是注册的存档字段 |
| `relics.PrismaticShard` | `StatMigration`：旧存档一次性补基础能量/抽牌量 |
| `relics.SneckoEye` | `Migration`：旧存档补第 3 个手牌位 |
| `relics.TinyHouse` | `tick()`、`reset()`：自由营火；常量 `REOPEN_DELAY = 0.3F`、`FORCE_AFTER = 3.0F` |
| `relics.Cauldron` | `isBrewing()`：大锅打开自己的奖励界面期间，破碎王冠不转换卡牌奖励 |
| `relics.HandDrill` | `bypass(DamageInfo)`：手钻无视格挡/坚不可摧 |
| `patches.ShivDescriptionPatch` | `refresh(AbstractCard)`、`refreshAll()`、`variant(boolean, boolean)`：乾坤一掷/死吧死吧死吧生效时的小刀卡面文案 |

新 Power（18）：`NextTurnFocus`、`GlassKnifeMark`、`Mark`、`HelloWorldAnyCard`、`ShivMastery`、`FreeSkill`、
`CreativeAIUpgraded`、`BattleHymnSmite`、`LikeWaterCalm`、`StudyInsight`、`DevotionDraw`、`ScuffleDraw`、
`ShuffleEnergy`、`ShuffleSeek`、`ShivAllEnemies`、`ShivPoison`、`BlurDraw`、`SadisticNature`； `WaveOfTheHandStancePower`，继承原版 `WaveOfTheHandPower` 并沿用其原版 ID。

新 Action（18）：`WhirlwindXAction`、`MultiCastXAction`、`GashClawAction`、`DiscardForShivsAction`、
`PlayShivsFromExhaustAction`（含 `reset()`）、`ExhaustAllStatusAction`、`ExhaustAllHandAction`、`IncreaseCostAction`、
`SanctityEnergyAction`、`ShivDamageAction`、`ShivDamageAllAction`、`TransformDrawPileToShivsAction`、`BouncingFlaskXAction`、
`RagnarokXAction`、`ReturnCardToHandAction`、`PercentJudgementAction`、`ReachHeavenAction`、
`PlayRandomAttackFromDrawPileAction`。

Power 一律用 `anotherspirerework:` 命名空间 ID，图标复用原版区域，不需要额外美术；
`Entrench` 刻意复用原版 `NextTurnBlockPower`。

观察者接口 `powers.DrawPileShuffleListener`：实现 `onDrawPileShuffled()` 的 power 自动接入洗牌事件
（结茧、羽化即通过它工作）。

### 存档

`BaseMod.addSaveField` 注册了四个自定义字段：

| 字段 | 内容 |
|---|---|
| `EliteKills` | `relics.BlackStar.SaveData`：黑星的全局精英击杀数；旧存档以原版前三幕击杀数兜底 |
| `TurnRelicStatsV1` | `relics.PrismaticShard.StatMigration`：一次性迁移，给已持有的棱镜碎片补基础能量/抽牌量，迁完打标记不再重复 |
| `SpoonNoExhaust` | `relics.StrangeSpoon.SaveData`：勺子"已去掉消耗"的牌，按"卡牌 ID + 升级次数 + misc"记张数，读档后按张数重新应用 |
| `SneckoHandV1` | `relics.SneckoEye.Migration`：给旧存档的异蛇之眼补上第 3 个手牌位 |

字段键（`makeID(...)`）与数据格式都没变，旧存档可继续读。

另有一项**不走自定义字段**的存档兼容处理：遗物池（`shop_relics` / `boss_relics`）随存档保存，读档时不会按品级重建，
因此在品级改动之前开局的存档会把棱镜碎片留在商店池（实例已是 BOSS 品级，`getPrice()` 返回 999 金币）、
把小屋子留在 BOSS 池。`RelicTierPatch.normalizePools`（`AbstractDungeon.loadSave` 的后缀补丁）读档后把
这两件挪回各自品级的池子，插入位置由该局种子决定（存档/读档不会重置遗物池），已持有的遗物丢弃不放回；
`ShopRelicPatch` 另有品级守卫，商店位置只接受自身品级为商店遗物的 id。新开局本来就按品级建池，不受影响。

原则：**不猜测存档没有保存的信息**（例如旧长蛇戒指是原版还是重写版，存档只有 ID）；
不承诺"先升级、再退回旧版本、再升级"的混用存档。

### 构建与验证

```powershell
# 必须用 JDK 8；默认的 JDK 25 会让 PatchContractTest 在 javassist 里抛 NPE
$env:JAVA_HOME='<JDK 8 安装目录>'; mvn -B clean package
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/verify.ps1 -CheckJar
```

- 产出 Java 8 字节码（`maven.compiler.source/target = 1.8`），输出 `target/anotherspire.jar`
  （artifactId 为 `anotherspire`；旧文档里的 `anotherspirerework.jar` 已废弃）。
- **默认不部署**：`mod.deploy.skip` 默认 `true`，而且 Ant 的 `unless` 判定的是"属性有没有被设置"，
  传 `-Dmod.deploy.skip=false` 同样会**跳过**复制。要部署就手动把 jar 拷贝到游戏 `mods/`，
  再用 Java 8 启动 ModTheSpire，只启用 BaseMod + 本模组。
- 代码改动必须重新构建并重启游戏才生效。
- 依赖不在默认 Steam 目录时，用 `mvn -f pom.xml '-Dlocal.dependencies=<目录>' clean package`。

| 层级 | 入口 | 能证明什么 |
|---|---|---|
| 纯逻辑回归 | `SaturatingMathTest` | 饱和算术与随机边界，不初始化图形环境 |
| 补丁契约 | `PatchContractTest`（随 `mvn package` 运行） | 用真实依赖 JAR 校验目标方法、locator、字段与 Instrument 后的字节码 |
| 资源与打包 | `scripts/verify.ps1 -CheckJar` | JSON、双语键与 `!D!`/`[E]` 词元、130 张唯一卡牌替换、45 件唯一遗物替换、卡图尺寸、JAR 条目 |
| 真实运行 | ModTheSpire + 游戏内场景 | 补丁组合、动作/UI 时序、卡面观感、存档迁移；**前三层都不能替代它** |

### 改代码的硬性规则

1. **有返回值的 `@SpirePostfixPatch`，第一个参数必须是 `__result`。** MTS 按位置传参（返回值在前、实例在后），
   写成 `(__instance, __result)` 会把源对象当副本返回——曾经导致"战斗中消耗一张牌，永久牌组里也消失、
   下局只剩发光底盘"。
2. **每个 `AbstractPower` 子类必须声明 `powerStrings` 字段**：能力列表按该字段名反射取标题，
   缺了就取不到本地化文本。
3. **新增/修改卡牌必须同步双语**：`localization/eng|zhs/CardStrings.json` 用 `anotherspirerework:<原版ID>` 键；
   中英键集合必须一一对应，`!D!`/`!M!`/`[E]` 等词元序列必须完全一致（大小写敏感）。遗物同理走 `RelicStrings.json`。
4. **替换表必须仍是 130 个唯一卡牌实例**（`CardSwapPatch.replacements()`）与 **45 个唯一遗物实例**
   （`RelicSwapPatch.replacements()`）；增删后同步更新 `verify.ps1` 的断言和本文数量。
5. **卡图只能是 250×190 或 500×380**，文件名用卡 ID 去空格或原版图片名；能力图标放 `images/powers/`，
   必须是 48×48（`_48`）或 128×128（`_128`）的正方形；拼错目录/文件名会被 `verify.ps1` 警告。
6. **遗物与卡牌同一范式：一件遗物一个类。** 在 `relics/<名称>.java` 继承原版遗物类，只覆写要改的钩子；
   **未覆写的原版钩子就是继承的**，所以不要再写包装类。原版类自己覆写 `updateDescription()` 的遗物，
   子类必须一并覆写，否则读档时会被原版文案覆盖。
7. **条件生成遗物**不满足条件时要留在池中，不能消耗掉本局的一次生成机会（`relics.KeepInPool` / `canSpawn()`）。
8. **补丁优先 Prefix/Postfix/语义 Locator**，`Replace`/`Insert` 只在上面的手段都不行时用；
   需要绕过原版 `hasRelic` 之类判断时走 `util.RelicChecks.ignoreHasRelic`。
9. **中文本地化的 ` NL ` 空格、`#`/`*` 标注与关键词边界不要机械删除**；JSON 能解析不等于卡面渲染正确。
10. **新内容用命名空间 ID**；刻意替换原版内容可以保留原版 ID，但要在文档说明冲突范围。
11. 新增延迟工作（需要等动画/界面结束的）沿用现有落点：每帧工作放 `receivePostDungeonUpdate`，
    战斗开始/结束的状态清理放 `receiveStartGame` / `receivePostBattle`。
12. **「消耗」标志必须与中英文本一致**：带消耗的牌两种语言都要写「消耗 / Exhaust」，
    不消耗的牌一处都不能出现（`verify.ps1` 都有断言）。带 `UPGRADE_DESCRIPTION` 的卡在 `upgrade()` 里要显式
    `rawDescription = cardStrings.UPGRADE_DESCRIPTION; initializeDescription();`——引擎不会自动应用它。

### 已知取舍

- 浏览牌堆、地图或单卡界面期间，洗牌触发、医药箱·蓝蜡烛的抽牌处理、勺子·星系仪选牌都**推迟到界面关闭后**；
  被推迟的牌若在此之前已离开手牌（例如被打出），本次处理会跳过。效果本身不取消。
- 勺子的"继承去消耗"按源卡牌对象判定，读档按"ID + 升级 + misc 张数"恢复；
  战斗牌组复制、多利之镜等**等价复制**会一并继承。
- 数值上限放宽只到 int 上限：这些值存在 `int` 字段里（`AbstractCreature.currentBlock`、`AbstractPower.amount`、
  `EnergyPanel.totalCount`），改成 `long` 意味着替换字段、所有读取公式和存档格式。
- **小屋子的自由营火**：每个营火选项用它自己的原版结算，本模组只做两件事——把刚用过的选项置灰
  （每个选项每次营火各一次），以及在该选项的**特效与界面都结束之后**用 `CampfireUI.reopen()` 把营火放回去，
  并把房间标记为完成。等待在 `receivePostDungeonUpdate` 里做，另有 `FORCE_AFTER = 3.0F` 的兜底。
  在锻造/抽烟/净化的选牌界面点取消会**退还**该选项（点选项时置灰的「回忆」也一并恢复；此前已经用掉其它选项则仍锁定）。已知小瑕疵：休息、举重、挖掘、回忆会淡出营火的循环环境音，
  放回营火后本处营火是安静的。
- **疾风连击**从消耗堆/抽牌堆回手用的是本模组自己的 `ReturnCardToHandAction`；手牌已满
  （`BaseMod.MAX_HAND_SIZE`）时它留在原处，不会掉进弃牌堆。
- **瓶装三遗物**的幕数门槛在**遗物池抽取时**生效：抽到但未到第三幕时 `KeepInPool` 让它们留在可存档的池里；
  其余条件仍按原版处理。
- **遗物文案**：读档时原版会对每个遗物调用 `updateDescription(PlayerClass)`；只有原版类自己覆写了该方法的
  五件遗物需要在子类里一并覆写，其余遗物的文案在构造函数里由 `getUpdatedDescription()` 设好即可。
- **同方法的同类型补丁顺序**：祈祷轮与恩洛斯的礼物现在是两个独立的 `setupItemReward` 后缀，
  精英房里两组额外奖励的排列顺序不保证（只影响显示顺序，不影响奖励内容）。
- **N'loth 事件**直接 `new NlothsGift()`，不走遗物库；由 `relics.NlothsGift` 内的嵌套仪器补丁换成重写类，
  这是原版唯一一处绕过 `RelicLibrary.getRelic` 的遗物构造。
- **残虐天性**的翻倍只作用于敌人的**普通伤害**；敌人身上的虚弱/易伤层数本身不变，改的是它们生效的比例。
  该 power 的 `amount` 固定为 `-1`，buff 图标不画数字；多张仍只留一个图标，`active()` 只看 ID。
