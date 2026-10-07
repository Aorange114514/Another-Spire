# AnotherSpire Rework

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
- 本机构建：`mvn -f pom.xml clean package` 在 **Zulu 8** 下 `BUILD SUCCESS`（122 个补丁目标与上述全部断言通过），`verify.ps1 -CheckJar` 通过。注意用 JDK 25 跑 `mvn package` 会在 `postfixBindingRegression()` 的 `CtClass.toClass()` 处抛 `NullPointerException`（javassist 的 `DefineClassHelper` 在新 JDK 上取不到 `defineClass`），与本轮改动无关，换 Java 8 即可。
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
- 自动验证：Zulu Java 8 执行 `mvn clean package`，123个补丁目标（含新遗物、洗牌事件、跳过与卡图加载补丁）通过契约检查，新增卡牌/能力/遗物的动作契约、第四幕27件清单与既有52+30项阈值回归全部通过；`verify.ps1 -CheckJar` 检查双语字段与能量词元、96张唯一替换、7个新卡牌类与全部顶层类已打包。以上均为无图形环境检查。
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
- 自动验证使用Zulu Java 8执行Maven clean package：算术30302项、既有遗物/回合边界检查、新卡牌动作与升级结构契约及实际JAR补丁接口检查；verify.ps1 -CheckJar检查双语字段/能量词元、89张唯一替换和全部顶层类打包。以上均为无图形环境验证，不等于游戏内实测。

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

A Slay the Spire mod with 89 card-library replacements (Ironclad / Silent / Defect / Watcher / Colorless), plus the separately patched Bite and existing relic reworks.
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
  with no extra work): `Expect a Fight` (the reworked `Pummel`) counts the Attacks in hand, `Storm of Steel`
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
| Action | `PlayShivsFromExhaustAction` | Storm of Steel (replays a copy of every Shiv in the exhaust pile) |
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
