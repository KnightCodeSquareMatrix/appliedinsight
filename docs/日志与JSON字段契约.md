# 运行产物字段契约说明

本文档描述 **Applied Energistics: Insight 当前运行时会写出的主要日志与 JSON 文件** 的字段契约。

## 1. 适用范围

当前覆盖以下产物：

### 日志文件
- `logs/appliedinsight.log`
- `logs/appliedinsight/plan-*.log`
- `logs/appliedinsight/plan-and-move-*.log`
- `logs/appliedinsight/merge-*.log`

### JSON 文件
- `dumps/appliedinsight/me-dump-*.json`
- `dumps/appliedinsight/storage-analysis-*.json`

## 2. 总体约定

### 2.1 文本日志格式约定
所有文本日志都遵守以下基础规则：

- UTF-8 文本
- 以 section 为基本组织单位
- section 形式为：`[section_name]`
- section 内部主要采用：`key=value`
- 部分列表行采用：
  - `name[index]=...`
  - `1. ...`
  - `- ...`
- `<none>` 表示当前字段无值或不适用

### 2.2 坐标格式约定
- 方块坐标：`x, y, z`
- 带槽位的 cell 引用：`x, y, z#slot=n`
- external/storage bus 附着引用：`x, y, z#slot=n->attached=a, b, c@side`

### 2.3 数值语义约定
- `count`：数量、个数、条目数
- `amount`：物品总量或本次搬运量
- `bytes`：AE2 cell 已用字节
- `group`：当前语义下指 AE2 cell 的 `remaining item types` / 可继续容纳的新类型数
- `delta`：`after - before`
- `released_*`：释放收益，通常是 `before - after` 或 `after - before`，以字段名说明为准

---

## 3. `logs/appliedinsight.log`

### 3.1 定位
总日志文件，记录命令级摘要，不承载完整复盘明细。

### 3.2 公共头字段
每段日志都会先输出：

| 字段 | 类型 | 说明 |
|---|---|---|
| `[...] <command>` | string | 时间戳 + 命令标题 |
| `executor` | string | 执行者名称 |
| `dimension` | string | 执行命令所在维度 |
| `position` | string | 执行者位置，三位小数 |
| `server_time` | integer | 服务器 tick |
| `entity` | string | 执行者实体类型，无则 `<none>` |

### 3.3 `/sorter merge` 失败段
标题：`/sorter merge`

| 字段 | 类型 | 说明 |
|---|---|---|
| `scan_status` | string | 来自 `Ae2GridTargetResult.status()` |
| `target_block` | string | 命令目标方块 id |
| `target_pos` | string | 命令目标坐标 |
| `controller_detected` | boolean | 是否检测到 controller |
| `controller_pos` | string | controller 坐标 |
| `grid_resolved` | boolean | 是否成功解析到 grid |
| `network_dimension` | string | 网络维度 id |

### 3.4 `/sorter merge preview` 段
标题：`/sorter merge preview`

| 字段 | 类型 | 说明 |
|---|---|---|
| `scan_status` | string | 当前固定为 `ok` |
| `target_block` | string | 命令目标方块 id |
| `target_pos` | string | 命令目标坐标 |
| `controller_detected` | boolean | 是否检测到 controller |
| `controller_pos` | string | controller 坐标 |
| `grid_resolved` | boolean | 是否解析到 grid |
| `network_dimension` | string | 网络维度 id |
| `network_node_count` | integer | grid 节点数 |
| `network_drive_count` | integer | 支持的 drive 数 |
| `scanned_cell_slot_count` | integer | 扫描过的总槽位数 |
| `mounted_cell_count` | integer | 挂载了 cell / storage 的槽位数 |
| `unique_item_key_count` | integer | 全网唯一 item key 数 |
| `duplicated_item_key_count` | integer | 分布在多个位置的 item key 数 |
| `duplicated_cell_reference_count` | integer | 重复分布 occurrence 数 |
| `network_pivot` | string | grid pivot 拥有者描述 |
| `planned_merge_count` | integer | planner 产出的计划搬运数 |
| `planned_total_amount` | integer | 计划搬运总量 |

列表字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `planned_merge[n]` | line | 单条计划：`key / amount / source / destination` |

### 3.5 `/sorter merge execute` 段
标题：`/sorter merge execute`

| 字段 | 类型 | 说明 |
|---|---|---|
| `scan_status` | string | 当前固定为 `ok` |
| `target_block` | string | 命令目标方块 id |
| `target_pos` | string | 命令目标坐标 |
| `controller_pos` | string | controller 坐标 |
| `attempted_merge_count` | integer | 实际尝试执行的 move 数 |
| `completed_merge_count` | integer | 完整成功的 move 数 |
| `failed_merge_count` | integer | 部分或完全失败的 move 数 |
| `requested_merge_amount` | integer | 请求搬运总量 |
| `merged_amount` | integer | 实际插入成功总量 |
| `merge_report_file` | string | 详细复盘日志相对路径 |

### 3.6 `/sorter me dump` 成功段
标题：`/sorter me dump`

| 字段 | 类型 | 说明 |
|---|---|---|
| `scan_status` | string | 固定为 `ok` |
| `target_block` | string | 命令目标方块 id |
| `target_pos` | string | 命令目标坐标 |
| `controller_pos` | string | controller 坐标 |
| `network_node_count` | integer | grid 节点数 |
| `network_drive_count` | integer | 支持 drive 数 |
| `mounted_cell_count` | integer | 挂载 cell 数 |
| `unique_item_key_count` | integer | 唯一 item key 数 |
| `item_occurrence_count` | integer | occurrence 总数 |
| `dump_file` | string | dump 相对路径 |

### 3.7 `/sorter me dump` 失败段
标题：`/sorter me dump`

| 字段 | 类型 | 说明 |
|---|---|---|
| `scan_status` | string | 固定为 `error` |
| `target_block` | string | 命令目标方块 id |
| `target_pos` | string | 命令目标坐标 |
| `controller_pos` | string | controller 坐标 |
| `network_drive_count` | integer | 支持 drive 数 |
| `mounted_cell_count` | integer | 挂载 cell 数 |
| `error` | string | 异常类型与 message |

### 3.8 `/sorter me storageDump` 成功段
标题：`/sorter me storageDump`

| 字段 | 类型 | 说明 |
|---|---|---|
| `scan_status` | string | 固定为 `ok` |
| `target_block` | string | 命令目标方块 id |
| `target_pos` | string | 命令目标坐标 |
| `controller_pos` | string | controller 坐标 |
| `network_node_count` | integer | grid 节点数 |
| `storage_location_count` | integer | 存储位置总数 |
| `unique_key_count` | integer | 唯一 key 数 |
| `total_amount` | integer | 全网总量 |
| `dump_file` | string | storage analysis dump 相对路径 |

### 3.9 `/sorter me storageDump` 失败段
标题：`/sorter me storageDump`

| 字段 | 类型 | 说明 |
|---|---|---|
| `scan_status` | string | 固定为 `error` |
| `target_block` | string | 命令目标方块 id |
| `target_pos` | string | 命令目标坐标 |
| `controller_pos` | string | controller 坐标 |
| `error` | string | 异常类型与 message |

---

## 4. `logs/appliedinsight/plan-*.log`

### 4.1 定位
`/sorter me plan` 的详细文本日志。

### 4.2 文件头字段
与公共头一致，随后输出：

| 字段 | 类型 | 说明 |
|---|---|---|
| `target_block` | string | 命令目标方块 id |
| `target_pos` | string | 命令目标坐标 |
| `controller_pos` | string | controller 坐标 |
| `network_dimension` | string | 网络维度 id |
| `grid_resolved` | boolean | 是否解析到 grid |
| `network_node_count` | integer | grid 节点数 |
| `network_drive_count` | integer | drive 数 |
| `scanned_cell_slot_count` | integer | 扫描槽位数 |
| `mounted_cell_count` | integer | 已挂载槽位数 |
| `unique_item_key_count` | integer | 唯一 item key 数 |
| `duplicated_item_key_count` | integer | 重复 item key 数 |
| `profile.id` | string | profile id |
| `profile.name` | string | profile 名称 |
| `profile.version` | string | profile 版本 |
| `profile.defaultZoneId` | string | 默认 zone id |

### 4.3 `[supported_drives]`
每行一个 drive：

| 字段 | 类型 | 说明 |
|---|---|---|
| `blockId` | string | drive 方块 id |
| `pos` | string | drive 坐标 |
| `attachedStoragePos` | string | 外部挂接坐标，无则 `<none>` |
| `attachmentSide` | string | 挂接方向，无则 `<none>` |
| `externalStorageBus` | boolean | 是否 external storage bus |
| `declaredZoneId` | string | DAV 声明的 zone id，无则 `<none>` |
| `cellCount` | integer | drive 槽位数 |
| `mountedCellCount` | integer | 非空 storage 槽位数 |

### 4.4 `[runtime_zones]`
先输出 zone 行，再输出其 cell 行。

zone 字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `zone` | string | zone id |
| `name` | string | zone 名称 |
| `cellCount` | integer | zone 可用 cell 数 |

cell 字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `cell` | string | `drivePos#slot=n` |
| `attachedStoragePos` | string | 外部附着位置 |
| `attachment` | string | 附着描述 |
| `sourceBlockId` | string | cell 来源方块 id |
| `distinctItemKeyCount` | integer | 当前 cell 内不同 key 数 |

### 4.5 `[plan_summary]`

| 字段 | 类型 | 说明 |
|---|---|---|
| `assignmentCount` | integer | assignment 总数 |
| `movableAssignmentCount` | integer | 可搬运 assignment 数 |

后续每个 zone 一行：

| 字段 | 类型 | 说明 |
|---|---|---|
| `zone` | string | zone id 或 `<none>` |
| `itemCount` | integer | 该 zone 下 item 数 |
| `movableItemCount` | integer | 可搬运 item 数 |
| `totalAmount` | integer | 该 zone 下物品总量 |

### 4.6 `[plan_assignments]`
每行一个 assignment：

| 字段 | 类型 | 说明 |
|---|---|---|
| `itemId` | string | 物品 id |
| `name` | string | 显示名称 |
| `targetZoneId` | string | 目标 zone id |
| `decision` | string | 路由决策类型 |
| `matchedRuleId` | string | 命中的 route rule id |
| `matchedFilterId` | string | 命中的 filter id |
| `totalAmount` | integer | 该 item 总量 |
| `occurrenceCount` | integer | occurrence 数 |
| `hasComponents` | boolean | 是否有 components |
| `moveCandidate` | boolean | 是否进入后续搬运候选 |

---

## 5. `logs/appliedinsight/plan-and-move-*.log`

### 5.1 定位
`/sorter me planAndMove` 的详细文本日志。

### 5.2 继承关系
该文件包含 `plan-*.log` 的全部字段和 section，并额外增加执行结果 section。

### 5.3 `[move_summary]`

| 字段 | 类型 | 说明 |
|---|---|---|
| `attemptedMoveCount` | integer | 尝试搬运数 |
| `completedMoveCount` | integer | 成功搬运数 |
| `failedMoveCount` | integer | 失败搬运数 |
| `requestedAmount` | integer | 请求搬运总量 |
| `movedAmount` | integer | 实际搬运总量 |
| `scannedSourceDriveCount` | integer | 执行器扫描过的 source drive 数 |
| `scannedSourceCellCount` | integer | 执行器扫描过的 source cell 数 |
| `scannedStackCount` | integer | 扫描 stack 数 |
| `moveCandidateAssignmentCount` | integer | move candidate assignment 数 |
| `skippedNoAssignmentCount` | integer | 因无 assignment 跳过数 |
| `skippedSameZoneCount` | integer | 因已在同 zone 跳过数 |
| `skippedMissingTargetZoneCount` | integer | 因目标 zone 缺失跳过数 |
| `skippedPlacementRejectedCount` | integer | 因 placement 被拒绝跳过数 |

### 5.4 `[move_samples]`
- 列表型文本 section
- 每行是 debug report 采样消息
- 格式不保证稳定解析，适合人类阅读，不适合作为强结构输入

### 5.5 `[energy_cost]`

电量消耗估计，仅在配置 `energyCostEnabled=true` 时输出。

| 字段 | 类型 | 示例 | 说明 |
|------|------|------|------|
| `enabled` | boolean | true | 是否启用电量消耗 |
| `total_cost` | long | 185 | 总消耗 |
| `move_count` | int | 12 | 搬运次数 |
| `moved_amount` | long | 896 | 实际搬运量 |
| `distinct_item_types` | int | 7 | 涉及物品种类数 |
| `average_distance` | int | 24 | 平均曼哈顿距离 |
| `cost_breakdown` | string | base=24\|amount=62\|type=76\|distance=23 | 各项明细 |

计算公式: `floor(α·N + β·log2(1+A) + γ·log2(1+T) + δ·log2(1+avgDist))`

其中 α=2.0（基础搬运费）, β=20.0（数量系数）, γ=30.0（种类系数）, δ=15.0（距离系数）。所有系数可通过 Config 调整。

---

## 6. `logs/appliedinsight/merge-*.log`

### 6.1 定位
`/sorter merge` 的详细复盘日志，是当前最适合做“价值展示”的 merge 输出。

### 6.2 文件头字段
公共头后追加：

| 字段 | 类型 | 说明 |
|---|---|---|
| `target_block` | string | 命令目标方块 id |
| `target_pos` | string | 命令目标坐标 |
| `controller_pos` | string | controller 坐标 |
| `network_dimension` | string | 网络维度 id |
| `grid_resolved` | boolean | 是否解析到 grid |
| `network_node_count` | integer | grid 节点数 |
| `planned_merge_count` | integer | 计划 merge 数 |
| `attempted_merge_count` | integer | 尝试执行数 |
| `completed_merge_count` | integer | 完整成功数 |
| `failed_merge_count` | integer | 失败或部分失败数 |
| `requested_merge_amount` | integer | 请求搬运总量 |
| `merged_amount` | integer | 实际搬运总量 |

### 6.3 `[before_scan_summary]` / `[after_scan_summary]`
两者字段完全相同：

| 字段 | 类型 | 说明 |
|---|---|---|
| `drive_count` | integer | drive 数 |
| `scanned_cell_slot_count` | integer | 扫描槽位数 |
| `mounted_cell_count` | integer | 已挂载槽位数 |
| `unique_item_key_count` | integer | 唯一 item key 数 |
| `duplicated_item_key_count` | integer | 分布在多个位置的 item key 数 |
| `duplicated_cell_reference_count` | integer | 重复 occurrence 数 |

### 6.4 `[merge_effect_review]`
核心价值展示 section。

| 字段 | 类型 | 说明 |
|---|---|---|
| `distribution_location_count_before` | integer | 合并前分布位置总数 |
| `distribution_location_count_after` | integer | 合并后分布位置总数 |
| `distribution_location_count_delta` | integer | `after - before` |
| `occupied_internal_cell_count_before` | integer | 合并前占用 internal cell 数 |
| `occupied_internal_cell_count_after` | integer | 合并后占用 internal cell 数 |
| `occupied_internal_cell_count_delta` | integer | `after - before` |
| `used_type_slot_count_before` | integer | 合并前已用类型槽数 |
| `used_type_slot_count_after` | integer | 合并后已用类型槽数 |
| `used_type_slot_count_delta` | integer | `after - before` |
| `used_bytes_before` | integer | 合并前已用字节 |
| `used_bytes_after` | integer | 合并后已用字节 |
| `released_bytes` | integer | `before - after` |
| `available_group_count_before` | integer | 合并前可用组数 |
| `available_group_count_after` | integer | 合并后可用组数 |
| `released_available_group_count` | integer | `after - before`，表示新增可用组数 |
| `internal_storage_location_count_before` | integer | 合并前 internal 存储位置数 |
| `internal_storage_location_count_after` | integer | 合并后 internal 存储位置数 |
| `external_storage_location_count_before` | integer | 合并前 external 存储位置数 |
| `external_storage_location_count_after` | integer | 合并后 external 存储位置数 |
| `non_empty_storage_location_count_before` | integer | 合并前非空存储位置数 |
| `non_empty_storage_location_count_after` | integer | 合并后非空存储位置数 |
| `moved_amount` | integer | 实际搬运总量 |
| `changed_item_count` | integer | 发生变化的物品数 |

### 6.5 `[top_benefit_items]`
按收益排序的物品列表。当前排序优先级为：
1. `reducedLocations` 降序
2. `movedAmount` 降序
3. `itemId` 升序

单行字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `itemId` | string | 物品 id |
| `name` | string | 显示名称 |
| `movedAmount` | integer | 实际搬运量 |
| `beforeLocations` | integer | 合并前位置数 |
| `afterLocations` | integer | 合并后位置数 |
| `reducedLocations` | integer | 减少的位置数 |
| `beforeMix` | enum | `cell_only` / `external_only` / `external+cell_mixed` / `not_present` |
| `afterMix` | enum | 同上 |
| `becameMoreConcentrated` | boolean | 是否变得更集中 |

### 6.6 `[item_merge_details]`
每个变化物品一段，结构如下：

1. `item[n] ...` 头行
2. `delta: ...` 汇总行
3. `moved_out_from:` 列表
4. `merged_into:` 列表
5. `before:` 列表
6. `after:` 列表

头行字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `itemId` | string | 物品 id |
| `name` | string | 显示名称 |

`delta:` 行字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `beforeLocations` | integer | 变更前位置数 |
| `afterLocations` | integer | 变更后位置数 |
| `reducedLocations` | integer | 减少位置数 |
| `beforeAmount` | integer | 变更前总量 |
| `afterAmount` | integer | 变更后总量 |
| `movedAmount` | integer | 实际搬运量 |
| `beforeMix` | enum | 混存状态 |
| `afterMix` | enum | 混存状态 |
| `becameMoreConcentrated` | boolean | 是否更集中 |

列表项格式：

| 列表 | 含义 |
|---|---|
| `moved_out_from` | 实际搬出来源位置及搬出量 |
| `merged_into` | 实际合并目标位置及搬入量 |
| `before` | 变更前各位置分布 |
| `after` | 变更后各位置分布 |

位置行格式：
- `- <location> amount=<n>`

### 6.7 `[executed_moves]`
每个实际执行 move 一行：

| 字段 | 类型 | 说明 |
|---|---|---|
| `itemId` | string | 物品 id |
| `name` | string | 显示名称 |
| `requested` | integer | 计划搬运量 |
| `extracted` | integer | 实际抽出量 |
| `inserted` | integer | 实际插入量 |
| `success` | boolean | `inserted == requested` |
| `source` | string | 来源位置 |
| `destination` | string | 目标位置 |

### 6.8 `[energy_cost]`

电量消耗估计，仅在配置 `energyCostEnabled=true` 时输出。

| 字段 | 类型 | 示例 | 说明 |
|------|------|------|------|
| `enabled` | boolean | true | 是否启用电量消耗 |
| `total_cost` | long | 185 | 总消耗 |
| `move_count` | int | 12 | 搬运次数 |
| `moved_amount` | long | 896 | 实际搬运量 |
| `distinct_item_types` | int | 7 | 涉及物品种类数 |
| `average_distance` | int | 24 | 平均曼哈顿距离 |
| `cost_breakdown` | string | base=24\|amount=62\|type=76\|distance=23 | 各项明细 |

计算公式: `floor(α·N + β·log2(1+A) + γ·log2(1+T) + δ·log2(1+avgDist))`

其中 α=2.0（基础搬运费）, β=20.0（数量系数）, γ=30.0（种类系数）, δ=15.0（距离系数）。所有系数可通过 Config 调整。

---

## 7. `dumps/appliedinsight/me-dump-*.json`

### 7.1 定位
网络原始事实导出，适合细粒度 drill-down 和离线分析。

### 7.2 根对象字段

| 字段 | 类型 | 说明 |
|---|---|---|
| `formatVersion` | string | 当前格式版本，当前为 `1` |
| `generatedAt` | string | ISO 风格时间戳 |
| `command` | object | 命令执行上下文 |
| `target` | object | 命令目标与 controller 信息 |
| `summary` | object | 网络摘要 |
| `cells` | array | 按 cell/view 导出的存储位置列表 |
| `items` | array | 按 item 聚合后的全网分布列表 |

### 7.3 `command`

| 字段 | 类型 | 说明 |
|---|---|---|
| `executor` | string | 执行者名称 |
| `dimension` | string | 执行维度 |
| `x` | number | 执行位置 x |
| `y` | number | 执行位置 y |
| `z` | number | 执行位置 z |
| `serverTime` | integer | 服务器 tick |

### 7.4 `target`

| 字段 | 类型 | 说明 |
|---|---|---|
| `targetBlockId` | string/null | 命令目标方块 id |
| `targetPos` | string/null | 目标坐标 |
| `controllerPos` | string/null | controller 坐标 |

### 7.5 `summary`

| 字段 | 类型 | 说明 |
|---|---|---|
| `networkNodeCount` | integer | grid 节点数 |
| `driveCount` | integer | drive 数 |
| `scannedCellSlotCount` | integer | 扫描槽位数 |
| `mountedCellCount` | integer | 已挂载槽位数 |
| `uniqueItemKeyCount` | integer | 唯一 item key 数 |
| `duplicatedItemKeyCount` | integer | 重复分布 item key 数 |
| `duplicatedCellReferenceCount` | integer | 重复 occurrence 数 |
| `itemOccurrenceCount` | integer | 全网 occurrence 总数 |

### 7.6 `cells[]`

| 字段 | 类型 | 说明 |
|---|---|---|
| `sourceBlockId` | string | drive/source 方块 id |
| `drivePos` | string | drive 坐标 |
| `attachedStoragePos` | string/null | 附着存储坐标 |
| `externalStorageBus` | boolean | 是否 external storage bus |
| `slot` | integer | 槽位号 |
| `driveCellCount` | integer | 所在 drive 的总槽位数，前端可用于计算空槽数 |
| `distinctItemKeyCount` | integer | 当前 cell 内不同 item key 数 |
| `totalAmount` | integer | 当前 cell 内总量 |
| `totalBytes` | long/null | AE2 cell 总字节容量；external storage bus 则为 null |
| `usedBytes` | long/null | AE2 cell 已用字节；external storage bus 则为 null |
| `totalItemTypes` | int/null | AE2 cell 最大可存物品种类数；external storage bus 则为 null |
| `remainingItemTypes` | int/null | AE2 cell 剩余可存种类数；external storage bus 则为 null |
| `cellKind` | string/null | cell 类型描述，由 totalBytes 推导（如 `64k_cell`）；external storage bus 则为 null |
| `zoneId` | string/null | 归属 zone id（由 DAV 声明）；未归入任何 zone 则为 null |
| `entries` | array | 当前 cell 内各 item 条目 |

### 7.7 `cells[].entries[]`

| 字段 | 类型 | 说明 |
|---|---|---|
| `itemId` | string | 物品 id |
| `displayName` | string | 显示名称 |
| `amount` | integer | 当前 cell 中该物品量 |
| `componentsPatchEmpty` | boolean | 组件补丁是否为空 |

### 7.8 `items[]`

| 字段 | 类型 | 说明 |
|---|---|---|
| `itemId` | string | 物品 id |
| `modId` | string | mod id |
| `displayName` | string | 显示名称 |
| `totalAmount` | integer | 全网总量 |
| `occurrenceCount` | integer | 出现位置数 |
| `componentsPatchEmpty` | boolean | 组件补丁是否为空 |
| `maxStackSize` | integer | 物品栈上限 |
| `tags` | array<string> | 物品标签列表 |
| `serializedStackNbt` | string | 归一化后的 SNBT |
| `occurrences` | array | 该物品的 occurrence 列表 |

### 7.9 `items[].occurrences[]`

| 字段 | 类型 | 说明 |
|---|---|---|
| `sourceBlockId` | string | drive/source 方块 id |
| `drivePos` | string | drive 坐标 |
| `attachedStoragePos` | string/null | 附着存储坐标 |
| `externalStorageBus` | boolean | 是否 external storage bus |
| `slot` | integer | 槽位号 |
| `amount` | integer | 该位置上的数量 |

---

## 8. `dumps/appliedinsight/storage-analysis-*.json`

### 8.1 定位
面向 dashboard / overview / 节点语义分析的宏观 snapshot。

### 8.2 根对象字段

| 字段 | 类型 | 说明 |
|---|---|---|
| `formatVersion` | string | 当前格式版本，当前为 `1` |
| `generatedAt` | string | 生成时间 |
| `dimensionId` | string/null | 网络维度 id |
| `targetBlockId` | string/null | 命令目标方块 id |
| `targetPos` | string/null | 目标坐标 |
| `controllerPos` | string/null | controller 坐标 |
| `report` | object | `StorageAnalyzerReport` |

### 8.3 `report.summary`

| 字段 | 类型 | 说明 |
|---|---|---|
| `networkNodeCount` | integer | grid 节点数 |
| `storageLocationCount` | integer | 存储位置数 |
| `nonEmptyStorageLocationCount` | integer | 非空存储位置数 |
| `internalStorageLocationCount` | integer | internal 位置数 |
| `externalStorageLocationCount` | integer | external 位置数 |
| `internalTotalAmount` | integer | internal 总量 |
| `externalTotalAmount` | integer | external 总量 |
| `uniqueKeyCount` | integer | 唯一 key 数 |
| `duplicatedKeyCount` | integer | 分布在多个位置的 key 数 |
| `totalAmount` | integer | 全网总量 |
| `totalOccurrenceCount` | integer | occurrence 总数 |
| `internalOnlyKeyCount` | integer | 仅 internal 的 key 数 |
| `externalOnlyKeyCount` | integer | 仅 external 的 key 数 |
| `mixedLocationKeyCount` | integer | internal/external 混存 key 数 |
| `fragmentationScore` | number | 碎片化分数 |
| `fragmentationLevel` | string | `low` / `medium` / `high` |

### 8.4 `report.storageLocations[]`

| 字段 | 类型 | 说明 |
|---|---|---|
| `locationId` | string | 存储位置稳定标识 |
| `sourceBlockId` | string | 来源方块 id |
| `attachedStorageBlockId` | string/null | 附着存储方块 id |
| `storageKind` | string | 存储类别 |
| `hostPos` | string | host 坐标 |
| `attachedStoragePos` | string/null | 附着存储坐标 |
| `externalStorageBus` | boolean | 是否 external storage bus |
| `slot` | integer | 槽位号 |
| `distinctKeyCount` | integer | 不同 key 数 |
| `totalAmount` | integer | 总量 |
| `networkAmountShare` | number | 占全网总量比例 |
| `totalBytes` | long/null | AE2 cell 总字节容量；external bus 则为 null |
| `usedBytes` | long/null | AE2 cell 已用字节；external bus 则为 null |
| `totalItemTypes` | int/null | AE2 cell 最大可存种类数；external bus 则为 null |
| `remainingItemTypes` | int/null | AE2 cell 剩余可存种类数；external bus 则为 null |
| `cellKind` | string/null | cell 类型描述（如 `64k_cell`）；external bus 则为 null |
| `zoneId` | string/null | 归属 zone id（由 DAV 声明）；未归入任何 zone 则为 null |
| `topKeys` | array | 头部 key 列表 |

### 8.5 `report.storageLocations[].topKeys[]`
与 `KeyAmountSummary` 一致：

| 字段 | 类型 | 说明 |
|---|---|---|
| `keyType` | string | key 类型 id |
| `keyId` | string | key id |
| `displayName` | string | 显示名称 |
| `amount` | integer | 数量 |

### 8.6 `report.itemDistributions[]`

| 字段 | 类型 | 说明 |
|---|---|---|
| `keyType` | string | key 类型 id |
| `keyId` | string | key id |
| `displayName` | string | 显示名称 |
| `totalAmount` | integer | 全网总量 |
| `locationCount` | integer | 位置数 |
| `internalLocationCount` | integer | internal 位置数 |
| `externalLocationCount` | integer | external 位置数 |
| `maxSingleLocationAmount` | integer | 单位置最大量 |

### 8.7 `report.healthFlags[]`
- 字符串数组
- 当前用于表达高层健康信号
- 例如：
  - `high_fragmentation`
  - `mixed_internal_external_distribution`
  - `fragmentation_score_high`
  - `external_amount_dominant`
  - `external_only_storage_network`
  - `low_fragmentation`
  - `has_empty_storage_locations`
  - `suspected_infinite_storage_present`

### 8.8 `report.mostFragmentedItems[]`
与 `itemDistributions[]` 同结构，表示最碎片化 item。

### 8.9 `report.largestStorages[]`
与 `storageLocations[]` 同结构，表示最大存储位置。

### 8.10 `report.mixedInternalExternalItems[]`
与 `itemDistributions[]` 同结构，表示 internal/external 混存 item。

### 8.11 `report.suspectedSemanticCandidates[]`

| 字段 | 类型 | 说明 |
|---|---|---|
| `candidateType` | string | 候选类型 |
| `suggestedClassification` | string | 建议分类 |
| `locationId` | string | 位置 id |
| `sourceBlockId` | string | 来源方块 id |
| `attachedStorageBlockId` | string/null | 附着存储方块 id |
| `storageKind` | string | 存储类别 |
| `hostPos` | string | host 坐标 |
| `attachedStoragePos` | string/null | 附着存储坐标 |
| `totalAmount` | integer | 当前总量 |
| `networkAmountShare` | number | 占全网比例 |
| `suspicionScore` | number | 0~1 之间的怀疑分数 |
| `matchedHeuristics` | array<string> | 命中的启发式依据 |

---

## 9. `routing-profile.json` — 过滤路由表契约

### 9.1 定位
路由配置文件，定义 zone / filter / routeRule 的层级结构，由 `RoutingProfileJsonCodec` 读写。

### 9.2 根对象字段

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | string | profile 唯一标识 |
| `name` | string | 显示名称 |
| `description` | string | 描述，默认为 `""` |
| `enabled` | boolean | 是否启用，默认为 `true` |
| `defaultZoneId` | string | 兜底 zone（路由无匹配时使用） |
| `version` | integer | 格式版本号，默认为 `1` |
| `zones` | array | StorageZone 列表 |
| `filters` | array | ItemFilter 列表 |
| `routeRules` | array | RouteRule 列表 |

### 9.3 `zones[]`

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | string | zone 唯一标识 |
| `name` | string | 显示名称 |
| `description` | string | 描述，默认为 `""` |
| `enabled` | boolean | 是否启用，默认为 `true` |
| `kind` | enum | `BULK` / `MISC` / `COMPONENT_SAFE` / `SPECIAL` / `CUSTOM` |
| `allowAsDefault` | boolean | 是否可作为默认 zone，默认为 `false` |

### 9.4 `filters[]`

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | string | filter 唯一标识 |
| `name` | string | 显示名称 |
| `description` | string | 描述，默认为 `""` |
| `enabled` | boolean | 是否启用，默认为 `true` |
| `root` | object | 树形过滤表达式根节点（见 9.6） |

过滤器向后兼容旧格式 `matchMode` + `conditions[]`（自动转换为树形结构）。

### 9.5 B 树形过滤表达式 (`filterExpression`)

| 节点类型 | 说明 |
|---|---|
| `FilterCondition` | 叶子节点 — 单条过滤条件 |
| `FilterGroup` | 复合节点 — 组合多个子表达式 |

### 9.6 `FilterCondition` 字段

| 字段 | 类型 | 说明 |
|---|---|---|
| `field` | enum | 过滤字段，见 9.8 |
| `operator` | enum | 操作符，见 9.9 |
| `value` | string/null | 比较值；对 `IS_TRUE` / `IS_FALSE` 可为 null |

### 9.7 `FilterGroup` 字段

| 字段 | 类型 | 说明 |
|---|---|---|
| `combinator` | enum | `AND` / `OR` |
| `rules` | array | `filterExpression[]` 子表达式列表 |

### 9.8 过滤字段 (`field`) 枚举

| 枚举值 | 类型 | 说明 |
|---|---|---|
| `ITEM_ID` | string | 完整物品 ID（如 `ae2:sky_stone_block`） |
| `MOD_ID` | string | 模组 namespace（如 `ae2` / `mekanism`） |
| `TAG` | string | 物品标签（如 `c:ingots/iron`） |
| `DISPLAY_NAME` | string | 本地化显示名称 |
| `HAS_COMPONENTS` | boolean | 组件补丁是否非空 |
| `TOTAL_AMOUNT` | number | 全网总量 |
| `NBT_PATH` | string | ⭐ NBT 路径提取匹配，见 9.10 |

### 9.9 操作符 (`operator`) 枚举

| 枚举值 | 语义 | 适用字段 |
|---|---|---|
| `EQUALS` | 大小写不敏感精确匹配 | ITEM_ID, MOD_ID, TAG, DISPLAY_NAME, NBT_PATH |
| `CONTAINS` | 大小写不敏感子串匹配 | ITEM_ID, MOD_ID, TAG, DISPLAY_NAME, NBT_PATH |
| `IN` | CSV 候选列表匹配 | ITEM_ID, MOD_ID, TAG, DISPLAY_NAME |
| `GREATER_OR_EQUAL` | 数值 >= 比较 | TOTAL_AMOUNT |
| `LESS_OR_EQUAL` | 数值 <= 比较 | TOTAL_AMOUNT |
| `IS_TRUE` | 布尔 true 检查 | HAS_COMPONENTS |
| `IS_FALSE` | 布尔 false 检查 | HAS_COMPONENTS |
| `REGEX` | ⭐ 正则表达式匹配（Java Pattern, CASE_INSENSITIVE） | ITEM_ID, TAG, DISPLAY_NAME, NBT_PATH |

### 9.10 NBT_PATH 字段说明
`NBT_PATH` 的 `value` 使用 `||` 作为分隔符编码**路径**和**比较值**：

格式：`nbtPath||comparisonValue`

| 场景 | 示例 value | 说明 |
|---|---|---|
| REGEX roll 值过滤 | `components.apotheosis:rarity.value||epic\|mythic` | 提取 NBT 路径值，用正则匹配 |
| EQUALS 精确匹配 | `components.apotheosis:rarity.value||epic` | 提取路径值，与 `epic` 比较 |
| CONTAINS 子串匹配 | `components.apotheosis:rarity.value||ep` | 提取路径值，检查是否包含 `ep` |

若不包含 `||`，整条 value 作为 NBT 路径，比较值等于路径本身（仅简单场景可用）。

**NBT 数据来源：** Smart Bus / dump 使用 `ItemStack.saveOptional(...).toString()` 的 SNBT 字符串；`NbtPathExtractor` 通过 Gson 解析（与测试 dump 中 `{components:{"minecraft:damage":0},...}` 格式兼容）。

**耐久相关路径（勿与攻击力混淆）：**

| 路径 | 含义 |
|---|---|
| `components.minecraft:damage` | 已消耗耐久（数值越大越接近损坏） |
| `components.minecraft:max_damage` | 最大耐久 |
| `components.minecraft:attribute_modifiers` | 属性修饰（含攻击力等，**不是**耐久预设的主条件） |

### 9.11 Smart Bus 内置预设

游戏内 GUI 与 `tools/filter-editor/templates.js` 共用以下预设（实现：`SmartBusFilterPresets`）。点击即写入 FilterExpression JSON；更多预设计划在后续版本扩展。

| 预设 id | 显示名 | 逻辑摘要 |
|---|---|---|
| `all_items` | 所有物品 | `ITEM_ID` REGEX `.+` |
| `durability_items` | 有耐久 | OR: `TAG` = `minecraft:enchantable/durability`；NBT `max_damage`；NBT `damage`（mod 兜底） |
| `ores` | 矿石 | OR: `TAG` CONTAINS `c:ores`；`TAG` REGEX `.+:ores/.*`；`ITEM_ID` REGEX `.*_ore$` |

### 9.12 `routeRules[]`

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | string | rule 唯一标识 |
| `name` | string | 显示名称 |
| `description` | string | 描述，默认为 `""` |
| `enabled` | boolean | 是否启用，默认为 `true` |
| `priority` | integer | 优先级（小→大执行），默认为 0 |
| `filterId` | string | 引用 `filters[]` 的 id |
| `targetZoneId` | string | 引用 `zones[]` 的 id |
| `continueOnMatch` | boolean | 匹配后是否继续执行后续规则，默认为 `false` |
| `action` | enum | `ROUTE_TO_ZONE` / `REJECT` / `ONLY_MARK` / `FALLBACK` |
| `explanation` | string | 说明文本，默认为 `""` |

### 9.13 RouteAction 枚举

| 枚举值 | 语义 |
|---|---|
| `ROUTE_TO_ZONE` | 路由到目标 zone（标准行为） |
| `REJECT` | 拒绝（不入任何 zone） |
| `ONLY_MARK` | 仅标记，不移动 |
| `FALLBACK` | 回退（兜底行为） |

---

## 10. 稳定性说明

### 9.1 相对稳定，适合程序消费
以下产物更适合做解析、前端读取或外部脚本处理：

- `me-dump-*.json`
- `storage-analysis-*.json`
- `merge-*.log` 的 section 名和 `key=value` 字段
- `plan-*.log` / `plan-and-move-*.log` 的 section 名和主要字段

### 9.2 偏向人类阅读，解析时要保守
以下内容更适合人类阅读，程序若要解析需容错：

- `appliedinsight.log` 的自由组合段落
- `planned_merge[n]` 的整行文本
- `move_samples` 中的样本文本
- `item_merge_details` 下的缩进列表文本

### 9.3 当前不做的保证
当前不保证：

- 所有文本日志都存在正式 schema
- 样本文本字段完全不变
- 列表顺序在未来绝不调整

如果后续要把某类日志升级为正式前端契约，建议优先把它改造成 JSON 或者更严格的 key-value block 格式。

---

## 10. 相关文档

| 文档 | 说明 |
|------|------|
| [`COMMANDS_REFERENCE.md`](COMMANDS_REFERENCE.md) | 命令参考 — 所有命令的详细说明 |
| [`docs/架构/FACTS.md`](docs/架构/FACTS.md) | 架构 + 整体逻辑 + 术语表 — 六层架构、三条能力路径、术语定义（FACT-091 ~ FACT-120） |
| [`前端对接说明.md`](前端对接说明.md) | 前端对接说明 — 前端边界与后端消费方式 |
| [`dashboard的设计哲学.md`](dashboard的设计哲学.md) | Dashboard 设计哲学 — 先给结果再纠偏 |
| [`存储节点语义层.md`](存储节点语义层.md) | 存储节点语义层 — 节点语义推断与 dashboard 理论基础 |
