# dump、ZoneAllocationResolver 与 Ae2ItemMoveExecutor 闭环计划

> 注：这份文档保留“dump -> resolve -> execute”思路作为阶段性背景。
> 当前更近的实现优先级，已经转向：
>
> - `RuntimeCell`
> - `RuntimeZone`
> - `AbstractSorter` / `MESorter` / `ZoneSorter`
> - executor 只负责把物品送到目标 zone 的 cell

## 1. 这一步的目标

这份计划文档记录的是上一轮收敛出的 snapshot 闭环思路。

当前阶段已经进一步收敛为两层：

1. executor：负责把物品送入目标 zone 的 cell
2. sorter：负责 ME 内部或 zone 内部的整理

因此，这份文档现在的定位更适合视为：

> **dump-based zone planning 的阶段性背景文档，而不是当前唯一的最近实现顺序。**

最小闭环仍然成立：

- `dump -> resolve -> instructions -> execute`

但近期实现顺序已经调整为：

- 先统一 cell / zone 运行时模型
- 再重构 sorter
- 最后让 executor 与 sorter 在该模型上协同

---

## 2. 总体原则

### 2.1 dump 是规划快照

`ZoneAllocationResolver` 不直接读取实时 `MEStorage` 做在线规划，而是：

- 读取 dump 文件
- 读取 profile 文件
- 在子线程中完成 route / zone / filter 判定
- 生成一整串搬运指令

也就是说，dump 在这一阶段的地位是：

> **规划输入快照，而不只是诊断输出。**

### 2.2 执行阶段采用 best-effort

`Ae2ItemMoveExecutor` 不保证执行时网络状态与 dump 完全一致。

执行阶段允许出现：

- 用户手动搬走物品
- 自动化让某个 item 数量下降
- 某些有 NBT / components 的物品在执行时已经不存在
- destination 在执行时已满或已变化

当前策略是：

- **不重新规划**
- **不补偿外部行为**
- **不追求强一致**
- **只保证本 mod 的 dump -> plan -> execute 闭环成立**

### 2.3 通过 threshold 保证稳定性

对于依赖数量判断的分发策略，要求玩家设置 threshold。

作用：

- 减少小幅波动导致的无意义搬运
- 降低 dump 与执行阶段之间的偏差敏感度
- 让规划指令更稳定

---

## 3. `ZoneAllocationResolver`

## 3.1 定位

`ZoneAllocationResolver` 是：

> **运行在子线程中的 dump 解析器、route 判定器与搬运指令生成器。**

它不是实时在线 resolver，也不是底层 move executor。

---

## 3.2 职责

它负责：

- 读取 dump 文件
- 读取前端生成的 profile 文件
- 解析 dump 中的 item / occurrence / cell / drive 信息
- 根据 `RoutingEngine` 计算每个 item 的目标 zone
- 根据 dump 中可识别的 DAV / cell 元信息确定建议去向
- 根据 threshold 过滤掉不值得搬运的 item
- 生成一整串搬运指令

它不负责：

- 实际执行 `extract / insert`
- 运行时重规划
- 对执行阶段状态偏差做在线补偿
- 保证外部自动化场景下的强一致

---

## 3.3 输入

当前最小输入建议包括：

- dump 文件路径
- profile 文件路径
- threshold 相关配置
- 可选的 resolver 运行参数（如最大指令数）

其中：

### dump 文件
提供：
- item 聚合信息
- occurrence 信息
- cell / drive / DAV 识别信息
- 有无 components / SNBT / tags 等上下文

### profile 文件
提供：
- zones
- filters
- route rules
- default zone

---

## 3.4 输出

输出为：

> **一整串搬运指令（instructions）**

每条指令最小应包含：

- 物品身份信息
  - `itemId`
  - `displayName`
  - `serializedStackNbt` 或等价可识别信息
- source 信息
  - `sourceDrivePos`
  - `sourceSlot`
- destination 信息
  - `targetZoneId`
  - `destinationDrivePos`
  - `destinationSlot`
- 计划搬运量
  - `requestedAmount`
- route 解释信息
  - `matchedRuleId`
  - `matchedFilterId`
  - `decisionType`

如果后续需要，也可为指令补充：

- 优先级
- threshold 命中原因
- 备注说明

---

## 3.5 内部流程

### 第一步：解析 dump

从 dump 中恢复：

- item 聚合视图
- item 出现位置
- drive / cell / DAV 信息
- zone 声明信息（后续随着 dump 增强而补全）

### 第二步：构造 `ItemMatchContext`

从 dump item 中提取：

- `itemId`
- `modId`
- `displayName`
- `tags`
- `hasComponents`
- `totalAmount`

然后构造 `ItemMatchContext`。

### 第三步：运行 `RoutingEngine`

对每个 item 执行：

- `RoutingEngine.decide(profile, context)`

得到：

- `finalZoneId`
- `matchedRuleId`
- `matchedFilterId`
- `decisionType`

### 第四步：应用 threshold

对数量相关策略：

- 若未达到 threshold，则不生成搬运指令
- 由此减少无意义的小额搬运

### 第五步：解析 destination

根据：

- 目标 `zoneId`
- dump 中识别到的 DAV / drive / cell 信息

为每个 source occurrence 选择一个 destination。

当前阶段 destination 选择可以保持简单，只需要：

- 可解释
- 可稳定重现
- 便于日志回溯

### 第六步：生成指令序列

将每个候选搬运动作转成指令对象，输出给执行阶段。

---

## 4. `Ae2ItemMoveExecutor`

## 4.1 定位

`Ae2ItemMoveExecutor` 是：

> **按 resolver 生成的指令执行 AE2 物品搬运的工具类。**

它不参与 route 逻辑，只负责“怎么搬”。

---

## 4.2 职责

它负责：

- 根据指令定位 source / destination
- 尝试执行一次 AE2 物品搬运
- 处理部分插入与 rollback
- 输出执行结果

它不负责：

- 读取 dump
- 解析 profile
- 计算 zone / filter / route
- 重新生成规划

---

## 4.3 执行哲学

这一阶段明确采用：

> **best-effort execution**

也就是说：

- 能搬就搬
- 数量不足就部分执行
- 找不到 source 就跳过
- 找不到目标就跳过
- 有 NBT / components 的物品在执行时不存在就直接跳过

不为了执行时偏差而回头重规划。

---

## 4.4 建议执行步骤

对每条指令：

1. 定位 source storage
2. 定位 destination storage
3. 尝试按指令的 item key / amount 执行 `extract`
4. 尝试对 destination 执行 `insert`
5. 若插入不足，则对余量执行 rollback
6. 返回执行结果

这部分可以直接复用当前 `SorterMoveOperation.execute()` 已验证过的 AE2 move 方法。

---

## 4.5 建议结果状态

执行结果建议至少能区分：

- `COMPLETED`
- `PARTIAL`
- `SKIPPED_SOURCE_MISSING`
- `SKIPPED_DESTINATION_UNAVAILABLE`
- `SKIPPED_KEY_NOT_FOUND`
- `FAILED`

这样可以让日志、调试与下一轮闭环更清晰。

---

## 5. NBT / components 物品策略

这一阶段对复杂物品的策略明确如下：

- dump 阶段保留足够强的身份信息
- resolver 生成指令时保留该身份信息
- executor 执行时尝试按该身份查找 / 搬运
- **如果执行时找不到，就直接跳过**

原因是：

- 这些物品更容易被玩家手动变化
- 强追踪收益低、复杂度高
- 当前阶段以闭环稳定为先

---

## 6. threshold 策略

这一阶段，凡是“按数量决定是否搬运”的策略，都要求玩家设置 threshold。

threshold 的作用：

- 避免少量波动频繁触发搬运
- 降低 dump 与执行期差异带来的噪声
- 让 route / zone 规划更稳定

也就是说，threshold 是这套 snapshot-planning 模式的重要稳定器，而不是临时补丁。

---

## 7. 当前明确不做的事

这一阶段不做：

- 执行阶段实时在线重规划
- 与玩家手动行为做一致性合并
- 与外部自动化系统做协同锁定
- 为每次执行失败重新生成新计划
- 复杂的跨阶段状态修正

如果执行后网络状态已经明显变化，正确做法是：

1. 重新 dump
2. 重新 resolve
3. 重新 execute

而不是在单轮执行中尝试纠偏所有外部变量。

---

## 8. 与后续 planner 的关系

这一阶段积累下来的价值仍然保留：

- dump 作为规划快照
- `item -> zone` 的离线解析
- best-effort executor 的执行哲学

但当前更近的重构目标已经明确为：

- `RuntimeCell`
- `RuntimeZone`
- `AbstractSorter`
- `MESorter`
- `ZoneSorter`

也就是说，后续不论是：

- 自动合并同类 sorter
- DAV 内部 sorter
- 按 zone 分发 executor

都会优先建立在统一的 cell / zone 运行时模型之上。

---

当前这份文档的一句话结论可以更新为：

> **dump -> resolve -> execute 仍然是重要背景思路，但当前最近的工程落点，已经切换为先统一 `RuntimeCell` / `RuntimeZone`，再分层实现 executor 与 `ZoneSorter`。**
