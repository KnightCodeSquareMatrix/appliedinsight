# DAV (DigitalAssetVault) UI 信息补充分析

> **生成时间**: 2026-05-28
> **分析角度**: 基于游戏内 GUI 设计方案 P0-B，深入思考 DAV UI 需要补充哪些信息
> **参考前提**: 游戏内GUI设计方案.md 已规划了 Zone 信息面板 + Cell 容量着色，本文在此基础上做更深入的补充分析

---

## 1. 当前 DAV UI 状态回顾

[`DigitalAssetVaultScreen.java`](src/main/java/com/knightcode/appliedstoragesorter/client/screen/DigitalAssetVaultScreen.java)

```
┌───────────────────────────┐
│ 数字资产库                  │  ← 标题（无 zone 信息）
│                           │
│  [C] [C]  ← 2×5 cell 槽   │
│  [C] [C]                  │
│  [C] [C]                  │  ← 无状态指示
│  [C] [C]                  │  ← 无健康提示
│  [C] [C]                  │
│ ───────────────────────── │
│ 玩家背包 (3×9)            │
│ 快捷栏 (1×9)              │
│ ───────────────────────── │
│ 管理卡槽      [MC]         │  ← 底部面板，无 zone 信息展示
└───────────────────────────┘
```

**关键问题**:
1. 管理卡在底部面板、cell 槽在主体区，两者缺乏视觉关联
2. 管理卡虽然能提供 zoneId/zoneName，但 GUI 完全不展示
3. Cell 容量数据存在（`CellCapacityInspector`），但客户端 GUI 无法消费
4. 玩家只有一个槽位视角，缺乏"这个 DAV 在网络中扮演什么角色"的全景

---

## 2. DAV 角色定位回顾

根据架构事实：

> **FACT-037**: 玩家输入层 — 让玩家声明 zone，让 DAV 成为 zone 容器，让输入具有可视化和可持久化形式。
> **FACT-038**: DAV (DigitalAssetVault) 是管理入口，不是架构中心本身。
> **FACT-039**: DAV 不承担路由决策，不承担搬运执行。
> **FACT-040**: 管理卡 (DigitalAssetManagementCard) 只负责携带 zone 数据，不承担规划和搬运职责。
> **ADR-005 (FACT-084)**: DAV 不是架构中心。架构中心是规则模型、运行时模型、分析模型。

**核心推论**: DAV UI 的信息展示应聚焦在"zone 容器的状态"，而不应滑向"网络治理中心"。

---

## 3. 信息补充分析：按数据就绪程度分层

### 3.1 数据已就绪，无需新增基础设施

| 信息 | 来源 | 当前可用性 | 说明 |
|------|------|-----------|------|
| zoneId | 管理卡 ItemStack NBT | ✅ Menu Slot 同步到 Client | via `DigitalAssetManagementCardItem.getZoneId()` |
| zoneName | 管理卡 ItemStack NBT | ✅ Menu Slot 同步到 Client | via `DigitalAssetManagementCardItem.getZoneName()` |
| 管理卡是否插入 | Menu Slot 是否为空 | ✅ 标准 Slot 容器同步 | 客户端可直接检测 |
| cell 槽物品 ItemStack | Menu Slot 同步 | ✅ AE2 标准机制 | 客户端能看到 ItemStack |
| 各槽位 cell 是否为空 | Menu Slot 中的 ItemStack | ✅ 标准 | `stack.isEmpty()` |

**结论**: zoneId/zoneName 和"是否已插卡"的感知可以零成本实现。

### 3.2 数据可选件（客户端可尝试读取）

| 信息 | 来源 | 复杂程度 | 说明 |
|------|------|---------|------|
| cell 容量信息（粗略） | Cell ItemStack NBT | ⚠️ 不稳定 | AE2 Cell NBT 格式是内部实现，不建议依赖 |
| cell 物品 ID | `ItemStack.getItem().builtInRegistryHolder().key().location()` | ✅ 简单 | 可用于显示 cell 类型图标或文字 |

### 3.3 数据需要新同步机制

| 信息 | 来源 | 所需机制 | 说明 |
|------|------|---------|------|
| cell totalBytes/usedBytes/usageRatio | `CellCapacityInspector.inspect(StorageCell)` | 需要 S→C 数据同步 | ✅ P0-B 任务核心依赖 |
| cell totalItemTypes/remainingItemTypes | 同上 | 同上 | ✅ P0-B 任务核心依赖 |
| cellKind (1k/4k/64k/256k/infinite) | `CellCapacityInspector.resolveCellKind()` | 同上 | 需要反射到 StorageCell |
| 是否 infinite cell | `CellCapacityInspector.isLikelyInfiniteCell()` | 同上 | 可选 |
| DAV 所属网络 ID | `Ae2ControllerTargetResolver` 或 Grid 查询 | 需要新增 | 当前 DAV 不保存网络引用 |

### 3.4 数据属于"网络上下文"，DAV 不直接拥有

| 信息 | 来源 | 获取难度 | 说明 |
|------|------|---------|------|
| 绑定到该网络的 profileId | `NetworkProfileBindingStore` | 需要跨层查询 | DAV 需要知道自己所属的网络 ID |
| 该 zone 在 profile 中的路由规则 | `RoutingProfileRepository` | 需要跨层查询 | 哪些物品应该流向此 zone |
| 该 zone 在网络中的全局健康 | `StorageAnalyzerReport` | 分析层产物 | 不属于在线命令链（FACT-049） |
| 其他 DAV 的状态 | 多个 block entity | 不现实 | 不在 DAV 范围内 |

**架构约束**:
- `blockentity/` 层不包含命令执行逻辑（FACT-051）
- DAV UI 不应滑向"网络治理中心"（FACT-038）
- 分析层不污染在线命令链（FACT-049）

---

## 4. 六项具体的 UI 信息补充建议

### 建议 1：标题区增强 — Zone 身份感知

**现状**: 标题只显示"数字资产库"

**建议**:

```
┌───────────────────────────────┐
│ 数字资产库                      │  ← 无管理卡时
│                               │

┌───────────────────────────────┐
│ 数字资产库 — 主存储            │  ← 有管理卡时（zoneName）
│ Zone: main_storage            │  ← 小字显示 zoneId
```

**实现**: 零后端改动，仅改 Screen 从 Menu Slot 读取管理卡数据

```
DigitalAssetVaultMenu menu → getSlot(CARD_SLOT_INDEX).getItem()
  → DigitalAssetManagementCardItem.getZoneId(stack)
  → DigitalAssetManagementCardItem.getZoneName(stack)
```

### 建议 2：管理卡状态指示 — 卡槽视觉反馈

**现状**: 管理卡槽在底部面板，插卡后几乎无交互反馈

**建议**:
- **未插卡**：卡槽边框闪烁/虚化，提示文字 "插入管理卡以声明区域"
- **已插卡但无 zone 数据**：卡槽显示黄色边框，提示 "管理卡未分配区域"
- **已插卡且有 zone**：卡槽绿色边框，显示 zone 名称

**实现**: Screen 的 `render()` 中根据 slot item 状态渲染不同纹理叠加层

### 建议 3：Zone 信息面板 — 新增

**现状**: 无 zone 信息展示

**建议**（与设计文档 §4.2.2 对齐）：

```
┌── Zone 信息 ─────────────────┐│
│ 区域: 主存储 (main_storage)   ││
│ 容量: 342MB / 1TB (33%)     ││  ← 需要新同步机制
│ 健康: ✅ 良好               ││  ← 聚合计算
│ 类型占用: 127/200 (63%)     ││  ← 需要新同步机制
└──────────────────────────────┘│
```

**数据依赖**:
- zoneId/zoneName → 零成本（Slot 同步）
- 聚合容量 → 需要 `CellCapacityInspector` 数据 S→C 同步
- 健康状态 → 聚合计算

### 建议 4：Cell 容量着色 — 增强

**现状**: Cell 槽位无任何状态指示

**建议**（与 P 级分析建议一致，从 P1 移到 P0）：

- **绿色**：`byteUsageRatio < 80%` → 健康
- **黄色**：`80% <= byteUsageRatio < 95%` → 容量接近满
- **红色**：`byteUsageRatio >= 95%` 或 `remainingItemTypes < 5` → 告警
- **紫色/特殊**：infinite cell → 特殊标识
- **灰色**：empty slot → 空

**数据依赖**: 需要 `CellCapacityInspector` 数据 S→C 同步

### 建议 5：DAV 级聚合状态 — 概览条

**现状**: 每个 cell 状态独立，无 DAV 级聚合

**建议**（在标题下方显示一行紧凑的聚合指标）：

```
┌──────────────────────────────────────────┐
│ 数字资产库 — 主存储     🟢 8/10 | 67% | 127 种 │
└──────────────────────────────────────────┘
```

- 🟢 8/10 = 8 个槽位已占用 / 共 10 个槽
- 67% = 总容量占用率
- 127 种 = 总物品种类数

**数据依赖**:
- 占用槽位数 → 零成本（检查 ItemStack.isEmpty）
- 总容量占用率 → 需要 `CellCapacityInspector` 数据
- 总物品种类数 → 需要 `CellCapacityInspector` 数据（或粗略从 NBT）

### 建议 6：空状态与引导

**现状**: 空 DAV 与满 DAV 显示一样

**建议**:

| 状态 | 视觉 | 文字 |
|------|------|------|
| 无管理卡 | 标题区显示提示 | "插入管理卡以声明此数字资产库所属区域" |
| 有卡无 zone | 卡槽黄色边框 | "管理卡未分配区域，请放入区域刻印机中使用" |
| 有卡有 zone + 空 cell | 标题区显示 zone | zone 信息 + "所有 cell 槽为空" |
| 所有状态正常 | 正常显示 | — |

**实现**: Screen 根据状态渲染不同图层/提示

---

## 5. 技术实现方案评估

### 方案 A：ContainerData 同步（推荐 P0）

AE2 的 `AEBaseMenu` 支持 `addDataSlot(IntDataSlot)` 机制，每个 data slot 可同步一个 int。

```
DAV 有 10 个 cell 槽，每个槽需要同步：
  - byteUsageRatio (int: 0~100, 百分比)
  - remainingItemTypes (int)
  - cellKindFlag (int: 编码类型)
  - isInfinite (int: 0/1)
  
共需 10 × 4 = 40 个 data slots
```

**优点**:
- 无新增网络包
- AE2 原生机制
- 轻量

**缺点**:
- 数据量受限（每个 slot 只能传 int）
- 需要 Menu 端编码/解码
- 不支持批量更新

**实现位置**: [`DigitalAssetVaultMenu.java`](src/main/java/com/knightcode/appliedstoragesorter/menu/DigitalAssetVaultMenu.java) 的构造函数中注册 data slots

### 方案 B: 自定义网络 Payload（推荐 P1 以后）

```
DAVCellStatusPayload(S→C):
  - blockPos
  - cellStatuses: List<CellStatus>
  - CellStatus: { totalBytes, usedBytes, totalItemTypes, remainingItemTypes, cellKind, isInfinite }
```

**优点**:
- 结构化数据，可读性强
- 支持批量

**缺点**:
- 需注册新 CustomPacketPayload + Handler
- 开发量较大

### 推荐策略: P0 用方案 A, P1 按需升级到方案 B

---

## 6. 六项建议的 P 级重评估

| 建议 | 数据就绪度 | 实现成本 | 推荐 P 级 | 说明 |
|------|-----------|---------|----------|------|
| 建议1: 标题区增强 | ✅ 零成本 | 小（仅 Screen 修改） | **P0** | 视觉提升大，零后端改动 |
| 建议2: 卡槽状态指示 | ✅ 零成本 | 小（Screen 渲染叠加） | **P0** | 填补缺失的交互反馈 |
| 建议3: Zone 信息面板 | ❌ 需要同步 | 中 | **P0** | 依赖方案 A，但这是核心价值 |
| 建议4: Cell 容量着色 | ❌ 需要同步 | 中 | **P0** | 与建议3共享数据同步 |
| 建议5: DAV 级聚合状态 | ⚠️ 部分就绪 | 小（部分需同步） | **P0** | 占用率需同步，槽位数零成本 |
| 建议6: 空状态引导 | ✅ 零成本 | 小（Screen 状态切换） | **P0** | UX 基础，应随 P0 一起做 |

**结论**: 建议1、2、6 可与 P0-B 零额外成本完成；建议3、4、5 依赖数据同步方案 A。

---

## 7. 需要新增的 i18n 条目

[`zh_cn.json`](src/main/resources/assets/appliedstoragesorter/lang/zh_cn.json)

```json
{
  "gui.appliedstoragesorter.dav.no_card": "插入管理卡以声明此数字资产库所属区域",
  "gui.appliedstoragesorter.dav.card_no_zone": "管理卡未分配区域",
  "gui.appliedstoragesorter.dav.zone_title": "%s 数字资产库",
  "gui.appliedstoragesorter.dav.zone_capacity": "容量: %s / %s (%s)",
  "gui.appliedstoragesorter.dav.zone_health": "健康: %s",
  "gui.appliedstoragesorter.dav.zone_types": "类型占用: %d/%d (%s)",
  "gui.appliedstoragesorter.dav.cell_healthy": "健康",
  "gui.appliedstoragesorter.dav.cell_nearly_full": "容量告警",
  "gui.appliedstoragesorter.dav.cell_type_exhausted": "类型耗尽",
  "gui.appliedstoragesorter.dav.cell_infinite": "无限容器",
  "gui.appliedstoragesorter.dav.slots_used": "%d/%d 槽已用",
  "gui.appliedstoragesorter.dav.total_usage": "总占用 %s",
  "gui.appliedstoragesorter.dav.total_types": "%d 种物品",
  "gui.appliedstoragesorter.dav.status_good": "良好",
  "gui.appliedstoragesorter.dav.status_warning": "需关注",
  "gui.appliedstoragesorter.dav.status_critical": "告警"
}
```

---

## 8. 纹理修改需求

当前纹理是三段 blit（0~84, 84~201, 201~223）。

如需添加 Zone 信息面板，有两种方案：

### 方案 A: 在现有纹理主体区(0~84)上方新增信息行

- 保持三段纹理不变
- 在标题行下方用文字渲染 zone 信息（不需要纹理修改）
- Cell 槽区域从 y=8 下移到 y=20+
- 优点：无纹理修改

### 方案 B: 重新设计纹理

- 增加一个 Zone 信息面板区（位于标题和 cell 槽之间）
- 需要修改 PNG
- 优点：视觉更精致

### 推荐方案 A（P0）
原因：减少资源依赖，信息展示优先于视觉完美

---

## 9. 增量修改清单（建议 P0-B 扩展后）

### 文件修改清单

| 文件 | 修改类型 | 说明 |
|------|---------|------|
| `DigitalAssetVaultScreen.java` | ✅ 大量修改 | 标题区增强、卡槽状态渲染、聚合状态行、Zone 信息面板渲染、空状态引导 |
| `DigitalAssetVaultMenu.java` | ✅ 新增 | 添加 CellCapacity DataSlots (10 × 3 ints) |
| `DigitalAssetVaultBlockEntity.java` | ✅ 新增 | 新增 `getCellCapacityData()` 方法，供 Menu 读取 |
| `zh_cn.json` / `en_us.json` | ✅ 新增 | 添加约 14 条 i18n 键 |
| `digital_asset_vault.png` | ⚠️ 可选 | 如需视觉面板则需修改纹理 |
| 新增 `SorterTextures.java` | ⚠️ 可选 | 纹理引用统一管理（按 P 级分析建议） |

### 不修改

| 文件 | 理由 |
|------|------|
| `CellCapacityInspector.java` | 数据读取逻辑不变，只是消费方式新增 |
| `DigitalAssetManagementCardItem.java` | 不修改核心物品行为 |
| `DigitalAssetVaultBlock.java` | 没有新增方块交互 |
| 任何 network/ 包 | 如果用 ContainerData 方案，无需新增网络包 |

---

## 10. 相关文档

| 文档 | 说明 |
|------|------|
| [`docs/参考/游戏内GUI设计方案.md`](../docs/参考/游戏内GUI设计方案.md) | 原始 GUI 设计（§4.2 DAV Zone 信息增强） |
| [`docs/参考/游戏内GUI设计方案.P级分析.md`](../docs/参考/游戏内GUI设计方案.P级分析.md) | P 级分析（建议 Cell 容量着色移至 P0） |
| [`docs/架构/FACTS.md`](../docs/架构/FACTS.md) | 架构事实（FACT-037~040 DAV 定位） |
| [`docs/类职责/blockentity/DigitalAssetVaultBlockEntity.md`](../docs/类职责/blockentity/DigitalAssetVaultBlockEntity.md) | DAV BlockEntity 职责 |
| [`docs/类职责/client/screen/DigitalAssetVaultScreen.md`](../docs/类职责/client/screen/DigitalAssetVaultScreen.md) | DAV Screen 当前职责 |
| [`docs/类职责/ae2/CellCapacityInspector.md`](../docs/类职责/ae2/CellCapacityInspector.md) | Cell 容量检测工具 |
| [`plans/管理卡合成路线设计.md`](../plans/管理卡合成路线设计.md) | 管理卡合成与 zone 刻印流程 |
