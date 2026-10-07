# AnotherSpire Rework

以**原版 ID** 重写《杀戮尖塔》的卡牌与遗物：卡池、初始牌组、商店、奖励、变化、图鉴与旧存档都照原样工作。

- mod id `anotherspirerework`，依赖 **仅 BaseMod**，Java 8。
- 完整改动需求：仓库根目录 `../requirements.txt`（UTF-8）。
- 逐次修复的来龙去脉：[CHANGELOG.md](CHANGELOG.md)。

> **不要同时启用旧的 `AnotherSpire`。** 两者修改相同的卡牌与方法，包名/modID 不同不代表机制可叠加。
> 切换版本前结束当前战斗并备份存档；进行中的战斗不承诺 power ID 可互相迁移。

## 1. 对原版卡牌的改动

### 替换机制

`patches/CardSwapPatch` 在 `CardLibrary.initialize()` 之后重写 `CardLibrary.cards`：把 96 个条目换成
`anotherspirerework.cards.<color>.<Name>`，**保留原版 cardID 与原版图片路径**，并带过 `isSeen`。
游戏所有取卡路径（`CardLibrary.getCardList`、`AbstractDungeon.getCardFromPool`、`initializeStarterDeck`…）
都读这张表，所以替换一处即全局生效。事件里 `new Bite()` 这类**绕过卡库的直接构造**由 `patches.BitePatch` 单独补丁。

### 清单（96 张）

| 颜色 | 数量 | 目录 |
|---|---|---|
| 铁甲战士 | 23 | `cards/red` |
| 静默猎手 | 22 | `cards/green` |
| 故障机器人 | 26 | `cards/blue` |
| 观者 | 18 | `cards/purple` |
| 无色 | 7 | `cards/colorless` |

每张牌的数值以 `../requirements.txt` 为准（下表即其完整清单）；**结构性**改动（不只是改数字）先看这里：

| 卡牌 | 改动 |
|---|---|
| 重刃、铁斩波 | 稀有度 普通→罕见 |
| 燃烧契约 | 稀有度 罕见→普通 |
| 乾坤一掷、致残毒云 | 稀有度 稀有→罕见 / 罕见→稀有 |
| 跃跃欲试（替换连续拳）、暴雪、熔化、核心电涌 | 攻击牌 → 技能牌 |
| 死吧死吧死吧、乾坤一掷、重编程、结茧、羽化 | 改为能力牌（重编程同时移除 Exhaust） |
| 双重释放 → 四重释放 | 重做为“激发最右侧充能球 4 次” |
| 雷霆打击、乱战、深谋远虑、秘密技法、秘密武器、结茧、羽化 | 本次新增的改动条目（沿用原版 ID） |

### 完整改动清单

96 张替换卡按角色列出；格式为 **费用（基础 → 升级后）**，"效果" 括号内为升级后的数值。
⚠ = 类型/稀有度等结构性变化。噬咬（`Bite`）不在 96 张内，由 `BitePatch` 单独处理。
全部沿用**原版 cardID**，因此没有注册任何新卡池。

#### 铁甲战士（23）

| 卡牌 | 本地化键（原版 ID） | 费用 | 效果（升级后） |
|---|---|---|---|
| 交锋 | `Clash` | 0 | 造成 14（18）点伤害；你手中每有一张非攻击牌，伤害降低 2 |
| 狂野打击 | `Wild Strike` | 1 | 造成 12（17）点伤害；将一张伤口放入你的弃牌堆 |
| 重刃 ⚠普通→罕见 | `Heavy Blade` | 2 | 造成 15（20）点伤害；力量在重刃上发挥 5（7）倍效果 |
| 金刚臂 | `Clothesline` | 2 | 造成 14（18）点伤害；给予 3 层虚弱 |
| 铁斩波 ⚠普通→罕见 | `Iron Wave` | 0 | 造成 3（5）点伤害；获得等同于所造成伤害的格挡（不依赖敌人实际生命损失） |
| 活动肌肉 | `Flex` | 0 | 获得 4（8）点临时力量（回合结束时失去） |
| 闪电霹雳 | `Thunderclap` | 1 | 对所有敌人造成 4（6）点伤害；给予 1（2）层易伤 |
| 顺劈斩 | `Cleave` | 1 | 对所有敌人造成 11（14）点伤害；消耗 |
| 飞剑回旋镖 | `Sword Boomerang` | 1 | 随机对敌人造成 3（4）点伤害 3（4）次 |
| 上勾拳 | `Uppercut` | 1 | 造成 7（8）点伤害；给予 1（2）层虚弱、1（2）层易伤 |
| 哨卫 | `Sentinel` | 1 | 获得 6（9）点格挡；抽 1 张牌；如果这张牌被消耗，获得 [E][E] |
| 巩固 | `Entrench` | 1（0） | 在下个回合开始时，获得等同于当前格挡的格挡 |
| 放血 ⚠罕见→普通 | `Bloodletting` | 0 | 获得 [E][E]（[E][E][E]）；失去 3（2）点生命 |
| 断魂斩 | `Sever Soul` | 2（1） | 消耗手牌中所有非攻击牌；造成 16 点伤害 |
| 旋风斩 | `Whirlwind` | X | 对所有敌人造成 6（10）点伤害 X（X+1）次 |
| 暴走 | `Rampage` | 1 | 造成 4 点伤害 2 次；这张牌每被打出一次，本场战斗伤害 +5 |
| 灼热攻击 | `Searing Blow` | 2 | 造成 10（13）点伤害；获得 10（13）点格挡；能被多次升级 |
| 狂暴 | `Berserk` | 2 | 每回合开始获得 [E]（[E][E]）；不再对自身施加易伤 |
| 金属化 | `Metallicize` | X | 在你的回合结束时，获得 X×3（4）点格挡 |
| 跃跃欲试 ⚠攻击→技能（替换连续拳） | `Pummel` | 2（1） | 你的手牌中每有一张攻击牌，获得 [E] |
| 燃烧契约 ⚠罕见→普通 | `Burning Pact` | 1 | 消耗 1 张牌；抽 2（3）张牌（原效果不变） |
| 盛怒 | `Seeing Red` | 1（0） | 如果你本回合消耗过牌，获得 [E][E][E] |
| 地狱之刃 | `Infernal Blade` | 1（0） | 从 3 张随机攻击牌中选择一张加入手牌，本回合耗能变为 0；消耗 |

#### 静默猎手（22）

| 卡牌 | 本地化键（原版 ID） | 费用 | 效果（升级后） |
|---|---|---|---|
| 匕首雨 | `Dagger Spray` | 1 | 对所有敌人造成 2 点伤害 5（8）次；消耗 |
| 后空翻 | `Backflip` | 1 | 获得 5（6）点格挡；抽 2（3）张牌 |
| 带毒刺击 | `Poisoned Stab` | 1 | 造成 4（6）点伤害；给予 5（6）层中毒 |
| 快斩 | `Quick Slash` | —（不能被打出） | 如果这张牌从手牌中被丢弃，对所有敌人造成 8 点伤害 |
| 投掷匕首 | `Dagger Throw` | 1 | 造成 9（12）点伤害；抽 1（2）张牌；丢弃 1（2）张牌 |
| 抢占先机 | `Outmaneuver` | 0 | 下一回合获得 [E]（[E][E]） |
| 勒脖 | `Choke` | 1 | 造成 6 点伤害；你在这个回合内每打出一张牌，该敌人失去 3（5）点生命 |
| 千穿百刺 | `Riddle With Holes` | 2 | 造成 3 点伤害 6（8）次 |
| 玻璃刀刃 | `Glass Knife` | 1 | 造成 8 点伤害 2 次；每当该敌人受到来自小刀的伤害，你获得 3（5）点格挡；消耗 |
| 凌迟 | `A Thousand Cuts` | 1 | 你每打出一张牌，对所有敌人造成 1（2）点伤害 |
| 乾坤一掷 ⚠稀有→罕见、攻击→能力 | `Unload` | 1 | 小刀获得保留；你每回合打出的第一张小刀伤害 +9（12） |
| 精巧刺击 | `Masterful Stab` | 0 | 丢弃最多 3 张牌；每丢弃一张，将一张小刀（+）加入手牌（手牌非空时总是打开可选 0 张的界面） |
| 致残毒云 ⚠罕见→稀有 | `Crippling Poison` | 3（2） | 给予所有敌人 12 层中毒和 3 层虚弱；消耗 |
| 独门技术 | `Expertise` | 1 | 抽牌直到手牌有 7（8）张牌；丢弃 1 张牌 |
| 钢铁风暴 | `Storm of Steel` | 2（1） | 将你消耗堆中所有小刀的复制对一名敌人打出（卡面显示张数） |
| 余像 | `After Image` | 1 | 你每打出一张牌，获得 1（2）点格挡 |
| 闪躲翻滚 | `Dodge and Roll` | —（不能被打出） | 如果这张牌从手牌中被丢弃，获得 8（11）点格挡 |
| 死吧死吧死吧 ⚠攻击→能力 | `Die Die Die` | 2（1） | 小刀现在会攻击所有敌人；将 3 张小刀加入你的手牌 |
| 涂毒 | `Envenom` | 1 | 你的小刀会给予 1（2）层中毒；将 1 张小刀加入你的手牌 |
| 声东击西 | `Distraction` | 1（0） | 从 3 张随机技能牌中选择一张加入手牌，本回合耗能变为 0；消耗 |
| 斗篷与匕首 | `Cloak And Dagger` | 1 | 获得 6 点格挡；选择你抽牌堆中的 1（2）张牌，将其变化为小刀 |
| 全神贯注 | `Concentrate` | 0 | 丢弃 3 张牌；获得 [E][E]（[E][E][E]） |

#### 故障机器人（26）

| 卡牌 | 本地化键（原版 ID） | 费用 | 效果（升级后） |
|---|---|---|---|
| 四重释放（由双重释放重做） | `Dualcast` | 1（0） | 激发你最右侧的充能球 4 次 |
| 弹回 | `Rebound` | 1 | 造成 3（5）点伤害；生成你本场战斗上一个生成的充能球的复制 |
| 弹幕齐射 | `Barrage` | 1 | 你每拥有一个充能球，造成 5（8）点伤害 |
| 扫荡射线 | `Sweeping Beam` | 1 | 对所有敌人造成 6（7）点伤害；抽 2（3）张牌 |
| 爪击 | `Gash` | 0 | 造成 1 点伤害 2（3）次；在本场战斗中所有爪击牌的伤害 +2（3） |
| 精简改良 | `Streamline` | 3 | 造成 18（22）点伤害；你每抽到这张牌时，它在本场战斗中的耗能 −1 |
| 蒸汽护壁 ⚠两档都消耗 | `Steam` | 0 | 生成 1（2）个冰霜充能球；消耗（升级后仍然消耗，中英升级文本已同步标注） |
| 飞跃 | `Leap` | 1 | 获得 9（10）点格挡；在本回合获得 2（4）点临时集中 |
| 你好，世界 | `Hello World` | 1 | 在你的回合开始时，增加一张随机牌到你的手牌（升级后固有） |
| 冰寒 | `Chill` | 0 | 当前每有一名敌人，生成一个冰霜充能球；消耗（升级后取消消耗） |
| 力场 | `Force Field` | 2 | 获得 12 点格挡；你每有一张能力牌，格挡 +4（6） |
| 暴雪 ⚠攻击→技能 | `Blizzard` | 1（0） | 你每有一个冰霜充能球，在本回合获得 2（3）点临时集中 |
| 狂乱撕扯 | `Rip and Tear` | 1 | 随机对敌人造成 7 点伤害 2（3）次 |
| 熔化 ⚠攻击→技能 | `Melter` | 1（0） | 移除一名敌人的所有人工制品和格挡；给予 2 层虚弱、2 层易伤；消耗 |
| 聚变 | `Fusion` | 1 | 生成一个等离子充能球；消耗（升级后取消消耗） |
| 重编程 ⚠技能→能力 | `Reprogram` | 2 | 失去一个充能球栏位；获得 2（3）点力量、2（3）点敏捷 |
| 多重释放 | `Multi-Cast` | X | 激发你最右侧的充能球 X（X×2）次 |
| 超能光束 | `Hyperbeam` | 2 | 对所有敌人造成 24（32）点伤害；在本回合失去 3 点集中 |
| 彩虹 | `Rainbow` | 2 | 生成闪电、冰霜、黑暗、等离子充能球各一个；消耗（升级后取消消耗） |
| 散热片 | `Heatsinks` | 1 | 你每打出一张能力牌，抽 2 张牌（升级后固有） |
| 核心电涌 ⚠攻击→技能 | `Core Surge` | 0 | 获得一个充能球栏位；抽 1（2）张牌；将这张牌在本场战斗中的耗能 +1 |
| 重启 | `Reboot` | 0 | 移除你身上的所有负面效果；消耗你所有的状态牌；将你所有未消耗的牌重新洗入抽牌堆；抽 4（5）张牌；消耗 |
| 创造性AI | `Creative AI` | 3 | 在每回合开始时，增加一张（升级过的）随机能力牌到你的手牌 |
| 陨石打击 | `Meteor Strike` | 5 | 造成 30 点伤害；生成 3（4）个等离子充能球 |
| 堆栈 | `Stack` | 1 | 获得相当于你当前抽牌堆中牌数（+3）的格挡；卡面显示实时数值 |
| 雷霆打击 | `Thunder Strike` | 3（2） | 你在本场战斗中每生成过一个闪电充能球，生成一个闪电充能球并随机造成一次 3 点伤害 |

#### 观者（18）

| 卡牌 | 本地化键（原版 ID） | 费用 | 效果（升级后） |
|---|---|---|---|
| 点穴 | `PathToVictory` | 1 | 给予 8（11）层印记；所有拥有印记的敌人受到攻击时额外失去相当于印记层数的生命；消耗 |
| 供奉 | `Consecrate` | 1 | 对所有敌人造成 6 点伤害；每命中一名敌人，抽 1（2）张牌 |
| 追击 | `FollowUp` | 1 | 造成 7（10）点伤害；愤怒：耗能降为 0 |
| 坚韧 | `Perseverance` | 1 | 保留；进入平静；每当这张牌被保留时，获得 5（7）点格挡 |
| 天眼 | `ThirdEye` | 1 | 将 2（3）张洞见加入你的手牌 |
| 战歌 | `BattleHymn` | 1（0） | 在每回合开始时，消耗一张手牌，并将一张惩恶加入你的手牌 |
| 如水 | `LikeWater` | 1 | 在你的回合结束时，进入平静（升级后固有） |
| 怒火中烧 | `Vengeance` | 1 | 进入愤怒；将 1（2）张洞见加入你的抽牌堆 |
| 旋身 | `Swivel` | 2 | 获得 10 点格挡；抽 2（3）张牌；你打出的下一张攻击牌耗能变为 0 |
| 旋转打击 | `WindmillStrike` | 2 | 保留；造成 5（6）点伤害 3 次；进入愤怒；每当被保留，其攻击次数本场 +1 |
| 欺瞒现实 | `DeceiveReality` | 1（0） | 将一张洞见和一张奇迹加入你的手牌 |
| 研习 | `Study` | 2（1） | 在你的回合开始时，将一张洞见加入你的手牌 |
| 迂回 | `Weave` | 0 | 造成 4 点伤害；愤怒：进入平静；平静：进入愤怒 |
| 虔信 | `Devotion` | 1 | 每当你获得真言时，抽 1 张牌（升级后固有） |
| 圣洁 | `Sanctity` | 1 | 获得 6（8）点格挡；如果本场战斗中打出的上一张牌是技能牌，获得 [E]（[E]） |
| 改造现实 | `CarveReality` | 1（0） | 将一张惩恶和一张平安加入你的手牌 |
| 回环踢 | `WheelKick` | 2 | 造成 15（20）点伤害；抽 2 张牌；你打出的下一张技能牌耗能变为 0 |
| 幸运一击 | `JustLucky` | 0 | 预见 1；获得 2 点格挡；造成 3 点伤害（升级后抽 1 张牌） |

#### 无色（7 张替换 + 噬咬）

| 卡牌 | 本地化键（原版 ID） | 费用 | 效果（升级后） |
|---|---|---|---|
| 净化 | `Purity` | 0 | 保留；从手牌中选择最多 3（5）张牌消耗；这张牌被打出时仍消耗 |
| 乱战 | `Mayhem` | 1（0） | 你每抽 10 张牌，获得 [E]（能力图标显示已抽张数） |
| 深谋远虑 | `Thinking Ahead` | 0 | 抽 2（3）张牌，然后将手牌中的一张放到你抽牌堆的顶端；消耗 |
| 秘密技法 | `Secret Technique` | 0 | 从抽牌堆中选择 1（2）张技能牌放入你的手牌；消耗 |
| 秘密武器 | `Secret Weapon` | 0 | 从抽牌堆中选择 1（2）张攻击牌放入你的手牌；消耗 |
| 结茧 ⚠改为能力牌 | `Chrysalis` | 2（1） | 你每次将抽牌堆洗牌时，获得 [E] |
| 羽化 ⚠改为能力牌 | `Metamorphosis` | 2（1） | 你每次将抽牌堆洗牌时，从抽牌堆中选择一张牌放入你的手牌 |
| 噬咬（不走卡库） | `Bite` | 1 | 造成 7（9）点伤害；未被格挡的伤害将回复你的生命。由 `BitePatch` 就地补丁（吸血鬼事件直接 `new Bite()`） |

#### 需求未指定、由实现决定的升级

| 卡牌 | 升级选择 |
|---|---|
| 放血 | 能量 [E][E]→[E][E][E]，失去生命 3→2 |
| 噬咬 | 伤害 +2 |
| 快斩 | 伤害 +4（沿用原版方向） |
| 暴走 | 每次打出增加的伤害 +3（沿用原版方向） |
| 弹回 | 伤害 +2 |
| 迂回 | 伤害 +2（沿用原版方向） |
| 暴雪 | 耗能 1→0 **且** 每个冰霜球的集中 2→3（需求同时列出了这两个值） |
| 闪躲翻滚 | 格挡 +3 |
| 幸运一击 | 追加“抽 1 张牌”；格挡与伤害保持 2 / 3 |
| 彩虹 | 保持原版结构：基础牌消耗，升级取消消耗 |
| 死吧死吧死吧 | 耗能 2→1 |
| 蒸汽护壁 | 两档都保留消耗（需求只列了一次“消耗”，没说升级取消） |
| 坚韧 | 需求未给“打出时”的格挡值，因此只在被保留时结算 |

### 卡图

`patches.CardArtLoaderPatch` **只做绘制**：有图就画，没图回退原版图集，不裁切、不缩放、不动卡牌坐标。

- 位置：`src/main/resources/anotherspirerework/images/` 或 `.../images/cards/`。
- 尺寸：卡面 **250×190**、放大界面 **500×380**，给其中一个即可（另一个按整图缩放使用）。
- 文件名：卡 ID 去空格（`DieDieDie.png`）或原版图片名（`die_die_die.png`），两种都识别；`_p` 后缀表示放大图。
- 改图后重新 `mvn package` 并重启游戏。

## 2. 对原版遗物的改动

两套机制并存：

1. **包装类**：`patches.RelicReworkPatch.Swap` 把 `RelicLibrary.getRelic()` 返回的 16 件原版遗物包装成
   `relics.ReworkedRelic`，保留原版 ID、图片与获取途径。属这 16 件的是：棱镜碎片、勇气投石索、手钻、
   邪教徒头套、大锅、融合之锤、痛楚印记、天鹅绒颈圈、灵体外质、黑暗之血、破碎王冠、神圣树皮、添水、
   长蛇戒指、壶铃、黑星（判定见 `ReworkedRelic.supports`）。
2. **直接补丁**：其余遗物直接给原版类打 Prefix/Postfix/Insert（`RelicReworkPatch`、`ReportedRelicFixes`、
   `TurnRelicFixes`、`NewRelicFixes`、`ShopRelicPatch`、`StrangeSpoonExhaust`），让**原版实例与包装实例共用同一行为**。

> ⚠️ 包装类**取代**了原版实例，它没有转发的原版钩子就等于不存在。例如原版 `VelvetChoker.onVictory()`
> 用 `counter = -1` 在战斗结束隐藏计数，包装类不转发就会把上回合的张数一直挂在遗物栏上。
> 改包装遗物的行为时，必须逐项核对它取代了哪些原版钩子。

### 改动一览（全部沿用原版遗物 ID）

| 遗物 | 改动 |
|---|---|
| 棱镜碎片 | 战斗奖励可掉落无色与其它颜色牌；每回合开始 +[E] 并抽 1 张 |
| 勇气投石索 | 每场战斗开始 +2 力量、+2 敏捷 |
| 手钻 | 玩家的普通攻击伤害无视敌人格挡（含心脏“坚不可摧”）；不影响中毒/荆棘，也不消除敌人格挡 |
| 邪教徒头套 | 保留原描述，附加隐藏效果：战斗开始获得 1 层仪式 |
| 化学物 X | 生成条件：仅当卡组或当前商店存在耗能为 X 的牌，否则跳过、下个商店再试 |
| 大锅 | 拾起时 +1 药水栏位并制作 5 瓶药水（果汁生成概率翻倍）；原 2 栏位改为 1 |
| 李家华夫饼 | 生成条件：当前生命低于 50%，否则跳过、下个商店再试 |
| 送货员 | 保留原描述，附加隐藏效果：买走商店遗物后在原位再生成一个商店遗物，池空后正常生成 |
| 融合之锤 | 拾起时随机降级 4 张（每次 1 级，可重复选中灼热攻击）；每回合 +[E]；不再禁止营火锻造 |
| 痛楚印记 | 每回合 +[E]；战斗开始将 2 张伤口放入弃牌堆 |
| 天鹅绒颈圈 | 每回合 +[E]；本回合打出超过 6 张时，下回合开始失去 [E] |
| 灵体外质 | 回合开始失去 5 金币换 [E]（金币不足 5 则不付）；可在第一、二幕 Boss 宝箱生成；恢复正常获取金币 |
| 黑暗之血 | 替换燃烧之血：每场战斗第一张耗能 ≥2 的攻击牌，其未格挡伤害回复生命；开战将 1 张疼痛加入手牌 |
| 破碎王冠 | 每份卡牌奖励替换为随机普通/罕见/稀有遗物；不再提供额外能量 |
| 神圣树皮 | 每回合 +[E]；药水少于 2 瓶时失去 [E]（空栏不计数） |
| 添水 | 每回合 +[E]；每场战斗锁定药水，直到主动丢弃一瓶（饮用/投掷/仙灵药水不解锁；战斗外不受限） |
| 长蛇戒指 | 替换蛇之戒指：每场战斗前 3 回合各额外抽 2 张；取消原版永久 +1 抽牌 |
| 壶铃 | 营火举重上限 5 次（原 3 次），每次同时 +1 力量、+1 敏捷；包装实例不重复结算 |
| 召唤铃铛 | 独特诅咒 + 5 个随机遗物，其中至少各 1 普通/罕见/稀有 |
| 黑星 | 本局击败 ≥5 个精英后每回合 +[E]；遗物角标显示本局累计精英击杀数（不封顶，读档同步） |
| 忍术卷轴 | 每回合开始将 1 张小刀加入手牌（替换原版开战 3 张） |
| 两仪 | 每打出攻击牌 +1 临时敏捷；每打出技能牌 +1 临时力量 |
| 英雄宝典 | 战斗开始可从 3 张随机能力牌中选 1 张加入手牌，本回合 0 费，可跳过 |
| 医药箱 | 抽到状态牌时将其消耗并抽 1 张；状态牌可打出并消耗 |
| 蓝蜡烛 | 抽到诅咒牌时将其消耗；诅咒牌可打出并消耗，同时失去 1 点生命 |
| 奇怪的勺子 | 拾起时从牌组中选最多 2 张移除消耗（替换原版 50% 不消耗）；记录跨存档保留 |
| 星系仪 | 拾起时从 10 普通/10 罕见/10 稀有中任选若干张加入牌组（替换原版 4 次卡牌奖励） |
| 冻结之眼 | 打击与防御获得虚无（中英关键词按词元边界写入，拾取/失去时同步牌组） |
| 机械臂 | 每回合开始获得 1 个充能球栏位 |
| 套娃 | 洗牌时抽 1 张（替换原版宝箱额外遗物） |
| 27 件遗物 | `actNum >= 4` 且非无尽模式时不再生成；清单见下表 |

**不进第四幕（心脏层）的 27 件遗物**（`NewRelicFixes.ACT_FOUR_BLOCKED`，对应 `../requirements.txt` 第 23 条）：

> 白兽雕像、黑石护符、昆虫标本、御守、微笑面具、佛珠手链、古茶具套装、小宝箱、捕梦网、皇家枕头、
> 陶瓷小鱼、餐券、地精之角、带骨肉、忍术卷轴、永恒羽毛、送货员、问号牌、颂钵、历石、古钱币、壶铃、
> 宁静烟斗、生物样本、羽翼之靴、转经轮、铲子

实现上：不覆盖 `canSpawn` 的 6 件走 `AbstractRelic` 基类补丁，自己声明了 `canSpawn` 的 21 件逐个打前缀，
避免基类补丁被子类覆盖绕过。

**保留原描述、只加隐藏效果的 4 件**：邪教徒头套、送货员、化学物 X（生成条件）、李家华夫饼（生成条件）。
其中化学物 X / 李家华夫饼不满足条件时**留在可存档的遗物池里**，下个商店再判定，不会消耗本局的生成机会。

### 遗物本地化

描述放在 `localization/eng|zhs/RelicStrings.json` 的 `anotherspirerework:<原版遗物ID>` 键下，不覆盖原版字符串。
遗物标注规范**与卡牌不同**：以 `#r卡名`、`#g遗物名`、`#y遗物` 标注（卡牌才用 `*卡名`）。
需求中以 `#` 标注的补充说明（生成条件、隐藏效果等）不进描述文本。

## 3. 其它机制改动

- **无色选牌加“跳过”**：`ColorlessSkipPatch` 给 `CardRewardScreen.customCombatOpen` 打开跳过硬开关，覆盖
  发现、无色药水、工具箱、尼尔里的典籍、外来影响；普通卡牌奖励与观者的“抉择”不受影响。
- **移除数值上限**：原版把格挡/力量·敏捷·集中·枷锁/中毒(9999)/充能/能量钳制在 ±999，现放宽到 int 上限。
  补丁为 `NoBlockCapPatch`、`NoPowerCapPatch`、`NoEnergyCapPatch`：**Prefix 算原值、仅当钳制真的触发时由
  Postfix 写回**，不复制也不替换原版代码，其它模组的补丁照常运行。
- **溢出饱和**：上限解除后，越界运算饱和到 int 上限而不是翻符号（“超过上限就是上限”）。由
  `util.SaturatingMath` 与 `NoPowerCapPatch`/`NoBlockCapPatch`/`NoEnergyCapPatch`/`TriplePoisonSaturatePatch`
  负责；重刃不再沿用原版“乘上去再除回来”的取巧写法。
- **洗牌事件**：`EmptyDeckShuffleAction` / `ShuffleAllAction` 结算完成时只标记“待处理”，等 `SoulGroup.isActive()`
  为 false（洗牌动画播完）且没有界面打开时才触发。结茧、羽化、套娃都依赖它。故意不接 `ShuffleAction`，
  避免同一次洗牌触发两次。
- **界面保护（只改时机，不取消效果）**：浏览牌堆/地图/单卡界面期间，洗牌触发、医药箱·蓝蜡烛的抽牌处理、
  勺子·星系仪选牌都推迟到界面关闭后执行。

## 4. 新增的方法与辅助类

### 主类 `AnotherSpireRework`

| 方法 | 用途 |
|---|---|
| `makeID(String)` | 生成 `modID:name`，本地化 lookup key |
| `getCardStrings(String)` / `getPowerStrings(String)` | 读带命名空间的字符串；缺失时记录 error 并回退 mock |
| `extendedDescription(CardStrings, int)` | 安全读 `EXTENDED_DESCRIPTION[i]`，越界或翻译缺失返回空串 |
| `localizationPath(String lang, String file)` / `imagePath(String file)` | 资源路径拼接 |
| `inCombat()` | 当前是否在战斗中 |

订阅的 BaseMod 钩子：`PostInitialize`、`PostBattle`、`PostDraw`、`PostExhaust`、`PostDungeonUpdate`、
`PostPowerApply`、`StartGame`、`PreStartGame`、`EditStrings`、`EditKeywords`。

### 洗牌事件 `patches.ShuffleTriggerPatch`

| 方法 | 用途 |
|---|---|
| `fire()` | 触发所有 `DrawPileShuffleListener` 与套娃 |
| `reset()` | 战斗开始/结束时清空待处理标记 |
| `tick()` | 每帧兜底（由 `receivePostDungeonUpdate` 调用），处理“抽牌先结束、动画还没播完”的路径 |
| `flush(AbstractGameAction)` | 包级；动画结束且无界面时触发，并把被中断抽牌的剩余张数放回队首 |

### 选牌界面 `effects.ChooseDeckCardsEffect`

`start(Choice, String message, int maxCards)`、`browsingCards()`、`enum Choice { REMOVE_EXHAUST, ADD_TO_DECK }`。
它挂在 `AbstractDungeon.effectList` 而**不是**战斗 Action：`GameActionManager.addToBottom` 只在战斗房间接收动作，
商店/宝箱拾取时会被直接丢弃。

### 饱和算术 `util.SaturatingMath`

`fromLong(long)`、`add(int,int)`、`subtract(int,int)`、`multiply(int,int)`。

### 卡图 `patches.CardArtLoaderPatch`

`candidateNames(String cardID, String assetUrl)`：按“卡 ID 去空格 → 图片路径转下划线 → 图片文件名”返回候选名。

### 供其它补丁复用的静态助手

| 类 | 方法 |
|---|---|
| `ExhaustTracker` | `onCardExhausted()`、`exhaustedThisTurn()`、`reset()`（燃愿判断本回合是否消耗过牌） |
| `TurnRelicFixes` | `serpentActive`、`starActive`、`potionLocked`、`potionsLocked`、`resetBattle`、`discard`、`prismEquip`、`serpentTurn`、`vanillaElites`、`victory`、`startGame`、`currentElites`、`syncStar`、`starTurn`；实现 `CustomSavable<Integer>`（精英击杀），内部 `StatMigration` 做旧存档一次性迁移 |
| `NewRelicFixes` | `inActFour`、`actFourBlocked`、`ninjaScrollTurn`、`yangUse`、`enchiridionStart`、`onCardDrawn`、`processPendingDraws`、`isStrikeOrDefend`、`frozenEyeOwned`、`applyFrozenEye`、`removeFrozenEye`、`keywordSuffix`、`inserterTurn`、`matryoshkaShuffle`、`spoonEquip`、`orreryEquip`、`giryaCampfireOptions` |
| `ReportedRelicFixes` | `penalizeChoker`、`penalizeBark`、`loseEnergy`、`markStart`、`chokerStart`、`barkStart`、`cauldronEquip` |
| `StrangeSpoonExhaust` | `removed`、`strip`、`mark`、`withoutExhaustText`、`formatCounts`、`restorePending`；实现 `CustomSavable<String>`（去消耗张数） |
| `RelicReworkPatch` | `ignoreRelic(String)`（生成 `ExprEditor` 关掉指定遗物的原版 `hasRelic` 分支）、`potionsLocked()`、`bypass(DamageInfo)`（手钻无视格挡）、公开字段 `bloodActive` |
| `ReworkedRelic` | `supports(String)`：判定哪些遗物需要包装；覆写 `onEquip`/`onUnequip`/`atBattleStart`/`atTurnStart`/`onVictory`/`onPlayCard`/`addCampfireOption` |
| `ShopRelicPatch` | 条件遗物（化学物 X、李家华夫饼）的跳过与补货/池持久化 |

### 新 Power（16）与新 Action（10）

| 类型 | 内容 |
|---|---|
| Power | `NextTurnFocus`、`GlassKnifeMark`、`Mark`、`HelloWorldAnyCard`、`ShivMastery`、`FreeSkill`、`CreativeAIUpgraded`、`BattleHymnSmite`、`LikeWaterCalm`、`StudyInsight`、`DevotionDraw`、`ScuffleDraw`、`ShuffleEnergy`、`ShuffleSeek`、`ShivAllEnemies`、`ShivPoison` |
| Action | `WhirlwindXAction`、`MultiCastXAction`、`GashClawAction`、`DiscardForShivsAction`、`PlayShivsFromExhaustAction`（含 `reset()`）、`ExhaustAllStatusAction`、`IncreaseCostAction`、`SanctityEnergyAction`、`ShivDamageAction`、`TransformDrawPileToShivsAction` |

Power 一律用 `anotherspirerework:` 命名空间 ID，图标复用原版区域，不需要额外美术；
`Entrench` 刻意复用原版 `NextTurnBlockPower`。

### 观察者接口 `powers.DrawPileShuffleListener`

实现 `onDrawPileShuffled()` 的 power 自动接入洗牌事件（结茧、羽化即通过它工作）。

## 5. 存档

`BaseMod.addSaveField` 注册了三个自定义字段：

| 字段 | 内容 |
|---|---|
| `EliteKills` | 黑星的全局精英击杀数；旧存档以原版前三幕击杀数兜底 |
| `TurnRelicStatsV1` | 一次性迁移：给已持有的棱镜碎片补基础能量/抽牌量，迁完打标记不再重复 |
| `SpoonNoExhaust` | 勺子“已去掉消耗”的牌，按“卡牌 ID + 升级次数 + misc”记张数，读档后按张数重新应用 |

原则：**不猜测存档没有保存的信息**（例如旧长蛇戒指是原版还是包装版，存档只有 ID）；不承诺“先升级、再退回旧版本、再升级”的混用存档。

## 6. 维护须知

### 构建与运行

```powershell
# 默认（走 pom 里的 Steam 路径，Windows profile）
mvn clean package

# 本机依赖不在 Steam 目录时
mvn -f pom.xml '-Dlocal.dependencies=C:/Users/riced/Games/OtherGames/sts mod/dependencies' clean package

# 资源与打包检查
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/verify.ps1 -CheckJar
```

- 产出 Java 8 字节码（`maven.compiler.source/target = 1.8`）。
- `mod.deploy.skip=true`：**默认不部署**。把 `target/anotherspirerework.jar` 手动复制到游戏 `mods/`，
  再用 Java 8 启动 ModTheSpire，只启用 BaseMod + 本模组。
- 代码改动必须重新构建并重启游戏才生效；**必须用 JDK 8 跑测试**——JDK 25 下
  `PatchContractTest.postfixBindingRegression()` 的 `CtClass.toClass()` 会抛 NPE（javassist 在新 JDK 上取不到 `defineClass`）。

### 验证层次

| 层级 | 入口 | 能证明什么 |
|---|---|---|
| 纯逻辑回归 | `src/test/java/anotherspirerework/util/SaturatingMathTest.java` | 饱和算术与随机边界，不初始化图形环境 |
| 补丁契约 | `src/test/java/anotherspirerework/PatchContractTest.java`（随 `mvn package` 运行） | 用真实依赖 JAR 校验目标方法、locator、字段与 Instrument 后的字节码 |
| 资源与打包 | `scripts/verify.ps1 -CheckJar` | JSON、双语键与 `!D!`/`[E]` 词元、96 张唯一替换、卡图尺寸、JAR 条目 |
| 真实运行 | ModTheSpire + 游戏内场景 | 补丁组合、动作/UI 时序、卡面观感、存档迁移；**前三层都不能替代它** |

### 改代码的硬性规则

改完请逐条自检，违反其中多数会被契约测试或 `verify.ps1` 直接拦下：

1. **有返回值的 `@SpirePostfixPatch`，第一个参数必须是 `__result`。** MTS 按位置传参（返回值在前、实例在后），
   写成 `(__instance, __result)` 会把源对象当副本返回——曾经导致“战斗中消耗一张牌，永久牌组里也消失、下局只剩发光底盘”。
2. **每个 `AbstractPower` 子类必须声明 `powerStrings` 字段**：能力列表按该字段名反射取标题，缺了就取不到本地化文本。
3. **新增/修改卡牌必须同步双语**：`localization/eng|zhs/CardStrings.json` 用 `anotherspirerework:<原版ID>` 键；
   中英键集合必须一一对应，`!D!`/`!M!`/`[E]` 等词元序列必须完全一致（大小写敏感）。遗物同理走 `RelicStrings.json`。
4. **替换表必须仍是 96 个唯一实例**（`CardSwapPatch.replacements()`）；增删后同步更新 `verify.ps1` 的断言和本文数量。
5. **卡图只能是 250×190 或 500×380**，文件名用卡 ID 去空格或原版图片名；拼错目录/文件名会被 `verify.ps1` 警告。
6. **包装遗物必须转发它取代的原版钩子**（见 §2 的警告）；新增遗物行为时先确认原版钩子是取消还是保留。
7. **条件生成遗物**不满足条件时要留在池中，不能消耗掉本局的一次生成机会。
8. **补丁优先 Prefix/Postfix/语义 Locator**，`Replace`/`Insert` 只在上面的手段都不行时用；
   需要绕过原版 `hasRelic` 之类判断时走 `RelicReworkPatch.ignoreRelic`。
9. **中文本地化的 ` NL ` 空格、`#`/`*` 标注与关键词边界不要机械删除**；JSON 能解析不等于卡面渲染正确。
10. **新内容用命名空间 ID**；刻意替换原版内容可以保留原版 ID，但要在本文档说明冲突范围。
11. 新增延迟工作（需要等动画/界面结束的）沿用现有落点：每帧工作放 `receivePostDungeonUpdate`，
    战斗开始/结束的状态清理放 `receiveStartGame` / `receivePostBattle`。
12. **「消耗」标志必须与中英文本一致**：升级后仍消耗的牌，升级文本也要写「消耗 / Exhaust」（蒸汽护壁就是
    这种情况，`verify.ps1` 已加断言）；反过来改为能力牌的牌（重编程）不得再出现消耗文本（也有断言）。
    带 `UPGRADE_DESCRIPTION` 的卡在 `upgrade()` 里要显式 `rawDescription = cardStrings.UPGRADE_DESCRIPTION;
    initializeDescription();`——引擎不会自动应用它，不写就是一段死数据（同时也会漏掉 `!M!` 刷新）。

### 已知取舍

- 浏览牌堆、地图或单卡界面期间，洗牌触发、医药箱·蓝蜡烛的抽牌处理、勺子·星系仪选牌都**推迟到界面关闭后**；
  被推迟的牌若在此之前已离开手牌（例如被打出），本次处理会跳过。效果本身不取消。
- 勺子的“继承去消耗”按源卡牌对象判定，读档按“ID + 升级 + misc 张数”恢复；
  战斗牌组复制、多利之镜等**等价复制**会一并继承。
- 数值上限放宽只到 int 上限：这些值存在 `int` 字段里（`AbstractCreature.currentBlock`、`AbstractPower.amount`、
  `EnergyPanel.totalCount`），改成 `long` 意味着替换字段、所有读取公式和存档格式。

（其余 14 张带消耗的牌已逐张核对过：文本与标志一致。）

