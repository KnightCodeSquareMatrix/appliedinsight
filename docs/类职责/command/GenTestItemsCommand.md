# GenTestItemsCommand
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/command/GenTestItemsCommand.java`
- **包**: `com.knightcode.appliedstoragesorter.command`
- **类型**: `class`
- **所属层**: 命令入口层

## 职责
实现游戏内命令 `/sorter genTestItems`，从 `tools/gen_enchant_items.py` 移植的附魔物品生成逻辑。
包含两个子命令：
1. 基础附魔物品生成（原版风格）
2. NBT Heavy 测试数据生成（用于性能测试）

### 命令签名
```
/sorter genTestItems [<slot-count>]
/sorter genTestItems nbtHeavy <level>
```

#### 基础命令
- `slot-count` — 可选，箱子槽位数，默认 27（单箱子），双箱子传 54

#### NBT Heavy 子命令
- `level` — 必选，复杂度级别（1=Standard, 2=HeavyEnchant, 3=ApotheosisLike）

### 执行流程

#### 基础命令
1. **RayTrace 检测**：玩家看向的方块，必须是容器（`Container` 接口）
2. **检测箱子容量**：计算已占用槽位和剩余空位
3. **生成随机附魔物品**：根据剩余空位生成对应数量的物品（默认使用全部 27 个物品模板）
4. **塞入箱子**：将物品依次放入空槽位
5. **反馈玩家**：控制台输出生成进度、塞入数量、剩余空位等信息

#### NBT Heavy 子命令
1. **RayTrace 检测**：同上
2. **检测箱子容量**：同上
3. **根据级别配置生成物品**：
   - Level 1 (Standard)：1-3 种原版附魔，等级 1-5，100 种物品类型，每种 1 组，custom_data 约 15 个键值对
   - Level 2 (HeavyEnchant)：8-20 种附魔，等级 1-255，200 种物品类型，每种 1-3 组，custom_data 约 75 个键值对
   - Level 3 (ApotheosisLike)：15-40 种附魔，等级 1-255（10% 概率 256+ 溢出等级），500 种物品类型，每种 1-8 组，custom_data 约 350 个键值对
4. **注入 custom_data**：每个物品通过 `DataComponents.CUSTOM_DATA` 注入嵌套 NBT 结构，包含：
   - 大量字符串键值对（`key_0` ~ `key_N`）
   - 嵌套浮点对象（`nested_data.float_0` ~ `float_N`）
   - 深层嵌套元数据（`metadata.child.timestamp`、`metadata.child.value`）
5. **塞入箱子**：将物品依次放入空槽位
6. **输出统计信息**：总物品数、总种类数、平均附魔数、最大附魔数、平均 NBT 长度、最长 NBT 长度、总 NBT 数据量

### NBT 长度预期

| 级别 | 名称 | 附魔数 | custom_data 条目数 | 预期 NBT 字符串长度 |
|------|------|--------|-------------------|-------------------|
| 1 | Standard | 1-3 | 10-20 个键值对 | ~500-1000 字符 |
| 2 | HeavyEnchant | 8-20 | 50-100 个键值对 | ~2000-5000 字符 |
| 3 | ApotheosisLike | 15-40 | 200-500 个键值对 | ~5000-20000 字符 |

## 边界检查
- 不依赖 AE2 API，仅使用 Minecraft 原生 API（`Container`、`ItemStack`、`Enchantment`、`CompoundTag`、`CustomData` 等）
- 不涉及 `rule/`、`ae2/`、`application/` 等内部层
- 受 `Config.DEVELOPER_MODE` 保护，仅在开发者模式下可用
- 属于开发调试工具，不属于 merge / me plan / storageDump 三条主线

## 抽象检查
没有过度抽象；使用 Java record 定义 `ItemTemplate`、`IntRange`、`ExclusiveGroup`、`NbtHeavyLevelConfig` 内部数据结构。

## 主要协作者
- `com.knightcode.appliedstoragesorter.Config` — 开发者模式检查
- `net.minecraft.world.Container` — 容器接口
- `net.minecraft.world.item.ItemStack` — 物品堆
- `net.minecraft.world.item.enchantment.Enchantment` — 附魔注册
- `net.minecraft.core.registries.Registries.ENCHANTMENT` — 附魔注册表
- `net.minecraft.core.component.DataComponents` — DataComponent 系统（ENCHANTMENTS / STORED_ENCHANTMENTS / DAMAGE / CUSTOM_DATA）
- `net.minecraft.nbt.CompoundTag` — NBT 标签构造
- `net.minecraft.world.item.component.CustomData` — 自定义数据组件

## 维护备注
- 附魔池常量和互斥附魔组从 `tools/gen_enchant_items.py` 移植，保持同步
- 使用 Minecraft 1.21.1 DataComponent 系统设置附魔和耐久
- 附魔书使用 `STORED_ENCHANTMENTS`，其他物品使用 `ENCHANTMENTS`
- 所有操作在服务端执行，不需要客户端代码
- `tools/gen_enchant_items.py` 作为独立开发工具保留不动
- **简化说明**：已移除 `type` 参数和 `TYPE_GROUPS`/`filterTemplates()`/`filterExact()` 方法，命令不再支持按类型过滤物品模板，默认生成全部 27 个物品模板
- **NBT Heavy 说明**：新增 `nbtHeavy` 子命令，使用 `ALL_VANILLA_ENCHANTS`（43 种附魔 ID）和 `NBT_HEAVY_ITEMS`（68 种物品类型）生成测试数据
- **bitset 测试**：Level 2 测试 8-bit 上限（等级 255），Level 3 测试溢出行为（等级 256+）
- **custom_data 注入**：`buildNbtHeavyItemStack()` 中通过 `CompoundTag` 构造嵌套 NBT 结构，通过 `DataComponents.CUSTOM_DATA` + `CustomData.of(tag)` 注入，大幅增加 NBT 字符串长度
- **NBT 长度统计**：`getNbtStringLength()` 方法通过 `ItemStack.getComponentsPatch().toString()` 获取组件数据的字符串表示并计算长度
- **统计输出增强**：命令执行后额外输出平均 NBT 长度、最长 NBT 长度、总 NBT 数据量
