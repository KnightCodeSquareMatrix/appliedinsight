# gen_enchant_items.py — 随机附魔物品生成工具

> 最后更新：2026-05-28

## 用途

为性能测试生成海量随机附魔物品的 Minecraft 1.21.1 [`give`](https://minecraft.wiki/w/Commands/give) 命令。生成的命令可直接在游戏内执行，用于填充测试用箱子/容器，配合 ME 存储总线接入 AE2 网络后，通过 [`/sorter me planAndMove`](../docs/COMMANDS_REFERENCE.md) 命令测试 Applied Energistics: Insight 的排序性能。

## 用法

```bash
python3 tools/gen_enchant_items.py [选项]
```

### 选项说明

| 选项 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `--count` | `int` | `1` | 每种物品模板生成的份数 |
| `--pos` | `str` | `~ ~ ~1` | 目标坐标（`X Y Z`），生成的命令将物品放置到该坐标处的容器 |
| `--output` | `str` | stdout | 输出文件路径；不指定则直接打印到终端 |
| `--type` | `str` | `all` | 物品类型过滤，可选值见下方 |

`--type` 可选值：

| 值 | 说明 |
|----|------|
| `all` | 全部 33 种物品模板（默认） |
| `sword` | 仅剑类（钻石剑、下界合金剑、铁剑） |
| `pickaxe` | 仅镐类（钻石镐、下界合金镐、铁镐、石镐、金镐） |
| `armor` | 仅盔甲类（头盔、胸甲、护腿、靴子各 3 种材质） |
| `bow` | 仅弓 |
| `crossbow` | 仅弩 |
| `trident` | 仅三叉戟 |
| `tool` | 仅工具类（镐、斧、锹、锄） |
| `book` | 仅附魔书 |

### 示例

```bash
# 生成每种物品各 64 份，输出到终端
python3 tools/gen_enchant_items.py --count 64 --pos 100 64 100

# 仅生成剑类物品各 1 份，输出到文件
python3 tools/gen_enchant_items.py --count 1 --type sword --output /tmp/swords.txt

# 生成全部物品各 16 份，输出到终端
python3 tools/gen_enchant_items.py --count 16
```

## 脚本特性

### 33 种物品模板

涵盖以下类别（详见 [`gen_enchant_items.py`](gen_enchant_items.py) 中的 `ITEM_TEMPLATES` 列表）：

| 类别 | 物品 | 数量 |
|------|------|------|
| 剑 | 钻石剑、下界合金剑、铁剑 | 3 |
| 镐 | 钻石镐、下界合金镐、铁镐、石镐、金镐 | 5 |
| 其他工具 | 钻石斧/锹/锄、下界合金斧/锹/锄、铁斧/锹/锄 | 9 |
| 头盔 | 钻石头盔、下界合金头盔、铁头盔 | 3 |
| 胸甲 | 钻石胸甲、下界合金胸甲、铁胸甲 | 3 |
| 护腿 | 钻石护腿、下界合金护腿、铁护腿 | 3 |
| 靴子 | 钻石靴子、下界合金靴子、铁靴子 | 3 |
| 远程武器 | 弓、弩 | 2 |
| 三叉戟 | 三叉戟 | 1 |
| 附魔书 | 附魔书（全附魔池） | 1 |
| **合计** | | **33** |

### 随机附魔组合

- 每种物品从对应的附魔池中随机选取 1~6 个附魔（取决于物品类型）
- 每个附魔的等级在允许范围内随机（如锋利 I~V、保护 I~IV）
- 附魔书使用全附魔池（所有附魔类型均可出现）

### 互斥检测

自动检测并避免以下互斥附魔组合同时出现：

| 互斥组 | 说明 |
|--------|------|
| `sharpness` / `smite` / `bane_of_arthropods` | 三种伤害类附魔互斥 |
| `silk_touch` / `fortune` | 精准采集与时运互斥 |
| `infinity` / `mending` | 无限与经验修补互斥 |
| `loyalty` / `riptide` | 忠诚与激流互斥 |
| `channeling` / `riptide` | 引雷与激流互斥 |
| `multishot` / `piercing` | 多重射击与穿透互斥 |

### 随机耐久值

- 非附魔书物品会附加随机耐久损耗（`minecraft:damage` 组件）
- 耐久损耗值在 `[0, max_damage]` 范围内均匀随机
- 附魔书不附加耐久组件

### 输出格式

输出内容包含：

1. **头部注释**：生成时间、命令行参数、命令总数
2. **命令列表**：按物品类型分组，每组前有注释分隔行
3. **尾部注释**：使用说明（6 步测试流程）

每条命令格式：

```
give @p <物品ID>[minecraft:enchantments={levels:{<附魔1>:<等级>,<附魔2>:<等级>,...}},minecraft:damage=<耐久损耗>]
```

示例输出：

```text
# ============================================================
# 随机附魔物品生成 — Minecraft 1.21.1
# 生成时间: (动态生成)
# 参数: --count 1 --pos ~ ~ ~1 --type all
# 总计: 33 条命令
# ============================================================

# ---- diamond_sword ----
give @p minecraft:diamond_sword[minecraft:enchantments={levels:{"minecraft:sharpness":4,"minecraft:looting":2,"minecraft:unbreaking":3}},minecraft:damage=1024]

# ---- netherite_sword ----
give @p minecraft:netherite_sword[minecraft:enchantments={levels:{"minecraft:smite":3,"minecraft:fire_aspect":2,"minecraft:unbreaking":2}},minecraft:damage=567]

...

# ============================================================
# 使用方法：
# 1. 在坐标 ~ ~ ~1 处放一个箱子
# 2. 将以上命令逐条输入命令方块（或写入 .mcfunction 数据包）
# 3. 执行后物品会出现在你的背包
# 4. 手动放入箱子
# 5. 用 ME 存储总线连接箱子到 AE2 网络
# 6. 执行 /sorter me planAndMove 测试性能
# ============================================================
```

## 测试流程

1. **准备容器**：在目标坐标处放置一个箱子（或任意容器）
2. **生成命令**：运行脚本生成 `give` 命令
3. **执行命令**：将命令逐条输入命令方块执行，或写入 `.mcfunction` 数据包批量执行
4. **填充容器**：执行后物品出现在背包，手动放入箱子
5. **接入 AE2**：用 ME 存储总线连接箱子到 AE2 网络
6. **测试排序**：执行 [`/sorter me planAndMove`](../docs/COMMANDS_REFERENCE.md) 测试 Applied Energistics: Insight 的排序性能

## 相关文档

- [命令参考](../docs/COMMANDS_REFERENCE.md) — `/sorter` 系列命令说明
- [整体逻辑](../docs/整体逻辑.md) — 模组核心逻辑概述
- [架构参考](../docs/ARCHITECTURE_REFERENCE.md) — 系统架构全景
- [术语表](../docs/GLOSSARY.md) — 项目术语定义
