# 命令参考手册 (Commands Reference)

> 本文档列出 **Applied Energistics: Insight** 的所有游戏内命令、参数、输出示例和常见问题。
> 面向玩家和模组管理员。
>
> **💡 普通玩家推荐使用 [`SorterCommandBlock`](类职责/block/SorterCommandBlock.md)（命令方块）**：放置于世界中，右键打开 GUI 点击按钮即可操作，无需记忆命令。
> 以下命令为**开发者模式入口**，主要用于调试和开发场景。

---

## 📋 目录

- [命令总览](#1-命令总览)
- [自动整理命令](#2-自动整理命令-sorter-merge)
- [网络诊断命令](#3-网络诊断命令-sorter-me-dump)
- [存储分析命令](#4-存储分析命令-sorter-me-storagedump)
- [路由配置管理命令](#5-路由配置管理命令)
- [规则路由命令](#6-规则路由命令)
- [配置项](#7-配置项)
- [输出文件路径](#8-输出文件路径)
- [常见问题](#9-常见问题)

---

## 1. 命令总览

> **💡 普通玩家推荐使用 [`SorterCommandBlock`](类职责/block/SorterCommandBlock.md) GUI 操作**，以下命令为开发者模式入口。

```
/sorter
├── merge                              # 自动整理（同类物品归并压实）— 普通玩家可用
├── genTestItems [<slot-count>]            # 生成随机附魔物品填入箱子（需 developerMode）🔧
│   └── nbtHeavy <level>               # 生成 NBT heavy 测试数据（需 developerMode）🔧
├── me
│   ├── dump                           # 导出网络快照（需 developerMode）🔧
│   ├── storageDump                    # 导出存储分析报告（需 developerMode）🔧
│   ├── listProfiles                   # 列出所有全局路由配置 🔧
│   ├── bindProfile <number>           # 绑定路由配置到当前网络 🔧
│   ├── showProfile                    # 查看当前绑定的路由配置 🔧
│   ├── plan                           # 预览规则路由规划 🔧
│   └── planAndMove                    # 执行规则路由规划 🔧
```

> 🔧 = 需要开发者模式（`DEVELOPER_MODE=true`）或开发者知识

---

## 2. 自动整理命令: `/sorter merge`

### 2.1 功能

扫描当前 AE2 网络中的所有内部 drive，规划并执行同类物品的归并压实操作。

### 2.2 流程

```
扫描网络 → 规划合并 → 执行搬运 → 输出报告
```

### 2.3 输出示例

```
[Applied Energistics: Insight] plannedMergeCount=12
[Applied Energistics: Insight] mergedAmount=448/512
[Applied Energistics: Insight] mergeReport=logs/appliedinsight/merge-20260525-103022.log
```

### 2.4 输出文件

| 文件 | 内容 |
|------|------|
| `logs/appliedinsight.log` | 命令摘要（plannedMergeCount、mergedAmount、report 路径） |
| `logs/appliedinsight/merge-*.log` | 详细 merge report（前后对比、收益排行、单物品变更明细） |

### 2.5 前置条件

- `ENABLE_SORTER` 配置为 `true`（默认）
- 必须由玩家执行（非控制台）
- 玩家必须在 AE2 网络范围内（ME Controller 附近）

> **💡 普通玩家推荐使用 [`SorterCommandBlock`](类职责/block/SorterCommandBlock.md)**：放置命令方块于世界中，右键打开 GUI 点击"碎片物品合并"按钮即可，无需记忆命令。

### 2.6 注意事项

- merge **源**只能是内部 cell（`ae2:drive`、`extendedae:ex_drive`、`appliedinsight:digital_asset_vault`）
- merge **目标**允许：内部 cell（cell→cell 归并），或已持有同种物品的 Storage Bus（cell→external 压入外置仓）
- **禁止** external→cell、external→external（不从抽屉/箱子往外抽货做整理）
- merge 不改变物品的"归属 zone"，只做物理位置优化

---

## 3. 网络诊断命令: `/sorter me dump`

### 3.1 功能

导出当前 AE2 网络的完整快照为 JSON 文件，包含所有 mounted cell、item key、容量信息。

### 3.2 输出示例

```
[Applied Energistics: Insight] Dumped 1234 unique item keys from 24 mounted cells to:
[Applied Energistics: Insight] dumpFile=dumps/appliedinsight/me-dump-20260525-103022.json
```

### 3.3 输出文件

`dumps/appliedinsight/me-dump-<timestamp>.json`

包含：
- `command` — 触发命令
- `target` — 目标信息（controller 坐标、维度、grid 节点数）
- `summary` — 摘要（drive 数、cell 数、item key 数、总字节等）
- `cells[]` — 每个 mounted cell 的详细信息
- `items[]` — 全局 item key 聚合

### 3.4 前置条件

- `DEVELOPER_MODE` 配置为 `true`
- 必须由玩家执行

### 3.5 用途

- 离线分析（通过 Gradle 任务）
- 前端 Dashboard 数据源
- 网络健康诊断

---

## 4. 存储分析命令: `/sorter me storageDump`

### 4.1 功能

导出存储分析报告，包含存储位置分布、碎片化指标、健康标志、疑似语义节点候选项。

### 4.2 输出示例

```
[Applied Energistics: Insight] dumpFile=dumps/appliedinsight/storage-analysis-20260525-103022.json
[Applied Energistics: Insight] storageLocations=36 (nonEmpty=30, internal=28, external=8)
[Applied Energistics: Insight] uniqueKeys=1234, duplicatedKeys=156, totalAmount=1048576
[Applied Energistics: Insight] fragmentation=MODERATE, externalAmountRatio=12.50%
[Applied Energistics: Insight] health=HIGH_EXTERNAL_RATIO, SUSPECTED_INFINITE_CELL
```

### 4.3 输出文件

`dumps/appliedinsight/storage-analysis-<timestamp>.json`

包含：
- `report.summary` — 摘要
- `report.storageLocations[]` — 每个存储位置的详情
- `report.itemDistributions[]` — 物品分布
- `report.healthFlags[]` — 健康标志
- `report.suspectedSemanticCandidates[]` — 疑似语义节点

### 4.4 前置条件

- `DEVELOPER_MODE` 配置为 `true`
- 必须由玩家执行

### 4.5 健康标志说明

| 标志 | 说明 |
|------|------|
| `HIGH_EXTERNAL_RATIO` | 外部存储占比过高 |
| `SUSPECTED_INFINITE_CELL` | 检测到疑似无限容量 cell |
| `HIGH_FRAGMENTATION` | 碎片化程度高 |
| `MANY_DUPLICATED_KEYS` | 大量物品 key 跨多个 cell 重复 |

---

## 5. 路由配置管理命令

### 5.1 `/sorter me listProfiles`

列出 `config/appliedinsight/profiles/` 下所有可用的全局路由配置。

```
[Applied Energistics: Insight] Available global profiles:
[Applied Energistics: Insight] 1. My Storage Profile [id=profile_abc123, version=1]
[Applied Energistics: Insight] 2. Default Profile [id=profile_def456, version=2]
```

### 5.2 `/sorter me bindProfile <number>`

将当前 ME 网络绑定到指定全局 profile。

参数：
- `number` — `listProfiles` 输出中的序号（从 1 开始）

```
[Applied Energistics: Insight] Bound current ME network to global profile: My Storage Profile
[Applied Energistics: Insight] profileId=profile_abc123
[Applied Energistics: Insight] controller=12, 64, -128
[Applied Energistics: Insight] dimension=minecraft:overworld
```

绑定信息通过 `NetworkProfileBindingStore` 持久化到 world save。

### 5.3 `/sorter me showProfile`

查看当前 ME 网络绑定的 profile 信息。

```
[Applied Energistics: Insight] Current ME network profile:
[Applied Energistics: Insight] name=My Storage Profile
[Applied Energistics: Insight] id=profile_abc123
[Applied Energistics: Insight] version=1
```

如果未绑定，返回：
```
[Applied Energistics: Insight] Current ME network is not bound to any global profile.
```

---

## 6. 规则路由命令

### 6.1 `/sorter me plan`

预览规则路由规划：读取绑定的 profile → 构建 RuntimeTopology → 生成 ZoneAllocationPlan。

```
[Applied Energistics: Insight] profile=My Storage Profile [id=profile_abc123]
[Applied Energistics: Insight] assignmentCount=630
[Applied Energistics: Insight] movableAssignmentCount=580
[Applied Energistics: Insight] zone=bulk | movableItems=15 | totalAmount=2147483647
[Applied Energistics: Insight] zone=misc | movableItems=565 | totalAmount=876543
[Applied Energistics: Insight] detailedLog=logs/appliedinsight/plan-20260525-103022.log
```

### 6.2 `/sorter me planAndMove`

执行规则路由规划：plan + 实际搬运。

```
[Applied Energistics: Insight] profile=My Storage Profile [id=profile_abc123]
[Applied Energistics: Insight] assignmentCount=630
[Applied Energistics: Insight] movableAssignmentCount=580
[Applied Energistics: Insight] zone=bulk | movableItems=15 | totalAmount=2147483647
[Applied Energistics: Insight] zone=misc | movableItems=565 | totalAmount=876543
[Applied Energistics: Insight] move.attempted=120
[Applied Energistics: Insight] move.completed=115
[Applied Energistics: Insight] move.failed=5
[Applied Energistics: Insight] move.requestedAmount=10240
[Applied Energistics: Insight] move.movedAmount=9984
[Applied Energistics: Insight] detailedLog=logs/appliedinsight/plan-and-move-20260525-103022.log
```

### 6.3 前置条件

- 当前 ME 网络必须已绑定 profile（通过 `bindProfile`）
- `ENABLE_SORTER` 配置为 `true`
- 必须由玩家执行

### 6.4 输出文件

| 命令 | 日志文件 |
|------|---------|
| `plan` | `logs/appliedinsight/plan-*.log` |
| `planAndMove` | `logs/appliedinsight/plan-and-move-*.log` |

---

## 7. 配置项

配置文件：`config/appliedinsight/server.toml`

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `ENABLE_SORTER` | boolean | `true` | 是否启用模组功能 |
| `DEVELOPER_MODE` | boolean | `false` | 是否启用开发者模式（控制 dump/storageDump 命令） |
| `MAX_TRANSFERS_PER_OPERATION` | int | `512` | 单次操作最大搬运次数 |

---

## 8. 输出文件路径

### 8.1 日志文件

| 路径 | 说明 |
|------|------|
| `logs/appliedinsight.log` | 命令级摘要日志 |
| `logs/appliedinsight/plan-*.log` | Plan 详细日志 |
| `logs/appliedinsight/plan-and-move-*.log` | PlanAndMove 详细日志 |
| `logs/appliedinsight/merge-*.log` | Merge 详细报告 |

### 8.2 JSON 产物

| 路径 | 说明 |
|------|------|
| `dumps/appliedinsight/me-dump-*.json` | 网络快照 |
| `dumps/appliedinsight/storage-analysis-*.json` | 存储分析报告 |

### 8.3 配置文件

| 路径 | 说明 |
|------|------|
| `config/appliedinsight/server.toml` | 服务端配置 |
| `config/appliedinsight/profiles/*.json` | 全局路由配置 |

---

## 9. 常见问题

### Q: 命令执行后没有反应？

检查：
1. 是否在 AE2 网络范围内（需要 ME Controller）
2. `ENABLE_SORTER` 配置是否为 `true`
3. 是否由玩家执行（非控制台命令方块）

### Q: `/sorter` 命令和 `SorterCommandBlock` 有什么区别？我该用哪个？

| 对比维度 | `/sorter` 命令 | `SorterCommandBlock` |
|---------|---------------|---------------------|
| **目标用户** | 开发者/模组管理员 | **普通玩家（推荐）** |
| **使用方式** | 聊天框输入 Brigadier 命令 | 放置方块，右键打开 GUI 点击按钮 |
| **是否需要记忆命令** | ✅ 需要 | ❌ 不需要 |
| **是否需要开发者模式** | 部分命令需要 | 不需要 |
| **功能覆盖** | 全部功能 | 3 个核心功能按钮（分析、按配置整理、碎片物品合并） |
| **适用场景** | 调试、自动化脚本、批量操作 | 日常使用、快速操作 |

**推荐**：普通玩家直接使用 [`SorterCommandBlock`](类职责/block/SorterCommandBlock.md)，开发者/管理员使用 `/sorter` 命令。

### Q: `dump` 和 `storageDump` 提示"仅在开发者模式下可用"？

在 `config/appliedinsight/server.toml` 中设置：
```toml
[general]
DEVELOPER_MODE = true
```

### Q: `plan` 提示"未绑定 profile"？

先执行：
```
/sorter me listProfiles
/sorter me bindProfile 1
```

### Q: 如何查看详细的搬运报告？

执行命令后，控制台会输出 `detailedLog=logs/appliedinsight/plan-and-move-*.log`，打开该文件查看详细内容。

### Q: merge 和 planAndMove 有什么区别？

- `merge` — 自动整理，同类物品归并压实，不改变归属 zone
- `planAndMove` — 规则路由，根据 profile 将物品搬运到指定 zone

---

---

## 7. 测试物品生成命令: `/sorter genTestItems`

### 7.1 功能

从 `tools/gen_enchant_items.py` 移植的附魔物品生成逻辑。在玩家看向的容器（箱子、桶等）中生成随机附魔物品，用于测试 sorter 性能。

### 7.2 命令签名

```
/sorter genTestItems [<slot-count>]
/sorter genTestItems nbtHeavy <level>
```

### 7.3 参数

#### 基础命令

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `slot-count` | int (1-54) | `27` | 箱子槽位数，单箱子 27，双箱子 54 |

#### NBT Heavy 子命令

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `level` | int (1-3) | 是 | 复杂度级别，1=Standard, 2=HeavyEnchant, 3=ApotheosisLike |

### 7.4 执行流程

#### 基础命令
1. RayTrace 检测玩家看向的方块（距离 5 格）
2. 检测是否为容器（`Container` 接口）
3. 计算已占用槽位和剩余空位
4. 根据剩余空位生成随机附魔物品
5. 将物品依次放入空槽位
6. 控制台输出生成进度

#### NBT Heavy 子命令
1. RayTrace 检测玩家看向的方块（距离 5 格）
2. 检测是否为容器（`Container` 接口）
3. 计算已占用槽位和剩余空位
4. 根据级别配置生成 NBT heavy 物品
5. 将物品依次放入空槽位
6. 控制台输出统计信息（总物品数、总种类数、平均附魔数、最大附魔数）

### 7.5 输出示例

#### 基础命令
```
§6=== 附魔物品生成器 ===
§e目标箱子: [100, 64, 100]
§e箱子槽位: 27/27 (已占用 5，剩余 22)
§e生成进度: [1/22] diamond_sword...
§e生成进度: [2/22] netherite_sword...
§a✅ 生成完成！共 22 件物品已塞入箱子
§c⚠️ 容量不足，剩余 8 种物品未生成
```

#### NBT Heavy 命令
```
§6=== NBT Heavy 测试数据生成器 ===
§e复杂度级别: ApotheosisLike (Level 3)
§e目标箱子: [100, 64, 100]
§e箱子槽位: 54/54 (已占用 0，剩余 54)
§a✅ NBT Heavy 测试数据生成完成！
§e━━━ 统计信息 ━━━
§e总物品数: 54
§e总种类数: 54
§e平均附魔数: 27.3
§e最大附魔数: 39
§e已用槽位: 54/54
§c⚠️ 容器空间不足，部分物品未生成
```

### 7.6 前置条件

- `DEVELOPER_MODE` 配置为 `true`
- 必须由玩家执行
- 玩家必须看向一个容器方块（距离 ≤ 5 格）

### 7.7 附魔池

#### 基础命令附魔池
支持 6 类附魔池（移植自 Python 脚本）：
- **武器附魔**：锋利、亡灵杀手、节肢杀手、耐久、抢夺、火焰附加、击退、横扫之刃、经验修补
- **工具附魔**：效率、耐久、时运、精准采集、经验修补
- **盔甲附魔**：保护、耐久、经验修补、荆棘、水下呼吸、水下速掘、摔落保护、深海探索者、冰霜行者、灵魂疾行、迅捷潜行
- **弓附魔**：力量、耐久、火焰、无限、冲击、经验修补
- **弩附魔**：快速装填、耐久、多重射击、穿透、经验修补
- **三叉戟附魔**：穿刺、耐久、经验修补、忠诚、激流、引雷

#### NBT Heavy 附魔池
使用 43 种附魔 ID（包含所有原版附魔 + 1.21 新增附魔），覆盖：
- 所有原版保护类、武器类、工具类、弓箭类、钓鱼类、三叉戟类附魔
- 1.21 新增附魔：风爆（wind_burst）、致密（density）、破甲（breach）
- 包含重复条目用于填充 40+ 附魔（Apotheosis 模拟场景）

### 7.8 互斥规则

#### 基础命令互斥规则
同一组内的附魔不会同时出现在一个物品上：
- 锋利 / 亡灵杀手 / 节肢杀手
- 精准采集 / 时运
- 无限 / 经验修补
- 忠诚 / 激流
- 引雷 / 激流
- 多重射击 / 穿透

#### NBT Heavy 互斥规则
NBT Heavy 模式不应用互斥规则，允许所有附魔同时出现，以模拟极端 NBT heavy 场景。

### 7.9 物品模板

#### 基础命令物品模板
共 27 个物品模板，涵盖剑、镐、斧、锹、锄、头盔、胸甲、护腿、靴子、弓、弩、三叉戟、附魔书。

#### NBT Heavy 物品模板
共 68 种物品类型，涵盖：
- 所有品质的剑、镐、斧、锹、锄（木/石/铁/金/钻石/下界合金）
- 所有品质的头盔、胸甲、护腿、靴子（皮革/铁/金/钻石/下界合金 + 锁链/海龟壳）
- 弓、弩、三叉戟、盾牌、鞘翅、钓鱼竿、剪刀、打火石、狼铠、重锤

### 7.10 三级复杂度说明

| 级别 | 名称 | 附魔数/物品 | 附魔等级 | 物品类型数 | 每组数 | 特殊行为 |
|------|------|------------|---------|-----------|-------|---------|
| 1 | Standard | 1-3 | 1-5 | 100 | 1 | 模拟普通玩家仓库 |
| 2 | HeavyEnchant | 8-20 | 1-255 | 200 | 1-3 | 测试 bitset 8-bit 上限 |
| 3 | ApotheosisLike | 15-40 | 1-255 (10% 256+) | 500 | 1-8 | 测试 bitset 溢出行为 |

---

> **相关文档**：[`API_REFERENCE.md`](API_REFERENCE.md) | [`日志与JSON字段契约.md`](日志与JSON字段契约.md) | [`前端对接说明.md`](前端对接说明.md)
